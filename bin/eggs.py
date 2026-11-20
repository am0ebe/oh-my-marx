#!/usr/bin/env python3
"""🥚 eggs — 1 cartoon icon per feast (D-066): th app icon + top-bar head + Today hero change on th day

why: a feast used 2 change only colours + a line ➡ 🦠 @1121 "th icon should change, th whole reason o Easter eggs"
wh:  1 Python src per egg ➡ Android vector drawables (fg + bg) + adaptive launcher icons + manifest aliases + Kotlin map
     + SVG previews + 1 gallery png 4 🦠 · re-run aftr any egg edit, commit th generated files w it

  bin/eggs.py              # write all (res · manifest block · EggRes.kt · img/eggs/*.svg + gallery.png)
  bin/eggs.py --only todestag halloween   # just re-render these previews (fast look while drawing)
  bin/eggs.py --no-png     # skip inkscape (CI / no inkscape)

coords = th 108 × 108 adaptive-icon canvas · launcher shows th inner ~72 circle, safe zone = inner 66 (21..87)
"""
import argparse
import math
import pathlib
import re
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "android/app/src/main/res"
KT = ROOT / "android/app/src/main/java/de/strix/marxversion/ui/ds/EggRes.kt"
MANIFEST = ROOT / "android/app/src/main/AndroidManifest.xml"
OUT = ROOT / "img/eggs"

# ── palette (= ui/ds/Tokens.kt where it overlaps) ──
INK, HAIR, SHADE, SKIN, NOSE = "#2B2118", "#F4EEE2", "#CFC4B2", "#F2C6A0", "#E3A57E"
STACHE = "#6B6158"   # Marx' moustache = dark grey (photo), th beard below = white
FIT = .8             # whole fg scaled ➡ hats + props stay inside th launcher's 72-dp circle
RED, GOLD, PINK, WHITE, PAPER = "#B3261E", "#E8B931", "#E8788A", "#FFFFFF", "#F6EEDC"
GREEN, BLUE, ORANGE, YELLOW, PURPLE = "#2E7D32", "#1565C0", "#E65100", "#FFC107", "#6A1B9A"


# ── shapes: evy shape = 1 path dict · groups = dict w 'g' ──
def circ(cx, cy, r):
    return f"M{cx - r:.2f},{cy:.2f} a{r:.2f},{r:.2f} 0 1,0 {2 * r:.2f},0 a{r:.2f},{r:.2f} 0 1,0 {-2 * r:.2f},0 Z"


def ell(cx, cy, rx, ry):
    return f"M{cx - rx:.2f},{cy:.2f} a{rx:.2f},{ry:.2f} 0 1,0 {2 * rx:.2f},0 a{rx:.2f},{ry:.2f} 0 1,0 {-2 * rx:.2f},0 Z"


def rect(x, y, w, h):
    return f"M{x:.2f},{y:.2f} h{w:.2f} v{h:.2f} h{-w:.2f} Z"


def rrect(x, y, w, h, r):
    return (f"M{x + r:.2f},{y:.2f} h{w - 2 * r:.2f} a{r},{r} 0 0,1 {r},{r} v{h - 2 * r:.2f} a{r},{r} 0 0,1 {-r},{r} "
            f"h{-(w - 2 * r):.2f} a{r},{r} 0 0,1 {-r},{-r} v{-(h - 2 * r):.2f} a{r},{r} 0 0,1 {r},{-r} Z")


def poly(*pts):
    return "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in pts) + " Z"


def star(cx, cy, r, ri=None, n=5, rot=-90):
    ri = ri or r * .45
    pts = []
    for i in range(2 * n):
        a = math.radians(rot + i * 180 / n)
        rr = r if i % 2 == 0 else ri
        pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
    return poly(*pts)


def heart(cx, cy, s):
    return (f"M{cx:.2f},{cy + s * .9:.2f} C{cx - s * 1.4:.2f},{cy:.2f} {cx - s * .9:.2f},{cy - s * 1.1:.2f} {cx:.2f},{cy - s * .35:.2f} "
            f"C{cx + s * .9:.2f},{cy - s * 1.1:.2f} {cx + s * 1.4:.2f},{cy:.2f} {cx:.2f},{cy + s * .9:.2f} Z")


def P(d, fill=None, stroke=None, sw=0, alpha=1.0, rule=None):
    return {"d": d, "fill": fill, "stroke": stroke, "sw": sw, "alpha": alpha, "rule": rule}


def L(d, stroke=INK, sw=1.6):  # open stroke
    return P(d, None, stroke, sw)


def G(*kids, rot=0, px=54, py=54, tx=0, ty=0, s=1.0, clip=None):
    return {"g": [k for k in kids if k], "rot": rot, "px": px, "py": py, "tx": tx, "ty": ty, "s": s, "clip": clip}


def flat(*xs):
    out = []
    for x in xs:
        if x is None:
            continue
        if isinstance(x, list):
            out += flat(*x)
        else:
            out.append(x)
    return out


def outlined(puffs, fill, ink=INK, w=1.5):
    """union o circles w 1 outline: ink discs (r + w) first, fill discs on top"""
    return [P(circ(x, y, r + w), ink) for x, y, r in puffs] + [P(circ(x, y, r), fill) for x, y, r in puffs]


# ── 🧔 th cartoon Marx (all eggs build on him) ──
HAIR_PUFFS = [(42, 35, 9), (54, 31, 10), (66, 35, 9), (34, 44, 8), (74, 44, 8), (31, 54, 7.5), (77, 54, 7.5)]
BEARD_PUFFS = [(35, 64, 8.5), (73, 64, 8.5), (40, 74, 9.5), (68, 74, 9.5), (54, 78, 10), (47, 80, 8), (61, 80, 8), (54, 60, 18)]
CHEEK_PUFFS = [(43, 61, 6), (65, 61, 6), (54, 66, 7)]


