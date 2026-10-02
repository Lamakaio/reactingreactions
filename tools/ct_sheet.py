#!/usr/bin/env python3
"""Generates a Create connected-texture sheet (AllCTTypes.OMNIDIRECTIONAL) from an ordinary block texture.

Create's connected-texture system swaps each face's UVs for a tile of an 8x8 sheet chosen from which neighbours the
face connects to (see Create's AllCTTypes.OMNIDIRECTIONAL.getTextureIndex, ported below). This tool draws that sheet:
every tile is the base texture with a bevelled frame on each side that is NOT connected, and a small dark notch in
each corner where both neighbours on either side connect but the diagonal one does not. Joined blocks therefore read
as one continuous plate. Programmatic, no hand-drawn art.

  ct_sheet.py base.png out_connected.png
"""
import sys

from PIL import Image


def texture_index(up, down, left, right, tl, tr, bl, br):
    """Port of AllCTTypes.OMNIDIRECTIONAL.getTextureIndex (tile index in the 8-wide sheet)."""
    tile_x = tile_y = 0
    borders = (not up) + (not down) + (not left) + (not right)
    if up:
        tile_x += 1
    if down:
        tile_x += 2
    if left:
        tile_y += 1
    if right:
        tile_y += 2
    if borders == 0:
        if tr:
            tile_x += 1
        if tl:
            tile_x += 2
        if br:
            tile_y += 2
        if bl:
            tile_y += 1
    if borders == 1:
        if not right and (tl or bl):
            tile_y = 4
            tile_x = -1 + (1 if bl else 0) + (1 if tl else 0) * 2
        if not left and (tr or br):
            tile_y = 5
            tile_x = -1 + (1 if br else 0) + (1 if tr else 0) * 2
        if not down and (tl or tr):
            tile_y = 6
            tile_x = -1 + (1 if tl else 0) + (1 if tr else 0) * 2
        if not up and (bl or br):
            tile_y = 7
            tile_x = -1 + (1 if bl else 0) + (1 if br else 0) * 2
    if borders == 2 and ((up and left and tl) or (down and left and bl) or (up and right and tr) or (down and right and br)):
        tile_x += 3
    return tile_x + 8 * tile_y


def _shade(px, factor):
    r, g, b, a = px
    return (min(255, int(r * factor)), min(255, int(g * factor)), min(255, int(b * factor)), a)


def draw_tile(base, up, down, left, right, tl, tr, bl, br):
    n = base.width
    tile = base.copy()
    px = tile.load()
    # Frame on every side that does not continue into a neighbour.
    for i in range(n):
        if not up:
            px[i, 0] = _shade(px[i, 0], 0.45)
            px[i, 1] = _shade(px[i, 1], 1.3)
        if not down:
            px[i, n - 1] = _shade(px[i, n - 1], 0.45)
            px[i, n - 2] = _shade(px[i, n - 2], 0.75)
        if not left:
            px[0, i] = _shade(px[0, i], 0.45)
            px[1, i] = _shade(px[1, i], 1.3)
        if not right:
            px[n - 1, i] = _shade(px[n - 1, i], 0.45)
            px[n - 2, i] = _shade(px[n - 2, i], 0.75)
    # Inner corner notches: both neighbours connect, the diagonal one does not.
    for cond, xs, ys in ((up and left and not tl, (0, 1), (0, 1)), (up and right and not tr, (n - 2, n - 1), (0, 1)),
                         (down and left and not bl, (0, 1), (n - 2, n - 1)), (down and right and not br, (n - 2, n - 1), (n - 2, n - 1))):
        if cond:
            for x in xs:
                for y in ys:
                    px[x, y] = _shade(px[x, y], 0.45)
    return tile


def ct_sheet(base):
    base = base.convert("RGBA")
    n = base.width
    sheet = Image.new("RGBA", (n * 8, n * 8), (0, 0, 0, 0))
    placed = set()
    for bits in range(256):
        ctx = tuple(bool(bits >> i & 1) for i in range(8))  # up, down, left, right, tl, tr, bl, br
        index = texture_index(*ctx)
        if index < 0 or index >= 64 or index in placed:
            continue
        placed.add(index)
        sheet.paste(draw_tile(base, *ctx), ((index % 8) * n, (index // 8) * n))
    return sheet


if __name__ == "__main__":
    ct_sheet(Image.open(sys.argv[1])).save(sys.argv[2])
