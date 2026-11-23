#!/usr/bin/env python3
"""Builds the official FIRE LAUNCHER website into docs/.

Run from the repository root:
    python3 tools/build_site.py

Generates static HTML pages into docs/ with a luminous Sky-Sapphire Blue
background and Frosted White Glass Boxes covering every UI screen, subsystem,
4GB / 6GB / 8GB / 12GB+ RAM FPS benchmark, system requirement, and APK download.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"


def current_version() -> str:
    for line in (ROOT / "version.properties").read_text().splitlines():
        if line.startswith("versionName="):
            return line.split("=", 1)[1].strip()
    raise SystemExit("version.properties has no versionName")


VERSION = current_version()
RELEASE_DATE = "23 Nov 2026"

REPO_URL = "https://github.com/saifanhussain678-droid/FIRE-LAUNCHER-"
RELEASES_LATEST_URL = f"{REPO_URL}/releases/latest"
DOWNLOAD_PRIMARY_URL = f"{REPO_URL}/releases/latest/download/FIRE-Launcher.apk"
DOWNLOAD_ARM64_URL = f"{REPO_URL}/releases/latest/download/FIRE-Launcher-arm64-v8a.apk"
DOWNLOAD_NORUNTIME_URL = f"{REPO_URL}/releases/latest/download/FIRE-Launcher-noruntime.apk"
SITE_URL = "https://fire-launcher.vercel.app/"
DISCORD_URL = "https://discord.gg/RS7q9KaCm6"

NAV = [
    ("index.html", "Home"),
    ("screenshots.html", "All UIs"),
    ("features.html", "All Systems"),
    ("renderers.html", "RAM & FPS"),
    ("install.html", "Download & Install"),
    ("faq.html", "FAQ"),
]


def head(title: str, description: str, page: str) -> str:
    canonical = SITE_URL + ("" if page == "index.html" else page)
    og_img = SITE_URL + "assets/screenshots/fire-launcher-prism-light-ui.png"
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>{title}</title>
<meta name="description" content="{description}">
<meta name="theme-color" content="#2563EB">
<meta name="color-scheme" content="light">
<link rel="icon" href="assets/aerix-logo.png">
<link rel="apple-touch-icon" href="assets/aerix-logo.png">
<link rel="canonical" href="{canonical}">
<meta property="og:type" content="website">
<meta property="og:site_name" content="FIRE LAUNCHER">
<meta property="og:title" content="{title}">
<meta property="og:description" content="{description}">
<meta property="og:url" content="{canonical}">
<meta property="og:image" content="{og_img}">
<meta property="og:image:alt" content="FIRE LAUNCHER Prism Light UI">
<meta name="twitter:card" content="summary_large_image">
<link rel="stylesheet" href="site.css">
</head>
<body>
<div class="bg" aria-hidden="true">
  <span class="bg-orb o1"></span>
  <span class="bg-orb o2"></span>
  <span class="bg-orb o3"></span>
  <span class="bg-grid"></span>
</div>
<a class="skip" href="#main">Skip to content</a>
"""


def nav(page: str) -> str:
    links = []
    for href, label in NAV:
        cur = ' aria-current="page"' if href == page else ""
        links.append(f'      <a href="{href}"{cur}>{label}</a>')
    joined = "\n".join(links)
    return f"""
<header class="nav">
  <div class="wrap nav-inner">
    <a class="brand" href="index.html">
      <img src="assets/branding/fire-mark.png" alt="FIRE LAUNCHER logo" width="36" height="36">
      <span>FIRE LAUNCHER<small>MINECRAFT JAVA · ANDROID</small></span>
    </a>
    <nav class="nav-links" id="navLinks" aria-label="Primary">
{joined}
    </nav>
    <a class="nav-cta" href="index.html#download">⬇ Download APK</a>
    <button class="nav-toggle" id="navToggle" type="button" aria-expanded="false" aria-controls="navLinks" aria-label="Menu">☰</button>
  </div>
</header>
"""


FOOTER = f"""
<footer class="footer">
  <div class="wrap">
    <div class="glass-box footer-box">
      <div class="footer-grid">
        <div>
          <a class="brand" href="index.html">
            <img src="assets/branding/fire-mark.png" alt="FIRE LAUNCHER" width="36" height="36">
            <span>FIRE LAUNCHER<small>VERSION {VERSION}</small></span>
          </a>
          <p style="margin-top:10px;font-size:13.5px;color:#2C4870;max-width:36ch">
            Next-generation Minecraft: Java Edition launcher for Android — featuring the inverted
            3-Wing Bento UI, Top Downloading Engine, 12 Renderers, OpenJDK 8/17/21, and built-in Modrinth &amp; CurseForge Armory.
          </p>
        </div>
        <div>
          <h4>Explore UI &amp; Engine</h4>
          <ul>
            <li><a href="screenshots.html">All 8 Launcher UIs</a></li>
            <li><a href="features.html">All 12 Subsystems</a></li>
            <li><a href="renderers.html">4GB / 6GB / 8GB FPS Guide</a></li>
            <li><a href="renderers.html#calculator">Interactive FPS Calculator</a></li>
          </ul>
        </div>
        <div>
          <h4>Get Started</h4>
          <ul>
            <li><a href="install.html">Download APK (v{VERSION})</a></li>
            <li><a href="install.html#steps">4-Step Setup Guide</a></li>
            <li><a href="install.html#requirements">System Requirements</a></li>
            <li><a href="faq.html">Troubleshooting &amp; FAQ</a></li>
          </ul>
        </div>
        <div>
          <h4>Project &amp; Community</h4>
          <ul>
            <li><a href="{RELEASES_LATEST_URL}" target="_blank" rel="noopener">GitHub Releases</a></li>
            <li><a href="{REPO_URL}" target="_blank" rel="noopener">Source Code (GPL-3.0)</a></li>
            <li><a href="{DISCORD_URL}" target="_blank" rel="noopener">Discord Community</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 FIRE LAUNCHER · Licensed under GNU GPL v3.0</span>
        <span>Not an official Minecraft product · Not approved by or associated with Mojang or Microsoft.</span>
      </div>
    </div>
  </div>
</footer>
<script src="site.js"></script>
</body>
</html>
"""


