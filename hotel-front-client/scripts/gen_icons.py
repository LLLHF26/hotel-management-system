"""Generate unified minimal solid icons for hotel-front-client.
Style: solid fill #3C3C46 (charcoal), transparent bg, 120x120px.
Matches /static/quick/*.png approved aesthetic.
"""
from PIL import Image, ImageDraw
import os

OUT = "D:/PyCharm 2025/PyCharm 2026.1.2/Projects/hotel/hotel-front-client/static/icons"
os.makedirs(OUT, exist_ok=True)

C = (60, 60, 70)   # #3C3C46
SZ = 120


def new_canvas():
    img = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def save(img, name):
    path = os.path.join(OUT, f"{name}.png")
    img.save(path)
    print(f"  OK {name}.png ({img.size})")


# ============================================================
# 1. user / person  (👤)
# ============================================================
def icon_user():
    img, d = new_canvas()
    # head circle
    d.ellipse([40, 22, 80, 62], fill=C)
    # body semi-circle
    d.ellipse([28, 50, 92, 108], fill=C)
    save(img, "user")


# ============================================================
# 2. search / magnifier  (🔍)
# ============================================================
def icon_search():
    img, d = new_canvas()
    # circle
    d.ellipse([24, 24, 84, 84], fill=C)
    # handle
    d.rectangle([72, 72, 100, 100], fill=C)
    save(img, "search")


# ============================================================
# 3. area / ruler  (📐)
# ============================================================
def icon_area():
    img, d = new_canvas()
    # L-shape ruler
    d.polygon([(26, 30), (90, 30), (90, 42), (38, 42), (38, 94), (26, 94)], fill=C)
    # small tick marks
    for x in [48, 58, 68, 78]:
        d.rectangle([x, 30, x+2, 38], fill=C)
    for y in [52, 62, 72, 82]:
        d.rectangle([26, y, 34, y+2], fill=C)
    save(img, "area")


# ============================================================
# 4. hotel / building  (🏨)
# ============================================================
def icon_hotel():
    img, d = new_canvas()
    # main building rect
    d.rounded_rectangle([20, 36, 100, 98], radius=6, fill=C)
    # door arch
    d.ellipse([48, 74, 72, 98], fill=(255, 255, 255, 0))  # cutout via transparent? No - use mask or just overlay white then make transparent... simpler: draw building with hole using polygon
    # Rebuild: draw building as frame + windows, skip complex door cutout
    img2, d2 = new_canvas()
    # building body
    d2.rounded_rectangle([18, 32, 102, 98], radius=5, fill=C)
    # door (lighter/darker - use same color but distinct shape)
    d2.rounded_rectangle([46, 70, 74, 98], radius=8, fill=(255,255,255,0))  # can't do transparency easily
    # Simpler: just a building shape with windows, door is a rectangle at bottom
    img3, d3 = new_canvas()
    d3.rounded_rectangle([20, 30, 100, 98], radius=4, fill=C)
    # roof triangle
    d3.polygon([(12, 32), (60, 6), (108, 32)], fill=C)
    # door
    d3.rounded_rectangle([48, 70, 72, 98], radius=3, fill=(255,255,255,0))
    # Actually let me keep it simple: building with windows, no door cutout needed for a silhouette
    img4, d4 = new_canvas()
    d4.polygon([(14, 34), (60, 8), (106, 34)], fill=C)           # roof
    d4.rounded_rectangle([22, 34, 98, 98], radius=4, fill=C)       # body
    # window grid (2x3)
    for row in range(3):
        for col in range(2):
            wx = 34 + col * 28
            wy = 44 + row * 18
            d4.rectangle([wx, wy, wx+14, wy+10], fill=(255,255,255,80))
    # door
    d4.rounded_rectangle([48, 76, 72, 98], radius=3, fill=(255,255,255,60))
    save(img4, "hotel")


# ============================================================
# 5. people / group  (👥)
# ============================================================
def icon_people():
    img, d = new_canvas()
    # person 1 (left, smaller)
    d.ellipse([22, 28, 46, 52], fill=C)
    d.ellipse([16, 52, 52, 92], fill=C)
    # person 2 (right, larger, front)
    d.ellipse([54, 18, 88, 52], fill=C)
    d.ellipse([44, 48, 96, 98], fill=C)
    save(img, "people")


