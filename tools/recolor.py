#!/usr/bin/env python3
"""Small recolor toolkit for the mod's open-licensed textures (offline use only).

Textures that belong to other mods are never edited - those are recolored at
runtime inside Minecraft instead. This tool is only for the CC-licensed source
textures listed in texture_manifest.json.

Operations (all keep the alpha channel and work on animated strips too):
  tint       multiply every pixel by a colour           tint(img, "#8090ff")
  colorize   map brightness onto a dark->light ramp     colorize(img, "#101820", "#c8d8e8")
  hue        shift hue (degrees), scale saturation/value hue(img, 200, sat=0.8, val=1.0)
  desaturate blend towards grey                         desaturate(img, 0.7)
  brighten   scale brightness                           brighten(img, 1.2)
  alpha      scale transparency
  crop       cut out a rectangle (a clean tile from a sheet)
  flat       one flat colour (the mean) - a featureless plate
  brushed    subtle horizontal brushed-metal streaks
  rivets     a small rivet near each corner
  uniform_rows  remove a vertical brightness gradient
  thicken    grow opaque pixels outward (fatten thin shapes)
  where      apply one of the above only to pixels in a brightness range
  box        keep (or clear) only a rectangle, e.g. to split a canister into base and label band

Command line:
  recolor.py in.png out.png op:arg,arg [op:arg,arg ...]
  e.g. recolor.py bronze_pickaxe.png titanium_pickaxe.png colorize:#1c2430:#b8c8d8
"""
import colorsys
import sys

from PIL import Image


def _rgb(value):
    if isinstance(value, (tuple, list)):
        return tuple(int(v) for v in value[:3])
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def _map(img, fn):
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a:
                px[x, y] = fn(r, g, b) + (a,)
    return img


