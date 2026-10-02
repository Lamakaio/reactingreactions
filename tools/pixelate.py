#!/usr/bin/env python3
"""Turn a permissively licensed photograph into a 16x16 (or other) pixel-art icon, programmatically.

No image is generated or painted: the photo is cut out from its background by flood
fill from the borders, cropped, box-downsampled, reduced to a small palette and given
a dark outline. The result is meant as a placeholder icon.

  pixelate.py photo.jpg out.png [--size 16] [--tolerance 45] [--colors 12] [--crop x0,y0,x1,y1] [--no-outline]
"""
import argparse
from collections import deque

from PIL import Image, ImageEnhance


def cut_out(img, tolerance, holes=False):
    """Make the border-connected background transparent (background colour = average of the corners).

    With holes=True every remaining pixel close to the background colour is cleared too, which opens up
    enclosed gaps such as the inside of a ring."""
    img = img.convert("RGBA")
    w, h = img.size
    px = img.load()
    # Median of all border pixels: robust when the subject touches a corner of the frame.
    border = [px[x, 0] for x in range(w)] + [px[x, h - 1] for x in range(w)] + [px[0, y] for y in range(h)] + [px[w - 1, y] for y in range(h)]
    bg = tuple(sorted(c[i] for c in border)[len(border) // 2] for i in range(3))

    def close(c):
        return sum((c[i] - bg[i]) ** 2 for i in range(3)) <= tolerance ** 2

    seen = [[False] * h for _ in range(w)]
    queue = deque()
    for x in range(w):
        queue += [(x, 0), (x, h - 1)]
    for y in range(h):
        queue += [(0, y), (w - 1, y)]
    while queue:
        x, y = queue.popleft()
        if not (0 <= x < w and 0 <= y < h) or seen[x][y]:
            continue
        seen[x][y] = True
        if close(px[x, y]):
            px[x, y] = (0, 0, 0, 0)
            queue += [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]
    if holes:
        for y in range(h):
            for x in range(w):
                if px[x, y][3] and close(px[x, y]):
                    px[x, y] = (0, 0, 0, 0)
    return img


def pixelate(img, size=16, tolerance=45, colors=12, outline=True, crop=None, margin=0.04, holes=False, rotate=0):
    # A photo that already carries transparency (some PNGs) needs no cut-out.
    has_alpha = img.mode in ("RGBA", "LA") and img.convert("RGBA").getchannel("A").getextrema()[0] < 255
    img = img.convert("RGBA" if has_alpha else "RGB")
    img.thumbnail((640, 640))
    if crop:
        img = img.crop(tuple(int(v) for v in crop))
    if rotate:
        corners = [img.getpixel(p) for p in ((0, 0), (img.width - 1, 0), (0, img.height - 1), (img.width - 1, img.height - 1))]
        fill = tuple(sum(c[i] for c in corners) // 4 for i in range(len(corners[0])))
        img = img.rotate(rotate, expand=True, fillcolor=fill)
    if not has_alpha:
        img = cut_out(img, tolerance, holes)
    box = img.getbbox()
    if box:
        img = img.crop(box)
    side = max(img.size)
    pad = int(side * margin)
    square = Image.new("RGBA", (side + 2 * pad, side + 2 * pad), (0, 0, 0, 0))
    square.paste(img, ((side - img.width) // 2 + pad, (side - img.height) // 2 + pad))
    # Two-step box downsampling keeps thin features better than one big jump.
    step = square.resize((size * 4, size * 4), Image.BOX).resize((size, size), Image.BOX)
    step = ImageEnhance.Color(step.convert("RGB")).enhance(1.25)
    step = ImageEnhance.Contrast(step).enhance(1.15)
    alpha = square.resize((size, size), Image.BOX).getchannel("A").point(lambda a: 255 if a >= 110 else 0)
    quant = step.quantize(colors=colors, method=Image.Quantize.MEDIANCUT).convert("RGB")
    out = quant.convert("RGBA")
    out.putalpha(alpha)
    if outline:
        src, dst = out.copy().load(), out.load()
        for y in range(size):
            for x in range(size):
                if src[x, y][3] == 0:
                    continue
                edge = any(not (0 <= x + dx < size and 0 <= y + dy < size) or src[x + dx, y + dy][3] == 0
                           for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if edge:
                    r, g, b, a = src[x, y]
                    dst[x, y] = (r * 6 // 10, g * 6 // 10, b * 6 // 10, a)
    return out


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("photo")
    ap.add_argument("out")
    ap.add_argument("--size", type=int, default=16)
    ap.add_argument("--tolerance", type=int, default=45)
    ap.add_argument("--colors", type=int, default=12)
    ap.add_argument("--crop")
    ap.add_argument("--no-outline", action="store_true")
    a = ap.parse_args()
    pixelate(Image.open(a.photo), a.size, a.tolerance, a.colors, not a.no_outline,
             a.crop.split(",") if a.crop else None).save(a.out)