def ui_explorer_section() -> str:
    return """
  <section class="section" id="all-ui">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">✦ Complete Launcher UI Tour</span>
        <h2>Explore Every Screen of <span class="gradient-text">FIRE LAUNCHER</span></h2>
        <p class="lede">
          Every button, dock, downloading bar, and studio panel has been redesigned from scratch.
          Click any tab below to inspect all 8 live launcher screens right inside this white-glass simulator.
        </p>
      </div>

      <div class="glass-box ui-explorer-shell">
        <div class="ui-tabs" role="tablist" aria-label="Launcher UI Screens">
          <button class="ui-tab-btn active" type="button" data-ui-tab="hq">🏠 1. HQ 3-Wing Deck</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="downloader">⬇ 2. Top Downloading Bar</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="vault">📦 3. Instance Vault &amp; +Forge</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="armory">🧩 4. Mod Armory &amp; Packs</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="client">⚡ 5. Fire Client &amp; Recorder</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="controls">🎮 6. Custom Controls Rig</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="renderers">🔬 7. 12 Renderers &amp; Drivers</button>
          <button class="ui-tab-btn" type="button" data-ui-tab="accounts">👤 8. Accounts, Skins &amp; Files</button>
        </div>

        <div class="sim-viewport">
          <!-- Relocated Top Downloading Bar -->
          <div class="sim-dl-bar">
            <button class="sim-mini-btn" id="simDlPause" type="button">⏸ Pause</button>
            <div style="flex:1;min-width:210px">
              <div style="display:flex;justify-content:space-between;font-size:11.5px;font-weight:900">
                <span><b style="color:#4F46E5">FIRE DOWNLOADER ·</b> Minecraft 1.21.4 + Sodium Crystal</span>
                <span style="color:#0284C7">24.8 MB/s · ETA 00:08</span>
              </div>
              <div class="sim-progress-track">
                <div class="sim-progress-fill" id="simDlFill" style="width:68%"></div>
              </div>
            </div>
            <span id="simDlPct" style="font-weight:900;font-size:14px;color:#DB2777">68%</span>
          </div>

          <!-- Top Command Header -->
          <div class="sim-top-bar">
            <div style="display:flex;align-items:center;gap:8px">
              <img src="assets/branding/fire-mark.png" alt="" width="24" height="24">
              <strong style="font-size:12.5px;letter-spacing:0.6px">FIRE LAUNCHER</strong>
              <span class="tag">ONLINE ID</span>
            </div>
            <div style="display:flex;gap:6px;flex-wrap:wrap">
              <span class="sim-mini-btn">⚙ CONFIG</span>
              <span class="sim-mini-btn">🛠 OPS</span>
              <span class="sim-mini-btn">📁 FILES</span>
              <span class="sim-mini-btn">▤ CONSOLE</span>
            </div>
            <div style="display:flex;align-items:center;gap:8px">
              <span style="font-size:11.5px;font-weight:800">Prism PvP · Fabric 1.21.4</span>
              <a href="#download" class="btn-primary" style="padding:6px 14px;font-size:11.5px">▶ IGNITE</a>
            </div>
          </div>

          <!-- Dynamic Screen Stage -->
          <div id="fireUiStage"></div>

          <!-- Bottom Floating Dock -->
          <div class="sim-bottom-dock" style="justify-content:center;gap:8px">
            <span class="tag">🔥 FIRE LAUNCHER</span>
            <span class="sim-mini-btn">⌂ HQ</span>
            <span class="sim-mini-btn">❖ VAULT</span>
            <span class="sim-mini-btn">＋ FORGE</span>
            <span class="sim-mini-btn">✦ ARMORY</span>
            <span class="sim-mini-btn">▣ PACKS</span>
            <span class="sim-mini-btn">⚡ REALMS</span>
            <span class="sim-mini-btn">◆ CLIENT</span>
            <span class="sim-mini-btn">◉ CREW</span>
          </div>
        </div>
      </div>
    </div>
  </section>
"""


