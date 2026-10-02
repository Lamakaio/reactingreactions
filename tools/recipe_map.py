#!/usr/bin/env python3
"""Build an interactive map of every recipe that touches this mod, as one HTML page.

Usage:  python tools/recipe_map.py [--out build/recipe-map.html] [--without cdg] [--without ee] [--without aeronautics] [--keep-colours] [--hub id] [--no-hubs]

Reads the generated recipe JSON (src/generated/resources/data, so run ./gradlew runData first) and keeps every recipe
that uses or makes one of this mod's items or fluids, or runs on one of its machines. Recipe conditions are resolved
for a chosen set of installed mods: by default the favoured setup (Create Diesel Generators, Electro Energetics and
Aeronautics all present); --without drops one, which swaps in its fallback recipes. Display names come from the
generated lang file plus the vanilla, Create and other jars in the Gradle cache when they are found. Families of
one-per-colour recipes (paints, envelopes, whitening) are left out unless --keep-colours is given. Generic reagents
(water, oxygen, steam...) get a small copy per recipe so they don't knot every chain together; --no-hubs turns that off.

Views are laid out left to right with dagre. The page needs an internet connection the first time, for Cytoscape.js and
dagre from jsdelivr.
"""
import argparse
import collections
import glob
import html
import json
import os
import re
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "generated", "resources", "data")
MOD = "reactingreactions"
COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue",
           "brown", "green", "red", "black"]
COLOUR_WORD = re.compile(r"(?<![a-z])(" + "|".join(sorted(COLOURS, key=len, reverse=True)) + r")(?![a-z])")
# A family of recipes whose ids differ only by a dye colour (paints, envelopes, whitening) repeats one recipe per colour.
COLOUR_FAMILY_SIZE = 8
# Generic reagents used by dozens of recipes. Drawn once, they pull every chain into one knot, so each recipe gets its own
# small copy instead (as production-chain planners do). Chain-defining materials, like the brines, stay single nodes.
HUBS = ["minecraft:water", "minecraft:lava", "reactingreactions:steam", "reactingreactions:oxygen", "reactingreactions:hydrogen",
        "reactingreactions:carbon_dioxide", "reactingreactions:carbon_monoxide", "reactingreactions:sulfuric_acid",
        "reactingreactions:bleach", "reactingreactions:solvent"]
OPTIONAL = {"cdg": "createdieselgenerators", "ee": "electroenergetics", "aeronautics": ["aeronautics", "aeronautics_bundled", "simulated", "offroad"]}

# Short machine names and colours, per recipe type.
MACHINES = {
    "reactingreactions:reaction_recipe": ("Reaction Chamber", "#e15759"),
    "reactingreactions:electrolysis_recipe": ("Electrolysis", "#4e79a7"),
    "reactingreactions:distillation_recipe": ("Distillation Tower", "#b07aa1"),
    "createdieselgenerators:distillation": ("CDG Distillation", "#b07aa1"),
    "reactingreactions:airless_oven_recipe": ("Airless Oven", "#f28e2b"),
    "createdieselgenerators:bulk_fermenting": ("CDG Bulk Fermenter", "#59a14f"),
    "create:mixing": ("Mixer", "#edc948"),
    "create:compacting": ("Press + Basin", "#9c755f"),
    "create:crushing": ("Crushing", "#bab0ac"),
    "create:milling": ("Millstone", "#d37295"),
    "create:pressing": ("Press", "#86bcb6"),
    "create:filling": ("Spout", "#17becf"),
    "create:splashing": ("Washing", "#79a8ff"),
    "create:sequenced_assembly": ("Sequenced Assembly", "#8cd17d"),
    "minecraft:crafting_shaped": ("Crafting", "#8c8c8c"),
    "minecraft:crafting_shapeless": ("Crafting", "#8c8c8c"),
    "minecraft:blasting": ("Blast Furnace", "#ff9d9a"),
    "minecraft:smelting": ("Furnace", "#ff9d9a"),
}


def loaded_mods(without):
    mods = {"minecraft", "create", "neoforge", "c", MOD}
    for key, value in OPTIONAL.items():
        if key not in without:
            mods.update(value if isinstance(value, list) else [value])
    return mods


