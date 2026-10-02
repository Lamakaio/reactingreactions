#!/usr/bin/env python3
"""Draws the hand-made item icons (own artwork): each sprite is a 16x16 character grid with its palette.

Usage: python tools/draw_icons.py [--out <dir>]   (default: the mod's item texture folder)
"""
import argparse
import math
import os

from PIL import Image

TEXTURES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions", "textures")
ITEMS = os.path.join(TEXTURES, "item")

OUTLINE = "#1c1d21"

SPRITES = {
    "gas_mask": ({
        "o": OUTLINE, "R": "#4b524c", "h": "#69726a", "r": "#363b37", "G": "#9aa09a", "g": "#6c716c",
        "L": "#5d97a8", "l": "#c9f0f6", "F": "#7b8048", "f": "#565a30", "m": "#a6ab6b", "d": "#2c2e1b", "s": "#5a4632",
    }, [
        "................",
        ".....oooooo.....",
        "...ooRhhhhRoo...",
        "..oRhRRRRRRRRo..",
        "ssoGGGGRRGGGGoss",
        "..oGlLgRRGlLgo..",
        "..oGLLgRRGLLgo..",
        "ssoggggRRggggoss",
        "..oRRRRhhRRRRo..",
        "...oRRrRRrRRo...",
        "....orRRRRro....",
        ".....oGGGGo.....",
        "....ofFmmFfo....",
        "....ofdFFdfo....",
        "....ofFmmFfo....",
        ".....oooooo.....",
    ]),
    "oxygen_mask": ({
        "o": OUTLINE, "W": "#5f7f62", "w": "#425a45", "h": "#86a688", "V": "#c9ced4", "v": "#7f878f",
        "C": "#c98a4b", "c": "#8f5a2c", "T": "#8a949e", "t": "#5a636c", "S": "#5b6470",
    }, [
        "................",
        "..oooooooooooo..",
        ".oSSSSSSSSSSSSo.",
        ".oSo...oo...oSo.",
        ".oSo..oWWo..oSo.",
        "..oo.oWhWWo.oo..",
        "....oWhWWWWo....",
        "....oWhWVWWo....",
        "...oWhWVvVWwo...",
        "...oWWWWvWwwo...",
        "....owwwwwwo....",
        ".....ooCCoo.....",
        "......ocCo......",
        ".....oTtTo......",
        "....otTto.......",
        "....oooo........",
    ]),
    "diving_fins": ({
        "o": OUTLINE, "B": "#2a2d33", "b": "#3c414a", "Y": "#f2c230", "y": "#c48f1c", "h": "#ffe680",
    }, [
        "................",
        "..oooo...oooo...",
        "..obbo...obbo...",
        "..oBBo...oBBo...",
        "..oBBo...oBBo...",
        ".oYhYYo.oYhYYo..",
        ".oYhYYo.oYhYYo..",
        ".oyYhYYooyYhYYo.",
        "oyYYhYYooyYYhYYo",
        "oyYYhYyooyYYhYyo",
        "oyYYYhyooyYYYhyo",
        "oyYyYhyooyYyYhyo",
        "oyYyYYhooyYyYYho",
        "oyyyyyyooyyyyyyo",
        ".oyoyoo..oyoyoo.",
        "..o.o.....o.o...",
    ]),
    "racing_anklet": ({
        "o": OUTLINE, "G": "#e3b23c", "g": "#a8771e", "h": "#fff0a8", "R": "#d8402f", "r": "#8e1f1a", "l": "#ff9a8a",
    }, [
        "................",
        "................",
        "................",
        ".....oooooo.....",
        "...ooghhhhgoo...",
        "..oghoo..oohgo..",
        ".ogho......ohgo.",
        ".oGo........oGo.",
        ".oGo........oGo.",
        ".ogGoo....ooGgo.",
        "..ogGGoooGGGgo..",
        "...oogggoRRoo...",
        ".......orlRro...",
        ".......orRRro...",
        "........orro....",
        ".........oo.....",
    ]),
    "flame_retardant_cloak": ({
        "o": OUTLINE, "S": "#c3c7cc", "s": "#8f959c", "h": "#f4f6f8", "d": "#5f656c", "O": "#e2702c", "k": "#26282c",
    }, [
        "......oooo......",
        ".....ohSSso.....",
        "....ohSSSSso....",
        "....oSkkkkso....",
        "....oSkkkkso....",
        "...ohSskkSSso...",
        "...ohSSOOSSso...",
        "..ohSSsSSsSSso..",
        "..ohSSsSSsSSso..",
        "..ohSSsSSsSSdo..",
        ".ohSSsSSSsSSsdo.",
        ".ohSSsSSSsSSsdo.",
        ".ohSsSSSSSsSsdo.",
        "ohSSsSSSSSsSSsdo",
        "oOOOOOOOOOOOOOOo",
        ".oooooooooooooo.",
    ]),
    "acetylene_lamp": ({
        "o": OUTLINE, "B": "#d4a547", "b": "#9b6f27", "h": "#f7dc8c", "M": "#d9dde2", "m": "#9aa1a9", "F": "#fff3b0",
        "f": "#ffb347", "K": "#5a5f66",
    }, [
        "........ooo.....",
        ".......oKKKo....",
        ".......oK.oKo...",
        ".......oo..oKo..",
        "....oooooo..oo..",
        "...oMMMMMMo.....",
        "..oMmmmmmMMo....",
        "..oMmffFmmMoo...",
        "..oMmfFFmMoBo...",
        "..oMmmmmmMobBo..",
        "...oMMMMMMobBo..",
        "....ooooooobBo..",
        ".........obhBBo.",
        ".........obhBBo.",
        ".........obhBBo.",
        "..........ooooo.",
    ]),
    # Sheet for the held 3D lamp: reflector | brass, then iron handle, steel edge, flame, dark brass.
    "acetylene_lamp_model": ({
        "m": "#7d848c", "M": "#c9ced4", "W": "#f1f3f5", "F": "#fff3b0", "f": "#ffb347", "c": "#7fb2ff",
        "K": "#5a5f66", "k": "#3c4046", "B": "#d4a547", "b": "#9b6f27", "h": "#f7dc8c", "d": "#6e4d1a",
    }, [
        "mmmmmmmmdbbbbbbd",
        "mMMMMMMmbBhhBBBb",
        "mMWWWWMmbBhhBBBb",
        "mMWFFWMmbBhhBBBb",
        "mMWFFWMmbBhhBBBb",
        "mMWWWWMmbBhhBBBb",
        "mMMMMMMmbBhhBBBb",
        "mmmmmmmmdbbbbbbd",
        "kKKkmMMm..F.dbbd",
        "kKKkmMMm.FF.dbbd",
        "kKKkmMMm.FF.dbbd",
        "kKKkmMMmfFFfdbbd",
        "kKKkmMMmfFFfdbbd",
        "kKKkmMMmffffdbbd",
        "kKKkmMMm.ff.dbbd",
        "kKKkmMMm.cc.dbbd",
    ]),
    "spring_boots": ({
        "o": OUTLINE, "L": "#8a5a34", "l": "#5e3a1e", "h": "#b07c4c", "S": "#bfc6cc", "s": "#7c858d", "K": "#2e2b28",
    }, [
        "................",
        "..oooo....oooo..",
        "..ohLo....ohLo..",
        "..ohLo....ohLo..",
        "..ohLo....ohLo..",
        "..ohLlo...ohLlo.",
        "..ohLLoo..ohLLoo",
        "..ohLLLlo.ohLLLl",
        "..oKKKKKo.oKKKKK",
        "...oSSo....oSSo.",
        "...osso....osso.",
        "...oSSo....oSSo.",
        "...osso....osso.",
        "...oSSo....oSSo.",
        "..oKKKKo..oKKKKo",
        "...oooo....oooo.",
    ]),
}