def ram_fps_section() -> str:
    return """
  <section class="section" id="fps">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">⚡ Real Device Performance Guide</span>
        <h2>4GB, 6GB &amp; 8GB+ RAM <span class="gradient-text">FPS Benchmarks</span></h2>
        <p class="lede">
          Exact expected FPS across 4GB, 6GB, and 8GB+ Android phones — plus optimal Heap RAM allocation,
          render distance, and renderer selection so your game runs butter-smooth without lag or crashes.
        </p>
      </div>

      <div class="ram-grid">
        <!-- 4GB RAM CARD -->
        <article class="card ram-card">
          <div class="ram-badge">
            <span>📱 4 GB RAM PHONES</span>
            <span>Allocate: 1.5 – 2.0 GB</span>
          </div>
          <h3>Smooth Entry &amp; Competitive PvP</h3>
          <p>Ideal for Redmi, Realme, Samsung A-Series &amp; Helio/Snapdragon 6-series devices using <b>MobileGlues (Fast)</b> or <b>Holy GL4ES</b> at 6–8 chunks.</p>

          <div class="fps-row">
            <div class="fps-row-top"><span>⚔️ 1.8.9 / 1.20 Competitive PvP</span><span class="fps-val">120 – 180 FPS</span></div>
            <div class="fps-bar"><span style="width:68%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>⚡ 1.21.4 + Sodium + Lithium</span><span class="fps-val">90 – 135 FPS</span></div>
            <div class="fps-bar"><span style="width:55%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>🧱 Vanilla 1.21.4 (No Mods)</span><span class="fps-val">55 – 85 FPS</span></div>
            <div class="fps-bar"><span style="width:38%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>🌤 Lite Shaders / 35–50 Mods</span><span class="fps-val">35 – 55 FPS</span></div>
            <div class="fps-bar"><span style="width:26%"></span></div>
          </div>

          <div class="ram-tips">
            <b>Best 4GB Settings:</b> Set RAM slider to <code>1792 MB</code>, Render Distance <code>6–8 chunks</code>, install <b>Sodium + FerriteCore</b> (cuts RAM usage by 45%), and close background apps.
          </div>
        </article>

        <!-- 6GB RAM CARD -->
        <article class="card ram-card featured">
          <div class="ram-badge">
            <span>💎 6 GB RAM PHONES (SWEET SPOT)</span>
            <span>Allocate: 2.8 – 3.3 GB</span>
          </div>
          <h3>High-FPS Modded &amp; Mid Shaders</h3>
          <p>Perfect balance for Snapdragon 7-series, Dimensity 7000/8000 &amp; mid-range gaming phones with 90Hz/120Hz displays.</p>

          <div class="fps-row">
            <div class="fps-row-top"><span>⚔️ 1.8.9 / 1.21 Crystal PvP</span><span class="fps-val">180 – 260 FPS</span></div>
            <div class="fps-bar"><span style="width:82%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>⚡ 1.21.4 + Sodium + Lithium</span><span class="fps-val">140 – 210 FPS</span></div>
            <div class="fps-bar"><span style="width:72%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>🧱 Vanilla 1.21.4 (10 Chunks)</span><span class="fps-val">85 – 125 FPS</span></div>
            <div class="fps-bar"><span style="width:52%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>🌈 Medium Shaders / 100 Mods</span><span class="fps-val">60 – 90 FPS</span></div>
            <div class="fps-bar"><span style="width:44%"></span></div>
          </div>

          <div class="ram-tips">
            <b>Best 6GB Settings:</b> Set RAM slider to <code>3072 MB</code>, Render Distance <code>10–12 chunks</code>, Renderer <b>MobileGlues (OpenGL 4.x)</b>, and enable <b>Max Refresh Rate (90/120Hz)</b>.
          </div>
        </article>

        <!-- 8GB / 12GB+ RAM CARD -->
        <article class="card ram-card">
          <div class="ram-badge">
            <span>🔥 8 GB &amp; 12 GB+ RAM PHONES</span>
            <span>Allocate: 4.0 – 6.0 GB</span>
          </div>
          <h3>Ultra 120Hz, Heavy Modpacks &amp; Shaders</h3>
          <p>Unlocks desktop-class Java performance on Snapdragon 8 Gen 1/2/3/Elite &amp; Dimensity 9000+ flagships with custom Turnip Vulkan drivers.</p>

          <div class="fps-row">
            <div class="fps-row-top"><span>⚔️ Competitive PvP (120Hz/144Hz)</span><span class="fps-val">300 – 450+ FPS</span></div>
            <div class="fps-bar"><span style="width:98%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>⚡ 1.21.4 + Sodium Vulkan Boost</span><span class="fps-val">220 – 360+ FPS</span></div>
            <div class="fps-bar"><span style="width:90%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>🧱 Vanilla 1.21.4 (16 Chunks)</span><span class="fps-val">125 – 180 FPS</span></div>
            <div class="fps-bar"><span style="width:66%"></span></div>
          </div>
          <div class="fps-row">
            <div class="fps-row-top"><span>🌟 Complementary Shaders / 200+ Mods</span><span class="fps-val">80 – 130 FPS</span></div>
            <div class="fps-bar"><span style="width:56%"></span></div>
          </div>

          <div class="ram-tips">
            <b>Best 8GB+ Settings:</b> Set RAM slider to <code>4352 MB</code> (or <code>6144 MB</code> on 12GB+), import latest <b>Turnip Vulkan Driver (.zip)</b>, Render Distance <code>14–18 chunks</code>.
          </div>
        </article>
      </div>

      <!-- INTERACTIVE LIVE FPS & JVM CALCULATOR -->
      <div class="glass-box calc-box" id="calculator">
        <div class="calc-controls">
          <div>
            <span class="kicker">🧮 Interactive Tuning Lab</span>
            <h3>Check Your Exact Device FPS &amp; Best Settings</h3>
            <p style="font-size:13.5px;color:#2C4870">Select your phone's RAM, processor tier, and game mode to calculate live FPS + copy optimal JVM flags:</p>
          </div>

          <div class="calc-group">
            <label>1. Your Phone RAM</label>
            <div class="pill-options">
              <button class="pill-opt" type="button" data-calc-group="ram" data-calc-val="4">4 GB RAM</button>
              <button class="pill-opt active" type="button" data-calc-group="ram" data-calc-val="6">6 GB RAM</button>
              <button class="pill-opt" type="button" data-calc-group="ram" data-calc-val="8">8 GB RAM</button>
              <button class="pill-opt" type="button" data-calc-group="ram" data-calc-val="12">12 GB / 16 GB RAM</button>
            </div>
          </div>

          <div class="calc-group">
            <label>2. Processor / Chipset Tier</label>
            <div class="pill-options">
              <button class="pill-opt" type="button" data-calc-group="soc" data-calc-val="budget">Budget (Helio G85–G99 / SD 680–695)</button>
              <button class="pill-opt active" type="button" data-calc-group="soc" data-calc-val="mid">Mid-Range (SD 7+ Gen 2 / Dimensity 8200)</button>
              <button class="pill-opt" type="button" data-calc-group="soc" data-calc-val="flagship">Flagship (SD 8 Gen 2 / Gen 3 / 8 Elite)</button>
            </div>
          </div>

          <div class="calc-group">
            <label>3. Minecraft Mode</label>
            <div class="pill-options">
              <button class="pill-opt active" type="button" data-calc-group="mode" data-calc-val="sodium">1.21.4 + Sodium</button>
              <button class="pill-opt" type="button" data-calc-group="mode" data-calc-val="pvp">1.8.9 / 1.21 PvP</button>
              <button class="pill-opt" type="button" data-calc-group="mode" data-calc-val="vanilla">Vanilla 1.21.4</button>
              <button class="pill-opt" type="button" data-calc-group="mode" data-calc-val="modpack">100+ Mods Pack</button>
              <button class="pill-opt" type="button" data-calc-group="mode" data-calc-val="shaders">Iris Shaders</button>
            </div>
          </div>
        </div>

        <div class="calc-result">
          <span class="tag" style="margin:0 auto">ESTIMATED PERFORMANCE</span>
          <div class="calc-big-fps" id="calcFpsOut">155 – 189 FPS</div>
          <div style="font-size:12.5px;font-weight:800;color:#047857" id="calcLowOut">1% Low: ~112 FPS · Smooth Frame Pacing</div>
          <div style="text-align:left;display:grid;gap:6px;font-size:12.5px;margin-top:6px">
            <div><b>Recommended Heap RAM:</b> <span id="calcHeapOut">3072 MB (3.0 GB)</span></div>
            <div><b>Render Distance:</b> <span id="calcChunksOut">10 – 12 Chunks</span></div>
            <div><b>Best Renderer:</b> <span id="calcRendererOut">MobileGlues (OpenGL 4.x / Vulkan)</span></div>
          </div>
          <div class="jvm-code" id="calcJvmOut">-Xms3072M -Xmx3072M -XX:+UseG1GC</div>
          <button class="btn btn-primary" id="copyJvmBtn" type="button" style="width:100%">📋 Copy Best JVM Flags</button>
        </div>
      </div>
    </div>
  </section>
"""


