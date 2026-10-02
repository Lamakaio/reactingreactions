#!/usr/bin/env python3
"""Build every imported texture from the open-licensed sources and regenerate credits.md.

Usage:  python tools/build_textures.py --repos <dir>

<dir> must contain a checkout per source, named as in texture_manifest.json
("sources"): unused-textures, foreck-textures, TextureRepository, ... Each manifest
entry copies one source PNG (optionally passing it through tools/recolor.py
operations) into src/main/resources/assets/reactingreactions/textures/<out>.png. The
script also writes credits.md from the sources that are actually used.
"""
import argparse
import json
import os
import shutil
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import ct_sheet  # noqa: E402
import pixelate  # noqa: E402
import recolor  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXTURES = os.path.join(ROOT, "src", "main", "resources", "assets", "reactingreactions", "textures")

OWN_ART = [
    ("draw_icons.py", ["item/gas_mask", "item/oxygen_mask", "item/diving_fins", "item/racing_anklet", "item/flame_retardant_cloak",
                       "item/acetylene_lamp", "item/acetylene_lamp_model", "item/spring_boots", "item/titanium_bow", "item/titanium_bow_pulling_0..2", "item/worn_equipment", "item/worn_equipment_2", "item/purger",
                       "block/controller_dial"]),
    ("make_neon_lamp.py", ["block/neon_lamp"]),
    ("make_coil.py", ["block/induction_coil", "block/induction_coil_circle"]),
    ("make_machine_materials.py", ["block/machine/reaction_chamber_plate", "block/machine/steel_band", "block/machine/steel_dark", "block/machine/steel_darker",
                                   "block/machine/motor_paint", "block/machine/paint_red", "block/machine/copper_plate", "block/machine/sight_glass", "block/machine/hazard", "block/machine/vat_paint",
                                   "block/machine/oven_brick (copy of block/airless_oven_wall)", "block/plastic_pipes",
                                   "block/plastic_pipes_connected"]),
    ("make_flecks.py", ["block/rich_vein_flecks"]),
    ("make_particles.py", ["particle/toxic_haze_0..3", "misc/toxic_vignette", "misc/toxic_stain"]),
    ("make_logo.py", ["logo.png (with imported item icons)"]),
]


def build(repos):
    with open(os.path.join(os.path.dirname(__file__), "texture_manifest.json"), encoding="utf-8") as fh:
        manifest = json.load(fh)
    used = {}
    for entry in manifest["textures"]:
        source = manifest["sources"][entry["source"]]
        src = os.path.join(repos, entry["source"], entry["src"])
        if not os.path.isfile(src):
            raise SystemExit(f"missing source texture: {src}")
        img = Image.open(src).convert("RGBA")
        for op in entry.get("ops", []):
            img = recolor.apply(img, op[0], *op[1:])
        if entry.get("ct"):
            # Connected-texture sheet (Create's omnidirectional layout) drawn from the finished base texture.
            img = ct_sheet.ct_sheet(img)
        out = os.path.join(TEXTURES, entry["out"] + ".png")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        img.save(out)
        mcmeta = src + ".mcmeta"
        if os.path.isfile(mcmeta):
            shutil.copyfile(mcmeta, out + ".mcmeta")
        used.setdefault(entry["source"], []).append(entry)
    photos = manifest.get("photos", [])
    for p in photos:
        src = os.path.join(os.path.dirname(__file__), "photo_sources", p["file"])
        out = os.path.join(TEXTURES, p["out"] + ".png")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        opts = p.get("pixelate", {})
        result = pixelate.pixelate(Image.open(src), opts.get("size", 16), opts.get("tolerance", 45), opts.get("colors", 12),
                          opts.get("outline", True), opts.get("crop"), holes=opts.get("holes", False),
                          rotate=opts.get("rotate", 0))
        for op in p.get("ops", []):
            result = recolor.apply(result, op[0], *op[1:])
        result.save(out)
    write_credits(manifest, used, photos)
    print(f"built {sum(len(v) for v in used.values())} textures from {len(used)} sources and {len(photos)} pixelated photographs")


def write_credits(manifest, used, photos):
    lines = [
        "# Credits",
        "",
        "Textures in `src/main/resources/assets/reactingreactions/textures/` are imported (and sometimes recolored) from the",
        "open-licensed sources below by `tools/build_textures.py`; the exact source file and recolor steps for every texture",
        "are in `tools/texture_manifest.json`. All artwork is credited to the original artists. Assets made by an LLM are",
        "CC0 (see `LICENSE-LLM-CONTENT.md`), the mod's other assets CC BY-NC-SA 4.0 (see `LICENSE-ASSETS`); code and data are",
        "MIT (see `LICENSE`).",
        "",
        "Textures belonging to other mods (Create, Create Aeronautics, ...) and to Minecraft itself are never copied or",
        "edited: they are referenced, and where needed recolored at runtime inside the game.",
        "",
    ]
    for key, entries in used.items():
        source = manifest["sources"][key]
        lines += [
            f"## {source['name']}",
            "",
            f"- Author: {source['author']}",
            f"- Source: {source['url']}",
            f"- Licence: {source['license']}",
            f"- Modifications: some textures recolored or split with `tools/recolor.py` (marked below).",
            "",
            "| Texture | Original file | Recolored |",
            "|---|---|---|",
        ]
        for e in entries:
            lines.append(f"| `{e['out']}` | `{e['src']}` | {'connected-texture sheet generated by tools/ct_sheet.py' if e.get('ct') else ('yes' if e.get('ops') else 'no')} |")
        lines.append("")
    if photos:
        lines += [
            "## Pixelated photographs (placeholders)",
            "",
            "These icons were made programmatically by `tools/pixelate.py` (background cut-out, downsampling, palette",
            "reduction, outline) from the photographs below, which are kept downscaled in `tools/photo_sources/`. No image",
            "was AI-generated. Photographs come from Wikimedia Commons under the licences shown.",
            "",
            "| Texture | Photograph | Author | Licence |",
            "|---|---|---|---|",
        ]
        for p in photos:
            lines.append(f"| `{p['out']}` | [{p['title']}]({p['url']}) | {p['author']} | {p['license']} |")
        lines.append("")
    lines += [
        "## Own artwork",
        "",
        "Drawn for this mod by an LLM through the scripts below (see `LICENSE-LLM-CONTENT.md`), no third-party image involved,",
        "except the logo, which reuses item textures credited above.",
        "",
        "| Script | Textures |",
        "|---|---|",
    ]
    for script, outs in OWN_ART:
        lines.append(f"| `tools/{script}` | {', '.join(f'`{o}`' for o in outs)} |")
    lines.append("")
    with open(os.path.join(ROOT, "credits.md"), "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--repos", required=True)
    build(parser.parse_args().repos)
