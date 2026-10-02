#!/usr/bin/env python3
"""Draws the Neon Lamp texture (own artwork, no third-party image): a bright orange-red glowing panel with a dark frame and a glass tube squiggle.

Usage: python tools/make_neon_lamp.py
"""
import os

from PIL import Image

OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions", "textures", "block", "neon_lamp.png")

TUBE = [(4, 4), (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4), (11, 5), (11, 6), (10, 6), (9, 6), (8, 6), (7, 6),
        (6, 6), (5, 6), (4, 6), (4, 7), (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8), (11, 9), (11, 10),
        (10, 10), (9, 10), (8, 10), (7, 10), (6, 10), (5, 10), (4, 10), (4, 11), (4, 12), (5, 12), (6, 12), (7, 12), (8, 12),
        (9, 12), (10, 12), (11, 12)]


def main():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if edge == 0:
                img.putpixel((x, y), (58, 40, 40, 255))
            elif edge == 1:
                img.putpixel((x, y), (150, 60, 40, 255))
            else:
                img.putpixel((x, y), (235, 105, 60, 255))
    for x, y in TUBE:
        img.putpixel((x, y), (255, 235, 200, 255))
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            px, py = x + dx, y + dy
            if 1 < px < 14 and 1 < py < 14 and (px, py) not in TUBE:
                img.putpixel((px, py), (255, 160, 90, 255))
    img.save(OUT)


if __name__ == "__main__":
    main()