def marx(eyes="dot", mouth=None, hair=HAIR, beard=None, skin=SKIN, brows="calm", halo=True, blush=False, face_extra=()):
    beard = beard or hair
    out = []
    if halo:
        out.append(halo_ring(halo))
    out += outlined(HAIR_PUFFS + BEARD_PUFFS, hair)
    if beard == "rainbow":  # 🌈 6 bands clipped 2 th beard discs
        out.append(rainbow(BEARD_PUFFS))
    elif beard != hair:  # beard colour differs: repaint th beard discs inside th outline
        out += [P(circ(x, y, r), beard) for x, y, r in BEARD_PUFFS]
    # curls = texture
    curl = SHADE if hair == HAIR else INK
    out += [L("M38,36 q3,-3 6,0", curl, 1.1), L("M60,30 q3,-3 6,0", curl, 1.1), L("M30,48 q2,-3 5,-1", curl, 1.1),
            L("M73,48 q3,-2 5,1", curl, 1.1), L("M41,77 q3,3 6,0", curl if beard == hair else INK, 1.1),
            L("M58,81 q3,3 6,0", curl if beard == hair else INK, 1.1), L("M34,66 q2,3 5,1", curl if beard == hair else INK, 1.1)]
    out.append(P(ell(54, 50, 12.5, 13.5), skin, INK, 1.3))
    if blush:
        out += [P(circ(44.5, 54.5, 2.6), PINK, alpha=.55), P(circ(63.5, 54.5, 2.6), PINK, alpha=.55)]
    out += [rainbow(CHEEK_PUFFS)] if beard == "rainbow" else [P(circ(x, y, r), beard) for x, y, r in CHEEK_PUFFS]
    out.append(L("M44,64 q4,4 8,2", INK if beard != HAIR else SHADE, 1.0))
    if beard == HAIR:  # th real Marx: dark streak under th mouth (Mayall photo) ➡ less Santa
        out.append(P("M49,60 C49,66 51,72 54,74 C57,72 59,66 59,60 Z", STACHE, alpha=.55))
    out += eyes_of(eyes)
    out += brows_of(brows, hair)
    out.append(P(ell(54, 53.2, 2.8, 3.2), NOSE, INK, 1.0))
    out += mouth_of(mouth)
    # moustache on top o th mouth (tongue / blower come out under it) · dark like th photo unless th beard is recoloured
    out.append(P("M54,57.2 C50.5,55 44.5,56 42.5,60.2 C46.5,62.4 51,61.4 54,59.6 C57,61.4 61.5,62.4 65.5,60.2 C63.5,56 57.5,55 54,57.2 Z",
                 STACHE if beard in (HAIR, "rainbow") else beard, INK, 1.0))
    out += list(face_extra)
    return out


def halo_ring(kind=True, col=GOLD):
    if kind == "tilt":  # 💀 slipped off, floating away
        return G(P(ell(54, 20, 13, 3.4), None, col, 2.4), rot=-18, px=54, py=20, tx=10, ty=-1)
    return P(ell(54, 20.5, 14, 3.6), None, col, 2.4)


def eyes_of(kind):
    l, r, y = 48.6, 59.4, 48.6
    if kind == "dot":
        return [P(circ(l, y, 1.8), INK), P(circ(r, y, 1.8), INK), P(circ(l + .6, y - .6, .55), WHITE), P(circ(r + .6, y - .6, .55), WHITE)]
    if kind == "x":
        return [L(f"M{x - 2.4},{y - 2.4} L{x + 2.4},{y + 2.4} M{x + 2.4},{y - 2.4} L{x - 2.4},{y + 2.4}", INK, 1.8) for x in (l, r)]
    if kind == "heart":
        return [P(heart(x, y, 3.1), RED, INK, .7) for x in (l, r)]
    if kind == "spiral":
        return flat([[P(circ(x, y, 3.2), WHITE, INK, .9), L(f"M{x},{y} m-1.8,0 a1.8,1.8 0 1,1 1.8,1.8 a1,1 0 1,1 -1,-1", INK, .8)] for x in (l, r)])
    if kind == "happy":
        return [L(f"M{x - 2.4},{y + .8} Q{x},{y - 2.6} {x + 2.4},{y + .8}", INK, 1.6) for x in (l, r)]
    if kind == "side":  # 🙄 side-eye
        return flat([[P(ell(x, y, 2.9, 2.2), WHITE, INK, .8), P(circ(x + 1.4, y + .2, 1.2), INK)] for x in (l, r)])
    if kind == "wide":
        return flat([[P(circ(x, y, 2.6), WHITE, INK, .8), P(circ(x, y + .3, 1.3), INK)] for x in (l, r)])
    if kind == "shades":
        return [P(rrect(41.5, 45, 11, 7, 2.6), INK), P(rrect(55.5, 45, 11, 7, 2.6), INK), L("M52.5,47.5 L55.5,47.5", INK, 1.6),
                L("M43,47 l3,0", "#FFFFFF", .9), L("M57,47 l3,0", "#FFFFFF", .9)]
    if kind == "glasses":
        return eyes_of("dot") + [P(circ(l, y, 4), None, INK, 1.2), P(circ(r, y, 4), None, INK, 1.2), L("M52.6,48.6 Q54,47.4 55.4,48.6", INK, 1.2)]
    if kind == "sad":
        return eyes_of("dot") + [P(circ(l - 1.6, y + 3.2, .9), "#64B5F6")]
    if kind == "none":
        return []
    raise ValueError(kind)


def brows_of(kind, hair):
    tilt = {"calm": (-8, 8), "angry": (18, -18), "sad": (-22, 22), "up": (-16, 16)}[kind]
    dy = -1.2 if kind == "up" else 0
    return [G(P(ell(48.4, 44.2 + dy, 4.6, 1.9), hair, INK, .9), rot=tilt[0], px=48.4, py=44.2),
            G(P(ell(59.6, 44.2 + dy, 4.6, 1.9), hair, INK, .9), rot=tilt[1], px=59.6, py=44.2)]


def mouth_of(kind):
    if kind == "tongue":  # 😵 comic dead: tongue lolls out o th beard, a bit 2 th side
        return [G(P("M51,59 L57,59 L57,66.5 C57,70.5 51,70.5 51,66.5 Z", PINK, INK, 1.1), L("M54,60 L54,66", "#C2185B", .8),
                  rot=-14, px=54, py=59)]
    if kind == "open":  # shouting
        return [P(ell(54, 64.2, 4.4, 4.2), INK), P(ell(54, 66.6, 2.6, 1.4), PINK)]
    if kind == "smile":
        return [P("M49,61.5 Q54,67 59,61.5 Z", INK), P("M51.5,64 Q54,65.6 56.5,64 Z", PINK)]
    return []


def party_hat(col=GOLD, dots=RED, tilt=-14):
    return G(P(poly((44, 27), (64, 27), (56, 4)), col, INK, 1.2), P(circ(50, 21, 1.6), dots), P(circ(57, 16, 1.6), dots),
             P(circ(55, 24, 1.6), dots), P(circ(56, 4, 3.2), dots, INK, 1), P(ell(54, 27.5, 11, 2.4), dots, INK, 1),
             rot=tilt, px=54, py=27, ty=-3)


def bg(col, *extra):
    return [P(rect(0, 0, 108, 108), col)] + flat(*extra)


def rays(col, alpha=.18, n=16, cx=54, cy=54):
    out = []
    for i in range(n):
        if i % 2:
            continue
        a0, a1 = math.radians(i * 360 / n), math.radians((i + 1) * 360 / n)
        out.append(P(poly((cx, cy), (cx + 90 * math.cos(a0), cy + 90 * math.sin(a0)), (cx + 90 * math.cos(a1), cy + 90 * math.sin(a1))), col, alpha=alpha))
    return out


def sprinkle(fn, pts, *a):
    return [fn(x, y, *a) for x, y in pts]