def condition_ok(cond, mods):
    kind = cond.get("type")
    if kind == "neoforge:mod_loaded":
        return cond["modid"] in mods
    if kind == "neoforge:not":
        return not condition_ok(cond["value"], mods)
    if kind == "neoforge:false":
        return False
    if kind == "neoforge:true":
        return True
    if kind == "neoforge:and":
        return all(condition_ok(c, mods) for c in cond["values"])
    if kind == "neoforge:or":
        return any(condition_ok(c, mods) for c in cond["values"])
    return True


# ---- display names -------------------------------------------------------------------------------------------------

def load_names():
    names = {}

    def add(lang):
        for key, value in lang.items():
            parts = key.split(".")
            if len(parts) == 3 and parts[0] in ("item", "block", "fluid"):
                names.setdefault(f"{parts[1]}:{parts[2]}", value)

    home = os.path.expanduser("~")
    jars = (glob.glob(os.path.join(ROOT, "build", "moddev", "artifacts", "*client-extra*.jar"))
            + glob.glob(os.path.join(home, ".gradle", "caches", "modules-2", "files-2.1", "curse.maven", "*", "*", "*", "*.jar"))
            + glob.glob(os.path.join(home, ".gradle", "caches", "modules-2", "files-2.1", "maven.modrinth", "*", "*", "*", "*.jar"))
            + glob.glob(os.path.join(ROOT, "libs", "*.jar"))
            + glob.glob(os.path.join(ROOT, "build", "*-submodules", "*.jar")))
    own = os.path.join(ROOT, "src", "generated", "resources", "assets", MOD, "lang", "en_us.json")
    if os.path.isfile(own):
        add(json.load(open(own, encoding="utf-8")))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                for entry in z.namelist():
                    if entry.startswith("assets/") and entry.endswith("/lang/en_us.json"):
                        try:
                            add(json.loads(z.read(entry).decode("utf-8")))
                        except ValueError:
                            pass
        except (zipfile.BadZipFile, OSError):
            pass
    return names


def pretty(ident, names):
    if ident.startswith("#"):
        return "#" + ident[1:]
    return names.get(ident) or ident.split(":")[-1].replace("_", " ").title()


# ---- tags ----------------------------------------------------------------------------------------------------------

def load_tags():
    """Tag files (kind, 'ns:path') -> entries, from the generated data and the NeoForge and Create jars."""
    tags = {}

    def add(kind, ident, data):
        tags.setdefault((kind, ident), []).extend(v if isinstance(v, str) else v.get("id") for v in data.get("values", []))

    for kind in ("item", "fluid"):
        for f in glob.glob(os.path.join(DATA, "*", "tags", kind, "**", "*.json"), recursive=True):
            rel = os.path.relpath(f, DATA).replace(os.sep, "/")
            ns, _, _, path = rel.split("/", 3)
            add(kind, f"{ns}:{path[:-5]}", json.load(open(f, encoding="utf-8")))
    home = os.path.expanduser("~")
    jars = (glob.glob(os.path.join(ROOT, "build", "moddev", "artifacts", "neoforge-*.jar"))
            + glob.glob(os.path.join(home, ".gradle", "caches", "modules-2", "files-2.1", "curse.maven", "create-*", "*", "*", "*.jar")))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                for entry in z.namelist():
                    m = re.match(r"data/([a-z0-9_]+)/tags/(item|fluid)/(.+)\.json$", entry)
                    if m:
                        add(m.group(2), f"{m.group(1)}:{m.group(3)}", json.loads(z.read(entry)))
        except (zipfile.BadZipFile, OSError):
            pass
    return tags


TAGS = {}


def resolve(kind, tag, seen=()):
    """The representative member of a tag: this mod's own entry if it has one, else the first concrete entry."""
    entries = TAGS.get((kind, tag), [])
    concrete = []
    for e in entries:
        if e.startswith("#"):
            if e[1:] not in seen:
                inner = resolve(kind, e[1:], seen + (tag,))
                if inner:
                    concrete.append(inner)
        else:
            concrete.append(e)
    ours = [e for e in concrete if e.startswith(MOD + ":")]
    return (ours or concrete or [None])[0]


# ---- recipe parsing ------------------------------------------------------------------------------------------------

