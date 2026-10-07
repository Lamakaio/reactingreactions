#!/usr/bin/env python3
"""Draws the square project icon (CurseForge, Modrinth): the formed Reaction Chamber, rendered from its own model shape
(machine_models.py) with only this mod's textures, running with a green liquid behind its windows.

Usage: python tools/make_project_icon.py   (writes project_icon.png at the repository root)
"""
import os

import numpy as np
from PIL import Image, ImageFilter

import machine_models as mm

ROOT = mm.ROOT
TEXTURES = os.path.join(mm.ASSETS, "textures")
OUT = os.path.join(ROOT, "project_icon.png")

SIZE = 512
SUPERSAMPLE = 3
# Create's brass is not ours: our copper plate stands in for it.
SUBSTITUTE = {"brass": "copper"}
LIQUID = (110, 225, 80)
VIEW = np.array([1.0, -0.75, 1.0])  # looking at the controller (north) and the west window
SHADE = {"up": 1.0, "down": 0.5, "north": 0.82, "south": 0.82, "west": 0.64, "east": 0.64}
NORMAL = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}


def texture(path):
    img = Image.open(os.path.join(TEXTURES, path.split(":")[1] + ".png")).convert("RGBA")
    return np.asarray(img.crop((0, 0, img.width, img.width)), dtype=np.float32)


def liquid():
    still = Image.open(os.path.join(TEXTURES, "block", "fluid_liquid_still.png")).convert("RGBA").crop((0, 0, 16, 16))
    pixels = np.asarray(still, dtype=np.float32).copy()
    pixels[..., :3] *= np.array(LIQUID) / 255
    pixels[..., 3] = 255
    return pixels


def boxes():
    """The chamber (3 by 3 by 3, with a gasket), its controller dial, the stirrer fan, and the liquid inside."""
    shapes = mm.reaction_chamber(3, 3, 3)
    out = [(f, t, SUBSTITUTE.get(m, m), full) for f, t, m, rot, full in shapes["base"].finished() + shapes["sealed"].finished()]
    out.append(([16 + 2, 18, -1.05], [16 + 14, 30, -1.0], "dial", True))
    for f, t, m, rot, full in mm.stirrer_fan().finished():
        out.append(([f[0] + 16, f[1] + 62, f[2] + 16], [t[0] + 16, t[1] + 62, t[2] + 16], m, full))
    out.append(([4, 9.5, 4], [44, 34, 44], "liquid", False))
    return out


def face_uv(face, p):
    """Per-block texture coordinates of points on a face, as machine_models.uv lays them out."""
    x, y, z = p[..., 0], p[..., 1], p[..., 2]
    u, v = {"north": (-x, -y), "south": (x, -y), "west": (z, -y), "east": (-z, -y), "up": (x, z), "down": (x, -z)}[face]
    return np.mod(u, 16), np.mod(v, 16)


def corners(face, f, t):
    """A face as its origin and two edges, with the texture's u and v along them for a stretched picture."""
    (x0, y0, z0), (x1, y1, z1) = f, t
    return {"north": ((x1, y1, z0), (x0 - x1, 0, 0), (0, y0 - y1, 0)), "south": ((x0, y1, z1), (x1 - x0, 0, 0), (0, y0 - y1, 0)),
            "west": ((x0, y1, z0), (0, 0, z1 - z0), (0, y0 - y1, 0)), "east": ((x1, y1, z1), (0, 0, z0 - z1), (0, y0 - y1, 0)),
            "up": ((x0, y1, z0), (x1 - x0, 0, 0), (0, 0, z1 - z0)), "down": ((x0, y0, z1), (x1 - x0, 0, 0), (0, 0, z0 - z1))}[face]


