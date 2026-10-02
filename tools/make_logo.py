#!/usr/bin/env python3
"""Draws the mod logo (own artwork plus the mod's own item icons): a row of icons over a pixel-font title.

Usage: python tools/make_logo.py
"""
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEMS = os.path.join(ROOT, "src", "main", "resources", "assets", "reactingreactions", "textures", "item")
OUT = os.path.join(ROOT, "src", "main", "resources", "logo.png")

ICONS = ["bromine", "iodine", "ruby", "circuit_board", "bleach_bottle"]
LINES = ["REACTING", "REACTIONS"]
SCALE = 3

GLYPHS = {
    "R": ["####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#"],
    "E": ["#####", "#....", "#....", "####.", "#....", "#....", "#####"],
    "A": [".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"],
    "C": [".###.", "#...#", "#....", "#....", "#....", "#...#", ".###."],
    "T": ["#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.."],
    "I": ["###", ".#.", ".#.", ".#.", ".#.", ".#.", "###"],
    "N": ["#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#", "#...#"],
    "G": [".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".###."],
    "O": [".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."],
    "S": [".###.", "#...#", "#....", ".###.", "....#", "#...#", ".###."],
}

TOP, BOTTOM = (255, 226, 140), (214, 128, 44)
OUTLINE = (28, 29, 33, 255)


def text_mask(text, size):
    """The title line as a set of pixels, each glyph pixel drawn as a size x size block."""
    pixels, x = set(), 0
    for ch in text:
        glyph = GLYPHS[ch]
        for gy, row in enumerate(glyph):
            for gx, c in enumerate(row):
                if c == "#":
                    for dy in range(size):
                        for dx in range(size):
                            pixels.add((x + gx * size + dx, gy * size + dy))
        x += (len(glyph[0]) + 1) * size
    return pixels, x - size


def draw_line(img, text, top, size):
    pixels, width = text_mask(text, size)
    left = (img.width - width) // 2
    height = 7 * size
    for x, y in pixels:
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1, 2):
                img.putpixel((left + x + dx, top + y + dy), OUTLINE)
    for x, y in pixels:
        t = y / (height - 1)
        img.putpixel((left + x, top + y), tuple(round(a + (b - a) * t) for a, b in zip(TOP, BOTTOM)) + (255,))


def main():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    gap = 4
    left = (img.width - (len(ICONS) * 16 + (len(ICONS) - 1) * gap)) // 2
    for i, name in enumerate(ICONS):
        icon = Image.open(os.path.join(ITEMS, name + ".png")).convert("RGBA").crop((0, 0, 16, 16))
        img.alpha_composite(icon, (left + i * (16 + gap), 3))
    draw_line(img, LINES[0], 24, 2)
    draw_line(img, LINES[1], 44, 2)
    img.resize((img.width * SCALE, img.height * SCALE), Image.NEAREST).save(OUT)


if __name__ == "__main__":
    main()