def stacks(obj):
    """Turn one ingredient JSON into a list of (id, kind, amount) alternatives; kind is item, fluid or tag. A common tag
    (c:ingots/steel, c:hydrogen) is drawn as its representative material, so the map reads "Steel Ingot", not a tag."""
    if isinstance(obj, list):
        out = []
        for o in obj:
            out += stacks(o)
        return out
    if isinstance(obj, str):
        return [(obj, "tag" if obj.startswith("#") else "item", 1)]
    if obj.get("type") == "neoforge:tag":
        member = resolve("fluid", obj["tag"])
        return [(member, "fluid", obj.get("amount", 1000))] if member else [("#" + obj["tag"], "tag", 1)]
    if "fluid" in obj:
        return [(obj["fluid"], "fluid", obj.get("amount", 1000))]
    if "tag" in obj:
        member = resolve("item", obj["tag"]) if obj["tag"].startswith("c:") else None
        return [(member, "item", 1)] if member else [("#" + obj["tag"], "tag", 1)]
    if "item" in obj:
        return [(obj["item"], "item", obj.get("count", 1))]
    if "id" in obj:
        return [(obj["id"], "item", obj.get("count", 1))]
    return []


def result(obj, fluids):
    ident = obj.get("id") or obj.get("item")
    if "amount" in obj or ident in fluids:
        return (ident, "fluid", obj.get("amount", 1000), obj.get("chance", 1.0))
    return (ident, "item", obj.get("count", 1), obj.get("chance", 1.0))


def parse(rid, j, fluids):
    """Inputs as [[alternatives]], outputs as [(id, kind, amount, chance)] and a few notes."""
    kind = j["type"]
    inputs, outputs, notes = [], [], []
    if kind == "minecraft:crafting_shaped":
        counts = {}
        for row in j["pattern"]:
            for ch in row:
                if ch != " ":
                    counts[ch] = counts.get(ch, 0) + 1
        for ch, n in counts.items():
            inputs.append([(i, k, n) for i, k, _ in stacks(j["key"][ch])])
        outputs.append(result(j["result"], fluids))
    elif kind == "minecraft:crafting_shapeless":
        merged = {}
        for ing in j["ingredients"]:
            key = json.dumps(ing, sort_keys=True)
            merged[key] = merged.get(key, 0) + 1
        for key, n in merged.items():
            inputs.append([(i, k, n) for i, k, _ in stacks(json.loads(key))])
        outputs.append(result(j["result"], fluids))
    elif kind in ("minecraft:blasting", "minecraft:smelting"):
        inputs.append(stacks(j["ingredient"]))
        outputs.append(result(j["result"], fluids))
    elif kind == "create:sequenced_assembly":
        loops = int(j.get("loops", 1))
        inputs.append(stacks(j["ingredient"]))
        extra = {}
        for step in j["sequence"]:
            for ing in step["ingredients"][1:]:
                for i, k, n in stacks(ing):
                    extra[(i, k)] = extra.get((i, k), 0) + n * loops
            notes.append(step["type"].split(":")[-1])
        for (i, k), n in extra.items():
            inputs.append([(i, k, n)])
        results = j.get("results", [])
        total = sum(r.get("chance", 1.0) for r in results) or 1.0
        for r in results:
            ident, rk, amount, chance = result(r, fluids)
            outputs.append((ident, rk, amount, chance / total if len(results) > 1 else chance))
        notes = [f"{loops} loop(s): " + ", ".join(notes)]
    else:
        merged = {}
        for ing in j.get("ingredients", []):
            alts = stacks(ing)
            key = json.dumps([(i, k) for i, k, _ in alts])
            if key in merged:
                merged[key] = [(i, k, n + alts[0][2] if k != "fluid" else n) for i, k, n in merged[key]]
            else:
                merged[key] = alts
        inputs = list(merged.values())
        for r in j.get("results", []):
            outputs.append(result(r, fluids))
    if "heat_requirement" in j and j["heat_requirement"] != "none":
        notes.append(j["heat_requirement"])
    if "min_rpm" in j:
        notes.append(f"{j['min_rpm']}-{j['max_rpm']} RPM")
    if "min_voltage" in j:
        notes.append(f"{j['min_voltage']:g} V")
    if "electrodes" in j:
        notes.append("electrodes: " + ", ".join(e.split(":")[1].replace("_electrode", "") for e in j["electrodes"]))
    if "processing_time" in j:
        notes.append(f"{j['processing_time']} ticks")
    return inputs, outputs, notes


def drop_colour_families(recipes):
    """Removes every family of 8+ recipes whose ids differ only by a colour name; singletons like white vinegar stay."""
    families = collections.defaultdict(list)
    for r in recipes:
        key, n = COLOUR_WORD.subn("<colour>", r["id"])
        if n:
            families[key].append(r["id"])
    dropped = {rid for ids in families.values() if len(ids) >= COLOUR_FAMILY_SIZE for rid in ids}
    return [r for r in recipes if r["id"] not in dropped], len(dropped)