# outer ring o th 108 canvas (outside th 72 circle) = only seen on square-ish launcher masks
CORNERS = [(22, 22), (86, 24), (20, 84), (88, 86), (14, 52), (94, 50)]


def burst(cx, cy, r, col):
    return [L(" ".join(f"M{cx + r * .35 * math.cos(math.radians(a)):.1f},{cy + r * .35 * math.sin(math.radians(a)):.1f} "
                       f"L{cx + r * math.cos(math.radians(a)):.1f},{cy + r * math.sin(math.radians(a)):.1f}" for a in range(0, 360, 45)), col, 1.4)]


def fist(cx, cy, s=1.0, col=RED, sleeve=None):
    """✊ raised fist, knuckles up · s = scale around (cx, cy)"""
    return G(*(([P(rect(cx - 5, cy + 6, 10, 14), sleeve, INK, 1.1)] if sleeve else []) +
               [P(rrect(cx - 7.5, cy - 7, 15, 14, 3.4), col, INK, 1.2),
                L(f"M{cx - 3.7},{cy - 7} L{cx - 3.7},{cy - 1.5} M{cx},{cy - 7} L{cx},{cy - 1.5} M{cx + 3.7},{cy - 7} L{cx + 3.7},{cy - 1.5}", INK, 1.0),
                P(rrect(cx - 8.6, cy - 1.5, 9.5, 4.4, 2.1), col, INK, 1.1)]), s=s, px=cx, py=cy)


def candle(cx, base, h=22, w=8):
    return [P(rect(cx - w / 2, base - h, w, h), "#F5F5F5", INK, 1.1), L(f"M{cx},{base - h} l0,-2.4", INK, 1),
            P(f"M{cx},{base - h - 11} C{cx + 4.2},{base - h - 5} {cx + 3},{base - h - 2.2} {cx},{base - h - 2.2} C{cx - 3},{base - h - 2.2} {cx - 4.2},{base - h - 5} {cx},{base - h - 11} Z", GOLD, ORANGE, .8),
            P(ell(cx, base - h - 4.8, 1.2, 2), WHITE, alpha=.8)]


def mourning(cx, top, s=1.0, col=INK):
    """🎗️ awareness ribbon: loop on top, tails crossing down · top = loop's top y"""
    return [G(L(f"M{cx},{top + 14} C{cx - 9},{top + 4} {cx - 6},{top} {cx},{top} C{cx + 6},{top} {cx + 9},{top + 4} {cx},{top + 14} "
                f"L{cx - 9},{top + 28} M{cx},{top + 14} L{cx + 9},{top + 28}", col, 4.2), s=s, px=cx, py=top)]


def carnation(cx, cy, s=1.0, col=RED):
    petals = [(0, 0, 7), (-6, 2, 5.5), (6, 2, 5.5), (-3.5, -5, 5.5), (3.5, -5, 5.5), (0, 4, 5)]
    return G(L(f"M{cx},{cy + 6} Q{cx - 3},{cy + 22} {cx - 1},{cy + 34}", GREEN, 2.4),
             P(ell(cx + 5, cy + 22, 5, 2), GREEN), P(poly((cx - 6, cy + 3), (cx + 6, cy + 3), (cx, cy + 12)), GREEN),
             *outlined([(cx + dx, cy + dy, r) for dx, dy, r in petals], col, INK, 1.1),
             L(f"M{cx - 5},{cy - 1} q2.5,2 5,0 q2.5,-2 5,0 M{cx - 3},{cy + 4} q3,2 6,0", "#7F0000", 1.0), s=s, px=cx, py=cy)


def rose(cx, cy, s=1.0):
    return G(L(f"M{cx},{cy + 8} L{cx},{cy + 34}", GREEN, 2.4), P(ell(cx - 6, cy + 22, 5.5, 2.2), GREEN), P(ell(cx + 6, cy + 28, 5.5, 2.2), GREEN),
             P(circ(cx, cy, 10), RED, INK, 1.2), L(f"M{cx - 5},{cy - 2} Q{cx},{cy - 8} {cx + 5},{cy - 2} Q{cx + 2},{cy + 5} {cx - 3},{cy + 2} Q{cx - 1},{cy - 3} {cx + 2},{cy - 1}", "#7F0000", 1.3),
             L(f"M{cx - 9},{cy + 3} Q{cx},{cy + 12} {cx + 9},{cy + 3}", "#7F0000", 1.1), s=s, px=cx, py=cy)


def dove(cx, cy, s=1.0, branch=True):
    return G(P(f"M{cx - 10},{cy} C{cx - 6},{cy - 7} {cx + 4},{cy - 6} {cx + 9},{cy - 2} L{cx + 13},{cy - 3} L{cx + 10},{cy + 1} "
               f"C{cx + 6},{cy + 7} {cx - 6},{cy + 7} {cx - 10},{cy} Z", WHITE, INK, 1.1),
             P(f"M{cx - 3},{cy - 2} C{cx - 6},{cy - 14} {cx + 4},{cy - 16} {cx + 6},{cy - 12} C{cx + 2},{cy - 9} {cx},{cy - 5} {cx + 2},{cy - 1} Z", WHITE, INK, 1.1),
             P(circ(cx + 7, cy - 2.5, .9), INK),
             *([L(f"M{cx + 12},{cy - 2} q4,1 7,5", GREEN, 1.4), P(ell(cx + 16, cy - 1, 2.2, 1.1), GREEN), P(ell(cx + 18.5, cy + 2.5, 2.2, 1.1), GREEN)] if branch else []),
             s=s, px=cx, py=cy)


# ═══ the 30 eggs · key = Feast enum name ═══
def e_neujahr():
    return (bg("#1B1446", burst(24, 26, 12, GOLD), burst(86, 30, 10, PINK), burst(84, 84, 9, "#64B5F6"), burst(22, 84, 8, GOLD)),
            flat(marx("happy", "smile", blush=True, halo=False), party_hat(),
                 G(P("M60,63 L80,57 L80,63 Z", RED, INK, 1), L("M80,60 q4,-4 1,-6 q-3,-1 -1,3", GOLD, 1.4))))


def e_valentin():
    return (bg("#C2185B", sprinkle(lambda x, y: P(heart(x, y, 4), "#F8BBD0", alpha=.6), CORNERS)),
            flat(marx("heart", "smile", blush=True),
                 G(P("M61,56 q2.5,-3 5,0 q2.5,3 0,4 q-2.5,1 -5,0 q-2.5,-1 0,-4 Z", RED, alpha=.85), rot=-15, px=63.5, py=57),
                 G(L("M22,40 L86,24", INK, 1.6), P(poly((86, 24), (79, 23), (82, 29)), RED), P(poly((22, 40), (17, 37), (19, 43)), GOLD))))


def e_frauentag():
    # ♀ w a raised fist inside th ring — th classic feminist sign (🦠 likes ♀)
    return (bg("#6A1B9A", rays("#CE93D8", .18)),
            flat(P(circ(54, 44, 22), None, WHITE, 7), L("M54,66 L54,92 M43,81 L65,81", WHITE, 7),
                 fist(54, 46, 1.25, RED)))