def all_systems_section() -> str:
    systems = [
        ("🔬", "12 Graphics Renderers", "Switch seamlessly between MobileGlues (OpenGL 4.6 over Vulkan), Vulkan Zink, Freedreno Turnip, Holy GL4ES, VirGL, and ANGLE globally or per instance.", ["MobileGlues", "Vulkan 1.3", "GL4ES"]),
        ("🧩", "Custom GPU Driver Loader", "Snapdragon / Adreno devices can import custom Mesa Turnip .zip Vulkan drivers directly inside Settings → Video for massive shader & FPS gains.", ["Turnip .ZIP", "Adreno", "Zero Root"]),
        ("☕", "Auto OpenJDK 8 / 17 / 21", "Automatically selects Java 8 for 1.7–1.16.5, Java 17 for 1.17–1.20.4, and Java 21 ARM64 for Minecraft 1.20.5–1.21.4+ with custom runtime import.", ["Java 8", "Java 17", "Java 21"]),
        ("⬇️", "Top Prism Downloading Bar", "Relocated top multi-threaded download dock with Pause/Cancel, expandable sub-task list, real-time MB/s speed, ETA, and SHA-1 integrity checks.", ["Multi-Thread", "Pause/Resume", "SHA-1"]),
        ("🛠️", "Every Mod Loader Built-In", "1-click automatic installation for Vanilla, Fabric, Quilt, Forge, NeoForge, OptiFine, Legacy Fabric, Better Than Adventure, plus .jar execution.", ["Fabric", "Forge", "NeoForge", "Quilt"]),
        ("📦", "Modrinth & CurseForge Armory", "Browse and install Mods, Full Modpacks (.mrpack & CurseForge), Shaders, Resource Packs, and Worlds with automatic dependency resolution.", ["Modrinth", "CurseForge", "Auto-Deps"]),
        ("⚡", "Built-In Fire Client HUD", "Zero-setup competitive HUD overlays: FPS Pill, CPS Counter, RGB Keystrokes, ToggleSprint, Zoom, Armor Status, Crosshair Editor & Fullbright.", ["PvP HUD", "Keystrokes", "CPS"]),
        ("🎥", "1080p60 Screen + Audio Recorder", "Record smooth gameplay with internal game audio directly from the floating in-game FIRE LAUNCHER crystal flame menu — no extra recorder app needed.", ["60 FPS MP4", "Internal Audio"]),
        ("📱", "120Hz / 144Hz Display Unlocker", "Requests native high-refresh display modes on 90Hz, 120Hz, and 144Hz gaming phones with frame-pacing tuning and resolution scaler (25%–100%).", ["120Hz", "144Hz", "Resolution Scale"]),
        ("🎮", "Controls Rig, Gyro & Gamepad", "Full touch layout studio with 11 animated button themes, custom macros, Gyroscope aiming, Xbox/PS5 Bluetooth gamepads, and OTG Keyboard/Mouse capture.", ["Touch Studio", "Gyro", "Mouse/KB"]),
        ("👤", "Accounts & 3D Skin Wardrobe", "Sign in with official Microsoft Xbox Live OAuth, Ely.by skin servers, or unlimited Offline accounts — complete with 3D rotatable Skin & Cape preview.", ["Microsoft", "Ely.by", "Offline"]),
        ("📁", "File Manager, Code Editor & Logs", "Built-in .minecraft file browser with ZIP/Unzip, multi-select, syntax-friendly config editor, live crash log analyzer, Crew invites & Discord Presence.", ["File Manager", "Log Viewer", "Discord RPC"]),
    ]
    cards = []
    for icon, title, desc, tags in systems:
        t_html = "".join(f'<span class="tag">{t}</span>' for t in tags)
        cards.append(f"""        <article class="card sys-card">
          <div class="sys-icon">{icon}</div>
          <h3>{title}</h3>
          <p>{desc}</p>
          <div class="tag-row">{t_html}</div>
        </article>""")
    joined = "\n".join(cards)
    return f"""
  <section class="section" id="systems">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">🛠 Complete System Architecture</span>
        <h2>Every Engine &amp; Subsystem Inside <span class="gradient-text">FIRE LAUNCHER</span></h2>
        <p class="lede">
          Everything you need to run modern Minecraft: Java Edition on Android is built straight into one unified glass launcher — zero external helper apps required.
        </p>
      </div>
      <div class="grid grid-3">
{joined}
      </div>
    </div>
  </section>
"""