def tint(img, color):
    cr, cg, cb = _rgb(color)
    return _map(img, lambda r, g, b: (r * cr // 255, g * cg // 255, b * cb // 255))


def colorize(img, dark, light, gamma=1.0):
    """Replace colour by a ramp: the darkest pixel becomes `dark`, the lightest `light`."""
    dr, dg, db = _rgb(dark)
    lr, lg, lb = _rgb(light)
    img = img.convert("RGBA")
    lums = [0.299 * r + 0.587 * g + 0.114 * b for r, g, b, a in img.getdata() if a]
    if not lums:
        return img
    lo, hi = min(lums), max(lums)
    span = (hi - lo) or 1

    def fn(r, g, b):
        t = ((0.299 * r + 0.587 * g + 0.114 * b) - lo) / span
        t = max(0.0, min(1.0, t)) ** gamma
        return (round(dr + (lr - dr) * t), round(dg + (lg - dg) * t), round(db + (lb - db) * t))

    return _map(img, fn)


def hue(img, degrees=0.0, sat=1.0, val=1.0):
    def fn(r, g, b):
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        h = (h + degrees / 360.0) % 1.0
        s = max(0.0, min(1.0, s * sat))
        v = max(0.0, min(1.0, v * val))
        return tuple(round(c * 255) for c in colorsys.hsv_to_rgb(h, s, v))

    return _map(img, fn)


def desaturate(img, amount=1.0):
    def fn(r, g, b):
        grey = 0.299 * r + 0.587 * g + 0.114 * b
        return (round(r + (grey - r) * amount), round(g + (grey - g) * amount), round(b + (grey - b) * amount))

    return _map(img, fn)


def brighten(img, factor):
    return _map(img, lambda r, g, b: (min(255, round(r * factor)), min(255, round(g * factor)), min(255, round(b * factor))))


def mask(img, predicate, fill=None):
    """Keep pixels for which predicate(r, g, b) is true (optionally repainted white/`fill`); the rest turn transparent."""
    img = img.convert("RGBA")
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    src, dst = img.load(), out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = src[x, y]
            if a and predicate(r, g, b):
                dst[x, y] = (fill + (a,)) if fill else (r, g, b, a)
    return out


def remove(img, other):
    """Make transparent every pixel of `img` that is opaque in `other` (used to split a base from its overlay)."""
    img = img.convert("RGBA")
    other = other.convert("RGBA")
    src, cut = img.load(), other.load()
    for y in range(img.height):
        for x in range(img.width):
            if cut[x, y][3]:
                src[x, y] = (0, 0, 0, 0)
    return img


def alpha(img, factor):
    """Scale the alpha channel (e.g. 0.75 for a see-through gas)."""
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, min(255, round(a * factor)))
    return img


def thicken(img, radius=1):
    """Grow opaque pixels outward by `radius` pixels (copying the neighbour's colour) - fattens thin shapes such as a bow."""
    img = img.convert("RGBA")
    src = img.copy().load()
    dst = img.load()
    r = int(radius)
    for y in range(img.height):
        for x in range(img.width):
            if src[x, y][3]:
                continue
            for dy in range(-r, r + 1):
                for dx in range(-r, r + 1):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < img.width and 0 <= ny < img.height and src[nx, ny][3]:
                        dst[x, y] = src[nx, ny]
                        break
                else:
                    continue
                break
    return img


def crop(img, x0, y0, x1, y1):
    """Cut out the rectangle [x0, x1) x [y0, y1) - e.g. one clean 16x16 tile from a larger texture sheet."""
    return img.convert("RGBA").crop((int(x0), int(y0), int(x1), int(y1)))


def flat(img):
    """Replace every opaque pixel by the texture's mean colour - a perfectly clean, featureless plate."""
    img = img.convert("RGBA")
    px = img.load()
    pixels = [px[x, y] for y in range(img.height) for x in range(img.width) if px[x, y][3]]
    if not pixels:
        return img
    mean = tuple(sum(p[i] for p in pixels) // len(pixels) for i in range(3))
    for y in range(img.height):
        for x in range(img.width):
            if px[x, y][3]:
                px[x, y] = mean + (px[x, y][3],)
    return img


def brushed(img, amount=0.03, seed=7):
    """Subtle horizontal brushed-metal streaks: every row gets a small brightness offset (+-amount), each with a slight fade along its length."""
    import random
    rng = random.Random(seed)
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        base = 1 + rng.uniform(-amount, amount)
        phase = rng.uniform(0, 6.28)
        for x in range(img.width):
            if not px[x, y][3]:
                continue
            import math
            factor = base + 0.25 * amount * math.sin(phase + x * 0.5)
            r, g, b, a = px[x, y]
            px[x, y] = (min(255, int(r * factor)), min(255, int(g * factor)), min(255, int(b * factor)), a)
    return img


def rivets(img, inset=3):
    """A small rivet (dark dot with a highlight) near each corner, `inset` pixels from the edges."""
    img = img.convert("RGBA")
    px = img.load()
    n = img.width
    for cx in (inset, n - 1 - inset):
        for cy in (inset, n - 1 - inset):
            px[cx, cy] = _shade_px(px[cx, cy], 0.72)
            hx, hy = (cx - 1, cy - 1) if cx < n // 2 else (cx + 1, cy - 1)
            if 0 <= hx < n and 0 <= hy < n:
                px[hx, hy] = _shade_px(px[hx, hy], 1.07)
    return img


def _shade_px(p, factor):
    return (min(255, int(p[0] * factor)), min(255, int(p[1] * factor)), min(255, int(p[2] * factor)), p[3])


def uniform_rows(img):
    """Remove a vertical brightness gradient: scale each row so its mean brightness equals the whole texture's."""
    img = img.convert("RGBA")
    px = img.load()
    lum = lambda p: 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]
    opaque = [(x, y) for y in range(img.height) for x in range(img.width) if px[x, y][3]]
    if not opaque:
        return img
    overall = sum(lum(px[x, y]) for x, y in opaque) / len(opaque)
    for y in range(img.height):
        row = [x for x in range(img.width) if px[x, y][3]]
        if not row:
            continue
        mean = sum(lum(px[x, y]) for x in row) / len(row)
        factor = overall / mean if mean else 1.0
        for x in row:
            r, g, b, a = px[x, y]
            px[x, y] = (min(255, int(r * factor)), min(255, int(g * factor)), min(255, int(b * factor)), a)
    return img


def where(img, lum_min, lum_max, op, *args):
    """Apply another operation only to pixels whose brightness is within [lum_min, lum_max] (e.g. recolor a tool head, keep the handle)."""
    img = img.convert("RGBA")
    changed = apply(img, op, *args)
    src, alt = img.load(), changed.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = src[x, y]
            if a and lum_min <= 0.299 * r + 0.587 * g + 0.114 * b <= lum_max:
                src[x, y] = alt[x, y]
    return img


def inside(img, x0, y0, x1, y1, op, *args):
    """Apply another operation only to pixels inside a rectangle (x1/y1 inclusive), e.g. a blade apart from its hilt."""
    img = img.convert("RGBA")
    changed = apply(img, op, *args)
    src, alt = img.load(), changed.load()
    for y in range(max(0, int(y0)), min(img.height, int(y1) + 1)):
        for x in range(max(0, int(x0)), min(img.width, int(x1) + 1)):
            src[x, y] = alt[x, y]
    return img


def box(img, x0, y0, x1, y1, keep=True):
    """Keep (or, with keep=False, clear) only the opaque pixels inside a rectangle (x1/y1 inclusive)."""
    img = img.convert("RGBA")
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    src, dst = img.load(), out.load()
    for y in range(img.height):
        for x in range(img.width):
            inside = x0 <= x <= x1 and y0 <= y <= y1
            if inside == bool(keep):
                dst[x, y] = src[x, y]
    return out


OPS = {"tint": tint, "colorize": colorize, "hue": hue, "desaturate": desaturate, "brighten": brighten, "alpha": alpha, "uniform_rows": uniform_rows, "brushed": brushed, "rivets": rivets, "flat": flat, "crop": crop, "thicken": thicken, "where": where, "inside": inside, "box": box}


def apply(img, op, *args):
    fn = OPS[op]
    return fn(img, *[_convert(a) for a in args])


def _convert(value):
    if isinstance(value, str) and value in OPS:
        return value
    if _is_number(value):
        return float(value) if not isinstance(value, (int, float)) else value
    return value


def _is_number(value):
    try:
        float(value)
        return not str(value).startswith("#")
    except (TypeError, ValueError):
        return False


def main(argv):
    if len(argv) < 4:
        print(__doc__)
        return 1
    img = Image.open(argv[1])
    for spec in argv[3:]:
        op, *args = spec.split(":")
        img = apply(img, op, *args)
    img.save(argv[2])
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