def render():
    view = VIEW / np.linalg.norm(VIEW)
    right = np.cross(view, [0, 1, 0]); right /= np.linalg.norm(right)
    up = np.cross(right, view)
    faces = []
    textures = {m: texture(p) for m, p in mm.MATERIALS.items() if p.startswith("reactingreactions:")}
    textures["dial"] = texture("reactingreactions:block/controller_dial")
    textures["liquid"] = liquid()
    for f, t, material, full in boxes():
        for face, n in NORMAL.items():
            if np.dot(n, view) < 0:
                faces.append((face, f, t, material, full))
    # Fit the drawing in the square.
    allc = np.array([[a, b, c] for _, f, t, _, _ in faces for a in (f[0], t[0]) for b in (f[1], t[1]) for c in (f[2], t[2])])
    sx, sy = allc @ right, allc @ up
    size = SIZE * SUPERSAMPLE
    scale = size * 0.86 / max(sx.max() - sx.min(), sy.max() - sy.min())
    ox = size / 2 - scale * (sx.max() + sx.min()) / 2
    oy = size / 2 + scale * (sy.max() + sy.min()) / 2 + size * 0.02

    colour = np.zeros((size, size, 4), dtype=np.float32)
    depth = np.full((size, size), np.inf, dtype=np.float32)
    translucent = []
    for pass_ in (0, 1):
        todo = faces if pass_ == 0 else sorted(translucent, key=lambda q: -q[0])
        for item in todo:
            face, f, t, material, full = item if pass_ == 0 else item[1]
            o, a, b = (np.array(v, dtype=np.float64) for v in corners(face, f, t))
            to_screen = lambda p: np.array([ox + scale * (p @ right), oy - scale * (p @ up)])
            O, A, B = to_screen(o), to_screen(o + a) - to_screen(o), to_screen(o + b) - to_screen(o)
            quad = np.array([O, O + A, O + B, O + A + B])
            x0, y0 = np.floor(quad.min(0)).astype(int).clip(0, size - 1)
            x1, y1 = np.ceil(quad.max(0)).astype(int).clip(0, size - 1)
            if x1 <= x0 or y1 <= y0:
                continue
            gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
            m = np.linalg.inv(np.array([A, B]).T)
            s, tt = np.tensordot(m, np.stack([gx - O[0], gy - O[1]]), axes=1)
            inside = (s >= 0) & (s <= 1) & (tt >= 0) & (tt <= 1)
            p = o + s[..., None] * a + tt[..., None] * b
            d = p @ view
            tex = textures[material]
            if full:
                u, v = s * 16, tt * 16
            else:
                u, v = face_uv(face, p)
            n = tex.shape[0]
            texel = tex[np.clip((v * n / 16).astype(int), 0, n - 1), np.clip((u * n / 16).astype(int), 0, n - 1)]
            alpha = texel[..., 3] / 255
            sub_c, sub_d = colour[y0:y1 + 1, x0:x1 + 1], depth[y0:y1 + 1, x0:x1 + 1]
            shaded = texel[..., :3] * (1.0 if material == "liquid" else SHADE[face])
            if pass_ == 0:
                if alpha[inside].size and alpha[inside].min() < 0.99 and material != "dial":
                    translucent.append((float(d[inside].mean()), item))
                    continue
                draw = inside & (d < sub_d) & (alpha > 0.5)
                sub_c[draw, :3] = shaded[draw]
                sub_c[draw, 3] = 255
                sub_d[draw] = d[draw]
            else:
                draw = inside & (d < sub_d) & (alpha > 0)
                k = alpha[draw][:, None]
                sub_c[draw, :3] = sub_c[draw, :3] * (1 - k) + shaded[draw] * k
                sub_c[draw, 3] = np.maximum(sub_c[draw, 3], alpha[draw] * 255)
    return Image.fromarray(colour.clip(0, 255).astype(np.uint8), "RGBA").resize((SIZE, SIZE), Image.LANCZOS)


def background():
    """Our dark steel, tiled large and dimmed toward the edges, with a green glow behind the chamber."""
    tile = Image.open(os.path.join(TEXTURES, "block", "machine", "steel_darker.png")).convert("RGB").resize((64, 64), Image.NEAREST)
    bg = Image.new("RGB", (SIZE, SIZE))
    for x in range(0, SIZE, 64):
        for y in range(0, SIZE, 64):
            bg.paste(tile, (x, y))
    yy, xx = np.mgrid[0:SIZE, 0:SIZE] / SIZE - 0.5
    r = np.sqrt(xx ** 2 + yy ** 2)
    pixels = np.asarray(bg, dtype=np.float32) * (0.95 - 0.7 * r)[..., None]
    glow = np.exp(-(r / 0.32) ** 2)[..., None] * np.array(LIQUID) * 0.7
    return Image.fromarray((pixels + glow).clip(0, 255).astype(np.uint8), "RGB").convert("RGBA")


def main():
    chamber = render()
    shadow = Image.new("RGBA", chamber.size, (0, 0, 0, 0))
    shadow.putalpha(chamber.getchannel("A").point(lambda a: a * 0.6))
    shadow = shadow.filter(ImageFilter.GaussianBlur(10))
    img = background()
    img.alpha_composite(shadow, (6, 10))
    img.alpha_composite(chamber)
    img.convert("RGB").save(OUT)


if __name__ == "__main__":
    main()