def e_todestag():
    # 💀 comic dead: X eyes · tongue out · halo slipped off · grey-green skin · a † wisp floating up
    return (bg("#37474F", sprinkle(lambda x, y: P(star(x, y, 2.6), "#B0BEC5", alpha=.5), CORNERS)),
            flat(marx("x", "tongue", skin="#CFD8C4", brows="sad", halo="tilt"),
                 G(L("M26,30 q3,-4 0,-8 q-3,-4 0,-8", "#B0BEC5", 1.2), L("M82,34 q3,-4 0,-8", "#B0BEC5", 1.2)),
                 P(poly((78, 70), (82, 66), (86, 70), (82, 74)), GOLD, alpha=0)))


def e_ostern():
    shell = "M24,74 L30,66 L36,73 L42,65 L48,73 L54,65 L60,73 L66,65 L72,73 L78,66 L84,74 C84,92 70,100 54,100 C38,100 24,92 24,74 Z"
    return (bg("#AED581", sprinkle(lambda x, y: P(ell(x, y, 3, 4), "#FFF59D", alpha=.8), CORNERS)),
            flat(marx("wide", "smile", halo=False, blush=True),
                 G(P("M40,24 L44,18 L48,23 L52,16 L56,22 L60,17 L64,23 L68,19 C68,10 62,4 54,4 C46,4 40,10 40,24 Z", "#FFF8E1", INK, 1.2),
                   L("M42,12 Q54,8 66,12", RED, 2), rot=-12, px=54, py=18, ty=2),
                 P(shell, "#FFF8E1", INK, 1.3), L("M28,84 Q54,92 80,84", RED, 2.4), L("M30,92 Q54,99 78,92", BLUE, 2.4),
                 P(circ(40, 80, 1.6), GOLD), P(circ(54, 82, 1.6), GOLD), P(circ(68, 80, 1.6), GOLD)))


def e_erster_mai():
    flag = G(L("M24,94 L24,22", INK, 2), P("M24,22 Q34,18 42,24 T58,26 L58,44 Q50,40 42,44 T24,42 Z", RED, INK, 1.1))
    return (bg("#7F0000", rays("#E53935", .35)),
            flat(flag, marx("dot", "open", brows="angry"), fist(82, 52, 1.0, SKIN, sleeve=RED)))


def e_geburtstag():
    cake = [P(rrect(30, 70, 48, 20, 3), "#F8BBD0", INK, 1.2), P("M30,76 q6,5 12,0 q6,5 12,0 q6,5 12,0 q6,5 12,0 L78,73 L30,73 Z", WHITE),
            P(rect(30, 84, 48, 3), RED)] + flat([candle(x, 70, 9, 3.2) for x in (40, 54, 68)])
    return (bg("#E8B931", rays(WHITE, .25), sprinkle(lambda x, y: P(circ(x, y, 2), RED, alpha=.7), CORNERS)),
            flat(marx("happy", "smile", blush=True, halo=False), party_hat(RED, GOLD, 10), cake))


def e_kapital():
    book = [P(poly((26, 66), (54, 70), (54, 96), (26, 92)), RED, INK, 1.2), P(poly((82, 66), (54, 70), (54, 96), (82, 92)), "#8E1B15", INK, 1.2),
            L("M31,74 L49,76.5 M31,79 L49,81.5 M59,76.5 L77,74 M59,81.5 L77,79", GOLD, 1.3), P(star(40, 86, 3), GOLD), P(star(68, 86, 3), GOLD)]
    return (bg("#4E342E", sprinkle(lambda x, y: L(f"M{x - 4},{y} l8,0 M{x - 4},{y + 3} l6,0", "#8D6E63", 1.2), CORNERS)),
            flat(marx("glasses", None, brows="up"), book))


def e_halloween():
    # 🎃 pumpkin w Marx' beard + hair (jack-o'-lantern face)
    pumpkin = [P(ell(54, 48, 24, 20), ORANGE, INK, 1.4), P(ell(54, 48, 11, 20), "#F57C00", INK, 1.1), P(ell(54, 48, 4, 20), "#FB8C00", INK, 1),
               P(rrect(51, 24, 6, 7, 1.5), GREEN, INK, 1.1), L("M57,27 q6,-6 10,-1", GREEN, 1.6)]
    face = [P(poly((40, 45), (49, 45), (44.5, 37)), "#FFEB3B", INK, .9), P(poly((59, 45), (68, 45), (63.5, 37)), "#FFEB3B", INK, .9),
            P(poly((51.5, 50), (56.5, 50), (54, 46)), "#FFEB3B", INK, .9)]
    beard = outlined([(36, 66, 8), (72, 66, 8), (42, 76, 9), (66, 76, 9), (54, 80, 10), (54, 66, 12)], HAIR) + \
        [L("M42,76 q3,3 6,0 M58,80 q3,3 6,0", SHADE, 1.1)]
    hair = outlined([(27, 44, 6), (81, 44, 6), (29, 54, 5.5), (79, 54, 5.5)], HAIR)
    mouth = P("M38,58 L44,62 L48,58 L54,62 L60,58 L64,62 L70,58 Q62,66 54,66 Q46,66 38,58 Z", "#FFEB3B", INK, .9)
    stache = P("M54,61 C50,59 44,60 42,64 C46,66 51,65 54,63.5 C57,65 62,66 66,64 C64,60 58,59 54,61 Z", HAIR, INK, 1)
    return (bg("#2A1036", [P(circ(84, 22, 10), "#FFF59D", alpha=.9), P(circ(88, 19, 9), "#2A1036")],
               sprinkle(lambda x, y: P(star(x, y, 1.6), "#FFF59D", alpha=.6), [(20, 30), (30, 18), (18, 80), (92, 70)])),
            flat(hair, beard, pumpkin, face, mouth, stache))


def e_black_friday():
    tag = G(L("M54,17 L72,28", INK, 1), P("M68,28 L84,24 L92,36 L78,44 Z", GOLD, INK, 1.1), P(circ(72, 31, 1.6), "#1A1A1A"),
            P(circ(80, 33.5, 1.6), INK), P(circ(85.5, 37.5, 1.6), INK), L("M79,39 L87,32", INK, 1.3))
    return (bg("#111111", sprinkle(lambda x, y: P(circ(x, y, 3.5), "#E8B931", alpha=.25), CORNERS)),
            flat(marx("spiral", "open", brows="up"), tag,
                 [P(rrect(18, 72, 16, 18, 2), RED, INK, 1.1), L("M22,72 q4,-7 8,0", INK, 1.2),
                  P(rrect(74, 74, 16, 16, 2), GOLD, INK, 1.1), L("M78,74 q4,-7 8,0", INK, 1.2)]))


