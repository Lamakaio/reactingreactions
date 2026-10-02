#!/usr/bin/env python3
"""Builds the formed models of the fixed-size machines (Reaction Chamber tiers): the whole machine is described once, in
pixels, then cut along block lines into one model per position, plus the blockstates that pick them.

A formed wall or controller has a `part` (1 + its tier's base + its position in the machine, see MachineTiers.java) and a
`facing` (the side its controller is on); part 0 is the plain unformed block. The machine is drawn with its controller on
the north side and turned by the blockstate. Pieces that stick out of the shell, or fall in the roof shaft's cell, are hosted
by the nearest wall block. Faces created by a cut are dropped, and textures are mapped per block, so materials tile seamlessly.

Usage: python tools/machine_models.py
"""
import json
import math
import os
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "reactingreactions")

MATERIALS = {
    "plate": "reactingreactions:block/machine/reaction_chamber_plate",
    "band": "reactingreactions:block/machine/steel_band",
    "dark": "reactingreactions:block/machine/steel_dark",
    "brass": "create:block/brass_block",
    "copper": "reactingreactions:block/machine/copper_plate",
    "glass": "reactingreactions:block/machine/sight_glass",
    "motor": "reactingreactions:block/machine/motor_paint",
    "red": "reactingreactions:block/machine/paint_red",
    "dial": "reactingreactions:block/controller_dial",
    "brick": "reactingreactions:block/machine/oven_brick",
    "darker": "reactingreactions:block/machine/steel_darker",
    "hazard": "reactingreactions:block/machine/hazard",
    "vat": "reactingreactions:block/machine/vat_paint",
}
FACES = ("north", "south", "east", "west", "up", "down")
STEP = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}


def subtract(f, t, hf, ht):
    """Box (f, t) minus box (hf, ht): up to six boxes covering the rest."""
    if any(t[i] <= hf[i] or f[i] >= ht[i] for i in range(3)):
        return [(f, t)]
    out = []
    lo, hi = list(f), list(t)
    for axis in range(3):
        if lo[axis] < hf[axis]:
            a, b = list(lo), list(hi)
            b[axis] = hf[axis]
            out.append((a, b))
            lo[axis] = hf[axis]
        if hi[axis] > ht[axis]:
            a, b = list(lo), list(hi)
            a[axis] = ht[axis]
            out.append((a, b))
            hi[axis] = ht[axis]
    return out


class Shape:
    """The whole machine as boxes: (from, to, material, rotation or None, full uv)."""

    def __init__(self):
        self.boxes = []
        self.cuts = []

    def cut(self, f, t):
        """Opens a hole through everything but glass, such as a window through the wall."""
        self.cuts.append((list(f), list(t)))

    def finished(self):
        """The boxes with the cuts taken out (turned boxes are left whole)."""
        out = []
        for f, t, material, rot, full in self.boxes:
            pieces = [(f, t)]
            if not rot and material != "glass":
                for hf, ht in self.cuts:
                    pieces = [p for a, b in pieces for p in subtract(a, b, hf, ht)]
            out.extend((a, b, material, rot, full) for a, b in pieces)
        return out

    def box(self, f, t, material, rot=None, uv=None):
        """{@code uv="full"} stretches the whole texture over each face, for pictures like a dial."""
        self.boxes.append((list(f), list(t), material, rot, uv == "full"))


# ---- Reaction Chamber ----------------------------------------------------------------------------------------------------

def pipe_riser(s, W, side, along, y0, y1, D=None):
    """A copper riser 1.5px wide standing off a side of a W by D box, from y0 up to an elbow into the wall at y1."""
    D = W if D is None else D
    a, b = along, along + 1.5
    if side == "n": s.box([a, y0, -2], [b, y1, -0.5], "copper"); s.box([a, y1, -2], [b, y1 + 1.5, 0.5], "copper")
    if side == "s": s.box([a, y0, D + 0.5], [b, y1, D + 2], "copper"); s.box([a, y1, D - 0.5], [b, y1 + 1.5, D + 2], "copper")
    if side == "w": s.box([-2, y0, a], [-0.5, y1, b], "copper"); s.box([-2, y1, a], [0.5, y1 + 1.5, b], "copper")
    if side == "e": s.box([W + 0.5, y0, a], [W + 2, y1, b], "copper"); s.box([W - 0.5, y1, a], [W + 2, y1 + 1.5, b], "copper")
    # A flange where it leaves the ring or the run below.
    if side in "ns":
        z0, z1 = (-2.25, -0.25) if side == "n" else (D + 0.25, D + 2.25)
        s.box([a - 0.5, y0, z0], [b + 0.5, y0 + 0.75, z1], "brass")
    else:
        x0, x1 = (-2.25, -0.25) if side == "w" else (W + 0.25, W + 2.25)
        s.box([x0, y0, a - 0.5], [x1, y0 + 0.75, b + 0.5], "brass")


def bolts(s, W, y, out, start, end, D=None, step=6):
    """Brass bolt heads every `step` px along the four sides, from `start` to `end`, `out` px proud."""
    D = W if D is None else D
    for t in range(int(start), int(end), step):
        s.box([t, y, -out - 0.5], [t + 1, y + 1, -out], "brass"); s.box([t, y, D + out], [t + 1, y + 1, D + out + 0.5], "brass")
    for t in range(int(start), int((end if D == W else D - start)), step):
        s.box([-out - 0.5, y, t], [-out, y + 1, t + 1], "brass"); s.box([W + out, y, t], [W + out + 0.5, y + 1, t + 1], "brass")