def collect(mods):
    files = glob.glob(os.path.join(DATA, "*", "recipe", "**", "*.json"), recursive=True)
    loaded = []
    fluids = set()
    for f in files:
        j = json.load(open(f, encoding="utf-8"))
        if "type" not in j or not all(condition_ok(c, mods) for c in j.get("neoforge:conditions", [])):
            continue
        for scan in (j.get("ingredients", []), j.get("results", [])):
            for x in scan if isinstance(scan, list) else []:
                if isinstance(x, dict) and "fluid" in x:
                    fluids.add(x["fluid"])
                if isinstance(x, dict) and "amount" in x and "id" in x:
                    fluids.add(x["id"])
        rel = os.path.relpath(f, DATA).replace(os.sep, "/")
        ns, _, path = rel.split("/", 2)
        loaded.append((f"{ns}:{path[:-5]}", j))
    recipes = []
    for rid, j in loaded:
        inputs, outputs, notes = parse(rid, j, fluids)
        ids = {i for alts in inputs for i, _, _ in alts} | {o[0] for o in outputs}
        if j["type"].startswith(MOD + ":") or any(i.startswith(MOD + ":") for i in ids):
            recipes.append({"id": rid, "type": j["type"], "inputs": inputs, "outputs": outputs, "notes": notes})
    return recipes


# ---- page ----------------------------------------------------------------------------------------------------------

def build(recipes, names, mods, hubs):
    nodes, edges, seen = [], [], {}

    def material(ident, kind, rid):
        """The node id for a material in one recipe: its own node, or a per-recipe copy for a hub."""
        node = f"{ident}@{rid}" if ident in hubs else ident
        if node not in seen:
            seen[node] = True
            nodes.append({"data": {"id": node, "canonical": ident, "label": pretty(ident, names), "kind": kind,
                                   "ours": ident.startswith(MOD + ":"), "copy": ident in hubs}})
        return node

    def amount(kind, n):
        return f"{n} mB" if kind == "fluid" else (f"{n}x" if n != 1 else "")

    for r in recipes:
        machine, colour = MACHINES.get(r["type"], (r["type"].split(":")[-1].replace("_", " ").title(), "#888888"))
        rid = "recipe:" + r["id"]
        detail_in = []
        for alts in r["inputs"]:
            for ident, kind, n in alts:
                node = material(ident, kind, rid)
                edges.append({"data": {"source": node, "target": rid, "label": amount(kind, n), "role": "in", "colour": colour, "machine": machine, "rid": rid}})
            detail_in.append(" or ".join(f"{amount(k, n)} {pretty(i, names)}".strip() for i, k, n in alts))
        detail_out = []
        for ident, kind, n, chance in r["outputs"]:
            node = material(ident, kind, rid)
            label = amount(kind, n) + (f" {chance:.0%}" if chance < 1 else "")
            edges.append({"data": {"source": rid, "target": node, "label": label.strip(), "role": "out", "colour": colour, "machine": machine, "rid": rid}})
            detail_out.append(f"{amount(kind, n)} {pretty(ident, names)}".strip() + (f" ({chance:.0%})" if chance < 1 else ""))
        nodes.append({"data": {"id": rid, "label": machine, "kind": "recipe", "machine": machine, "colour": colour,
                               "recipe": r["id"], "inputs": detail_in, "outputs": detail_out, "notes": r["notes"]}})
    legend = sorted({(n["data"]["machine"], n["data"]["colour"]) for n in nodes if n["data"]["kind"] == "recipe"})
    setup = ", ".join(sorted(m for m in mods if m not in ("minecraft", "create", "neoforge", "c", MOD))) or "Create only"
    return PAGE.replace("__ELEMENTS__", json.dumps(nodes + edges)).replace("__LEGEND__", json.dumps(legend)) \
        .replace("__SETUP__", html.escape(setup)).replace("__COUNT__", str(len(recipes)))