def e_weihnachten():
    hat = G(P("M33,30 Q50,2 76,14 L84,28 L72,22 Q74,26 75,30 Z", RED, INK, 1.2), P(rrect(30, 27, 48, 8, 4), WHITE, INK, 1.1),
            P(circ(84, 30, 4.4), WHITE, INK, 1.1), ty=-2)
    return (bg("#1B5E20", sprinkle(lambda x, y: P(circ(x, y, 2), WHITE, alpha=.8), CORNERS + [(30, 96), (78, 98), (54, 102)])),
            flat(marx("happy", "smile", blush=True, halo=False), hat))


def e_silvester():
    sparkler = [L("M74,92 L84,44", "#9E9E9E", 1.6), *burst(85, 40, 11, GOLD), P(star(85, 40, 4.5), "#FFF59D")]
    glass = [P("M20,58 L32,58 L30,70 Q26,74 22,70 Z", "#FFF59D", INK, 1), L("M26,73 L26,86 M21,86 L31,86", INK, 1.2),
             P(circ(24, 62, .9), WHITE), P(circ(28, 65, .8), WHITE)]
    return (bg("#0D1340", sprinkle(lambda x, y: P(star(x, y, 2.2), "#FFF59D", alpha=.8), CORNERS)),
            flat(marx("dot", "smile", halo=False), party_hat("#5C6BC0", GOLD, -6), sparkler, glass))


# ── C3 · D-052 the 18 · † days stay respectful: a symbol, no comic face ──
def e_rosa_karl():
    return (bg("#263238"), flat(carnation(54, 40, 1.35), P(circ(54, 40, 30), None, "#B0BEC5", .8, alpha=.0)))


def e_manifest():
    # 👻 "Ein Gespenst geht um in Europa" — a ghost w Marx' beard
    ghost = P("M30,92 L30,46 C30,26 42,18 54,18 C66,18 78,26 78,46 L78,92 L72,86 L66,92 L60,86 L54,92 L48,86 L42,92 L36,86 Z", "#ECEFF1", INK, 1.4)
    beard = outlined([(42, 64, 7), (66, 64, 7), (46, 73, 8), (62, 73, 8), (54, 78, 8.5), (54, 64, 10)], HAIR) + [L("M46,74 q3,3 6,0 M57,78 q3,3 6,0", SHADE, 1)]
    face = [P(ell(46, 46, 3.4, 5), INK), P(ell(62, 46, 3.4, 5), INK), P(circ(47, 44.5, 1), WHITE), P(circ(63, 44.5, 1), WHITE),
            P("M54,58 C50.5,56 44.5,57 42.5,61 C46.5,63 51,62 54,60.5 C57,62 61.5,63 65.5,61 C63.5,57 57.5,56 54,58 Z", HAIR, INK, 1)]
    pamph = G(P(rect(72, 64, 18, 24), PAPER, INK, 1.1), L("M75,70 l12,0", RED, 2), L("M75,75 l12,0 M75,79 l12,0 M75,83 l8,0", INK, .9), rot=12, px=81, py=76)
    return (bg("#7F0000", rays("#B3261E", .5)), flat(ghost, face, beard, pamph))


def e_kommune():
    cap = G(P("M30,32 Q28,6 58,8 Q80,10 82,22 Q76,18 70,22 L78,32 Z", RED, INK, 1.3), P(rrect(28, 28, 52, 7, 3.5), "#8E1B15", INK, 1.1),
            P(circ(38, 30, 5), "#1E3A8A", INK, .9), P(circ(38, 30, 3.4), WHITE), P(circ(38, 30, 1.8), RED), ty=-2)
    return (bg("#0D1B4F", rays("#B3261E", .35)), flat(marx("dot", "smile", halo=False, brows="up"), cap))


def e_workers_memorial():
    hat = [P("M28,62 Q28,30 54,30 Q80,30 80,62 Z", YELLOW, INK, 1.4), P(rrect(20, 60, 68, 8, 4), "#FFB300", INK, 1.3), P(rrect(50, 32, 8, 28, 3), "#FFD54F", INK, 1)]
    ribbon = mourning(54, 72, 1.0)
    return (bg("#263238"), flat(hat, ribbon))


def e_befreiung():
    # 8 Mai: th red flag over a broken chain
    chain = [P(rrect(20, 72, 22, 12, 6), None, "#B0BEC5", 4), G(P(rrect(36, 72, 22, 12, 6), None, "#B0BEC5", 4), rot=-25, px=47, py=78, tx=-2, ty=-4),
             G(P(rrect(58, 72, 22, 12, 6), None, "#B0BEC5", 4), rot=25, px=69, py=78, tx=4, ty=-4), P(rrect(76, 72, 14, 12, 6), None, "#B0BEC5", 4)]
    flag = [L("M40,70 L40,18", "#CFD8DC", 2), P("M40,18 Q52,13 62,19 T84,21 L84,45 Q74,40 62,44 T40,42 Z", RED, INK, 1.1)]
    return (bg("#37474F"), flat(flag, chain))


def e_kindertag():
    beanie = [P("M33,32 Q33,16 54,16 Q75,16 75,32 Z", RED, INK, 1.2), P("M44,17 Q54,14 64,17 L64,32 L44,32 Z", YELLOW, alpha=.0),
              L("M54,16 L54,9", INK, 1.4), P(ell(45, 8, 9, 2.6), BLUE, INK, 1), P(ell(63, 8, 9, 2.6), YELLOW, INK, 1), P(circ(54, 8, 2), RED, INK, .8),
              P(rrect(31, 30, 46, 5, 2.5), BLUE, INK, 1)]
    kite = [G(P(poly((86, 60), (96, 72), (86, 88), (76, 72)), YELLOW, INK, 1), L("M86,60 L86,88 M76,72 L96,72", RED, 1), rot=10, px=86, py=72),
            L("M84,88 q-4,4 0,8 q4,4 0,8", INK, 1), L("M72,82 L64,78", INK, .8, )]
    lolly = [L("M24,92 L30,70", "#ECEFF1", 2), P(circ(30, 66, 7), PINK, INK, 1), L("M26,66 q4,-5 8,0 q-4,5 -6,1", WHITE, 1.4)]
    return (bg("#42A5F5", sprinkle(lambda x, y: P(circ(x, y, 2.4), WHITE, alpha=.6), CORNERS)),
            flat(marx("wide", "smile", blush=True, halo=False), beanie, kite, lolly))