def hex_rgba(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def draw(palette, rows):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), "sprites are 16x16"
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), hex_rgba(palette[ch]))
    return img


def bow(pull):
    """Titanium bow, drawn like vanilla's: limbs bulging to the upper left, string pulled back by `pull` pixels (0 = idle)."""
    limb, limb_dark, grip, string = hex_rgba("#c9d2dc"), hex_rgba("#6b7684"), hex_rgba("#2f4f7a"), hex_rgba("#eef1f4")
    shaft, head, fletch = hex_rgba("#8a6a42"), hex_rgba("#aab3bd"), hex_rgba("#e8e8e8")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    tip_a, tip_b, ctrl = (14.0, 1.0), (1.0, 14.0), (1.0 + pull * 0.4, 1.0 + pull * 0.4)
    arc = []
    for i in range(200):
        t = i / 199
        x = (1 - t) ** 2 * tip_a[0] + 2 * (1 - t) * t * ctrl[0] + t * t * tip_b[0]
        y = (1 - t) ** 2 * tip_a[1] + 2 * (1 - t) * t * ctrl[1] + t * t * tip_b[1]
        p = (round(x), round(y))
        if not arc or arc[-1] != p:
            arc.append(p)
    nock = (7.5 + pull, 7.5 + pull)
    for a, b in ((tip_a, nock), (nock, tip_b)):
        steps = 40
        for i in range(steps + 1):
            t = i / steps
            img.putpixel((round(a[0] + (b[0] - a[0]) * t), round(a[1] + (b[1] - a[1]) * t)), string)
    for x, y in arc:
        if 0 <= x + 1 < 16 and 0 <= y + 1 < 16 and img.getpixel((x + 1, y + 1))[3] == 0:
            img.putpixel((x + 1, y + 1), limb_dark)
    for x, y in arc:
        img.putpixel((x, y), limb)
    mid = arc[len(arc) // 2]
    for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
        img.putpixel((mid[0] + dx, mid[1] + dy), grip)
    if pull:
        nx, ny = round(nock[0]), round(nock[1])
        length = 7 + pull * 2
        for i in range(length):
            x, y = nx - i, ny - i
            if 0 <= x < 16 and 0 <= y < 16:
                img.putpixel((x, y), shaft)
        hx, hy = nx - length, ny - length
        for x, y in ((hx, hy), (hx + 1, hy), (hx, hy + 1)):
            if 0 <= x < 16 and 0 <= y < 16:
                img.putpixel((x, y), head)
        for x, y in ((nx + 1, ny), (nx, ny + 1)):
            if x < 16 and y < 16:
                img.putpixel((x, y), fletch)
    return img


# Material swatches for the 3D equipment models (worn and held), 4x4 each in reading order (index used by SwatchModels).
WORN_SWATCHES = [
    "#3a3f3b", "#5d6660", "#7fc8d8", "#7b8048",  # rubber, light rubber, lens, filter canister
    "#d4a547", "#c9ced4", "#5a5f66", "#5f7f62",  # brass, steel, dark steel, oxygen-mask green
    "#d0602a", "#8a3a1a", "#2f6fa8", "#1f4a72",  # cloak, cloak lining, fin, fin strap
    "#8a5a34", "#5e3a1e", "#b8433a", "#2e2b28",  # leather, dark leather, aerozine tank, nozzle
    # Second sheet (worn_equipment_2), indices 16 on.
    "#e8c040", "#c0283a", "#e0b020", "#9a7410",  # gold, ruby, glove rubber, glove cuff
    "#3f6f4a", "#2a4a32", "#c8402a", "#e8e8e8",  # boot rubber, dark boot rubber, anklet red, white
    "#a0a4a8", "#9ad8e8", "#7ff0ff", "#ff4a3a",  # chain, helium glass, neon cyan, neon red
    "#d8d0c0", "#a89880", "#606870", "#202428",  # spare greys
]


def shade(rgba, factor):
    return tuple(max(0, min(255, round(c * factor))) for c in rgba[:3]) + (255,)


def worn_swatches(sheet):
    """Sheet 0 or 1 of 16 swatches, each lit from above: a light top row, a dark bottom row."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i, colour in enumerate(WORN_SWATCHES[sheet * 16:sheet * 16 + 16]):
        base = hex_rgba(colour)
        for y in range(4):
            factor = 1.18 if y == 0 else 0.82 if y == 3 else 1.0
            for x in range(4):
                img.putpixel(((i % 4) * 4 + x, (i // 4) * 4 + y), shade(base, factor))
    return img


def controller_dial():
    """The progress dial on multiblock controllers: a 270 degree scale, red at the end. The needle takes its colour from the red tick at (13, 8)."""
    rim, face, tick, red = hex_rgba("#2e2b28"), hex_rgba("#e8e2cf"), hex_rgba("#46403a"), hex_rgba("#c0392b")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
            if r <= 6.4:
                img.putpixel((x, y), face)
            elif r <= 7.6:
                img.putpixel((x, y), rim)
    for angle in range(-135, 136, 45):
        t = math.radians(angle)
        for radius in (5.0, 5.8):
            x, y = int(8 + math.sin(t) * radius), int(8 - math.cos(t) * radius)
            img.putpixel((min(15, max(0, x)), min(15, max(0, y))), red if angle >= 90 else tick)
    return img


def purger():
    """The Purger: a valve wand leaning like a tool, rubber grip at the lower left, red handwheel round the brass valve, nozzle up."""
    colours = {
        "grip": hex_rgba("#3a3f3b"), "shaft": hex_rgba("#c9ced4"), "valve": hex_rgba("#d4a547"), "nozzle": hex_rgba("#5a5f66"),
        "wheel": hex_rgba("#c8402a"),
    }
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    start, length = (2.0, 14.0), 12 * math.sqrt(2)
    for y in range(16):
        for x in range(16):
            vx, vy = x + 0.5 - start[0], y + 0.5 - start[1]
            along = (vx - vy) / math.sqrt(2) / length
            across = (vx + vy) / math.sqrt(2)
            # The handwheel, seen edge-on: a flat ellipse across the shaft just below the valve, behind it.
            if ((along - 0.48) * length / 1.6) ** 2 + (across / 4.3) ** 2 <= 1.0:
                img.putpixel((x, y), shade(colours["wheel"], 0.75 if across > 0.5 else 1.1))
            for part, until, half in (("grip", 0.4, 1.1), ("shaft", 0.54, 0.6), ("valve", 0.7, 1.5), ("nozzle", 0.95, 0.75)):
                if along < until:
                    if 0 <= along and abs(across) <= half:
                        img.putpixel((x, y), shade(colours[part], 0.75 if across > 0.2 else 1.15 if across < -0.2 else 1.0))
                    break
    outline = hex_rgba(OUTLINE)
    filled = {(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3]}
    for x, y in [(x + dx, y + dy) for x, y in filled for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]:
        if 0 <= x < 16 and 0 <= y < 16 and (x, y) not in filled:
            img.putpixel((x, y), outline)
    return img


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", default=ITEMS)
    out = parser.parse_args().out
    os.makedirs(out, exist_ok=True)
    for name, (palette, rows) in SPRITES.items():
        draw(palette, rows).save(os.path.join(out, name + ".png"))
    worn_swatches(0).save(os.path.join(out, "worn_equipment.png"))
    worn_swatches(1).save(os.path.join(out, "worn_equipment_2.png"))
    controller_dial().save(os.path.join(TEXTURES, "block", "controller_dial.png"))
    bow(0).save(os.path.join(out, "titanium_bow.png"))
    purger().save(os.path.join(out, "purger.png"))
    for i, pull in enumerate((1, 2, 3)):
        bow(pull).save(os.path.join(out, f"titanium_bow_pulling_{i}.png"))


if __name__ == "__main__":
    main()
