#!/usr/bin/env python3
"""Draws the toxic haze sprites and screen overlay (own artwork).

Usage: python tools/make_particles.py
Writes textures/particle/toxic_haze_<0..3>.png (soft white puffs, tinted in game), textures/misc/toxic_vignette.png and
textures/misc/toxic_stain.png (blotches left on contaminated ground).
"""
import math
import os
import random

from PIL import Image

TEXTURES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions",
                        "textures")
OUT = os.path.join(TEXTURES, "particle")
FRAMES = 4


def puff(frame, rng):
    """A few overlapping soft blobs, wispier in later frames: alpha falls off smoothly towards each blob's edge."""
    img = Image.new("RGBA", (16, 16), (255, 255, 255, 0))
    blobs = [(8 + rng.uniform(-2, 2), 8 + rng.uniform(-2, 2), rng.uniform(3.5, 5.5)) for _ in range(3 + frame)]
    for y in range(16):
        for x in range(16):
            density = 0.0
            for bx, by, r in blobs:
                d = math.hypot(x + 0.5 - bx, y + 0.5 - by) / r
                density += max(0.0, 1 - d * d)
            alpha = min(1.0, density) * (1 - frame * 0.15)
            img.putpixel((x, y), (255, 255, 255, round(alpha * 200)))
    return img


def vignette(rng, size=256):
    """Clear in the middle, a sickly green creeping in from the edges, uneven like drifting haze."""
    blobs = [(rng.uniform(0, size), rng.uniform(0, size), rng.uniform(30, 70)) for _ in range(18)]
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            # 0 in the middle, 1 at the edges, measured as a rounded rectangle.
            dx = abs(x + 0.5 - size / 2) / (size / 2)
            dy = abs(y + 0.5 - size / 2) / (size / 2)
            r = (dx ** 4 + dy ** 4) ** 0.25
            edge = min(1.0, max(0.0, (r - 0.55) / 0.45)) ** 1.6
            lump = sum(max(0.0, 1 - math.hypot(x - bx, y - by) / br) for bx, by, br in blobs)
            alpha = min(1.0, edge * (0.75 + 0.25 * min(1.0, lump)))
            img.putpixel((x, y), (110, 160, 32, round(alpha * 220)))
    return img


def stain(rng, size=32):
    """Irregular dark olive blotches with soft edges, fading out towards the block's border."""
    blobs = [(rng.uniform(4, size - 4), rng.uniform(4, size - 4), rng.uniform(3, 9)) for _ in range(9)]
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            density = sum(max(0.0, 1 - math.hypot(x + 0.5 - bx, y + 0.5 - by) / br) for bx, by, br in blobs)
            border = min(1.0, min(x + 0.5, y + 0.5, size - x - 0.5, size - y - 0.5) / 5)
            alpha = min(1.0, density * 1.4) * border
            shade = 1 - 0.25 * min(1.0, density)
            img.putpixel((x, y), (round(78 * shade), round(92 * shade), round(28 * shade), round(alpha * 200)))
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    os.makedirs(os.path.join(TEXTURES, "misc"), exist_ok=True)
    rng = random.Random(11)
    for frame in range(FRAMES):
        puff(frame, rng).save(os.path.join(OUT, f"toxic_haze_{frame}.png"))
    vignette(rng).save(os.path.join(TEXTURES, "misc", "toxic_vignette.png"))
    stain(rng).save(os.path.join(TEXTURES, "misc", "toxic_stain.png"))
    print("wrote", FRAMES, "haze frames and the vignette")


if __name__ == "__main__":
    main()