# ============================================================
# 6. check / tick  (✅)
# ============================================================
def icon_check():
    img, d = new_canvas()
    # circle bg
    d.ellipse([16, 16, 104, 104], fill=C)
    # checkmark (white/transparent cutout effect - just draw thick check on top)
    # Draw checkmark as polygon overlay in background color... simpler: just check shape
    img2, d2 = new_canvas()
    # Just a bold checkmark
    pts = [(26, 62), (48, 82), (94, 34)]
    # thick line via polygon
    w = 14
    d2.polygon([
        (pts[0][0]-w, pts[0][1]), (pts[1][0]-w//2, pts[1][1]+w),
        (pts[1][0]+w//2, pts[1][1]-w), (pts[0][0]+w, pts[0][1]),
        (pts[1][0]-w//2, pts[1][1]+w), (pts[2][0]-w, pts[2][1]),
        (pts[2][0]+w, pts[2][1]+w*2), (pts[1][0]+w*2, pts[1][1]-w),
        (pts[1][0]+w*2, pts[1][1]+w), (pts[2][0], pts[2][1]),
    ], fill=C)
    save(img2, "check")


# ============================================================
# 7. lock  (🔒)
# ============================================================
def icon_lock():
    img, d = new_canvas()
    # shackle (U-shape)
    d.rectangle([38, 18, 82, 28], fill=C)
    d.ellipse([34, 22, 46, 56], fill=C)
    d.ellipse([74, 22, 86, 56], fill=C)
    # body
    d.rounded_rectangle([24, 48, 96, 100], radius=10, fill=C)
    # keyhole
    d.ellipse([54, 64, 66, 76], fill=(255,255,255,0))
    d.rectangle([58, 70, 62, 84], fill=(255,255,255,0))
    # Simplify: solid lock body + shackle
    img2, d2 = new_canvas()
    d2.rounded_rectangle([26, 50, 94, 100], radius=10, fill=C)
    # shackle arc
    d2.arc([36, 20, 84, 68], start=180, end=0, width=10, fill=C)
    # keyhole dot
    d2.ellipse([53, 66, 67, 80], fill=(255,255,255,100))
    d2.rectangle([57, 74, 63, 88], fill=(255,255,255,100))
    save(img2, "lock")


# ============================================================
# 8. door / logout  (🚪)
# ============================================================
def icon_door():
    img, d = new_canvas()
    # door frame
    d.rounded_rectangle([20, 16, 100, 104], radius=4, fill=C)
    # inner panel
    d.rounded_rectangle([32, 28, 88, 92], radius=3, fill=(255,255,255,60))
    # handle
    d.ellipse([78, 58, 90, 70], fill=C)
    # arrow out (subtle)
    d.polygon([(52, 52), (68, 60), (52, 68)], fill=C)
    save(img, "door")


# ============================================================
# 9. pencil / edit  (✏️)
# ============================================================
def icon_pencil():
    img, d = new_canvas()
    # pencil body (rotated ~45deg)
    d.polygon([
        (30, 90), (50, 90),   # flat bottom
        (90, 50), (90, 30),   # tip top-right
        (70, 30), (30, 70),   # eraser top-left
    ], fill=C)
    # tip point
    d.polygon([(85, 35), (95, 25), (95, 45)], fill=C)
    # eraser
    d.rounded_rectangle([24, 66, 38, 94], radius=4, fill=(255,255,255,80))
    save(img, "pencil")


# ============================================================
# 10. book  (📖)
# ============================================================
def icon_book():
    img, d = new_canvas()
    # left page
    d.polygon([(28, 24), (56, 32), (56, 96), (28, 88)], fill=C)
    # right page
    d.polygon([(56, 32), (92, 24), (92, 88), (56, 96)], fill=C)
    # spine
    d.rectangle([54, 30, 58, 98], fill=C)
    # pages lines
    for y in [44, 56, 68, 80]:
        d.line([(34, y), (50, y)], fill=(255,255,255,60), width=2)
        d.line([(62, y), (84, y)], fill=(255,255,255,60), width=2)
    save(img, "book")


# ============================================================
# 11. pool / swim / facilities  (🏊 → wave/water)
# ============================================================
def icon_pool():
    img, d = new_canvas()
    # three wave lines
    def wave(y_base, offset):
        pts = []
        for x in range(14, 110, 16):
            pts.append((x, y_base + (offset if (x//16)%2==0 else -offset)))
        # connect as thick line
        for i in range(len(pts)-1):
            d.line([pts[i], pts[i+1]], fill=C, width=11)
    wave(40, 6)
    wave(60, 8)
    wave(80, 6)
    save(img, "pool")


# ============================================================
# 12. question  (❓)
# ============================================================
def icon_question():
    img, d = new_canvas()
    # circle
    d.ellipse([16, 16, 104, 104], fill=C)
    # question mark (white-ish overlay)
    # Use simple geometry: curved stem + dot
    d.ellipse([50, 78, 70, 94], fill=(255,255,255,120))  # dot
    # hook curve approximated by thick arc + line
    d.arc([36, 28, 84, 76], start=200, end=340, width=12, fill=(255,255,255,120))
    d.rectangle([66, 48, 78, 64], fill=(255,255,255,120))
    save(img, "question")


# ============================================================
# 13. sparkle / star  (✨)
# ============================================================
def icon_sparkle():
    img, d = new_canvas()
    cx, cy = 60, 56
    import math
    pts = []
    for i in range(8):
        angle = math.radians(i * 45 - 90)
        r = 48 if i % 2 == 0 else 20
        pts.append((cx + r * math.cos(angle), cy + r * math.sin(angle)))
    d.polygon(pts, fill=C)
    # center glow circle
    d.ellipse([48, 44, 72, 68], fill=(255,255,255,60))
    save(img, "sparkle")


# ============================================================
# 14. phone  (📱)
# ============================================================
def icon_phone():
    img, d = new_canvas()
    # phone body
    d.rounded_rectangle([34, 14, 86, 106], radius=10, fill=C)
    # screen
    d.rounded_rectangle([40, 24, 80, 82], radius=4, fill=(255,255,255,80))
    # home button
    d.ellipse([54, 90, 66, 102], fill=(255,255,255,80))
    save(img, "phone")


# ============================================================
# 15. calendar  (📅)
# ============================================================
def icon_calendar():
    img, d = new_canvas()
    # body
    d.rounded_rectangle([18, 28, 102, 102], radius=6, fill=C)
    # header bar
    d.rounded_rectangle([18, 28, 102, 48], radius=6, fill=(255,255,255,60))
    # rings
    d.rectangle([36, 14, 44, 32], fill=C)
    d.rectangle([76, 14, 84, 32], fill=C)
    # grid dots
    positions = [(34, 58), (60, 58), (86, 58), (34, 78), (60, 78), (86, 78)]
    for px, py in positions:
        d.ellipse([px-4, py-4, px+4, py+4], fill=(255,255,255,100))
    save(img, "calendar")


# ============================================================
# 16. moon  (🌙)
# ============================================================
def icon_moon():
    img, d = new_canvas()
    # crescent moon: large circle minus smaller offset circle
    d.ellipse([24, 18, 102, 96], fill=C)
    # cut out (draw bg color circle to simulate crescent)
    d.ellipse([50, 18, 112, 96], fill=(255,255,255,0))  # can't erase...
    # Better: draw crescent directly as polygon or use alpha
    img2, d2 = new_canvas()
    from PIL import ImageChops
    big = Image.new("RGBA", (SZ, SZ), (0,0,0,0))
    bd = ImageDraw.Draw(big)
    bd.ellipse([22, 16, 100, 94], fill=C)
    small = Image.new("RGBA", (SZ, SZ), (0,0,0,0))
    sd = ImageDraw.Draw(small)
    sd.ellipse([48, 14, 112, 94], fill=C)
    # XOR: big AND NOT small = crescent
    result = ImageChops.difference(big, small)
    result.putalpha(255)  # opaque where non-zero
    # Clean up: only keep left-crescent area
    final = Image.new("RGBA", (SZ, SZ), (0,0,0,0))
    fd = ImageDraw.Draw(final)
    # Just draw crescent shape manually
    fd.polygon([
        (28, 28), (48, 20), (72, 24), (84, 40), (82, 70),
        (64, 88), (40, 84), (24, 64), (20, 44),
    ], fill=C)
    save(final, "moon")


# ============================================================
# 17. money / coin  (💰)
# ============================================================
def icon_money():
    img, d = new_canvas()
    # coin circle
    d.ellipse([18, 22, 102, 106], fill=C)
    # inner ring
    d.ellipse([30, 34, 90, 94], outline=(255,255,255,80), width=3)
    # ¥ symbol simplified (Y with two horizontal bars)
    d.line([(46, 48), (74, 80)], fill=(255,255,255,120), width=5)
    d.line([(74, 48), (46, 80)], fill=(255,255,255,120), width=5)
    d.line([(42, 54), (78, 54)], fill=(255,255,255,120), width=4)
    d.line([(42, 72), (78, 72)], fill=(255,255,255,120), width=4)
    save(img, "money")


# ============================================================
# 18. pending / hourglass  (⏳)
# ============================================================
def icon_pending():
    img, d = new_canvas()
    # upper bulb
    d.polygon([(34, 16), (86, 16), (66, 58), (54, 58)], fill=C)
    # lower bulb
    d.polygon([(34, 104), (86, 104), (66, 62), (54, 62)], fill=C)
    # frame sides
    d.rectangle([28, 16, 36, 104], fill=C)
    d.rectangle([84, 16, 92, 104], fill=C)
    # sand stream (dots)
    for y in range(58, 64, 3):
        d.ellipse([57, y, 63, y+3], fill=(255,255,255,100))
    save(img, "pending")


# ============================================================
# 19. key / checkin  (🔑)
# ============================================================
def icon_key():
    img, d = new_canvas()
    # bow (circle head)
    d.ellipse([18, 30, 62, 74], fill=C)
    d.ellipse([28, 40, 52, 64], fill=(255,255,255,0))  # hole
    # shaft
    d.rectangle([54, 52, 96, 60], fill=C)
    # teeth
    d.rectangle([80, 60, 88, 72], fill=C)
    d.rectangle([90, 60, 98, 68], fill=C)
    # Simplify with proper hole
    img2, d2 = new_canvas()
    d2.ellipse([16, 28, 64, 76], fill=C)
    d2.ellipse([28, 40, 52, 64], fill=(255,255,255,200))  # keyhole
    d2.rounded_rectangle([50, 50, 102, 62], radius=2, fill=C)
    d2.rectangle([78, 60, 86, 74], fill=C)
    d2.rectangle([88, 60, 96, 70], fill=C)
    save(img2, "key")


# ============================================================
# 20. completed / party  (🎉 → flag/star)
# ============================================================
def icon_completed():
    img, d = new_canvas()
    # flag on pole
    d.rectangle([30, 16, 38, 100], fill=C)
    # flag pennant
    d.polygon([(38, 20), (96, 36), (38, 52)], fill=C)
    # star accent
    import math
    sx, sy = 68, 36
    sp = []
    for i in range(5):
        a1 = math.radians(i * 72 - 90)
        a2 = math.radians(i * 72 - 90 + 36)
        sp.append((sx + 10*math.cos(a1), sy + 10*math.sin(a1)))
        sp.append((sx + 5*math.cos(a2), sy + 5*math.sin(a2)))
    d.polygon(sp, fill=(255,255,255,150))
    save(img, "completed")


# ============================================================
# 21. cancel / close  (❌)
# ============================================================
def icon_cancel():
    img, d = new_canvas()
    # circle
    d.ellipse([16, 16, 104, 104], fill=C)
    # X mark
    w = 12
    d.polygon([(40-w, 40-w), (48+w, 32-w), (80+w, 64+w), (72+w, 56+w),
               (80+w, 40-w), (72+w, 48+w), (40-w, 80+w), (48+w, 72+w)],
              fill=(255,255,255,140))
    # Simpler: just two thick crossing rectangles
    img2, d2 = new_canvas()
    d2.ellipse([16, 16, 104, 104], fill=C)
    t = 14
    d2.polygon([(36-t, 36-t), (48+t, 24-t), (84+t, 60+t), (72+t, 48+t)],
               fill=(255,255,255,130))
    d2.polygon([(84+t, 36-t), (72+t, 48+t), (36-t, 84+t), (48+t, 72+t)],
               fill=(255,255,255,130))
    save(img2, "cancel")


# ============================================================
# 22. document / page  (📄)
# ============================================================
def icon_document():
    img, d = new_canvas()
    # paper body
    d.rounded_rectangle([26, 16, 94, 104], radius=4, fill=C)
    # folded corner
    d.polygon([(66, 16), (94, 44), (66, 44)], fill=(255,255,255,80))
    # lines
    for y in [38, 54, 70, 86]:
        d.rectangle([38, y, 78, y+4], fill=(255,255,255,90))
    save(img, "document")


# ============================================================
# 23. lightbulb / tip  (💡)
# ============================================================
def icon_lightbulb():
    img, d = new_canvas()
    # bulb
    d.ellipse([28, 24, 92, 88], fill=C)
    # base
    d.rectangle([44, 82, 76, 100], fill=C)
    d.rectangle([40, 100, 80, 106], fill=C)
    # filament rays
    ray_len = 12
    for angle in [45, 135, 225, 315]:
        import math
        rad = math.radians(angle)
        x0 = 60 + 38 * math.cos(rad)
        y0 = 56 + 38 * math.sin(rad)
        x1 = 60 + 52 * math.cos(rad)
        y1 = 56 + 52 * math.sin(rad)
        d.line([(x0, y0), (x1, y1)], fill=C, width=6)
    # filament inside
    d.line([(54, 52), (54, 72), (66, 72), (66, 52)], fill=(255,255,255,100), width=3)
    save(img, "lightbulb")


# ============================================================
# 24. clipboard / list  (📋 — used by chat/order empty state)
# ============================================================
def icon_clipboard():
    img, d = new_canvas()
    # clip body
    d.rounded_rectangle([26, 28, 94, 106], radius=6, fill=C)
    # clip top bar
    d.rounded_rectangle([42, 18, 78, 34], radius=4, fill=(255,255,255,0))
    # redraw properly: body first, then top bar overlaps
    img2, d2 = new_canvas()
    d2.rounded_rectangle([26, 30, 94, 106], radius=6, fill=C)
    d2.rounded_rectangle([42, 20, 78, 36], radius=4, fill=(255,255,255,80))
    # lines
    for y in [50, 66, 82]:
        d2.rectangle([40, y, 80, y+4], fill=(255,255,255,90))
    save(img2, "clipboard")


# ============================================================
# 25. target / recommend  (🎯 — already have quick/recommend.png,
#     but also used in recommend hero & room detail qa)
# ============================================================
def icon_target():
    img, d = new_canvas()
    # outer circle
    d.ellipse([14, 14, 106, 106], fill=C)
    # middle circle (white/translucent)
    d.ellipse([30, 30, 90, 90], fill=(255,255,255,80))
    # inner circle
    d.ellipse([46, 46, 74, 74], fill=C)
    # center dot
    d.ellipse([55, 55, 65, 65], fill=(255,255,255,160))
    save(img, "target")


# ============================================================
# 26. robot / ai  (🤖 — already have quick/ai.png, also used in detail qa)
# ============================================================
def icon_robot():
    img, d = new_canvas()
    # head (rounded square)
    d.rounded_rectangle([28, 24, 92, 76], radius=14, fill=C)
    # eyes
    d.ellipse([44, 42, 56, 54], fill=(255,255,255,180))
    d.ellipse([64, 42, 76, 54], fill=(255,255,255,180))
    # antenna
    d.line([(60, 24), (60, 10)], fill=C, width=5)
    d.ellipse([55, 6, 65, 16], fill=C)
    # mouth
    d.rectangle([46, 62, 74, 68], fill=(255,255,255,120))
    save(img, "robot")


# ============================================================
# MAIN — generate all icons
# ============================================================
if __name__ == "__main__":
    icons = [
        ("user", icon_user),
        ("search", icon_search),
        ("area", icon_area),
        ("hotel", icon_hotel),
        ("people", icon_people),
        ("check", icon_check),
        ("lock", icon_lock),
        ("door", icon_door),
        ("pencil", icon_pencil),
        ("book", icon_book),
        ("pool", icon_pool),
        ("question", icon_question),
        ("sparkle", icon_sparkle),
        ("phone", icon_phone),
        ("calendar", icon_calendar),
        ("moon", icon_moon),
        ("money", icon_money),
        ("pending", icon_pending),
        ("key", icon_key),
        ("completed", icon_completed),
        ("cancel", icon_cancel),
        ("document", icon_document),
        ("lightbulb", icon_lightbulb),
        ("clipboard", icon_clipboard),
        ("target", icon_target),
        ("robot", icon_robot),
    ]
    print(f"Generating {len(icons)} icons -> {OUT}")
    for name, fn in icons:
        fn()
    print("Done!")
