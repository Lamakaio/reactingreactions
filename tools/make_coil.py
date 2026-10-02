#!/usr/bin/env python3
"""Draws the induction heater's coil segment (own artwork): a hub and a line on transparency, in light gray.

The block tints it by the heat level, so it glows from dull gray to white-hot. Usage: python tools/make_coil.py
"""
import os

from PIL import Image

OUT_CIRCLE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions", "textures", "block", "induction_coil_circle.png")
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions", "textures", "block", "induction_coil.png")


def main():
    """One segment of the big coil: a hub in the middle and a line from it to the north edge, in light gray.

    The block model turns it to each side where the coil's loop continues (see InductionHeaterCoil), so the plates
    together show concentric rectangular loops.
    """
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(0, 9):
        for x in (7, 8):
            img.putpixel((x, y), (255, 255, 255, 255) if y % 4 else (225, 225, 225, 255))
    for x in (6, 7, 8, 9):
        for y in (6, 7, 8, 9):
            img.putpixel((x, y), (255, 255, 255, 255))
    img.save(OUT)
    # The circle for blocks whose loop is too thin to close: a ring around the centre with a hub.
    circle = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 4.0 <= d <= 5.6 or d <= 1.4:
                circle.putpixel((x, y), (255, 255, 255, 255) if d > 1.4 else (230, 230, 230, 255))
    circle.save(OUT_CIRCLE)


if __name__ == "__main__":
    main()