def requirements_and_download_section() -> str:
    return f"""
  <section class="section" id="requirements">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">📋 Hardware &amp; OS Compatibility</span>
        <h2>System Requirements</h2>
        <p class="lede">Runs across budget 4GB phones, mid-range 6GB phones, and 8GB–16GB flagship Android devices &amp; tablets.</p>
      </div>

      <div class="glass-box table-wrap">
        <table class="spec-table">
          <thead>
            <tr>
              <th>Component</th>
              <th>Minimum (Playable)</th>
              <th>Recommended (High FPS)</th>
              <th>Ultra 120Hz + Shaders</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><b>Android Version</b></td>
              <td>Android 8.0 (Oreo, API 26)+</td>
              <td>Android 11 – Android 15</td>
              <td>Android 13 – Android 15</td>
            </tr>
            <tr>
              <td><b>Device RAM</b></td>
              <td><b>4 GB RAM</b> (1.5–2.0 GB Heap)</td>
              <td><b>6 GB RAM</b> (2.8–3.3 GB Heap)</td>
              <td><b>8 GB – 16 GB RAM</b> (4.2–6.0 GB Heap)</td>
            </tr>
            <tr>
              <td><b>CPU Architecture</b></td>
              <td>64-bit <code>arm64-v8a</code> / <code>armeabi-v7a</code></td>
              <td>Octa-core <code>arm64-v8a</code> (2.2 GHz+)</td>
              <td>Snapdragon 8 Gen 2/3/Elite or Dimensity 9200+</td>
            </tr>
            <tr>
              <td><b>GPU &amp; Graphics API</b></td>
              <td>OpenGL ES 3.1+</td>
              <td>OpenGL ES 3.2 + Vulkan 1.1+</td>
              <td>Vulkan 1.3 + Adreno Turnip / MobileGlues</td>
            </tr>
            <tr>
              <td><b>Free Storage</b></td>
              <td>2.5 GB free space</td>
              <td>6 GB+ free (for mods &amp; worlds)</td>
              <td>12 GB+ UFS 3.1 / UFS 4.0 storage</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>

  <section class="section" id="download">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">⬇ Official Release Downloads · v{VERSION}</span>
        <h2>Download <span class="gradient-text">FIRE LAUNCHER</span></h2>
        <p class="lede">
          Free, open-source (GPL-3.0), zero ads, zero telemetry. Pick the APK build that matches your Android device below.
        </p>
      </div>

      <div class="grid grid-3">
        <article class="card dl-card primary-dl">
          <div class="ram-badge"><span>⭐ RECOMMENDED FOR ALL PHONES</span><span>v{VERSION}</span></div>
          <h3>FIRE LAUNCHER — Full Universal APK</h3>
          <p>Includes bundled OpenJDK runtime, all 12 renderers (MobileGlues, Vulkan Zink, GL4ES), Fire Client HUD, and Modrinth/CurseForge Armory ready out of the box.</p>
          <div class="tag-row">
            <span class="tag">Android 8.0+</span>
            <span class="tag">ARM64 + ARMv7 + x86_64</span>
            <span class="tag">Bundled Java</span>
          </div>
          <a class="btn btn-primary mt-20" data-primary-download href="{DOWNLOAD_PRIMARY_URL}">
            ⬇ Download FIRE-Launcher.apk (v{VERSION})
          </a>
        </article>

        <article class="card dl-card">
          <div class="ram-badge"><span>⚡ 64-BIT OPTIMIZED</span><span>arm64-v8a</span></div>
          <h3>FIRE LAUNCHER — ARM64 Edition</h3>
          <p>Smaller APK stripped of legacy 32-bit libraries — built specifically for modern 4GB, 6GB, 8GB, and 12GB+ 64-bit Android smartphones.</p>
          <div class="tag-row">
            <span class="tag">64-bit Phones</span>
            <span class="tag">Fast Install</span>
            <span class="tag">Full Renderers</span>
          </div>
          <a class="btn btn-ghost mt-20" href="{DOWNLOAD_ARM64_URL}">
            ⬇ Download ARM64 APK
          </a>
        </article>

        <article class="card dl-card">
          <div class="ram-badge"><span>🪶 ULTRA-LIGHT DOWNLOAD</span><span>No-Runtime</span></div>
          <h3>FIRE LAUNCHER — Lightweight APK</h3>
          <p>Smallest initial APK size. Downloads the exact OpenJDK 8 / 17 / 21 runtime automatically through the Top Downloading Bar on first launch.</p>
          <div class="tag-row">
            <span class="tag">Minimal APK</span>
            <span class="tag">Auto Runtime Fetch</span>
          </div>
          <a class="btn btn-ghost mt-20" href="{DOWNLOAD_NORUNTIME_URL}">
            ⬇ Download Lightweight APK
          </a>
        </article>
      </div>

      <div class="glass-box center mt-20" style="padding:18px 24px;display:flex;flex-wrap:wrap;justify-content:space-between;align-items:center;gap:12px">
        <span style="font-size:13.5px;font-weight:800;color:#0F2544">
          Looking for release notes, SHA-256 checksums, or older versions?
        </span>
        <div class="btn-row">
          <a class="btn btn-ghost" href="{RELEASES_LATEST_URL}" target="_blank" rel="noopener">All GitHub Releases ↗</a>
          <a class="btn btn-ghost" href="install.html">Step-by-Step Install Guide →</a>
        </div>
      </div>
    </div>
  </section>
"""


def page_header(kicker: str, title: str, lede: str) -> str:
    return f"""
  <section class="page-hero">
    <div class="wrap">
      <div class="glass-box page-hero-box">
        <span class="kicker">{kicker}</span>
        <h1>{title}</h1>
        <p class="lede" style="margin-bottom:0">{lede}</p>
      </div>
    </div>
  </section>
"""