def e_weber():
    yarn = [P(circ(78, 76, 12), RED, INK, 1.2), L("M68,70 Q78,64 88,72 M67,77 Q78,72 89,80 M70,84 Q78,80 86,86", "#7F0000", 1.1),
            L("M70,58 L94,92", "#8D6E63", 2.2), L("M88,58 L66,92", "#8D6E63", 2.2), P(circ(70, 58, 1.8), GOLD), P(circ(88, 58, 1.8), GOLD),
            L("M66,76 Q58,80 56,86", RED, 1.4)]
    return (bg("#558B2F", sprinkle(lambda x, y: L(f"M{x - 5},{y} l10,0 M{x},{y - 5} l0,10", "#8BC34A", 1.4), CORNERS)),
            flat(marx("dot", "smile"), yarn,
                 L("M40,74 q3,3 6,0 q3,3 6,0 q3,3 6,0", RED, 1.5), L("M44,82 q3,3 6,0 q3,3 6,0", RED, 1.5)))


def e_laengster_tag():
    # ☀️ Sun-Marx: hair + beard = th corona, + shades
    sun_rays = []
    for i in range(16):
        a = math.radians(i * 22.5)
        sun_rays.append(P(poly((54 + 30 * math.cos(a - .12), 54 + 30 * math.sin(a - .12)), (54 + 40 * math.cos(a), 54 + 40 * math.sin(a)),
                               (54 + 30 * math.cos(a + .12), 54 + 30 * math.sin(a + .12))), ORANGE, INK, .9))
    return (bg("#4FC3F7", sprinkle(lambda x, y: P(ell(x, y, 6, 2.6), WHITE, alpha=.7), [(20, 22), (88, 90), (90, 20)])),
            flat(sun_rays, marx("shades", "smile", hair="#FFC107", halo=False)))


BANDS = ["#E53935", "#FB8C00", "#FDD835", "#43A047", "#1E88E5", "#8E24AA"]


def rainbow(puffs):
    return G(*[P(rect(0, 55 + i * 6, 108, 6.1), c) for i, c in enumerate(BANDS)], clip=" ".join(circ(x, y, r) for x, y, r in puffs))


def e_csd():
    return (bg("#FCE4EC", [P(rect(0, i * 18, 108, 18), c, alpha=.35) for i, c in enumerate(BANDS)]),
            flat(marx("happy", "smile", beard="rainbow", blush=True)))


def e_matchgirls():
    # 🔥 STRIKE = strike a match · matchbox + burning match
    box = [G(P(rect(22, 62, 46, 30), "#FBC02D", INK, 1.3), P(rect(22, 62, 46, 8), RED, INK, 1.1), P(rect(22, 84, 46, 8), RED, INK, 1.1),
             rot=-8, px=45, py=77), fist(45, 77, .7, RED)]
    match = [L("M62,90 L80,40", "#D7B98E", 3.4), P(ell(80.6, 38.4, 3.6, 4.4), RED, INK, 1),
             P("M82,12 C92,22 92,32 82,36 C74,36 70,28 74,20 C76,26 79,26 80,22 C79,18 80,15 82,12 Z", ORANGE, INK, 1),
             P("M81,22 C86,28 85,33 81,34 C78,33 77,29 79,26 Z", YELLOW)]
    return (bg("#4E342E", rays("#FF7043", .3, cx=82, cy=30)), flat(match, box))


def e_bastille():
    cockade = [P(circ(36, 40, 7), "#1E3A8A", INK, 1), P(circ(36, 40, 4.6), WHITE), P(circ(36, 40, 2.4), RED), L("M33,46 L30,56 M39,46 L42,56", RED, 2)]
    return (bg(WHITE, [P(rect(0, 0, 36, 108), "#1E3A8A"), P(rect(72, 0, 36, 108), RED)]),
            flat(marx("side", None, brows="up"), cockade))


def e_engels_tod():
    return (bg("#212121", [P(circ(54, 42, 26), GOLD, alpha=.12)]), flat(candle(54, 84, 32, 12), [L("M38,86 L70,86", "#616161", 2)]))


def e_peterloo():
    # Peterloo banners carried a cap o liberty on a pole · muted, black ribbon
    banner = [L("M30,40 L78,40", "#BCAAA4", 2), P("M32,40 L76,40 L76,70 Q65,66 54,70 Q43,74 32,70 Z", "#2E7D32", INK, 1.2),
              L("M38,50 L70,50 M38,57 L70,57", "#C8E6C9", 2.2), L("M42,63 L66,63", "#C8E6C9", 2.2)]
    cap = [P("M44,32 Q42,12 58,14 Q70,16 70,24 Q66,22 62,24 L66,32 Z", "#8E1B15", INK, 1.2), P(rrect(42, 30, 26, 5, 2.5), "#5D4037", INK, 1)]
    ribbon = mourning(70, 74, .7)
    return (bg("#5D4037"), flat(L("M54,98 L54,30", "#BCAAA4", 2.4), banner, cap, ribbon))


def e_antikrieg():
    rifle = G(P(rrect(20, 86, 30, 5, 1.5), "#795548", INK, 1), P(rrect(56, 86, 32, 5, 1.5), "#795548", INK, 1),
              P(poly((50, 86), (53, 82), (56, 86), (53, 92)), "#90A4AE"), rot=0)
    return (bg("#64B5F6", sprinkle(lambda x, y: P(ell(x, y, 7, 3), WHITE, alpha=.6), [(20, 22), (90, 34), (18, 66)])),
            flat(marx("happy", "smile", halo=False), dove(56, 26, 1.1), rifle))


def e_internationale():
    # 🌍 th globe wears Marx' hair + beard
    globe = [P(circ(54, 50, 22), "#1E88E5", INK, 1.4), P("M40,36 Q48,32 52,38 Q50,46 44,46 Q38,44 40,36 Z", "#43A047"),
             P("M58,44 Q68,40 72,48 Q70,60 62,62 Q56,56 58,44 Z", "#43A047"), P("M42,56 Q48,54 48,62 Q44,66 40,62 Z", "#43A047"),
             L("M33,50 L75,50", "#90CAF9", .8), P(ell(54, 50, 9, 22), None, "#90CAF9", .8)]
    hair = outlined([(30, 38, 6.5), (78, 38, 6.5), (28, 50, 6), (80, 50, 6)], HAIR)
    beard = outlined([(36, 68, 8), (72, 68, 8), (42, 78, 9), (66, 78, 9), (54, 82, 10), (54, 70, 13)], HAIR)
    face = [P(circ(47, 46, 2), INK), P(circ(61, 46, 2), INK), P(circ(47.6, 45.4, .6), WHITE), P(circ(61.6, 45.4, .6), WHITE),
            P("M54,64 C50,62 44,63 42,67 C46,69 51,68 54,66.5 C57,68 62,69 66,67 C64,63 58,62 54,64 Z", HAIR, INK, 1)]
    hands = [P(rrect(16, 84, 18, 8, 4), SKIN, INK, 1), P(rrect(74, 84, 18, 8, 4), "#8D5524", INK, 1), P(rrect(32, 82, 44, 11, 5), RED, INK, 1.1)]
    return (bg("#B3261E", rays("#E53935", .5)), flat(hair, beard, globe, face, hands[:2]))