def reaction_chamber(nx, nz, ny):
    s = Shape()
    W, H = nx * 16, ny * 16
    C = 7 if nx == 3 else 11  # how far the vertical corners are cut back
    mid = W / 2
    top = H - 4

    def octagon(y0, y1, out, material, s=s):
        lo, hi = 0.5 - out, W - 0.5 + out
        s.box([lo, y0, C], [hi, y1, W - C], material)
        s.box([C, y0, lo], [W - C, y1, hi], material)
        half = (C - lo) / 2
        length = (C - lo) * 0.7071 + 0.6
        for cx in (lo + half, hi - half):
            for cz in (lo + half, hi - half):
                angle = 45 if (cx < mid) == (cz < mid) else -45
                s.box([cx - length, y0, cz - 1.2], [cx + length, y1, cz + 1.2], material,
                      {"origin": [cx, (y0 + y1) / 2, cz], "axis": "y", "angle": angle})

    octagon(8, top, 0, "plate")
    # Hollow inside, with walls, floor and roof about 1.5px thick: an octagon (two crossed boxes) so the cut corners stay closed.
    # ReactionChamberRenderer draws the fluid inside it (FLUID_INSET).
    s.cut([2, 9.5, C + 1.5], [W - 2, top - 1.5, W - C - 1.5])
    s.cut([C + 1.5, 9.5, 2], [W - C - 1.5, top - 1.5, W - 2])
    s.box([0, 0, 0], [W, 6, W], "dark")
    # Ribs every 12px, centred on the side.
    ribs = [mid - 1 + 12 * k for k in range(-4, 5) if 6 <= mid - 1 + 12 * k and mid + 1 + 12 * k <= W - 6]
    for p in ribs:
        s.box([p, 0, -0.5], [p + 2, 6, 0], "band"); s.box([p, 0, W], [p + 2, 6, W + 0.5], "band")
        s.box([-0.5, 0, p], [0, 6, p + 2], "band"); s.box([W, 0, p], [W + 0.5, 6, p + 2], "band")
    octagon(6, 8, 0.75, "dark")
    for y in range(22, top - 4, 16):
        octagon(y, y + 2, 0.5, "band")
    octagon(top - 2, top, 0.6, "brass")
    octagon(top, top + 2, 1, "dark")
    steps = [(4, 2, "band"), (9, 2, "plate"), (14, 1, "plate")] if nx == 3 else [(4, 2, "band"), (10, 2, "band"), (16, 2, "plate"), (24, 2, "plate")]
    y = top + 2
    for inset, h, material in steps:
        s.box([inset, y, inset], [W - inset, y + h, W - inset], material); y += h
    roof_top = y
    # The manway stands proud of the dome step it sits on, so their tops never share a plane.
    s.box([W - 15, top + 2, 7], [W - 7, top + 4.75, 15], "dark"); s.box([W - 14, top + 4.75, 8], [W - 8, top + 5.25, 14], "brass")

    g0, g1, y0, y1 = mid - 7, mid + 7, 23, H - 15
    for side in "swe":
        if side == "s": fr, gl = ([g0, y0, W - 0.5], [g1, y1, W + 0.75]), ([g0 + 2, y0 + 2, W + 0.75], [g1 - 2, y1 - 2, W + 1])
        if side == "w": fr, gl = ([-0.75, y0, g0], [0.5, y1, g1]), ([-1, y0 + 2, g0 + 2], [-0.75, y1 - 2, g1 - 2])
        if side == "e": fr, gl = ([W - 0.5, y0, g0], [W + 0.75, y1, g1]), ([W + 0.75, y0 + 2, g0 + 2], [W + 1, y1 - 2, g1 - 2])
        s.box(fr[0], fr[1], "dark"); s.box(gl[0], gl[1], "glass")
        # Through the wall behind the glass, so the contents show.
        if side == "s": s.cut([g0 + 2, y0 + 2, W - 17], [g1 - 2, y1 - 2, W + 0.75])
        if side == "w": s.cut([-0.75, y0 + 2, g0 + 2], [17, y1 - 2, g1 - 2])
        if side == "e": s.cut([W - 17, y0 + 2, g0 + 2], [W + 0.75, y1 - 2, g1 - 2])

    # The controller's panel, in its own block (middle of the north side, second row); the renderer draws the dial on it.
    s.box([mid - 8, 17, -1], [mid + 8, 31, 0.5], "dark")

    # Stirrer motor over the roof shaft (the slicer leaves a hole for the shaft). It stays within the block above the roof.
    r = 6 if nx == 3 else 8
    room = H + 16 - roof_top
    s.box([mid - r, roof_top, mid - r], [mid + r, roof_top + 2, mid + r], "dark")
    body = min(9, room - 4)
    s.box([mid - r + 1, roof_top + 2, mid - r + 1], [mid + r - 1, roof_top + 2 + body, mid + r - 1], "motor")
    for fy in range(4, body, 3):
        s.box([mid - r + 0.5, roof_top + fy, mid - r + 0.5], [mid + r - 0.5, roof_top + fy + 1, mid + r - 0.5], "dark")
    # The fan on top turns with the shaft: drawn by ReactionChamberRenderer (moving/stirrer_fan) at roof_top + 2 + body
    # (78 px up for the small tier, 94 for the large; keep FAN_BASE in step).
    s.box([mid + r - 1, roof_top + 3, mid - 2], [mid + r + 2, roof_top + 3 + min(6, body - 1), mid + 2], "dark")

    if nx >= 5:
        for rib in (mid - 22, mid + 20):
            s.box([rib, 8, -0.75], [rib + 2, top, 0.5], "band"); s.box([rib, 8, W - 0.5], [rib + 2, top, W + 0.75], "band")
            s.box([-0.75, 8, rib], [0.5, top, rib + 2], "band"); s.box([W - 0.5, 8, rib], [W + 0.75, top, rib + 2], "band")
        lz = W - C - 14
        for rz in (lz, lz + 7):
            s.box([-4, 8, rz], [-3, top + 10, rz + 1], "dark")
        for ry in range(12, int(top + 8), 4):
            s.box([-4, ry, lz + 1], [-3, ry + 1, lz + 7], "dark")
        rail_y = top + 10
        for x0, z0, x1, z1 in ((C, 1, W - C, 2), (C, W - 2, W - C, W - 1), (1, C, 2, W - C), (W - 2, C, W - 1, W - C)):
            s.box([x0, rail_y, z0], [x1, rail_y + 1, z1], "brass")
        for px, pz in ((C, 1), (W - C - 1, 1), (C, W - 2), (W - C - 1, W - 2), (1, C), (1, W - C - 1), (W - 2, C), (W - 2, W - C - 1),
                       (mid, 1), (mid, W - 2), (W - 2, mid)):
            s.box([px, top + 2, pz], [px + 1, rail_y + 1, pz + 1], "brass")
    # Outlet Manifold installed: a copper ring above the base and risers up each face, clear of the windows and the panel.
    piped = Shape()
    piped.cuts = list(s.cuts)
    octagon(12, 13.5, 2.5, "copper", piped)
    for x in (C + 2, W - C - 3.5):
        for side in "nswe":
            pipe_riser(piped, W, side, x, 13.5, top - 3)
    # Gasket installed: thicker bands with bolts, and a heavier top trim.
    sealed = Shape()
    sealed.cuts = list(s.cuts)
    for y in range(22, top - 4, 16):
        octagon(y - 0.75, y + 2.75, 1.25, "dark", sealed)
        bolts(sealed, W, y + 0.5, 1.25, C + 3, W - C - 3)
    octagon(5.5, 8.5, 1.5, "dark", sealed)
    octagon(top - 2.5, top + 0.25, 1.2, "brass", sealed)
    return {"base": s, "piped": piped, "sealed": sealed}