def build_index() -> str:
    return (
        head(
            f"FIRE LAUNCHER v{VERSION} — Official Website · All UIs, 4GB/6GB/8GB FPS & APK Download",
            "Official FIRE LAUNCHER website for Android: explore all 8 launcher UIs, 12 graphics renderers, 4GB / 6GB / 8GB RAM FPS benchmarks, system architecture, and direct APK downloads.",
            "index.html",
        )
        + nav("index.html")
        + f"""
<main id="main">
  <section class="hero">
    <div class="wrap">
      <div class="hero-grid">
        <div class="glass-box hero-glass">
          <div>
            <div class="hero-crest-row">
              <img class="hero-crest" src="assets/branding/fire-mark.png" alt="FIRE LAUNCHER emblem">
              <div>
                <span class="kicker" style="margin-bottom:4px">🔥 Official Android Release · v{VERSION}</span>
                <div style="font-size:12px;font-weight:800;color:#527199">Updated {RELEASE_DATE} · Open-Source GPL-3.0</div>
              </div>
            </div>

            <h1 style="margin-top:16px">
              <span class="gradient-text">FIRE LAUNCHER</span><br>
              Java Edition Reimagined in Blue &amp; Glass.
            </h1>
            <p class="lede">
              Play full PC <b>Minecraft: Java Edition (1.0 – 1.21.4+)</b> on Android with an inverted 3-Wing Glass UI,
              Top Multi-Thread Downloading Bar, 12 Graphics Renderers, built-in Modrinth &amp; CurseForge Armory, and blazing 120Hz+ FPS on 4GB, 6GB &amp; 8GB+ phones.
            </p>

            <div class="stat-pills">
              <span class="stat-pill">⚡ 90–180 FPS on 4GB RAM</span>
              <span class="stat-pill">💎 140–260 FPS on 6GB RAM</span>
              <span class="stat-pill">🔥 220–450+ FPS on 8GB+ RAM</span>
              <span class="stat-pill">🔬 12 Renderers + Turnip Vulkan</span>
              <span class="stat-pill">☕ OpenJDK 8 / 17 / 21</span>
            </div>
          </div>

          <div class="btn-row">
            <a class="btn btn-primary" data-primary-download href="{DOWNLOAD_PRIMARY_URL}">⬇ Download FIRE LAUNCHER APK</a>
            <a class="btn btn-ghost" href="#all-ui">🖥 View All Launcher UIs</a>
            <a class="btn btn-ghost" href="#fps">📊 4GB / 6GB / 8GB FPS</a>
          </div>
        </div>

        <div class="glass-box hero-showcase">
          <div class="showcase-frame">
            <img src="assets/screenshots/fire-launcher-prism-light-ui.png" alt="FIRE LAUNCHER Prism Light 3-Wing Bento UI">
          </div>
          <div class="showcase-bar">
            <span>✦ <b>FIRE LAUNCHER UI:</b> Top Command Header · 3-Wing Bento Deck · Bottom Floating Dock</span>
            <a href="screenshots.html">Full UI Gallery →</a>
          </div>
        </div>
      </div>

      <div class="metrics-strip">
        <div class="glass-box metric-box">
          <div class="metric-num">12</div>
          <div class="metric-label">Graphics Renderers (MobileGlues, Vulkan Zink, Turnip, GL4ES)</div>
        </div>
        <div class="glass-box metric-box">
          <div class="metric-num">8</div>
          <div class="metric-label">Mod Loaders (Fabric, Forge, NeoForge, Quilt, OptiFine, BTA)</div>
        </div>
        <div class="glass-box metric-box">
          <div class="metric-num">450+</div>
          <div class="metric-label">Peak Competitive FPS on 120Hz / 144Hz Android Displays</div>
        </div>
        <div class="glass-box metric-box">
          <div class="metric-num">100%</div>
          <div class="metric-label">Free &amp; Private — Zero Ads, Zero Tracking, Direct Modrinth/CF</div>
        </div>
      </div>
    </div>
  </section>
"""
        + ui_explorer_section()
        + ram_fps_section()
        + all_systems_section()
        + requirements_and_download_section()
        + "\n</main>"
        + FOOTER
    )


def build_screenshots() -> str:
    shots = [
        ("assets/screenshots/fire-launcher-prism-light-ui.png", "1. HQ 3-Wing Bento Dashboard", "Top Command Header (Account, Config/Ops/Files/Console, Active Instance + IGNITE), Left 3D Skin Wardrobe, Center 2×2 Tactical Bento Cards, Right Active Instance & Rack, and Bottom Floating Dock."),
        ("assets/branding/fire-launcher-logo.png", "2. Official FIRE LAUNCHER Emblem", "3D Crystalline Prismatic Flame cradling a faceted Minecraft prism cube — used across Adaptive Icons, Top Header, Bottom Dock, Splash Screen & In-Game Floating Button."),
        ("assets/screenshots/shot-instances.jpg", "3. Instance Vault & +Forge Builder", "Create isolated instances for any Minecraft version (1.0 – 1.21.4+) with 1-click Fabric, Quilt, Forge, NeoForge, OptiFine, or Legacy Fabric installation."),
        ("assets/screenshots/shot-discover.jpg", "4. Mod Armory (Modrinth & CurseForge)", "Search and install Mods, Shaders, Resource Packs, Worlds, and complete Modpacks directly inside the launcher with background queue progress."),
        ("assets/screenshots/shot-renderer.jpg", "5. 12 Renderers & GPU Driver Studio", "Pick MobileGlues, Vulkan Zink, VirGL, or Holy GL4ES per instance and import custom Adreno Turnip .zip Vulkan drivers."),
        ("assets/screenshots/shot-accounts.jpg", "6. Accounts & 3D Skin/Cape Wardrobe", "Manage Microsoft Xbox Live OAuth, Ely.by, and Offline accounts with live 3D character rotation and cape toggles."),
    ]
    cards = []
    for src, title, desc in shots:
        cards.append(f"""        <article class="card" style="padding:16px 0;display:flex;flex-direction:column;gap:12px">
          <div class="showcase-frame" style="aspect-ratio:16/10;display:grid;place-items:center">
            <img src="{src}" alt="{title}" style="width:100%;height:100%;object-fit:cover">
          </div>
          <div style="padding:4px 2px">
            <h3>{title}</h3>
            <p>{desc}</p>
          </div>
        </article>""")
    joined = "\n".join(cards)
    return (
        head(
            "All Launcher UIs & Interactive Tour — FIRE LAUNCHER",
            "Inspect every UI screen of FIRE LAUNCHER: HQ 3-Wing Bento Deck, Top Downloading Bar, Instance Vault, Mod Armory, Fire Client HUD, Controls Rig, and Settings.",
            "screenshots.html",
        )
        + nav("screenshots.html")
        + page_header(
            "🖥 Complete UI Showcase",
            "Every Screen of FIRE LAUNCHER",
            "Test all 8 interactive UI modules live below, or browse the visual showcase cards underneath.",
        )
        + "<main id=\"main\">"
        + ui_explorer_section()
        + f"""
  <section class="section">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">📸 Visual Breakdown</span>
        <h2>Key Interface Modules</h2>
      </div>
      <div class="grid grid-2">
{joined}
      </div>
    </div>
  </section>
</main>"""
        + FOOTER
    )


