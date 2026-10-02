#!/usr/bin/env python3
"""Synthesises this mod's sound effects (no third-party audio is used) and encodes them as OGG with ffmpeg.

Usage: python tools/make_sounds.py [--all]   (only missing clips, unless --all: re-encoding changes the files' bytes)
Needs numpy, scipy and an ffmpeg with libvorbis. Writes src/main/resources/assets/reactingreactions/sounds/*.ogg.
"""
import os
import sys
import subprocess
import tempfile

import numpy as np
from scipy.io import wavfile
from scipy.signal import butter, lfilter

RATE = 44100
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "assets", "reactingreactions", "sounds")
rng = np.random.default_rng(7)


def t(seconds):
    return np.arange(int(RATE * seconds)) / RATE


def noise(seconds):
    return rng.standard_normal(int(RATE * seconds))


def band(x, low=None, high=None, order=3):
    if low and high:
        b, a = butter(order, [low / (RATE / 2), high / (RATE / 2)], btype="band")
    elif low:
        b, a = butter(order, low / (RATE / 2), btype="high")
    else:
        b, a = butter(order, high / (RATE / 2), btype="low")
    return lfilter(b, a, x)


def fade(x, fade_in=0.02, fade_out=0.05):
    n = len(x)
    env = np.ones(n)
    i = int(RATE * fade_in)
    o = int(RATE * fade_out)
    env[:i] = np.linspace(0, 1, i)
    env[-o:] = np.linspace(1, 0, o)
    return x * env


def crossfade_loop(x, overlap=0.15):
    """Makes the ends meet so a repeated clip does not click."""
    k = int(RATE * overlap)
    head, tail = x[:k].copy(), x[-k:].copy()
    ramp = np.linspace(0, 1, k)
    x = x.copy()
    x[:k] = head * ramp + tail * (1 - ramp)
    return x[:-k]


def drill_rumble():
    """A low, smooth engine rumble: low-passed noise and two slowly beating sines, no high-frequency hiss."""
    s = 1.2
    tt = t(s)
    rumble = band(noise(s), high=110) * 2.2
    engine = (np.sin(2 * np.pi * 47 * tt) * 0.35 + np.sin(2 * np.pi * 94 * tt) * 0.2 + np.sin(2 * np.pi * 141 * tt) * 0.06)
    engine *= 0.8 + 0.2 * np.sin(2 * np.pi * 6 * tt)
    grind = band(noise(s), 180, 520) * (0.5 + 0.5 * np.sin(2 * np.pi * 11 * tt)) * 0.1
    return fade(crossfade_loop(rumble + engine + grind), 0.04, 0.08)


def scrubber_hum():
    s = 2.4
    tt = t(s)
    hum = np.sin(2 * np.pi * 110 * tt) * 0.3 + np.sin(2 * np.pi * 220 * tt) * 0.15 + np.sin(2 * np.pi * 331 * tt) * 0.05
    hum *= 0.8 + 0.2 * np.sin(2 * np.pi * 5 * tt)
    fan = band(noise(s), 600, 3000) * 0.18
    return crossfade_loop(hum + fan)


def gas_hiss():
    s = 1.6
    tt = t(s)
    x = band(noise(s), low=2500) * 0.5
    env = np.minimum(1, tt / 0.08) * np.exp(-tt * 1.6)
    return fade(x * env)


def ignite_whoosh():
    s = 1.1
    tt = t(s)
    x = band(noise(s), 200, 5000) * 0.7
    env = np.sin(np.pi * np.clip(tt / s, 0, 1)) ** 2 * np.exp(-tt * 0.8)
    crackle = (rng.random(len(tt)) > 0.997) * rng.standard_normal(len(tt)) * 2 * (tt > 0.15)
    return fade(x * env + band(crackle, low=1500) * 0.4 * np.exp(-tt * 2))


def pool_splash():
    s = 0.35
    tt = t(s)
    x = band(noise(s), 400, 4000) * np.exp(-tt * 14)
    drop = np.sin(2 * np.pi * (900 - 1500 * tt) * tt) * np.exp(-tt * 18) * 0.4
    return fade(x + drop, 0.002, 0.05)


def leak_drip():
    """A single drop landing: a short falling plink over a soft tap."""
    s = 0.25
    tt = t(s)
    plink = np.sin(2 * np.pi * (1400 - 1800 * tt) * tt) * np.exp(-tt * 30) * 0.6
    tap = band(noise(s), 800, 5000) * np.exp(-tt * 60) * 0.3
    return fade(plink + tap, 0.001, 0.04)


def vat_buzz():
    """Mains hum with harmonics and a faint crackle of arcing."""
    s = 2.15
    tt = t(s)
    hum = sum(np.sin(2 * np.pi * 100 * k * tt) * a for k, a in ((1, 0.35), (2, 0.2), (3, 0.12), (5, 0.05)))
    hum *= 0.85 + 0.15 * np.sin(2 * np.pi * 3 * tt)
    crackle = band((rng.random(len(tt)) > 0.9985) * rng.standard_normal(len(tt)), low=2000) * 0.6
    return crossfade_loop(hum + crackle)


def boiling():
    """A low simmer with bubbles popping at random."""
    s = 2.15
    tt = t(s)
    simmer = band(noise(s), 80, 400) * 0.25
    bubbles = np.zeros(len(tt))
    for start in rng.uniform(0, s - 0.1, 26):
        i = int(start * RATE)
        bt = t(0.06)
        pitch = rng.uniform(300, 900)
        pop = np.sin(2 * np.pi * pitch * (1 + bt * 6) * bt) * np.exp(-bt * 70) * rng.uniform(0.3, 0.8)
        bubbles[i:i + len(pop)] += pop[:len(bubbles) - i]
    return crossfade_loop(simmer + bubbles)


def oven_roar():
    """A deep, steady fire roar."""
    s = 2.15
    tt = t(s)
    roar = band(noise(s), 40, 260) * 1.6
    roar *= 0.8 + 0.2 * np.sin(2 * np.pi * 1.3 * tt)
    flutter = band(noise(s), 300, 1200) * (0.5 + 0.5 * np.sin(2 * np.pi * 7 * tt)) * 0.12
    return crossfade_loop(roar + flutter)


SOUNDS = {
    "drill_rumble": drill_rumble,
    "scrubber_hum": scrubber_hum,
    "gas_hiss": gas_hiss,
    "ignite_whoosh": ignite_whoosh,
    "pool_splash": pool_splash,
    "leak_drip": leak_drip,
    "vat_buzz": vat_buzz,
    "boiling": boiling,
    "oven_roar": oven_roar,
}


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, make in SOUNDS.items():
        x = make()
        if os.path.exists(os.path.join(OUT, name + ".ogg")) and "--all" not in sys.argv:
            continue
        x = x / max(1e-6, np.max(np.abs(x))) * 0.8
        with tempfile.TemporaryDirectory() as tmp:
            wav = os.path.join(tmp, name + ".wav")
            wavfile.write(wav, RATE, (x * 32767).astype(np.int16))
            subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-ac", "1", "-c:a", "libvorbis", "-q:a", "4",
                            os.path.join(OUT, name + ".ogg")], check=True)
        print("wrote", name)


if __name__ == "__main__":
    main()