def e_armut():
    pockets = [P("M22,74 L40,74 L38,92 Q31,96 24,92 Z", WHITE, INK, 1.2), P("M68,74 L86,74 L84,92 Q77,96 70,92 Z", WHITE, INK, 1.2),
               L("M25,80 l12,0 M71,80 l12,0", SHADE, 1)]
    moth = G(P(ell(-4, 0, 4, 2.6), "#BCAAA4", INK, .8), P(ell(4, 0, 4, 2.6), "#BCAAA4", INK, .8), P(ell(0, 0, 1, 3.4), "#6D4C41"),
             L("M-1,-3 l-2,-3 M1,-3 l2,-3", INK, .6), tx=80, ty=26, rot=0, px=0, py=0)
    return (bg("#795548", sprinkle(lambda x, y: P(circ(x, y, 1.4), "#A1887F"), CORNERS)), flat(marx("sad", None, brows="sad"), pockets, moth))


def e_engels_geb():
    # 🤝 Engels (brown beard, party hat) shakes hands w Marx' sleeve · th rent got paid
    m = marx("happy", "smile", hair="#A1887F", brows="calm", halo=False, blush=True)
    shake = [P(rrect(22, 82, 26, 10, 5), "#1565C0", INK, 1.1), P(rrect(60, 82, 26, 10, 5), INK, INK, 1.1),
             P(rrect(42, 80, 24, 13, 6), SKIN, INK, 1.2), L("M48,82 l0,9 M53,82 l0,9 M58,82 l0,9", INK, .8)]
    return (bg("#1565C0", rays("#E8B931", .3), sprinkle(lambda x, y: P(rect(x, y, 3, 3), GOLD), CORNERS)),
            flat([G(*m, s=.88, px=54, py=46)], party_hat(GOLD, RED, 14), shake))


def e_jenny():
    return (bg("#311B92", [P(circ(54, 54, 34), "#4A148C")]), flat(rose(54, 40, 1.4)))


EGGS = {  # Feast enum name ➡ (designer, 1-line idea 4 th gallery)
    "NEUJAHR": (e_neujahr, "party Marx + horn · fireworks"),
    "VALENTIN": (e_valentin, "heart eyes · lipstick kiss · Cupid arrow thru"),
    "FRAUENTAG": (e_frauentag, "♀ w a red fist inside"),
    "TODESTAG": (e_todestag, "comic dead: X eyes · tongue out · halo slipping off"),
    "OSTERN": (e_ostern, "hatches fr a painted egg · shell cap"),
    "ERSTER_MAI": (e_erster_mai, "shouting · red flag · raised fist"),
    "GEBURTSTAG": (e_geburtstag, "party hat + cake w 3 candles"),
    "KAPITAL": (e_kapital, "reading glasses over th open book"),
    "HALLOWEEN": (e_halloween, "jack-o'-lantern w Marx' beard + hair"),
    "BLACK_FRIDAY": (e_black_friday, "hypnotised (fetish) · % price tag · shopping bags"),
    "WEIHNACHTEN": (e_weihnachten, "Marx = Santa (th beard was always ready)"),
    "SILVESTER": (e_silvester, "sparkler + bubbly"),
    "ROSA_KARL": (e_rosa_karl, "† red carnation (quiet)"),
    "MANIFEST": (e_manifest, "👻 th spectre — ghost w Marx' beard + pamphlet"),
    "KOMMUNE": (e_kommune, "Phrygian cap + cockade"),
    "WORKERS_MEMORIAL": (e_workers_memorial, "† hard hat + black ribbon (quiet)"),
    "BEFREIUNG": (e_befreiung, "† red flag over a broken chain (quiet)"),
    "KINDERTAG": (e_kindertag, "propeller beanie · kite · lolly"),
    "WEBER": (e_weber, "yarn + needles, knitting his beard"),
    "LAENGSTER_TAG": (e_laengster_tag, "Sun-Marx: hair = corona, shades"),
    "CSD": (e_csd, "rainbow beard"),
    "MATCHGIRLS": (e_matchgirls, "STRIKE = a burning match + ✊ matchbox"),
    "BASTILLE": (e_bastille, "side-eye at th bourgeois revolution · cockade · tricolore"),
    "ENGELS_TOD": (e_engels_tod, "† a candle (quiet)"),
    "PETERLOO": (e_peterloo, "† cap o liberty on a pole + ribbon (quiet)"),
    "ANTIKRIEG": (e_antikrieg, "dove on his head · broken rifle"),
    "INTERNATIONALE": (e_internationale, "th globe wears Marx' beard"),
    "ARMUT": (e_armut, "sad · empty pockets · a moth"),
    "ENGELS_GEB": (e_engels_geb, "Engels (brown beard, party hat) · handshake"),
    "JENNY": (e_jenny, "† a red rose (quiet)"),
}


# ── emit ──
def a(col):  # #RRGGBB ➡ #FFRRGGBB
    return "#FF" + col[1:] if col and len(col) == 7 else col


