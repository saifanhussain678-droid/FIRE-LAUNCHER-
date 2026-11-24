package net.kdt.pojavlaunch.game;

import java.util.Locale;

/**
 * Pure formatting and dynamic color state helper for the FIRE LAUNCHER Real FPS Display box:
 *   Fps - <value>
 *   Renderer - <name>
 *   Ms - <frameTimeMs>
 *
 * Kept free of Android framework imports so it can be javac-verified directly in smoke tests.
 */
public final class RealFpsBoxFormatter {
    /** Smooth high frame rate (>= 60 FPS, <= 16.7 ms). */
    public static final int COLOR_GREEN = 0xFF22C55E;
    /** Playable mid frame rate (30 - 59 FPS, 16.8 - 33.3 ms). */
    public static final int COLOR_YELLOW = 0xFFFACC15;
    /** Low / laggy frame rate (1 - 29 FPS, > 33.3 ms). */
    public static final int COLOR_RED = 0xFFEF4444;
    /** Waiting or paused state before frames arrive. */
    public static final int COLOR_IDLE = 0xFF94A3B8;

    public static final int COLOR_LABEL = 0xFFE2E8F0;
    public static final int COLOR_RENDERER = 0xFF38BDF8;
    public static final int COLOR_BOX_BG = 0xD60B1220;

    public static final String PREFIX_FPS = "Fps - ";
    public static final String PREFIX_RENDERER = "Renderer - ";
    public static final String PREFIX_MS = "Ms - ";

    private RealFpsBoxFormatter() {}

    /**
     * Returns dynamic ARGB status color for a real measured FPS reading:
     * Green (>= 60), Yellow (30..59), Red (1..29), or Idle (<= 0).
     */
    public static int colorForFps(int fps) {
        if (fps <= 0) return COLOR_IDLE;
        if (fps >= 60) return COLOR_GREEN;
        if (fps >= 30) return COLOR_YELLOW;
        return COLOR_RED;
    }

    /** Formats the value portion after "Fps - ". */
    public static String formatFpsValue(int fps, String fallbackStatus) {
        if (fps > 0) return Integer.toString(fps);
        if (fallbackStatus != null && !fallbackStatus.trim().isEmpty()) {
            return fallbackStatus.trim();
        }
        return "--";
    }

    /** Formats the value portion after "Ms - ". */
    public static String formatMsValue(int fps, double frameTimeMs) {
        double resolved = frameTimeMs;
        if (resolved <= 0d && fps > 0) {
            resolved = 1000.0d / fps;
        }
        if (resolved <= 0d || fps <= 0) return "--";
        if (resolved >= 999.9d) return "999+ ms";
        return String.format(Locale.US, "%.1f ms", resolved);
    }

    /** Maps internal renderer tags to crisp short HUD labels that fit cleanly in the box. */
    public static String shortRendererLabel(String tagOrName) {
        if (tagOrName == null) return "Auto";
        String raw = tagOrName.trim();
        if (raw.isEmpty()) return "Auto";
        switch (raw) {
            case "opengles_mobileglues":
                return "MobileGlues";
            case "vulkan_zink":
                return "Vulkan Zink";
            case "vulkan_legacyzink":
                return "Legacy Zink";
            case "opengles3_ltw":
                return "LTW (GLES 3)";
            case "opengles2":
            case "opengles2_4":
            case "opengles2_5":
                return "Holy GL4ES";
            case "opengles_nggl4es":
                return "Krypton GL4ES";
            case "freedreno_kgsl":
                return "Freedreno";
            case "mesa_desktop":
            case "mesa_desktop_ext":
                return "Mesa 3D";
            case "opengles_sfpew":
                return "ANGLE";
            case "opengles_proton":
                return "Proton GLES";
            case "emesis":
                return "Emesis";
            default:
                if (raw.startsWith("plugin:")) {
                    String sub = raw.substring("plugin:".length()).trim();
                    return sub.isEmpty() ? "Plugin" : sub;
                }
                return raw.length() > 16 ? raw.substring(0, 16) : raw;
        }
    }
}
