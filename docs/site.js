/* ============================================================================
 * FIRE LAUNCHER — Interactive Website Engine (UI Simulator + RAM/FPS Lab)
 * ============================================================================ */

(function () {
  // 1. Mobile Navigation Toggle
  const toggle = document.getElementById('navToggle');
  const links = document.getElementById('navLinks');
  if (toggle && links) {
    toggle.addEventListener('click', () => {
      const open = links.classList.toggle('open');
      toggle.setAttribute('aria-expanded', String(open));
    });
  }

  // 2. Interactive 8-Screen FIRE LAUNCHER UI Explorer
  const uiStage = document.getElementById('fireUiStage');
  const uiTabs = document.querySelectorAll('[data-ui-tab]');
  let dlProgress = 68;
  let dlPaused = false;

  const UI_SCREENS = {
    hq: () => `
      <div class="sim-stage-body sim-3col">
        <div class="sim-pane">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="tag">OPERATOR STUDIO</span>
            <span style="font-size:11px;font-weight:800;color:#10B981">● CLOUD ID ✓</span>
          </div>
          <div style="flex:1;display:grid;place-items:center;padding:14px;border-radius:14px;background:radial-gradient(circle,rgba(56,189,248,0.22),rgba(236,72,153,0.12),transparent 72%)">
            <div style="text-align:center">
              <div style="width:68px;height:68px;margin:0 auto 8px;border-radius:18px;background:linear-gradient(135deg,#0EA5E9,#4F46E5,#EC4899);display:grid;place-items:center;color:#FFF;font-weight:900;font-size:22px;box-shadow:0 10px 24px rgba(37,99,235,0.28)">3D</div>
              <div style="font-weight:900;font-size:14px">Saifan_OP</div>
              <div style="font-size:11px;color:#527199">360° Interactive Skin &amp; Cape Doll</div>
            </div>
          </div>
          <div style="display:flex;gap:6px">
            <span class="sim-mini-btn" style="flex:1;text-align:center">SKIN WARDROBE</span>
            <span class="sim-mini-btn" style="flex:1;text-align:center">CAPES</span>
          </div>
        </div>

        <div class="sim-pane">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="tag">2×2 TACTICAL BENTO DECK</span>
            <span style="font-size:11px;font-weight:800;color:#2563EB">VULKAN · 120HZ READY</span>
          </div>
          <div class="pastel-grid-2x2">
            <div class="pastel-tile pt-sky">◉ CREW &amp; PARTY<div style="font-size:10.5px;font-weight:600;margin-top:3px">Friends &amp; World Invites</div></div>
            <div class="pastel-tile pt-pink">✦ MOD ARMORY<div style="font-size:10.5px;font-weight:600;margin-top:3px">Modrinth &amp; CurseForge</div></div>
            <div class="pastel-tile pt-mint">🎮 CONTROL RIG<div style="font-size:10.5px;font-weight:600;margin-top:3px">Touch, Mouse &amp; Gamepad</div></div>
            <div class="pastel-tile pt-amber">⚙ RUN .JAR<div style="font-size:10.5px;font-weight:600;margin-top:3px">OptiFine &amp; Forge Installer</div></div>
          </div>
          <div style="margin-top:auto;padding:9px 12px;border-radius:12px;background:#EFF6FF;font-size:11.5px;font-weight:800;color:#0F2544;display:flex;justify-content:space-between">
            <span>⚡ ENGINE READY · OpenJDK 21 ARM64</span>
            <span style="color:#2563EB">MobileGlues 1.21.4</span>
          </div>
        </div>

        <div class="sim-pane">
          <span class="tag">ACTIVE INSTANCE &amp; RACK</span>
          <div style="padding:12px;border-radius:14px;background:linear-gradient(135deg,#EFF6FF,#DBEAFE);border:1px solid rgba(37,99,235,0.25)">
            <div style="font-weight:900;font-size:14.5px">Prism PvP 1.21.4</div>
            <div style="font-size:11.5px;color:#2C4870">Fabric 0.16.9 · Sodium + Lithium + Iris · 48 Mods</div>
          </div>
          <div style="display:grid;gap:6px;font-size:12px;font-weight:800">
            <div style="padding:8px 10px;border-radius:10px;background:#F8FAFC;border:1px solid rgba(37,99,235,0.14)">🌈 Aurora Shaders Ultra · NeoForge 1.21.1</div>
            <div style="padding:8px 10px;border-radius:10px;background:#F8FAFC;border:1px solid rgba(37,99,235,0.14)">⚔️ Competitive Bedwars · 1.8.9 OptiFine</div>
          </div>
        </div>
      </div>`,

    downloader: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">TOP FLOATING DOWNLOAD ENGINE</span>
          <h3>Relocated Top Prism Downloading Bar</h3>
          <p style="font-size:13px;color:#2C4870">Downloads never block your screen: Pause, Cancel &amp; Sub-Task Drawer sit on the Left; Multi-Color Cyan → Indigo → Pink progress bar + live MB/s speed + ETA sit in the Center; bold % readout sits on the Right.</p>
          <div style="display:grid;gap:7px;margin-top:6px;font-size:12px;font-weight:800">
            <div style="padding:9px 12px;border-radius:10px;background:#ECFDF5;color:#047857">✓ OpenJDK 21 ARM64 Runtime — Verified (SHA-1 OK)</div>
            <div style="padding:9px 12px;border-radius:10px;background:#EFF6FF;color:#1D4ED8">⟳ Minecraft 1.21.4 Client + 208 Libraries — 24.8 MB/s</div>
            <div style="padding:9px 12px;border-radius:10px;background:#FDF2F8;color:#BE185D">⟳ Sodium 0.6.5 + Fabric API — Auto-Resolved</div>
          </div>
        </div>
        <div class="sim-pane">
          <span class="tag">MOD &amp; MODPACK INSTALL QUEUE</span>
          <h3>Minimizable Background Queue Pill</h3>
          <p style="font-size:13px;color:#2C4870">Install 10+ mods or a 150-mod CurseForge/Modrinth pack in the background while editing controls or skins. Minimize into a floating glass pill anytime.</p>
          <div style="padding:12px;border-radius:14px;background:#EFF6FF;border:1px solid rgba(37,99,235,0.22);display:grid;gap:6px">
            <div style="display:flex;justify-content:space-between;font-size:12.5px;font-weight:900"><span>Complementary Reimagined v5.3</span><span style="color:#DB2777">92%</span></div>
            <div class="sim-progress-track"><div class="sim-progress-fill" style="width:92%"></div></div>
            <div style="font-size:11px;color:#527199">Installing into: Prism PvP 1.21.4 + Aurora Shaders</div>
          </div>
        </div>
      </div>`,

    vault: () => `
      <div class="sim-stage-body" style="grid-template-columns:1.1fr 0.9fr">
        <div class="sim-pane">
          <span class="tag">INSTANCE VAULT</span>
          <h3>Isolated Per-Instance Worlds, Mods &amp; Renderers</h3>
          <div style="display:grid;gap:8px;font-size:12.5px">
            <div style="padding:10px 12px;border-radius:12px;background:#EFF6FF;border:1px solid rgba(37,99,235,0.25);display:flex;justify-content:space-between"><b>💎 Prism PvP 1.21.4</b><span>Fabric · Java 21 · MobileGlues</span></div>
            <div style="padding:10px 12px;border-radius:12px;background:#F8FAFC;border:1px solid rgba(37,99,235,0.14);display:flex;justify-content:space-between"><b>🔥 RLCraft Mobile</b><span>Forge 1.12.2 · Java 8 · GL4ES</span></div>
            <div style="padding:10px 12px;border-radius:12px;background:#F8FAFC;border:1px solid rgba(37,99,235,0.14);display:flex;justify-content:space-between"><b>⚡ Cobblemon 1.21.1</b><span>NeoForge · Java 21 · Vulkan Zink</span></div>
          </div>
        </div>
        <div class="sim-pane">
          <span class="tag">+ FORGE INSTANCE CREATOR</span>
          <h3>1-Click Version &amp; Loader Builder</h3>
          <p style="font-size:12.5px;color:#2C4870">Every release from Minecraft 1.0 to 1.21.4+ &amp; Snapshots with automatic loader installation:</p>
          <div class="tag-row">
            <span class="tag">Vanilla</span><span class="tag">Fabric</span><span class="tag">Quilt</span><span class="tag">Forge</span><span class="tag">NeoForge</span><span class="tag">OptiFine</span><span class="tag">Legacy Fabric</span><span class="tag">BTA 1.7.3</span>
          </div>
        </div>
      </div>`,

    armory: () => `
      <div class="sim-stage-body" style="grid-template-columns:repeat(3,1fr)">
        <div class="sim-pane">
          <span class="tag">MODRINTH + CURSEFORGE</span>
          <h3>⚡ Sodium + Lithium</h3>
          <p style="font-size:12.5px;color:#2C4870">2.5× to 3.5× higher FPS on 4GB, 6GB &amp; 8GB phones. Auto-selects exact Minecraft &amp; Loader version.</p>
          <span class="sim-mini-btn" style="margin-top:auto;text-align:center">1-TAP INSTALL ✓</span>
        </div>
        <div class="sim-pane">
          <span class="tag">SHADERPACKS</span>
          <h3>🌈 Iris + Complementary</h3>
          <p style="font-size:12.5px;color:#2C4870">Real-time water reflections, volumetric clouds &amp; dynamic shadows via MobileGlues OpenGL 4.x.</p>
          <span class="sim-mini-btn" style="margin-top:auto;text-align:center">1-TAP INSTALL ✓</span>
        </div>
        <div class="sim-pane">
          <span class="tag">FULL MODPACKS</span>
          <h3>📦 .mrpack &amp; CurseForge ZIP</h3>
          <p style="font-size:12.5px;color:#2C4870">Search &amp; install complete modpacks with parallel multi-file download &amp; override extraction.</p>
          <span class="sim-mini-btn" style="margin-top:auto;text-align:center">BROWSE PACKS →</span>
        </div>
      </div>`,

    client: () => `
      <div class="sim-stage-body" style="grid-template-columns:1.1fr 0.9fr">
        <div class="sim-pane">
          <span class="tag">BUILT-IN FIRE CLIENT HUD</span>
          <h3>Competitive PvP &amp; FPS Overlay Suite</h3>
          <div class="pastel-grid-2x2">
            <div class="pastel-tile pt-sky">FPS &amp; Frame-Time Pill</div>
            <div class="pastel-tile pt-pink">RGB Keystrokes + CPS</div>
            <div class="pastel-tile pt-mint">ToggleSprint &amp; Zoom</div>
            <div class="pastel-tile pt-amber">Armor HUD &amp; Crosshair</div>
          </div>
        </div>
        <div class="sim-pane">
          <span class="tag">IN-GAME RECORDER &amp; MENU</span>
          <h3>🎥 1080p60 Screen + Internal Audio Recorder</h3>
          <p style="font-size:12.5px;color:#2C4870">Tap the floating in-game FIRE LAUNCHER crystal flame button to record lag-free MP4 gameplay with internal game sound, live FPS counter, and instant log overlay.</p>
        </div>
      </div>`,

    controls: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">TOUCH &amp; GYRO STUDIO</span>
          <h3>11 Animated Control Themes + Custom Macros</h3>
          <p style="font-size:12.5px;color:#2C4870">Drag, resize, set opacity, bind multi-key combos, customize virtual mouse cursor PNG/SVG, and enable precision Gyroscope aiming.</p>
          <div class="tag-row"><span class="tag">Aurora</span><span class="tag">Comet</span><span class="tag">Nebula</span><span class="tag">Galaxy</span><span class="tag">Stardust</span></div>
        </div>
        <div class="sim-pane">
          <span class="tag">GAMEPAD + KEYBOARD/MOUSE</span>
          <h3>Xbox, DualSense PS5 &amp; USB/BT Mouse Lock</h3>
          <p style="font-size:12.5px;color:#2C4870">Full native pointer capture for USB OTG / Bluetooth mice &amp; keyboards plus customizable analog deadzones for Bluetooth controllers.</p>
        </div>
      </div>`,

    renderers: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">12 GRAPHICS RENDERERS</span>
          <h3>MobileGlues, Vulkan Zink, Turnip &amp; GL4ES</h3>
          <p style="font-size:12.5px;color:#2C4870">Pick a global renderer or override per instance. Supports OpenGL 4.6 translation over Vulkan for modern Minecraft 1.21.4 + Iris Shaders.</p>
          <div class="tag-row"><span class="tag">MobileGlues</span><span class="tag">Vulkan Zink</span><span class="tag">Holy GL4ES</span><span class="tag">VirGL</span><span class="tag">ANGLE</span></div>
        </div>
        <div class="sim-pane">
          <span class="tag">CUSTOM GPU DRIVER IMPORTER</span>
          <h3>Import Adreno / Freedreno Turnip .ZIP Drivers</h3>
          <p style="font-size:12.5px;color:#2C4870">Snapdragon users can import custom Mesa Turnip Vulkan drivers directly in Settings → Video for massive shader stability &amp; +40% extra FPS.</p>
        </div>
      </div>`,

    accounts: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">ACCOUNTS &amp; SKIN WARDROBE</span>
          <h3>Microsoft OAuth, Ely.by &amp; Offline Profiles</h3>
          <p style="font-size:12.5px;color:#2C4870">Sign in securely with official Microsoft Xbox Live tokens, Ely.by skin servers, or instant Offline usernames — with full 3D Skin &amp; Cape preview.</p>
        </div>
        <div class="sim-pane">
          <span class="tag">FILES, CODE EDITOR &amp; LOGS</span>
          <h3>Built-In File Manager + Crash Log Analyzer</h3>
          <p style="font-size:12.5px;color:#2C4870">Edit options.txt &amp; mod configs with line numbers &amp; search/replace, zip/unzip worlds, share crash logs in 1 tap, and show live status on Discord Rich Presence.</p>
        </div>
      </div>`
  };

  function renderUiScreen(key) {
    if (!uiStage || !UI_SCREENS[key]) return;
    uiStage.innerHTML = UI_SCREENS[key]();
    uiTabs.forEach((b) => b.classList.toggle('active', b.dataset.uiTab === key));
  }

  if (uiStage && uiTabs.length) {
    uiTabs.forEach((btn) => {
      btn.addEventListener('click', () => renderUiScreen(btn.dataset.uiTab));
    });
    renderUiScreen('hq');
  }

  // Live Top Downloading Bar animation inside UI Simulator
  const simFill = document.getElementById('simDlFill');
  const simPct = document.getElementById('simDlPct');
  const simPause = document.getElementById('simDlPause');
  if (simPause) {
    simPause.addEventListener('click', () => {
      dlPaused = !dlPaused;
      simPause.textContent = dlPaused ? '▶ Resume' : '⏸ Pause';
    });
  }
  if (simFill && simPct) {
    setInterval(() => {
      if (dlPaused) return;
      dlProgress = dlProgress >= 100 ? 18 : dlProgress + 2;
      simFill.style.width = dlProgress + '%';
      simPct.textContent = dlProgress + '%';
    }, 950);
  }

  // 3. Interactive RAM & FPS Performance Calculator (4GB / 6GB / 8GB / 12GB+)
  const calcState = { ram: '6', soc: 'mid', mode: 'sodium' };

  const RAM_PROFILES = {
    '4': {
      heap: '1792 MB (1.75 GB)',
      chunks: '6 – 8 Chunks',
      renderer: 'MobileGlues (Fast) / Holy GL4ES',
      baseFps: 95,
      jvm: '-Xms1792M -Xmx1792M -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Dsodium.checks.issue2561=false'
    },
    '6': {
      heap: '3072 MB (3.0 GB)',
      chunks: '10 – 12 Chunks',
      renderer: 'MobileGlues (OpenGL 4.x / Vulkan)',
      baseFps: 155,
      jvm: '-Xms3072M -Xmx3072M -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:MaxGCPauseMillis=45'
    },
    '8': {
      heap: '4352 MB (4.25 GB)',
      chunks: '12 – 16 Chunks · 120Hz Mode ON',
      renderer: 'MobileGlues Vulkan + Custom Turnip Driver',
      baseFps: 245,
      jvm: '-Xms4352M -Xmx4352M -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=35 -XX:G1HeapRegionSize=16M'
    },
    '12': {
      heap: '6144 MB (6.0 GB)',
      chunks: '16 – 22 Chunks · 120Hz/144Hz Unlocked',
      renderer: 'MobileGlues Ultra / Turnip + Vulkan Zink',
      baseFps: 320,
      jvm: '-Xms6144M -Xmx6144M -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=30 -XX:G1HeapRegionSize=32M'
    }
  };

  const SOC_MULT = { budget: 0.72, mid: 1.0, flagship: 1.38 };
  const MODE_MULT = {
    pvp: 1.35,
    sodium: 1.0,
    vanilla: 0.62,
    shaders: 0.42,
    modpack: 0.48
  };

  function updateCalculator() {
    const fpsOut = document.getElementById('calcFpsOut');
    if (!fpsOut) return;
    const prof = RAM_PROFILES[calcState.ram];
    const mult = SOC_MULT[calcState.soc] * MODE_MULT[calcState.mode];
    const avg = Math.round(prof.baseFps * mult);
    const low = Math.max(24, Math.round(avg * 0.72));
    const peak = Math.round(avg * 1.22);

    fpsOut.textContent = `${avg} – ${peak} FPS`;
    document.getElementById('calcLowOut').textContent = `1% Low: ~${low} FPS · Smooth Frame Pacing`;
    document.getElementById('calcHeapOut').textContent = prof.heap;
    document.getElementById('calcChunksOut').textContent = prof.chunks;
    document.getElementById('calcRendererOut').textContent = prof.renderer;
    document.getElementById('calcJvmOut').textContent = prof.jvm;
  }

  document.querySelectorAll('[data-calc-group]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const group = btn.dataset.calcGroup;
      const val = btn.dataset.calcVal;
      calcState[group] = val;
      document.querySelectorAll(`[data-calc-group="${group}"]`).forEach((b) => {
        b.classList.toggle('active', b === btn);
      });
      updateCalculator();
    });
  });

  const copyJvmBtn = document.getElementById('copyJvmBtn');
  if (copyJvmBtn) {
    copyJvmBtn.addEventListener('click', () => {
      const code = document.getElementById('calcJvmOut')?.textContent || '';
      if (navigator.clipboard && code) {
        navigator.clipboard.writeText(code);
        copyJvmBtn.textContent = '✓ Copied JVM Flags!';
        setTimeout(() => { copyJvmBtn.textContent = '📋 Copy Best JVM Flags'; }, 1800);
      }
    });
  }

  updateCalculator();
})();