def build_features() -> str:
    return (
        head(
            "All Systems & Engine Architecture — FIRE LAUNCHER",
            "Complete technical breakdown of FIRE LAUNCHER: 12 graphics renderers, OpenJDK 8/17/21, Top Downloading Bar, Modrinth/CurseForge Armory, Fire Client HUD, and Controls Studio.",
            "features.html",
        )
        + nav("features.html")
        + page_header(
            "⚙ Engine & Subsystems",
            "What Powers FIRE LAUNCHER",
            "From native Vulkan/OpenGL translation and automatic multi-Java switching to built-in PvP HUD mods and 1080p60 internal-audio recording.",
        )
        + "<main id=\"main\">"
        + all_systems_section()
        + requirements_and_download_section()
        + "</main>"
        + FOOTER
    )


def build_renderers() -> str:
    renderers = [
        ("MobileGlues (OpenGL 4.x → Vulkan)", "Best Overall for 1.17 – 1.21.4+", "Translates modern desktop OpenGL 4.6 draw calls onto Vulkan with low CPU overhead. Delivers the highest Sodium FPS and full Iris + Complementary Shaders compatibility on 4GB, 6GB, and 8GB+ phones."),
        ("Vulkan Zink + Custom Turnip Driver", "Best for Snapdragon Flagships & Heavy Shaders", "Mesa Zink paired with custom Freedreno Turnip .zip drivers on Adreno GPUs unlocks desktop-grade OpenGL 4.6 compliance for heavy 200-mod NeoForge/Forge packs."),
        ("Holy GL4ES (OpenGL 2.1 → GLES)", "Best for 4GB RAM & Legacy 1.7.10 – 1.16.5", "Ultra-lightweight memory footprint — ideal for 1.8.9 Bedwars PvP, 1.12.2 Forge packs, and budget Mali / PowerVR / older Adreno devices."),
        ("VirGL & ANGLE Backends", "Compatibility Fallbacks", "Specialized translation layers for older mods or specific GPU shader quirks when testing experimental snapshots."),
    ]
    r_cards = "\n".join(
        f"""        <article class="card sys-card">
          <span class="tag" style="width:fit-content">{sub}</span>
          <h3>{name}</h3>
          <p>{desc}</p>
        </article>"""
        for name, sub, desc in renderers
    )
    return (
        head(
            "4GB, 6GB & 8GB RAM FPS Guide & Renderers — FIRE LAUNCHER",
            "Detailed Minecraft Java FPS benchmarks on 4GB, 6GB, and 8GB+ Android phones, interactive FPS & JVM calculator, and renderer comparison.",
            "renderers.html",
        )
        + nav("renderers.html")
        + page_header(
            "📊 RAM Benchmarks & Renderers",
            "4GB, 6GB & 8GB+ RAM FPS Lab",
            "See exact FPS numbers for every RAM tier, calculate your phone's optimal Heap & JVM arguments, and pick the fastest renderer.",
        )
        + "<main id=\"main\">"
        + ram_fps_section()
        + f"""
  <section class="section">
    <div class="wrap">
      <div class="section-head">
        <span class="kicker">🔬 Graphics Backend Guide</span>
        <h2>Which Renderer Should You Choose?</h2>
      </div>
      <div class="grid grid-2">
{r_cards}
      </div>
    </div>
  </section>
</main>"""
        + FOOTER
    )


def build_install() -> str:
    steps = [
        ("Step 1 · Download the Official APK", "Tap the primary Download FIRE-Launcher.apk button below. On Android, allow installation from your browser when prompted."),
        ("Step 2 · Automatic Runtime Verification", "Open FIRE LAUNCHER. The bundled OpenJDK runtime unpacks automatically (or downloads via the Top Prism Downloading Bar on the Lightweight build)."),
        ("Step 3 · Sign In (Microsoft, Ely.by or Offline)", "Tap the Top-Left Operator Pod to log in with your Microsoft account, Ely.by profile, or create an instant Offline username."),
        ("Step 4 · Create Instance, Tune RAM & Hit IGNITE", "Open + FORGE in the Bottom Dock, pick Minecraft 1.21.4 + Fabric (add Sodium from Armory for 2×–3× FPS!), set RAM (1.8GB on 4GB phones, 3GB on 6GB, 4.3GB on 8GB+), and press ▶ IGNITE!"),
    ]
    s_html = "\n".join(
        f"""        <article class="card sys-card">
          <span class="kicker" style="width:fit-content;margin-bottom:4px">0{i+1}</span>
          <h3>{t}</h3>
          <p>{d}</p>
        </article>"""
        for i, (t, d) in enumerate(steps)
    )
    return (
        head(
            f"Download & Install FIRE LAUNCHER v{VERSION} (APK)",
            "Download FIRE LAUNCHER APK for Android (Universal, ARM64, and Lightweight editions), view system requirements, and follow the 4-step setup guide.",
            "install.html",
        )
        + nav("install.html")
        + page_header(
            f"⬇ Download & Setup · v{VERSION}",
            "Install FIRE LAUNCHER in 60 Seconds",
            "Direct APK downloads, 4-step first-launch setup, and recommended RAM settings for 4GB, 6GB, and 8GB+ Android devices.",
        )
        + f"""
<main id="main">
  <section class="section" id="steps">
    <div class="wrap">
      <div class="grid grid-2">
{s_html}
      </div>
    </div>
  </section>
"""
        + requirements_and_download_section()
        + "</main>"
        + FOOTER
    )


