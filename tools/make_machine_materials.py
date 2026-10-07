#!/usr/bin/env python3
"""Draws the seamless materials of the formed machine models (own artwork): flat brushed metals, a riveted plate and a sight glass.

Each tiles once per block, so a machine reads as plating rather than a grid of blocks. Usage: python tools/make_machine_materials.py
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import ct_sheet  # noqa: E402
import recolor  # noqa: E402

OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions",
                   "textures", "block", "machine")


def metal(colour, rivets=False, seed=7):
    img = Image.new("RGBA", (16, 16), recolor._rgb(colour) + (255,))
    img = recolor.brushed(img, 0.035, seed)
    return recolor.rivets(img, 3) if rivets else img


def sight_glass():
    """Clear glass: only its frame-side glints are drawn, so the machine's contents show through."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for i in range(3, 10):
        px[i, 12 - i] = (190, 232, 240, 255)
    for i in range(6, 10):
        px[i, 15 - i] = (143, 208, 220, 255)
    return img


def copy_of(name):
    """A block texture of this mod, unchanged: the formed models need their own sprite, untouched by the walls' connected textures."""
    return lambda: Image.open(os.path.join(os.path.dirname(OUT), name + ".png")).convert("RGBA")


def plastic(seams):
    """32px off-white HDPE for the Plastic Pipe (the size of the pipe sprites it replaces); `seams` draws a faint line round each
    8px tile, the pieces the connected-pipe sheet is cut into."""
    img = recolor.brushed(Image.new("RGBA", (32, 32), (222, 224, 218, 255)), 0.03, 37)
    if seams:
        px = img.load()
        for y in range(32):
            for x in range(32):
                if x % 8 in (0, 7) or y % 8 in (0, 7):
                    r, g, b, a = px[x, y]
                    px[x, y] = (int(r * 0.85), int(g * 0.85), int(b * 0.85), a)
    return img


def steel_casing_panel():
    """The Steel Casing's face without its frame (ct_sheet adds the frame on each side not joined to a neighbour): brushed steel
    round a recessed plate held by four bolts, so a wall of casing reads as bolted plates."""
    img = recolor.brushed(Image.new("RGBA", (16, 16), recolor._rgb("#939daa") + (255,)), 0.03, 41)
    px = img.load()

    def shade(x, y, f):
        r, g, b, a = px[x, y]
        px[x, y] = (min(255, int(r * f)), min(255, int(g * f)), min(255, int(b * f)), a)

    for i in range(3, 13):
        shade(i, 3, 0.62); shade(3, i, 0.62)      # the plate's recess, shadowed top and left
        shade(i, 12, 1.22); shade(12, i, 1.22)    # and lit bottom and right
    for y in range(4, 12):
        for x in range(4, 12):
            shade(x, y, 1.06)
    for x, y in ((5, 5), (10, 5), (5, 10), (10, 10)):
        shade(x, y, 1.45); shade(x + 1, y + 1, 0.55)
    return img


def hazard():
    """Yellow and black diagonal stripes, 4px wide, tiling across blocks."""
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), (224, 176, 32, 255) if ((x + y) // 4) % 2 == 0 else (36, 36, 38, 255))
    return img


MATERIALS = {
    "hazard": hazard,
    "vat_paint": lambda: metal("#4e5866", rivets=True, seed=29),
    "oven_brick": copy_of("airless_oven_wall"),
    # The Reaction Chamber wall's own colour with its tint baked in, riveted like the walls.
    "reaction_chamber_plate": lambda: metal("#9aa5b3", rivets=True),
    "steel_band": lambda: metal("#7c838c", seed=11),
    "steel_dark": lambda: metal("#43484f", seed=13),
    "steel_darker": lambda: metal("#24272b", seed=23),
    "motor_paint": lambda: metal("#58775b", seed=17),
    "paint_red": lambda: metal("#b23a2e", seed=19),
    "copper_plate": lambda: metal("#b4694a", seed=31),
    "sight_glass": sight_glass,
}


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, make in MATERIALS.items():
        make().save(os.path.join(OUT, name + ".png"))
    blocks = os.path.dirname(OUT)
    panel = steel_casing_panel()
    # The plain texture shows the whole frame; the connected sheet drops it where casings join.
    ct_sheet.draw_tile(panel, False, False, False, False, False, False, False, False).save(os.path.join(blocks, "steel_casing.png"))
    ct_sheet.ct_sheet(panel).save(os.path.join(blocks, "steel_casing_connected.png"))
    plastic(False).save(os.path.join(blocks, "plastic_pipes.png"))
    plastic(True).save(os.path.join(blocks, "plastic_pipes_connected.png"))


if __name__ == "__main__":
    main()