def ring(s, W, y0, y1, out, material):
    """A band all round a W-wide box, `out` px proud of its sides."""
    lo, hi = -out, W + out
    s.box([lo, y0, lo], [hi, y1, 0.5], material); s.box([lo, y0, W - 0.5], [hi, y1, hi], material)
    s.box([lo, y0, 0.5], [0.5, y1, W - 0.5], material); s.box([W - 0.5, y0, 0.5], [hi, y1, W - 0.5], material)


def airless_oven(nx, nz, ny):
    """A coke-oven retort: brick on a steel plinth, held by steel buckstays and tie bands, a chimney at the back."""
    s = Shape()
    W, H = nx * 16, ny * 16
    top = H - 4
    s.box([0, 0, 0], [W, 3, W], "dark")
    s.box([0.5, 3, 0.5], [W - 0.5, top, W - 0.5], "brick")
    ring(s, W, 4, 6, 0.5, "band")
    ring(s, W, top - 4, top - 2, 0.5, "band")
    # Buckstays: steel posts at the corners and on the block lines of each side.
    for p in (-0.5, 15, 31, W - 2.5):
        width = 3 if p in (-0.5, W - 2.5) else 2
        s.box([p, 2, -1.5], [p + width, top + 1, 0.5], "dark"); s.box([p, 2, W - 0.5], [p + width, top + 1, W + 1.5], "dark")
        s.box([-1.5, 2, p], [0.5, top + 1, p + width], "dark"); s.box([W - 0.5, 2, p], [W + 1.5, top + 1, p + width], "dark")
    # Roof: a steel rim, charging lids along the middle, the chimney in the back corner.
    ring(s, W, top, top + 1.5, 0, "dark")
    for x in (6, 20, 34):
        s.box([x, top, 20], [x + 8, top + 1, 28], "dark"); s.box([x + 3, top + 1, 23], [x + 5, top + 1.5, 25], "brass")
    s.box([W - 15, top, W - 15], [W - 3, top + 2, W - 3], "dark")
    s.box([W - 14, top + 2, W - 14], [W - 4, H + 14, W - 4], "brick")
    s.box([W - 15, H + 11, W - 15], [W - 3, H + 13, W - 3], "dark")
    s.box([W - 12, H + 14, W - 12], [W - 6, H + 14.25, W - 6], "darker")
    # Controller side (north, middle block): the firebox door, where the renderer shows the fire, and the dial's panel.
    mid = W / 2
    x0 = mid + 0  # the door's left edge seen from inside is its right edge seen from outside
    s.box([x0 + 0, 17, 0.25], [x0 + 6, 27, 0.5], "darker")
    s.box([x0 - 1, 17, -1], [x0, 28, 0.5], "dark"); s.box([x0 + 6, 17, -1], [x0 + 7, 28, 0.5], "dark")
    s.box([x0 - 1, 27, -1], [x0 + 7, 28.5, 0.5], "dark"); s.box([x0 - 1.5, 16.5, -1.5], [x0 + 7.5, 18, 0.5], "dark")
    s.box([mid - 6.5, 24.5, -1], [mid - 0.5, 31.5, 0.5], "dark")
    # Charging doors on the other three sides.
    for side in "swe":
        if side == "s": d, latch = ([mid - 5, 19, W - 0.5], [mid + 5, 31, W + 0.75]), ([mid + 2, 24, W + 0.75], [mid + 4, 26, W + 1.25])
        if side == "w": d, latch = ([-0.75, 19, mid - 5], [0.5, 31, mid + 5]), ([-1.25, 24, mid + 2], [-0.75, 26, mid + 4])
        if side == "e": d, latch = ([W - 0.5, 19, mid - 5], [W + 0.75, 31, mid + 5]), ([W + 0.75, 24, mid - 4], [W + 1.25, 26, mid - 2])
        s.box(d[0], d[1], "dark"); s.box(latch[0], latch[1], "brass")
    # Outlet Manifold installed: a copper ring in front of the buckstays, with drops between them to the base.
    piped = Shape()
    y0 = top - 10
    for f, t in (([-3.5, y0, -3.5], [W + 3.5, y0 + 1.5, -2]), ([-3.5, y0, W + 2], [W + 3.5, y0 + 1.5, W + 3.5]),
                 ([-3.5, y0, -2], [-2, y0 + 1.5, W + 2]), ([W + 2, y0, -2], [W + 3.5, y0 + 1.5, W + 2])):
        piped.box(f, t, "copper")
    for x in (7, W - 8.5):
        piped.box([x, 4, -3.5], [x + 1.5, y0, -2], "copper"); piped.box([x, 3, -3.5], [x + 1.5, 4.5, 0.5], "copper")
        piped.box([x, 4, W + 2], [x + 1.5, y0, W + 3.5], "copper"); piped.box([x, 3, W - 0.5], [x + 1.5, 4.5, W + 3.5], "copper")
        piped.box([-3.5, 4, x], [-2, y0, x + 1.5], "copper"); piped.box([-3.5, 3, x], [0.5, 4.5, x + 1.5], "copper")
        piped.box([W + 2, 4, x], [W + 3.5, y0, x + 1.5], "copper"); piped.box([W - 0.5, 3, x], [W + 3.5, 4.5, x + 1.5], "copper")
    # Gasket installed: thicker tie bands, bolted.
    sealed = Shape()
    ring(sealed, W, 3.25, 6.75, 1.25, "dark")
    ring(sealed, W, top - 4.75, top - 1.25, 1.25, "dark")
    bolts(sealed, W, 4.5, 1.25, 4, W - 4, step=8)
    bolts(sealed, W, top - 3.5, 1.25, 4, W - 4, step=8)
    return {"base": s, "piped": piped, "sealed": sealed}