def build_faq() -> str:
    faqs = [
        ("4GB RAM phone me kitne FPS aayenge aur best settings kya hain?",
         "<p>4GB RAM phone par <b>1.8.9 / 1.20 PvP me 120–180 FPS</b>, <b>Minecraft 1.21.4 + Sodium + Lithium me 90–135 FPS</b>, aur Vanilla me <b>55–85 FPS</b> milte hain. Settings → Java me RAM allocation ko <code>1792 MB</code> (1.75 GB) par rakhein (2200 MB se upar mat rakhein taaki Android OS ko jagah mile), Render Distance <code>6–8 chunks</code> rakhein, aur Armory se <b>Sodium + FerriteCore</b> zaroor lagayein.</p>"),
        ("6GB aur 8GB RAM phone me kitne FPS milte hain?",
         "<p><b>6GB RAM</b> phone par (<code>3072 MB</code> Heap allocate karne par) Sodium ke saath <b>140–210 FPS</b>, PvP me <b>180–260 FPS</b>, aur Medium Shaders / 100 Mods ke saath <b>60–90 FPS</b> aate hain. <b>8GB / 12GB+ RAM</b> flagship phone par (<code>4352 MB – 6144 MB</code> Heap + 120Hz Mode + MobileGlues/Turnip driver) Sodium me <b>220–360+ FPS</b>, PvP me <b>300–450+ FPS</b>, aur Complementary Shaders / 200+ Mods modpacks me <b>80–130 FPS</b> milte hain.</p>"),
        ("Konsa APK download karna chahiye — Universal, ARM64, ya Lightweight?",
         "<p>Sabse easy setup ke liye <b>Full Universal APK</b> download karein kyunki usme Java Runtime pehle se bundled hota hai. Agar aapka phone modern 64-bit hai aur chhota installer chahiye to <b>ARM64 Edition</b> ya <b>Lightweight No-Runtime APK</b> le sakte hain.</p>"),
        ("Kya Mods, Modpacks aur Shaders launcher ke andar se hi download hote hain?",
         "<p>Haan! Bottom Dock me <b>✦ ARMORY</b> aur <b>▣ PACKS</b> kholein — wahan Modrinth aur CurseForge dono built-in hain. Aap 1-tap me Sodium, Iris, Complementary Shaders, Resource Packs aur poore Modpacks install kar sakte hain, aur upar <b>Top Prism Downloading Bar</b> me live speed aur percentage dikhta hai.</p>"),
        ("Kya Microsoft Official Account aur Offline Username dono chalte hain?",
         "<p>Bilkul. Aap official Microsoft Xbox Live OAuth se login karke premium servers (Hypixel, Hoplite, MCC Island) khel sakte hain, Ely.by skin account use kar sakte hain, ya bina internet ke instant Offline Username bana kar singleplayer aur cracked SMP servers khel sakte hain.</p>"),
        ("Mera game crash ho raha hai ya black screen aa rahi hai — kaise fix karein?",
         "<p>1) Top bar me <b>CONFIG → Video</b> me jaakar Renderer ko <b>MobileGlues</b> ya <b>Holy GL4ES</b> par switch karein.<br>2) Check karein ki RAM slider aapke phone ki total RAM ke 50% se zyada na ho (4GB par ~1800 MB, 6GB par ~3000 MB, 8GB par ~4300 MB).<br>3) Top bar me <b>▤ CONSOLE</b> kholkar crash log check karein.</p>"),
    ]
    items = []
    for idx, (q, a) in enumerate(faqs):
        op = " open" if idx == 0 else ""
        items.append(f"""        <details{op}>
          <summary>{q}</summary>
          <div class="body">{a}</div>
        </details>""")
    joined = "\n".join(items)
    return (
        head(
            "FAQ & Troubleshooting — FIRE LAUNCHER",
            "Frequently asked questions about FIRE LAUNCHER: 4GB/6GB/8GB FPS settings, APK selection, Modrinth/CurseForge mods, shaders, Microsoft & Offline login, and crash fixes.",
            "faq.html",
        )
        + nav("faq.html")
        + page_header(
            "❓ Help & Troubleshooting",
            "Frequently Asked Questions",
            "Everything about 4GB / 6GB / 8GB RAM optimization, renderers, mods, shaders, accounts, and crash fixes.",
        )
        + f"""
<main id="main">
  <section class="section">
    <div class="wrap">
      <div class="faq">
{joined}
      </div>
    </div>
  </section>
</main>"""
        + FOOTER
    )


def build_404() -> str:
    return (
        head(
            "404 Page Not Found — FIRE LAUNCHER",
            "Page not found — return to the official FIRE LAUNCHER website.",
            "404.html",
        )
        + nav("")
        + """
<main id="main">
  <section class="section">
    <div class="wrap">
      <div class="glass-box center" style="padding:56px 24px">
        <span class="kicker">404 · Not Found</span>
        <h1>Page Not Found</h1>
        <p class="lede" style="margin-inline:auto">Head back to the FIRE LAUNCHER homepage, UI tour, FPS calculator, or APK download center:</p>
        <div class="btn-row" style="justify-content:center">
          <a class="btn btn-primary" href="index.html">🏠 FIRE LAUNCHER Home</a>
          <a class="btn btn-ghost" href="screenshots.html">🖥 All UIs</a>
          <a class="btn btn-ghost" href="renderers.html">📊 RAM &amp; FPS</a>
          <a class="btn btn-ghost" href="install.html">⬇ Download APK</a>
        </div>
      </div>
    </div>
  </section>
</main>"""
        + FOOTER
    )


def main() -> None:
    pages = {
        "index.html": build_index(),
        "features.html": build_features(),
        "renderers.html": build_renderers(),
        "screenshots.html": build_screenshots(),
        "install.html": build_install(),
        "faq.html": build_faq(),
        "404.html": build_404(),
    }
    for name, html in pages.items():
        (DOCS / name).write_text(html, encoding="utf-8")
        print(f"wrote docs/{name}  ({len(html) // 1024} KB)")


if __name__ == "__main__":
    main()