PAGE = r"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Recipe Map</title>
<script src="https://cdn.jsdelivr.net/npm/cytoscape@3.30.2/dist/cytoscape.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/dagre@0.8.5/dist/dagre.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/cytoscape-dagre@2.5.0/cytoscape-dagre.min.js"></script>
<style>
:root { --bg:#f6f6f4; --panel:#ffffff; --text:#1d1f22; --muted:#6b7078; --line:#d9d9d4; --item:#ffffff; --fluid:#e3eefa; --tag:#efe9f7; --node-border:#b5b8bd; }
@media (prefers-color-scheme: dark) { :root { --bg:#16181b; --panel:#1f2226; --text:#e6e7e9; --muted:#9aa0a8; --line:#33373d; --item:#2a2e33; --fluid:#1d3246; --tag:#2e2838; --node-border:#555a61; } }
* { box-sizing:border-box; }
body { margin:0; font:14px/1.4 system-ui, sans-serif; background:var(--bg); color:var(--text); display:flex; height:100vh; }
#side { width:320px; flex:none; background:var(--panel); border-right:1px solid var(--line); padding:14px; overflow:auto; }
#cy { flex:1; }
h1 { font-size:17px; margin:0 0 4px; }
.muted { color:var(--muted); font-size:12px; }
label { display:block; margin-top:12px; font-weight:600; font-size:12px; text-transform:uppercase; letter-spacing:.04em; color:var(--muted); }
input[type=text], select { width:100%; padding:7px 8px; border:1px solid var(--line); border-radius:6px; background:var(--bg); color:var(--text); font:inherit; }
.row { display:flex; gap:6px; margin-top:6px; }
button { flex:1; padding:6px; border:1px solid var(--line); border-radius:6px; background:var(--bg); color:var(--text); cursor:pointer; font:inherit; }
button.on { background:var(--text); color:var(--bg); }
.legend div { display:flex; align-items:center; gap:8px; font-size:13px; margin:3px 0; cursor:pointer; user-select:none; }
.legend span { width:18px; height:4px; border-radius:2px; display:inline-block; }
.legend .off { opacity:.3; }
#detail { margin-top:12px; font-size:13px; }
#detail ul { margin:4px 0 8px; padding-left:18px; }
@media (max-width:700px) { body { flex-direction:column; } #side { width:auto; max-height:45vh; border-right:none; border-bottom:1px solid var(--line); } }
</style>
</head>
<body>
<div id="side">
  <h1>Recipe Map</h1>
  <div class="muted">__COUNT__ recipes. Mods assumed installed: __SETUP__.</div>
  <label for="q">Focus on</label>
  <input id="q" type="text" list="materials" placeholder="Item or fluid name">
  <datalist id="materials"></datalist>
  <div class="row"><button data-dir="up">Made from</button><button data-dir="both" class="on">Both</button><button data-dir="down">Used in</button></div>
  <label for="depth">Depth: <span id="depthv">1</span> recipe step(s)</label>
  <input id="depth" type="range" min="1" max="8" value="1" style="width:100%">
  <div class="row"><button id="all">Show everything</button><button id="relayout">Re-layout</button></div>
  <label>Machines (line colour, click to hide)</label>
  <div class="legend" id="legend"></div>
  <div id="detail" class="muted">Lines are recipes: inputs run into a dot, arrows leave it toward the outputs. Hover a material to highlight its recipes, click it to focus, click a dot or line for the recipe.</div>
</div>
<div id="cy"></div>
<script>
const elements = __ELEMENTS__;
const legend = __LEGEND__;
const css = getComputedStyle(document.documentElement);
const v = name => css.getPropertyValue(name).trim();
const labelWidth = n => Math.min(n.data('label').length, 18) * 6.3 + 6;
const labelHeight = n => Math.ceil(n.data('label').length / 18) * 13 + 4;
const cy = cytoscape({
  container: document.getElementById('cy'), elements, minZoom: 0.05, maxZoom: 4,
  style: [
    { selector: 'node', style: { 'label': 'data(label)', 'font-size': 11, 'color': v('--text'), 'text-valign': 'center', 'text-wrap': 'wrap', 'text-max-width': 110,
      'min-zoomed-font-size': 7, 'width': labelWidth, 'height': labelHeight, 'padding': 6, 'shape': 'round-rectangle',
      'background-color': v('--item'), 'border-width': 1, 'border-color': v('--node-border') } },
    { selector: 'node[kind="fluid"]', style: { 'shape': 'ellipse', 'background-color': v('--fluid') } },
    { selector: 'node[kind="tag"]', style: { 'shape': 'cut-rectangle', 'background-color': v('--tag') } },
    { selector: 'node[?ours]', style: { 'border-width': 2, 'border-color': v('--text') } },
    { selector: 'node[?copy]', style: { 'font-size': 9, 'border-style': 'dashed', 'border-width': 1, 'border-color': v('--node-border'), 'padding': 3 } },
    // A recipe is only a junction: its machine colour lives on the lines.
    { selector: 'node[kind="recipe"]', style: { 'label': '', 'width': 7, 'height': 7, 'padding': 0, 'shape': 'ellipse', 'background-color': 'data(colour)', 'border-width': 0 } },
    { selector: 'edge', style: { 'width': 1.6, 'line-color': 'data(colour)', 'opacity': 0.75, 'curve-style': 'bezier',
      'label': 'data(label)', 'font-size': 8, 'min-zoomed-font-size': 9, 'color': v('--muted'),
      'text-background-color': v('--bg'), 'text-background-opacity': 0.85, 'text-background-padding': 1 } },
    { selector: 'edge[role="out"]', style: { 'target-arrow-shape': 'triangle', 'target-arrow-color': 'data(colour)', 'arrow-scale': 0.8 } },
    { selector: '.focus', style: { 'border-width': 4, 'border-color': '#e0a31c' } },
    { selector: '.faded', style: { 'opacity': 0.08 } },
    { selector: 'node.lit', style: { 'border-width': 2, 'border-color': '#e0a31c' } },
    { selector: 'edge.lit', style: { 'width': 3, 'opacity': 1 } },
    { selector: '.hidden', style: { 'display': 'none' } },
  ],
});

// Layered (dagre): recipes flow left to right, inputs before outputs.
function layout() {
  cy.elements(':visible').layout({ name: 'dagre', rankDir: 'LR', nodeSep: 6, rankSep: 28, animate: false }).run();
  fitView();
}
function fitView() {
  cy.fit(cy.elements(':visible'), 30);
  if (focus && cy.zoom() < 0.7) { cy.zoom(0.7); cy.center(focus.first()); }
}

// Crafting is half of all recipes and mostly ties everything to steel, so the overview starts without it.
const hiddenMachines = new Set(['Crafting']);
let dir = 'both', focus = null;

const materials = cy.nodes().filter(n => n.data('kind') !== 'recipe').map(n => n.data('label')).sort();
// Every node of one material: itself, or all the per-recipe copies of a hub.
const group = n => cy.nodes().filter(m => m.data('canonical') === n.data('canonical'));
document.getElementById('materials').innerHTML = [...new Set(materials)].map(m => `<option value="${m}">`).join('');
const legendBox = document.getElementById('legend');
legend.forEach(([name, colour]) => {
  const row = document.createElement('div');
  row.innerHTML = `<span style="background:${colour}"></span>${name}`;
  if (hiddenMachines.has(name)) row.classList.add('off');
  row.onclick = () => { hiddenMachines.has(name) ? hiddenMachines.delete(name) : hiddenMachines.add(name); row.classList.toggle('off'); apply(); };
  legendBox.appendChild(row);
});

const hiddenRecipe = n => n.data('kind') === 'recipe' && hiddenMachines.has(n.data('machine'));
function walk(start, depth, forward) {
  let frontier = cy.collection(start), all = cy.collection(start);
  for (let i = 0; i < depth * 2; i++) {
    const next = (forward ? frontier.outgoers('node') : frontier.incomers('node')).filter(n => !hiddenRecipe(n));
    frontier = next.difference(all);
    all = all.union(next);
    if (frontier.empty()) break;
  }
  return all;
}

function apply() {
  cy.batch(() => {
    cy.elements().removeClass('hidden focus faded lit');
    let keep;
    if (focus) {
      const depth = +document.getElementById('depth').value;
      keep = focus;
      focus.forEach(f => {
        if (dir !== 'down') keep = keep.union(walk(f, depth, false));
        if (dir !== 'up') keep = keep.union(walk(f, depth, true));
      });
      // A recipe in view brings all its inputs and outputs, so each one reads completely.
      keep = keep.union(keep.filter('[kind="recipe"]').neighborhood('node'));
      focus.addClass('focus');
    } else {
      keep = cy.nodes();
    }
    keep = keep.filter(n => !hiddenRecipe(n));
    cy.nodes().difference(keep).addClass('hidden');
    // A material left with no kept recipe is noise. (Checked against keep, not :visible, which only updates after the batch.)
    keep.filter(n => n.data('kind') !== 'recipe' && !(focus && focus.contains(n)) && n.neighborhood('node[kind="recipe"]').intersection(keep).empty()).addClass('hidden');
  });
  layout();
}

function setFocus(node) {
  focus = node ? group(node) : null; document.getElementById('q').value = node ? node.data('label') : '';
  history.replaceState(null, '', node ? '#' + encodeURIComponent(node.data('label')) : location.pathname + location.search);
  apply();
}
document.getElementById('q').addEventListener('change', e => {
  const node = cy.nodes().filter(n => n.data('kind') !== 'recipe' && n.data('label').toLowerCase() === e.target.value.toLowerCase()).first();
  if (node.nonempty()) setFocus(node);
});
document.querySelectorAll('[data-dir]').forEach(b => b.onclick = () => {
  dir = b.dataset.dir; document.querySelectorAll('[data-dir]').forEach(x => x.classList.toggle('on', x === b)); apply();
});
document.getElementById('depth').oninput = e => { document.getElementById('depthv').textContent = e.target.value; if (focus) apply(); };
document.getElementById('all').onclick = () => setFocus(null);
document.getElementById('relayout').onclick = () => layout();


// Hover: light up a material's recipes (or one recipe) and fade the rest.
function highlight(target) {
  cy.batch(() => {
    const nodes = target.data('kind') === 'recipe' ? target : group(target);
    const recipes = target.data('kind') === 'recipe' ? target : nodes.neighborhood('node[kind="recipe"]');
    const lit = recipes.union(recipes.neighborhood()).union(nodes);
    cy.elements(':visible').difference(lit).addClass('faded');
    lit.addClass('lit');
  });
}
cy.on('mouseover', 'node', e => highlight(e.target));
cy.on('mouseover', 'edge', e => highlight(cy.getElementById(e.target.data('rid'))));
cy.on('mouseout', 'node, edge', () => cy.elements().removeClass('faded lit'));

const esc = s => s.replace(/[&<>]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;'}[c]));
function showRecipe(n) {
  const list = xs => '<ul>' + xs.map(x => `<li>${esc(x)}</li>`).join('') + '</ul>';
  document.getElementById('detail').innerHTML = `<b style="color:${n.data('colour')}">${esc(n.data('machine'))}</b> <span class="muted">${esc(n.data('recipe'))}</span>`
    + `<div>In:</div>${list(n.data('inputs'))}<div>Out:</div>${list(n.data('outputs'))}`
    + (n.data('notes').length ? `<div class="muted">${esc(n.data('notes').join(' · '))}</div>` : '');
}
cy.on('tap', 'node', e => e.target.data('kind') === 'recipe' ? showRecipe(e.target) : setFocus(e.target));
cy.on('tap', 'edge', e => showRecipe(cy.getElementById(e.target.data('rid'))));

// A link like recipe-map.html#Steel Ingot opens focused on that material.
const fromHash = decodeURIComponent(location.hash.slice(1));
const start = fromHash && cy.nodes().filter(n => n.data('kind') !== 'recipe' && n.data('label').toLowerCase() === fromHash.toLowerCase()).first();
if (start && start.nonempty()) setFocus(start); else apply();
</script>
</body>
</html>
"""


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", default=os.path.join(ROOT, "build", "recipe-map.html"))
    parser.add_argument("--without", action="append", default=[], choices=sorted(OPTIONAL), help="treat this optional mod as absent")
    parser.add_argument("--hub", action="append", default=[], help="also split this material id into per-recipe copies")
    parser.add_argument("--no-hubs", action="store_true", help="draw every material once, even water and oxygen")
    parser.add_argument("--keep-colours", action="store_true", help="keep one-per-colour recipe families (paints, envelopes, whitening)")
    args = parser.parse_args()
    mods = loaded_mods(set(args.without))
    TAGS.update(load_tags())
    recipes = collect(mods)
    dropped = 0
    if not args.keep_colours:
        recipes, dropped = drop_colour_families(recipes)
    hubs = set() if args.no_hubs else set(HUBS) | set(args.hub)
    page = build(recipes, load_names(), mods, hubs)
    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fh:
        fh.write(page)
    print(f"{len(recipes)} recipes -> {args.out}" + (f" ({dropped} one-per-colour recipes left out, --keep-colours to include)" if dropped else ""))


if __name__ == "__main__":
    main()