def vat_electrodes(nx, nz):
    """The two electrode columns' (x, z) for a tier: next to each end wall, on the middle line."""
    return [(1, nz // 2), (nx - 2, nz // 2)]


def vat_terminals(nx, nz):
    """The two terminals' (x, y, z): in the end walls beside the electrodes, one block above the floor."""
    return [(0, 1, nz // 2), (nx - 1, 1, nz // 2)]


def electrolysis_vat(nx, nz, ny):
    """An electrolytic cell: a painted tank on skids, hazard-banded rim, big sight windows, copper bus bars from the terminals."""
    s = Shape()
    W, D, H = nx * 16, nz * 16, ny * 16
    mid, midz = W / 2, D / 2
    # Skids and insulator feet under the tank.
    for z0 in (1.5, D - 5.5):
        s.box([0, 0, z0], [W, 3, z0 + 4], "dark")
    for x0 in (2, W - 6):
        for z0 in (1, D - 5):
            s.box([x0, 3, z0], [x0 + 4, 5, z0 + 4], "plate")
    s.box([0.5, 3, 0.5], [W - 0.5, H - 3, D - 0.5], "vat")
    # Hollow inside, walls about 1.5px thick, so the electrodes and the electrolyte show through the windows.
    s.cut([2, 4.5, 2], [W - 2, H - 4.5, D - 2])
    s.box([0, 0, 5.5], [W, 3, D - 5.5], "darker")

    def rect_ring(y0, y1, out, material, s=s):
        lo_x, hi_x, lo_z, hi_z = -out, W + out, -out, D + out
        s.box([lo_x, y0, lo_z], [hi_x, y1, 0.5], material); s.box([lo_x, y0, D - 0.5], [hi_x, y1, hi_z], material)
        s.box([lo_x, y0, 0.5], [0.5, y1, D - 0.5], material); s.box([W - 0.5, y0, 0.5], [hi_x, y1, D - 0.5], material)

    rect_ring(5, 7, 0.5, "dark")
    rect_ring(H - 9, H - 6, 0.6, "hazard")
    rect_ring(H - 3, H - 1.5, 1, "dark")
    s.box([1, H - 3, 1], [W - 1, H - 1, D - 1], "dark")
    # Sight windows along both long sides, a block each, sparing the controller's block.
    for cx in range(1, nx - 1):
        for side in (0, 1):
            if side == 0 and cx == nx // 2:
                continue
            x0, x1 = cx * 16 + 2, cx * 16 + 14
            z_frame = ([x0, 8, -0.75], [x1, H - 10, 0.5]) if side == 0 else ([x0, 8, D - 0.5], [x1, H - 10, D + 0.75])
            z_glass = ([x0 + 1.5, 9.5, -1], [x1 - 1.5, H - 11.5, -0.75]) if side == 0 else ([x0 + 1.5, 9.5, D + 0.75], [x1 - 1.5, H - 11.5, D + 1])
            s.box(*z_frame, "dark"); s.box(*z_glass, "glass")
            if side == 0:
                s.cut([x0 + 1.5, 9.5, -0.75], [x1 - 1.5, H - 11.5, 17])
            else:
                s.cut([x0 + 1.5, 9.5, D - 17], [x1 - 1.5, H - 11.5, D + 0.75])
    # Controller panel (north, middle block, second row), for the renderer's dial.
    s.box([mid - 8, 17, -1], [mid + 8, 31, 0.5], "dark")
    # From a brass gland over each electrode, a copper strap runs across the roof and down the end wall to the terminal's
    # junction box, where the wire attaches (ElectrolysisVatTerminalBlock's node, at the brass knob).
    for (ex, ez), end in zip(vat_electrodes(nx, nz), (0, W)):
        cx, z0 = ex * 16 + 8, ez * 16 + 6
        s.box([cx - 3, H - 1, ez * 16 + 5], [cx + 3, H + 1, ez * 16 + 11], "brass")
        if end == 0:
            s.box([0.5, H - 1, z0], [cx - 3, H - 0.25, z0 + 4], "copper")
            s.box([-0.5, 28, z0], [0.5, H - 0.25, z0 + 4], "copper")
            s.box([-3.5, 20, z0 - 2], [0.5, 28, z0 + 6], "dark"); s.box([-4.5, 23, z0 + 1], [-3.5, 25, z0 + 3], "brass")
        else:
            s.box([cx + 3, H - 1, z0], [W - 0.5, H - 0.25, z0 + 4], "copper")
            s.box([W - 0.5, 28, z0], [W + 0.5, H - 0.25, z0 + 4], "copper")
            s.box([W - 0.5, 20, z0 - 2], [W + 3.5, 28, z0 + 6], "dark"); s.box([W + 3.5, 23, z0 + 1], [W + 4.5, 25, z0 + 3], "brass")
    # Outlet Manifold installed: a copper run along both long sides over the base rim, risers between the windows.
    piped = Shape()
    piped.cuts = list(s.cuts)
    piped.box([0.5, 7.25, -2], [W - 0.5, 8.75, -0.5], "copper"); piped.box([0.5, 7.25, D + 0.5], [W - 0.5, 8.75, D + 2], "copper")
    for k in range(1, nx):
        x = 16 * k - 0.75
        if abs(16 * k - mid) != 8:
            pipe_riser(piped, W, "n", x, 8.75, H - 11, D)
        pipe_riser(piped, W, "s", x, 8.75, H - 11, D)
    # Gasket installed: heavier, bolted rims at the base and under the roof.
    sealed = Shape()
    sealed.cuts = list(s.cuts)
    rect_ring(4.25, 7.75, 1.25, "dark", sealed)
    rect_ring(H - 3.75, H - 0.75, 1.75, "dark", sealed)
    bolts(sealed, W, 5.5, 1.25, 4, W - 4, D, step=8)
    return {"base": s, "piped": piped, "sealed": sealed}
# ---- Derrick tower --------------------------------------------------------------------------------------------------------

def derrick(nx, nz, ny):
    """A drilling derrick: red lattice legs, X-bracing, a grated deck with a railing, a crown at the top."""
    s = Shape()
    W, top = nx * 16, 48
    # Legs: four posts each, tied every 8px, zig-zag braced on their two outer faces.
    for lx in (1, W - 7):
        for lz in (1, W - 7):
            for px in (lx, lx + 4.5):
                for pz in (lz, lz + 4.5):
                    s.box([px, 0, pz], [px + 1.5, top + 12, pz + 1.5], "red")
            for y in range(4, top + 12, 8):
                s.box([lx, y, lz], [lx + 6, y + 1, lz + 6], "dark")
            out_z = lz if lz == 1 else lz + 5
            out_x = lx if lx == 1 else lx + 5
            for y in range(4, top + 4, 8):
                angle = 45 if (y // 8) % 2 == 0 else -45
                s.box([lx + 0.5, y + 3.5, out_z], [lx + 5.5, y + 4.5, out_z + 1], "dark",
                      {"origin": [lx + 3, y + 4, out_z + 0.5], "axis": "z", "angle": angle})
                s.box([out_x, y + 3.5, lz + 0.5], [out_x + 1, y + 4.5, lz + 5.5], "dark",
                      {"origin": [out_x + 0.5, y + 4, lz + 3], "axis": "x", "angle": angle})
    # Girts and X-bracing on each side, between the legs, at every layer.
    for y in (16, 32, 48):
        s.box([7, y - 1, 1.5], [W - 7, y + 1, 3], "dark"); s.box([7, y - 1, W - 3], [W - 7, y + 1, W - 1.5], "dark")
        s.box([1.5, y - 1, 7], [3, y + 1, W - 7], "dark"); s.box([W - 3, y - 1, 7], [W - 1.5, y + 1, W - 7], "dark")
    for y0 in (0, 16, 32):
        for angle in (45, -45):
            s.box([W / 2 - 11, y0 + 7.5, 1.75], [W / 2 + 11, y0 + 8.5, 2.75], "band", {"origin": [W / 2, y0 + 8, 2.25], "axis": "z", "angle": angle})
            s.box([W / 2 - 11, y0 + 7.5, W - 2.75], [W / 2 + 11, y0 + 8.5, W - 1.75], "band", {"origin": [W / 2, y0 + 8, W - 2.25], "axis": "z", "angle": angle})
            s.box([1.75, y0 + 7.5, W / 2 - 11], [2.75, y0 + 8.5, W / 2 + 11], "band", {"origin": [2.25, y0 + 8, W / 2], "axis": "x", "angle": angle})
            s.box([W - 2.75, y0 + 7.5, W / 2 - 11], [W - 1.75, y0 + 8.5, W / 2 + 11], "band", {"origin": [W - 2.25, y0 + 8, W / 2], "axis": "x", "angle": angle})
    # Work deck at the base, open round the pipe, railed except at the front (north).
    d0, d1 = -12, W + 12
    s.box([d0, 0, d0], [d1, 1.5, d1], "darker")
    for x in (d0, d0 + 18, W / 2 + 6, d1 - 1):
        for z in (d0, d1 - 1):
            s.box([x, 1.5, z], [x + 1, 11, z + 1], "brass")
    for z in (d0 + 18, W / 2 + 6):
        for x in (d0, d1 - 1):
            s.box([x, 1.5, z], [x + 1, 11, z + 1], "brass")
    s.box([d0, 10, d0], [W / 2 - 6, 11, d0 + 1], "brass"); s.box([W / 2 + 6, 10, d0], [d1, 11, d0 + 1], "brass")
    s.box([d0, 10, d1 - 1], [d1, 11, d1], "brass")
    s.box([d0, 10, d0 + 1], [d0 + 1, 11, d1 - 1], "brass"); s.box([d1 - 1, 10, d0 + 1], [d1, 11, d1 - 1], "brass")
    # Crown: a railed platform round the top (clear of the controller's block), and crown beams.
    s.cut([16, top - 0.01, 16], [32, top + 2.01, 32])
    s.box([-3, top, -3], [W + 3, top + 2, W + 3], "darker")
    for x0, z0, x1, z1 in ((-3, -3, W + 3, -2), (-3, W + 2, W + 3, W + 3), (-3, -2, -2, W + 2), (W + 2, -2, W + 3, W + 2)):
        s.box([x0, top + 8, z0], [x1, top + 9, z1], "brass")
    for x, z in ((-3, -3), (W + 2, -3), (-3, W + 2), (W + 2, W + 2), (W / 2, -3), (W / 2, W + 2)):
        s.box([x, top + 2, z], [x + 1, top + 9, z + 1], "brass")
    s.box([1, top + 12, 1], [W - 1, top + 14, 7], "dark"); s.box([1, top + 12, W - 7], [W - 1, top + 14, W - 1], "dark")
    s.box([1, top + 12, 7], [7, top + 14, W - 7], "dark"); s.box([W - 7, top + 12, 7], [W - 1, top + 14, W - 7], "dark")
    return s


# ---- moving parts: separate models the renderers turn, each centred on its pivot at (8, 8, 8) unless noted ------------------

def stirrer_fan():
    """On the stirrer motor, around the shaft (a 4px hole at the centre); its base is at y 0."""
    s = Shape()
    for f, t in (([4, 0, 4], [12, 1.5, 6]), ([4, 0, 10], [12, 1.5, 12]), ([4, 0, 6], [6, 1.5, 10]), ([10, 0, 6], [12, 1.5, 10])):
        s.box(f, t, "dark")
    for f, t in (([12, 0.25, 7], [15, 1.25, 9]), ([1, 0.25, 7], [4, 1.25, 9]), ([7, 0.25, 12], [9, 1.25, 15]), ([7, 0.25, 1], [9, 1.25, 4])):
        s.box(f, t, "band")
    return s


def gauge_needle():
    """Pointing up from its pivot, in the plane z 0-0.25."""
    s = Shape()
    s.box([7.5, 8, 0], [8.5, 11.5, 0.25], "red")
    s.box([7.25, 7.25, -0.25], [8.75, 8.75, 0.25], "dark")
    return s


MOVING = {"stirrer_fan": stirrer_fan, "gauge_needle": gauge_needle}


def write_moving():
    out = os.path.join(ASSETS, "models", "block", "moving")
    os.makedirs(out, exist_ok=True)
    for name, make in MOVING.items():
        elements = []
        for f, t, material, rot, full in make().boxes:
            faces = {face: {"uv": [r(v) for v in uv(face, [max(0, min(16, c)) for c in f], [max(0, min(16, c)) for c in t])],
                            "texture": "#" + material} for face in FACES}
            e = {"from": f, "to": t, "faces": faces}
            if rot:
                e["rotation"] = rot
            elements.append(e)
        used = sorted({e["faces"]["north"]["texture"][1:] for e in elements})
        textures = {m: MATERIALS[m] for m in used}
        textures["particle"] = MATERIALS[used[0]]
        with open(os.path.join(out, name + ".json"), "w") as fh:
            json.dump({"parent": "minecraft:block/block", "textures": textures, "elements": elements}, fh, indent=1)


# ---- slicing ----------------------------------------------------------------------------------------------------------

def uv(face, f, t):
    """Per-block texture coordinates of a face, so neighbouring blocks' textures line up."""
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {"north": [16 - x1, 16 - y1, 16 - x0, 16 - y0], "south": [x0, 16 - y1, x1, 16 - y0],
            "west": [z0, 16 - y1, z1, 16 - y0], "east": [16 - z1, 16 - y1, 16 - z0, 16 - y0],
            "up": [x0, z0, x1, z1], "down": [x0, 16 - z1, x1, 16 - z0]}[face]


def r(v):
    return round(v, 4)


def cut_faces(f, t, cell_lo, cell_hi):
    """The faces of a box clipped to a cell that lie on a cut (the original box went on past the cell)."""
    cut = set()
    names = (("west", "east"), ("down", "up"), ("north", "south"))
    for axis in range(3):
        if f[axis] < cell_lo[axis]:
            cut.add(names[axis][0])
        if t[axis] > cell_hi[axis]:
            cut.add(names[axis][1])
    return cut


def subtract_hole(f, t, x0, x1, z0, z1):
    """Splits a box around a vertical hole from (x0, z0) to (x1, z1); returns the remaining boxes."""
    if t[0] <= x0 or f[0] >= x1 or t[2] <= z0 or f[2] >= z1:
        return [(f, t)]
    out = []
    if f[0] < x0:
        out.append(([f[0], f[1], f[2]], [x0, t[1], t[2]]))
    if t[0] > x1:
        out.append(([x1, f[1], f[2]], [t[0], t[1], t[2]]))
    xa, xb = max(f[0], x0), min(t[0], x1)
    if f[2] < z0:
        out.append(([xa, f[1], f[2]], [xb, t[1], z0]))
    if t[2] > z1:
        out.append(([xa, f[1], z1], [xb, t[1], t[2]]))
    return out


def slice_machine(shape, nx, nz, ny, foreign=(), hole_columns=(), owned=None, hole_through=False):
    """
    {(mx, my, mz): [elements in that block's coordinates]} for every block of the shell that draws something. {@code foreign}
    blocks belong to other blocks (a shaft, a terminal): their pieces go to the wall north of them. Above the roof, the columns in
    {@code hole_columns} keep a 6px hole for whatever passes through them.
    """

    def inside(c):
        return 0 <= c[0] < nx and 0 <= c[1] < ny and 0 <= c[2] < nz

    def shell(c):
        if owned is not None:
            return c in owned
        return inside(c) and (c[0] in (0, nx - 1) or c[1] in (0, ny - 1) or c[2] in (0, nz - 1))

    near = [(0, -1, 0), (1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (1, 0, 1), (1, 0, -1), (-1, 0, 1), (-1, 0, -1), (0, 1, 0),
            (1, -1, 0), (-1, -1, 0), (0, -1, 1), (0, -1, -1)]

    def host(c):
        if owned is not None:
            # A sparse machine: the first owned block next to it.
            for dx, dy, dz in near:
                n = (c[0] + dx, c[1] + dy, c[2] + dz)
                if n in owned:
                    return n
            return None
        h = tuple(min(max(v, 0), n - 1) for v, n in zip(c, (nx, ny, nz)))
        return (h[0], h[1], h[2] - 1) if h in foreign else h

    parts = defaultdict(list)

    def hollow(c):
        return owned is None and inside(c) and not shell(c) and c not in foreign

    def add(cell, f, t, material, faces, rot=None):
        if owned is not None:
            h = cell if cell in owned else host(cell)
            if h is None:
                return
        elif inside(cell):
            if cell in foreign or not shell(cell):
                return  # another block's own cell, or the hollow inside
            h = cell
        else:
            h = host(cell)
        off = [h[0] * 16, h[1] * 16, h[2] * 16]
        lo = [f[i] - off[i] for i in range(3)]
        hi = [t[i] - off[i] for i in range(3)]
        cell_lo = [cell[i] * 16 for i in range(3)]
        element = {"from": [r(v) for v in lo], "to": [r(v) for v in hi], "faces": {}}
        for face in faces:
            if rot:
                w = {"north": (t[0] - f[0], t[1] - f[1]), "south": (t[0] - f[0], t[1] - f[1]), "east": (t[2] - f[2], t[1] - f[1]),
                     "west": (t[2] - f[2], t[1] - f[1]), "up": (t[0] - f[0], t[2] - f[2]), "down": (t[0] - f[0], t[2] - f[2])}[face]
                coords = [0, 0, min(16, w[0]), min(16, w[1])]
            else:
                coords = uv(face, [f[i] - cell_lo[i] for i in range(3)], [t[i] - cell_lo[i] for i in range(3)])
            element["faces"][face] = {"uv": [r(v) for v in coords], "texture": "#" + material}
        if rot:
            o = rot["origin"]
            element["rotation"] = {"origin": [r(o[i] - off[i]) for i in range(3)], "axis": rot["axis"], "angle": rot["angle"]}
        if element["faces"]:
            parts[h].append(element)

    for f, t, material, rot, _ in shape.finished():
        if rot and rot["axis"] != "y":
            # Turned about a horizontal axis: kept whole, in the block holding its centre.
            centre = tuple(math.floor((f[i] + t[i]) / 2 / 16) for i in range(3))
            add(centre, f, t, material, FACES, rot)
            continue
        if rot:
            # Turned about y: cut by height only, each slice going to the block holding its centre.
            cx, cz = math.floor((f[0] + t[0]) / 2 / 16), math.floor((f[2] + t[2]) / 2 / 16)
            for cy in range(math.floor(f[1] / 16), math.ceil(t[1] / 16)):
                y0, y1 = max(f[1], cy * 16), min(t[1], cy * 16 + 16)
                if y1 - y0 <= 0.001:
                    continue
                faces = [face for face in FACES if not (face == "down" and f[1] < y0 or face == "up" and t[1] > y1)]
                slice_rot = dict(rot, origin=[rot["origin"][0], (y0 + y1) / 2, rot["origin"][2]])
                add((cx, cy, cz), [f[0], y0, f[2]], [t[0], y1, t[2]], material, faces, slice_rot)
            continue
        lo = [math.floor(f[i] / 16) for i in range(3)]
        hi = [math.ceil(t[i] / 16) - 1 for i in range(3)]
        for cx in range(lo[0], hi[0] + 1):
            for cy in range(lo[1], hi[1] + 1):
                for cz in range(lo[2], hi[2] + 1):
                    cell_lo = [cx * 16, cy * 16, cz * 16]
                    cell_hi = [v + 16 for v in cell_lo]
                    cf = [max(f[i], cell_lo[i]) for i in range(3)]
                    ct = [min(t[i], cell_hi[i]) for i in range(3)]
                    if any(ct[i] - cf[i] <= 0.001 for i in range(3)):
                        continue
                    pieces = [(cf, ct)]
                    if (cx, cz) in hole_columns and (cy >= ny or hole_through):
                        pieces = subtract_hole(cf, ct, cx * 16 + 5, cx * 16 + 11, cz * 16 + 5, cz * 16 + 11)
                    for pf, pt in pieces:
                        cut = cut_faces(f, t, cell_lo, cell_hi)
                        # A cut facing the hollow inside stays: it lines the inside, seen through the windows.
                        cut = {face for face in cut if not hollow(tuple(c + d for c, d in zip((cx, cy, cz), STEP[face])))}
                        faces = [face for face in FACES if face not in cut]
                        add((cx, cy, cz), pf, pt, material, faces)
    return parts


# ---- attachments: one block each, drawn with the machine to the south -----------------------------------------------------

def outlet_manifold():
    s = Shape()
    s.box([1, 6, 12], [15, 10, 16], "copper"); s.box([0.5, 5.5, 15], [15.5, 10.5, 16], "brass")
    for x in (2, 6.5, 11):
        s.box([x, 7, 3], [x + 3, 9, 12], "copper"); s.box([x - 0.5, 6.5, 2], [x + 3.5, 9.5, 3], "brass")
        s.box([x + 1, 9, 7], [x + 2, 9.5, 8], "dark"); s.box([x, 9.5, 6], [x + 3, 10.5, 9], "red")
    return s


def expansion_tank():
    s = Shape()
    s.box([3, 0, 3], [13, 1, 13], "dark")
    s.box([3.5, 1, 3.5], [12.5, 15, 12.5], "plate")
    for y in (4, 11):
        s.box([3, y, 3], [13, y + 1, 13], "brass")
    s.box([5, 15, 5], [11, 16, 11], "dark")
    s.box([6, 5, 12.5], [10, 9, 16], "copper"); s.box([5.5, 4.5, 15.5], [10.5, 9.5, 16], "brass")
    return s


def machine_gauge():
    s = Shape()
    s.box([6, 5, 15], [10, 9, 16], "dark"); s.box([7, 6, 11], [9, 8, 15], "dark")
    s.box([3, 3, 9], [13, 13, 11], "brass")
    s.box([4, 4, 8.75], [12, 12, 9], "dial", uv="full")
    # The needle turns: drawn by the gauge's renderer (moving/gauge_needle).
    return s


def gasket():
    s = Shape()
    s.box([1, 1, 14], [15, 3, 16], "dark"); s.box([1, 13, 14], [15, 15, 16], "dark")
    s.box([1, 3, 14], [3, 13, 16], "dark"); s.box([13, 3, 14], [15, 13, 16], "dark")
    for x, y in ((1.5, 1.5), (7.25, 1.5), (13, 1.5), (1.5, 7.25), (13, 7.25), (1.5, 13), (7.25, 13), (13, 13)):
        s.box([x, y, 13.5], [x + 1.5, y + 1.5, 14], "brass")
    return s


def circulation_pump():
    s = Shape()
    s.box([2, 0, 3], [14, 2, 14], "dark")
    s.box([3, 2, 3], [13, 13, 13], "motor")
    for y in (5, 8, 11):
        s.box([2.5, y, 2.5], [13.5, y + 1, 13.5], "dark")
    s.box([4, 4, 13], [12, 12, 16], "brass")
    # The bearing the shaft comes in through; Create draws the turning shaft itself.
    s.box([5, 5, 1], [11, 11, 3], "dark")
    return s


def derrick_drive():
    """The Derrick's controller, a top drive: a flanged base, a ribbed gearbox, a bearing the input shaft comes down into from
    above, and copper lines for its fluids. Create draws the shaft itself (the full height, hidden inside the drive below the bearing)."""
    s = Shape()
    s.box([1, 0, 1], [15, 2, 15], "dark")
    s.box([3, 2, 3], [13, 10, 13], "motor")
    for y in (4, 7):
        s.box([2.5, y, 2.5], [13.5, y + 1, 13.5], "dark")
    s.box([4, 10, 4], [12, 12, 12], "dark")
    s.box([5, 12, 5], [11, 14, 11], "brass")
    for x0, z0, x1, z1 in ((13, 7, 15.5, 9), (0.5, 7, 3, 9)):
        s.box([x0, 3, z0], [x1, 5, z1], "copper"); s.box([x0, 2, z0], [x1, 3, z1], "brass")
    return s


def gas_vent_stack():
    """The Gas Vent, pointing up (the pipe comes in at the bottom): a bolted flange, a steel stack with a red warning band, and a
    rain cap on posts over its grilled mouth; the gas escapes through the gap under the cap (GasVentBlockEntity's nozzle)."""
    s = Shape()
    s.box([3.5, 0, 3.5], [12.5, 1.5, 12.5], "brass")
    for x, z in ((4, 4), (11, 4), (4, 11), (11, 11)):
        s.box([x, 1.5, z], [x + 1, 2, z + 1], "dark")
    s.box([5, 1.5, 5], [11, 11, 11], "band")
    s.box([4.5, 3.5, 4.5], [11.5, 4.5, 11.5], "dark")
    s.box([4.75, 7, 4.75], [11.25, 8.5, 11.25], "red")
    s.box([4.5, 11, 4.5], [11.5, 12, 11.5], "dark")
    s.box([5.5, 11.9, 5.5], [10.5, 12.1, 10.5], "darker")
    for x, z in ((4.5, 4.5), (10.5, 4.5), (4.5, 10.5), (10.5, 10.5)):
        s.box([x, 12, z], [x + 1, 14, z + 1], "dark")
    s.box([3, 14, 3], [13, 15, 13], "dark")
    s.box([5, 15, 5], [11, 16, 11], "band")
    return s


# Upgrades used on a machine rather than mounted, and other single models: only the model, no blockstate.
ITEM_ONLY = {"outlet_manifold", "gasket", "derrick_drive", "gas_vent_stack"}
ATTACHMENTS = {"gas_vent_stack": gas_vent_stack, "derrick_drive": derrick_drive, "outlet_manifold": outlet_manifold, "expansion_tank": expansion_tank, "machine_gauge": machine_gauge, "gasket": gasket,
               "circulation_pump": circulation_pump}


def write_attachments():
    for name, make in ATTACHMENTS.items():
        elements = []
        for f, t, material, rot, full in make().boxes:
            faces = {face: {"uv": [0, 0, 16, 16] if full else [r(v) for v in uv(face, f, t)], "texture": "#" + material} for face in FACES}
            elements.append({"from": f, "to": t, "faces": faces})
        used = sorted({e["faces"]["north"]["texture"][1:] for e in elements})
        textures = {m: MATERIALS[m] for m in used}
        textures["particle"] = MATERIALS["brass"]
        with open(os.path.join(ASSETS, "models", "block", name + ".json"), "w") as fh:
            json.dump({"parent": "minecraft:block/block", "textures": textures, "elements": elements}, fh, indent=1)
        if name in ITEM_ONLY:
            continue
        variants = {}
        for facing, y in (("south", 0), ("west", 90), ("north", 180), ("east", 270)):
            variant = {"model": f"reactingreactions:block/{name}"}
            if y:
                variant["y"] = y
            variants[f"facing={facing}"] = variant
        with open(os.path.join(ASSETS, "blockstates", name + ".json"), "w") as fh:
            json.dump({"variants": variants}, fh, indent=1)


# ---- output ---------------------------------------------------------------------------------------------------------

class Machine:
    """
    {@code tiers}: (width, depth, height) of each size, matching MachineTiers.java (width along the controller's north side).
    {@code foreign(w, d, h)}: cells owned by other blocks; {@code holes(w, d, h)}: columns to keep open above the roof.
    """

    def __init__(self, name, tiers, geometry, blocks, foreign=lambda w, d, h: [], holes=lambda w, d, h: [], owned=None, facing=True,
                 hole_through=False):
        self.name, self.tiers, self.geometry, self.blocks, self.foreign, self.holes = name, tiers, geometry, blocks, foreign, holes
        self.owned, self.facing, self.hole_through = owned, facing, hole_through

    def part_index(self, tier, mx, my, mz):
        base = sum(w * d * h for w, d, h in self.tiers[:tier])
        w, d, _ = self.tiers[tier]
        return 1 + base + (my * d + mz) * w + mx


MACHINES = [
    Machine("reaction_chamber", [(3, 3, 4), (5, 5, 5)], reaction_chamber, ["reaction_chamber_wall", "reaction_chamber_controller"],
            foreign=lambda w, d, h: [(w // 2, h - 1, d // 2)], holes=lambda w, d, h: [(w // 2, d // 2)]),
    Machine("airless_oven", [(3, 3, 3)], airless_oven, ["airless_oven_wall", "airless_oven_controller"]),
    # The terminals are part of the shell (their junction boxes are in the model): wall, controller and terminal share the pieces.
    Machine("electrolysis_vat", [(5, 3, 3), (7, 5, 4)], electrolysis_vat,
            ["electrolysis_vat_wall", "electrolysis_vat_controller", "electrolysis_vat_terminal"]),
    # The rig's three layers (bottom up) and the controller's layer; the pipe runs up the middle. Symmetric, so no facing.
    Machine("derrick", [(3, 3, 4)], derrick, ["derrick_block", "derrick_truss"], facing=False, hole_through=True,
            holes=lambda w, d, h: [(1, 1)],
            owned=lambda w, d, h: [(x, y, z) for y in range(3) for x in (0, 2) for z in (0, 2)]
            + [c for y in (0, 2) for c in ((1, y, 0), (1, y, 2), (0, y, 1), (2, y, 1))]),
]
# Highest part value the shared block property allows (MachineTiers.PART).
MAX_PART = 185


def write_machine(machine):
    models = os.path.join(ASSETS, "models", "block", machine.name)
    os.makedirs(models, exist_ok=True)
    for old in os.listdir(models):
        if old.startswith("part_"):
            os.remove(os.path.join(models, old))
    # formed[layer][part index] = model; layer "base" is the machine itself, the others are the upgrades' overlays.
    formed = defaultdict(dict)
    for tier, (w, d, h) in enumerate(machine.tiers):
        layers = machine.geometry(w, d, h)
        if not isinstance(layers, dict):
            layers = {"base": layers}
        for layer, shape in layers.items():
            parts = slice_machine(shape, w, d, h, machine.foreign(w, d, h), machine.holes(w, d, h),
                                  None if machine.owned is None else set(machine.owned(w, d, h)), machine.hole_through)
            for cell, elements in parts.items():
                index = machine.part_index(tier, *cell)
                used = sorted({face["texture"][1:] for e in elements for face in e["faces"].values()})
                textures = {m: MATERIALS[m] for m in used}
                textures["particle"] = MATERIALS[used[0]]
                name = f"part_{index}" if layer == "base" else f"part_{index}_{layer}"
                with open(os.path.join(models, name + ".json"), "w") as fh:
                    json.dump({"parent": "minecraft:block/block", "textures": textures, "elements": elements}, fh, indent=1)
                formed[layer][index] = f"reactingreactions:block/{machine.name}/{name}"
    assert max(formed["base"]) <= MAX_PART, machine.name
    os.makedirs(os.path.join(ASSETS, "blockstates"), exist_ok=True)
    facings = (("north", 0), ("east", 90), ("south", 180), ("west", 270)) if machine.facing else ((None, 0),)
    for block in machine.blocks:
        if len(formed) == 1:
            variants = {}
            for facing, y in facings:
                for part in range(MAX_PART + 1):
                    variant = {"model": formed["base"].get(part, f"reactingreactions:block/{block}")}
                    if part in formed["base"] and y:
                        variant["y"] = y
                    variants[f"part={part}" if facing is None else f"facing={facing},part={part}"] = variant
            state = {"variants": variants}
        else:
            # The plain block unformed; formed, its piece plus, with piped or sealed set, that upgrade's overlay.
            cases = [{"when": {"part": "0"}, "apply": {"model": f"reactingreactions:block/{block}"}}]
            for layer, models_of in formed.items():
                for part, model in sorted(models_of.items()):
                    for facing, y in facings:
                        when = {"part": str(part), "facing": facing}
                        if layer != "base":
                            when[layer] = "true"
                        apply = {"model": model}
                        if y:
                            apply["y"] = y
                        cases.append({"when": when, "apply": apply})
            state = {"multipart": cases}
        with open(os.path.join(ASSETS, "blockstates", block + ".json"), "w") as fh:
            json.dump(state, fh, indent=1)
    print(f"{machine.name}: " + ", ".join(f"{len(v)} {k}" for k, v in formed.items()))


if __name__ == "__main__":
    for m in MACHINES:
        write_machine(m)
    write_attachments()
    write_moving()
