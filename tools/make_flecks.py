#!/usr/bin/env python3
"""Draws the ore flecks overlay of the Rich Vein blocks (own artwork, no third-party image): a few light clusters on transparency.

The block model tints this overlay by the deposit's richness, so the pixels are light grays, not gold.
Usage: python tools/make_flecks.py
"""
import os
import random

from PIL import Image

OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions", "textures", "block", "rich_vein_flecks.png")


def main():
    rng = random.Random(11)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    shades = [(255, 255, 255), (235, 235, 235), (210, 210, 210)]
    centres = [(3, 3), (11, 2), (7, 8), (13, 11), (3, 12), (9, 14)]
    for cx, cy in centres:
        for dx, dy in [(0, 0), (1, 0), (0, 1), (-1, 0), (0, -1), (1, 1)]:
            if rng.random() < 0.62:
                x, y = cx + dx, cy + dy
                if 0 <= x < 16 and 0 <= y < 16:
                    r, g, b = rng.choice(shades)
                    img.putpixel((x, y), (r, g, b, 255))
    img.save(OUT)


if __name__ == "__main__":
    main()