def to_vector(shapes, comment):
    lines = ['<?xml version="1.0" encoding="utf-8"?>', f"<!-- GENERATED by bin/eggs.py — {comment} · edit th .py, not this -->",
             '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
             '    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">']

    def emit(s, ind):
        sp = " " * ind
        if "g" in s:
            attrs = []
            if s["rot"]:
                attrs.append(f'android:rotation="{s["rot"]}"')
            if s["rot"] or s["s"] != 1:
                attrs += [f'android:pivotX="{s["px"]}"', f'android:pivotY="{s["py"]}"']
            if s["s"] != 1:
                attrs += [f'android:scaleX="{s["s"]}"', f'android:scaleY="{s["s"]}"']
            if s["tx"]:
                attrs.append(f'android:translateX="{s["tx"]}"')
            if s["ty"]:
                attrs.append(f'android:translateY="{s["ty"]}"')
            lines.append(f"{sp}<group {' '.join(attrs)}>")
            if s["clip"]:
                lines.append(f'{sp}  <clip-path android:pathData="{s["clip"]}" />')
            for k in s["g"]:
                emit(k, ind + 2)
            lines.append(f"{sp}</group>")
            return
        attrs = [f'android:pathData="{s["d"]}"']
        if s["fill"]:
            attrs.append(f'android:fillColor="{a(s["fill"])}"')
            if s["alpha"] != 1:
                attrs.append(f'android:fillAlpha="{s["alpha"]}"')
        if s["stroke"] and s["sw"]:
            attrs += [f'android:strokeColor="{a(s["stroke"])}"', f'android:strokeWidth="{s["sw"]}"',
                      'android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
            if s["alpha"] != 1:
                attrs.append(f'android:strokeAlpha="{s["alpha"]}"')
        if s["rule"]:
            attrs.append('android:fillType="evenOdd"')
        lines.append(f"{sp}<path {' '.join(attrs)} />")

    for s in shapes:
        emit(s, 4)
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def to_svg(layers, size=108, mask=None):
    n = [0]
    body = []

    def emit(s):
        if "g" in s:
            t = f'translate({s["tx"]} {s["ty"]}) translate({s["px"]} {s["py"]}) rotate({s["rot"]}) scale({s["s"]}) translate({-s["px"]} {-s["py"]})'
            clip = ""
            if s["clip"]:
                n[0] += 1
                body.append(f'<clipPath id="c{n[0]}"><path d="{s["clip"]}"/></clipPath>')
                clip = f' clip-path="url(#c{n[0]})"'
            body.append(f'<g transform="{t}"><g{clip}>')
            for k in s["g"]:
                emit(k)
            body.append("</g></g>")
            return
        st = f'fill="{s["fill"] or "none"}"'
        if s["fill"] and s["alpha"] != 1:
            st += f' fill-opacity="{s["alpha"]}"'
        if s["stroke"] and s["sw"]:
            st += f' stroke="{s["stroke"]}" stroke-width="{s["sw"]}" stroke-linecap="round" stroke-linejoin="round"'
            if s["alpha"] != 1:
                st += f' stroke-opacity="{s["alpha"]}"'
        if s["rule"]:
            st += ' fill-rule="evenodd"'
        body.append(f'<path d="{s["d"]}" {st}/>')

    for layer in layers:
        for s in layer:
            emit(s)
    inner = "\n".join(body)
    if mask:  # launcher circle (72 o 108)
        inner = f'<clipPath id="m"><circle cx="54" cy="54" r="36"/></clipPath><g clip-path="url(#m)">{inner}</g>'
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{mask or "0 0 108 108"}" width="{size}" height="{size}">{inner}</svg>\n'


def snake(k):
    return k.lower()


def camel(k):
    return "".join(w.capitalize() for w in k.split("_"))


def write(p, txt):
    p.parent.mkdir(parents=True, exist_ok=True)
    if not p.exists() or p.read_text() != txt:
        p.write_text(txt)


def adaptive(k):
    return ('<?xml version="1.0" encoding="utf-8"?>\n<!-- GENERATED by bin/eggs.py · 🥚 launcher icon on th feast day (activity-alias, D-066) -->\n'
            '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
            f'    <background android:drawable="@drawable/egg_bg_{k}" />\n    <foreground android:drawable="@drawable/egg_{k}" />\n'
            '    <!-- no <monochrome> on purpose: themed-icon launchers show th egg i full colour = it pops on th day -->\n'
            '</adaptive-icon>\n')


BEGIN, END = "<!-- 🥚 eggs:begin (GENERATED by bin/eggs.py) -->", "<!-- 🥚 eggs:end -->"


def manifest_block():
    out = [BEGIN]
    for k in EGGS:
        out += [f'        <activity-alias android:name=".Egg{camel(k)}" android:targetActivity=".HomeActivity"',
                f'            android:enabled="false" android:exported="true" android:label="@string/app_name"',
                f'            android:icon="@mipmap/ic_egg_{snake(k)}" android:roundIcon="@mipmap/ic_egg_{snake(k)}">',
                '            <intent-filter><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.LAUNCHER" /></intent-filter>',
                '            <meta-data android:name="android.app.shortcuts" android:resource="@xml/shortcuts" />',
                '        </activity-alias>']
    out.append("        " + END)
    return "\n".join(out)


def kotlin():
    rows = lambda f: "\n".join(f"        Feast.{k} -> {f(k)}" for k in EGGS)
    return f"""package de.strix.marxversion.ui.ds

// GENERATED by bin/eggs.py — edit th .py, re-run, commit both
import de.strix.marxversion.R
import de.strix.marxversion.logic.Feast

/** 🥚 D-066 · 1 cartoon icon per feast: fg + bg vectors (in-app head + hero) + launcher activity-alias (data/EggIcon.kt) */
object EggRes {{
    fun fg(f: Feast): Int = when (f) {{
{rows(lambda k: f"R.drawable.egg_{snake(k)}")}
    }}

    fun bg(f: Feast): Int = when (f) {{
{rows(lambda k: f"R.drawable.egg_bg_{snake(k)}")}
    }}

    /** activity-alias class name (manifest) · namespace, not applicationId ➡ same in DEV */
    fun alias(f: Feast): String = "de.strix.marxversion.Egg" + when (f) {{
{rows(lambda k: f'"{camel(k)}"')}
    }}
}}
"""


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--only", nargs="*", help="feast keys (any case) ➡ previews only")
    ap.add_argument("--no-png", action="store_true")
    o = ap.parse_args()
    keys = [k for k in EGGS if not o.only or k.lower() in [x.lower() for x in o.only]]
    OUT.mkdir(parents=True, exist_ok=True)
    for k in keys:
        b, f = EGGS[k][0]()
        f = [G(*f, s=FIT, px=54, py=56)]
        write(OUT / f"{snake(k)}.svg", to_svg([b, f], 216))
        write(OUT / f"{snake(k)}_launcher.svg", to_svg([b, f], 216, mask="18 18 72 72"))
        if o.only:
            continue
        write(RES / f"drawable/egg_{snake(k)}.xml", to_vector(f, f"🥚 {k} fg: {EGGS[k][1]}"))
        write(RES / f"drawable/egg_bg_{snake(k)}.xml", to_vector(b, f"🥚 {k} bg"))
        write(RES / f"mipmap-anydpi-v26/ic_egg_{snake(k)}.xml", adaptive(snake(k)))
    if not o.only:
        write(KT, kotlin())
        m = MANIFEST.read_text()
        if BEGIN not in m:
            sys.exit(f"❌ {MANIFEST.name}: marker missing ({BEGIN})")
        m = re.sub(re.escape(BEGIN) + r".*?" + re.escape(END), lambda _: manifest_block(), m, flags=re.S)
        write(MANIFEST, m)
    if o.no_png:
        return
    pngs = []
    for k in keys:
        png = OUT / f"{snake(k)}.png"
        subprocess.run(["inkscape", str(OUT / f"{snake(k)}_launcher.svg"), "-o", str(png), "-w", "216"], check=True, capture_output=True)
        pngs.append((k, png))
    tiles = []
    for k, png in pngs:
        t = OUT / f".tile_{snake(k)}.png"
        subprocess.run(["magick", str(png), "-background", "#F6EEDC", "-gravity", "south", "-splice", "0x34", "-font", "DejaVu-Sans",
                        "-pointsize", "17", "-fill", "#2B2118", "-annotate", "+0+6", snake(k), str(t)], check=True)
        tiles.append(str(t))
    name = "gallery.png" if not o.only else "gallery_only.png"
    subprocess.run(["magick", "montage", *tiles, "-tile", "6x", "-geometry", "+10+10", "-background", "#F6EEDC", str(OUT / name)], check=True)
    for t in tiles:
        pathlib.Path(t).unlink()
    for _, png in pngs:
        png.unlink()
    print(f"🥚 {len(keys)} eggs ➡ {OUT / name}")


if __name__ == "__main__":
    main()
