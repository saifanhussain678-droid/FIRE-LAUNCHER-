#!/usr/bin/env python3
"""Smoke checks for the FIRE LAUNCHER Real FPS Display box (Fps / Renderer / Ms + Green/Yellow/Red colors + Settings toggle)."""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app_pojavlauncher/src/main/java/net/kdt/pojavlaunch"

formatter = (JAVA / "game/RealFpsBoxFormatter.java").read_text(encoding="utf-8")
sampler = (JAVA / "game/FrameRateSampler.java").read_text(encoding="utf-8")
fps_view = (JAVA / "game/FpsView.java").read_text(encoding="utf-8")
game_activity = (JAVA / "game/GameActivity.java").read_text(encoding="utf-8")
settings_screen = (JAVA / "aerix/ui/screens/SettingsScreen.kt").read_text(encoding="utf-8")
settings_bridge = (JAVA / "aerix/bridge/PojavSettingsBridge.kt").read_text(encoding="utf-8")
strings = (ROOT / "app_pojavlauncher/src/main/res/values/strings.xml").read_text(encoding="utf-8")

problems = []


def check(cond: bool, msg: str) -> None:
    if not cond:
        problems.append(msg)


# 1. Exact 3-line box prefixes requested ("Fps - ", "Renderer - ", "Ms - ")
check('PREFIX_FPS = "Fps - "' in formatter, "RealFpsBoxFormatter must define 'Fps - '")
check('PREFIX_RENDERER = "Renderer - "' in formatter, "RealFpsBoxFormatter must define 'Renderer - '")
check('PREFIX_MS = "Ms - "' in formatter, "RealFpsBoxFormatter must define 'Ms - '")

# 2. Dynamic Green / Yellow / Red color tiers
check("COLOR_GREEN = 0xFF22C55E" in formatter, "Green FPS tier color must be defined")
check("COLOR_YELLOW = 0xFFFACC15" in formatter, "Yellow FPS tier color must be defined")
check("COLOR_RED = 0xFFEF4444" in formatter, "Red FPS tier color must be defined")
check("if (fps >= 60) return COLOR_GREEN;" in formatter, "60+ FPS must map to Green")
check("if (fps >= 30) return COLOR_YELLOW;" in formatter, "30-59 FPS must map to Yellow")
check("return COLOR_RED;" in formatter, "<30 FPS must map to Red")

# 3. Real frame-time ms measurement in FrameRateSampler and FpsView rendering
check("double frameTimeMs()" in sampler, "FrameRateSampler must expose real frameTimeMs()")
check("(elapsed / 1_000_000.0d) / delta" in sampler, "FrameRateSampler must compute ms from elapsed ns / frames")
check("RealFpsBoxFormatter.PREFIX_FPS" in fps_view, "FpsView must render Fps - row")
check("RealFpsBoxFormatter.PREFIX_RENDERER" in fps_view, "FpsView must render Renderer - row")
check("RealFpsBoxFormatter.PREFIX_MS" in fps_view, "FpsView must render Ms - row")
check("RealFpsBoxFormatter.colorForFps(liveFps)" in fps_view, "FpsView must apply Green/Yellow/Red status color")
check("mFpsView.setRendererTag(mGameRenderer.getCurrentRenderer().tag())" in game_activity,
      "GameActivity must pass the active renderer tag to FpsView")

# 4. Settings ON/OFF switch + synced Client overlay preference
check("RealFpsDisplaySettingsCard(" in settings_screen, "SettingsScreen must mount RealFpsDisplaySettingsCard")
check('PojavSettingsBridge.writeBoolean(activity, "show_fps"' in settings_screen,
      "Settings card toggle must write the show_fps preference")
check('if (key == "show_fps")' in settings_bridge and "AerixClientStore.fpsOverlayEnabled = value" in settings_bridge,
      "PojavSettingsBridge must sync AerixClientStore.fpsOverlayEnabled when show_fps is toggled")
check(">Show Real FPS Display<" in strings, "strings.xml must label the toggle Show Real FPS Display")

if problems:
    print("real_fps_box_smoke: FAILED")
    for p in problems:
        print("  -", p)
    sys.exit(1)

print("real_fps_box_smoke: OK (Fps / Renderer / Ms box + Green/Yellow/Red colors + Settings ON/OFF toggle)")
