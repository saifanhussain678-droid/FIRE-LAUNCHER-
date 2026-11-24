/* ============================================================================
 * FIRE LAUNCHER — Frameless Editorial Interactive Engine (Zero Box Fill)
 * ============================================================================ */

(function () {
  const toggle = document.getElementById('navToggle');
  const links = document.getElementById('navLinks');
  if (toggle && links) {
    toggle.addEventListener('click', () => {
      const open = links.classList.toggle('open');
      toggle.setAttribute('aria-expanded', String(open));
    });
  }

  const uiStage = document.getElementById('fireUiStage');
  const uiTabs = document.querySelectorAll('[data-ui-tab]');
  let dlProgress = 68;
  let dlPaused = false;

  const rowStyle = 'padding:9px 0;border-bottom:1px solid rgba(2,132,199,0.18);display:flex;justify-content:space-between;align-items:center;font-size:12.5px;font-weight:700';

  const UI_SCREENS = {
    hq: () => `
      <div class="sim-stage-body sim-3col">
        <div class="sim-pane">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="tag">OPERATOR STUDIO</span>
            <span style="font-size:11px;font-weight:800;color:#0284C7">● CLOUD ID ✓</span>
          </div>
          <div style="flex:1;display:grid;place-items:center;padding:18px 8px">
            <div style="text-align:center">
              <img src="assets/branding/fire-mark.png" alt="" style="width:62px;height:62px;margin:0 auto 8px;filter:drop-shadow(0 8px 18px rgba(14,165,233,0.30))">
              <div style="font-weight:900;font-size:15px">Player_OP</div>
              <div style="font-size:11.5px;color:#55789E">360° Interactive 3D Skin &amp; Cape Studio</div>
            </div>
          </div>
          <div style="display:flex;gap:8px">
            <span class="sim-mini-btn" style="flex:1;text-align:center">SKIN WARDROBE</span>
            <span class="sim-mini-btn" style="flex:1;text-align:center">CAPES</span>
          </div>
        </div>

        <div class="sim-pane">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="tag">2×2 TACTICAL DECK</span>
            <span style="font-size:11px;font-weight:800;color:#0284C7">VULKAN · 120HZ</span>
          </div>
          <div class="pastel-grid-2x2">
            <div class="pastel-tile">◉ CREW &amp; PARTY<div style="font-size:10.5px;font-weight:600;color:#55789E;margin-top:2px">Friends &amp; World Invites</div></div>
            <div class="pastel-tile">✦ MOD ARMORY<div style="font-size:10.5px;font-weight:600;color:#55789E;margin-top:2px">Modrinth &amp; CurseForge</div></div>
            <div class="pastel-tile">🎮 CONTROL RIG<div style="font-size:10.5px;font-weight:600;color:#55789E;margin-top:2px">Touch, Mouse &amp; Gamepad</div></div>
            <div class="pastel-tile">⚙ RUN .JAR<div style="font-size:10.5px;font-weight:600;color:#55789E;margin-top:2px">OptiFine &amp; Forge Installer</div></div>
          </div>
          <div style="margin-top:auto;padding-top:10px;border-top:1px solid rgba(2,132,199,0.18);font-size:11.5px;font-weight:800;display:flex;justify-content:space-between">
            <span>⚡ ENGINE READY · OpenJDK 21 ARM64</span>
            <span style="color:#0284C7">MobileGlues 1.21.4</span>
          </div>
        </div>

        <div class="sim-pane">
          <span class="tag">ACTIVE INSTANCE &amp; RACK</span>
          <div style="padding:8px 0;border-bottom:1.5px solid rgba(2,132,199,0.32)">
            <div style="font-weight:900;font-size:15px">Prism PvP 1.21.4</div>
            <div style="font-size:12px;color:#2B4C73">Fabric 0.16.9 · Sodium + Lithium + Iris · 48 Mods</div>
          </div>
          <div>
            <div style="${rowStyle}"><span>🌈 Aurora Shaders Ultra</span><span style="color:#0284C7">NeoForge 1.21.1</span></div>
            <div style="${rowStyle};border-bottom:none"><span>⚔️ Competitive Bedwars</span><span style="color:#0284C7">1.8.9 OptiFine</span></div>
          </div>
        </div>
      </div>`,

    downloader: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">TOP FLOATING DOWNLOAD ENGINE</span>
          <h3>Relocated Top Downloading Bar</h3>
          <p style="font-size:13px;color:#2B4C73">Pause, Cancel &amp; Sub-Task Drawer sit on the Left; smooth sky-blue gradient progress bar + live MB/s speed + ETA sit in the Center; bold % readout sits on the Right.</p>
          <div>
            <div style="${rowStyle}"><span>✓ OpenJDK 21 ARM64 Runtime</span><span style="color:#0284C7">SHA-1 Verified</span></div>
            <div style="${rowStyle}"><span>⟳ Minecraft 1.21.4 + 208 Libraries</span><span style="color:#0284C7">24.8 MB/s</span></div>
            <div style="${rowStyle};border-bottom:none"><span>⟳ Sodium 0.6.5 + Fabric API</span><span style="color:#0284C7">Auto-Resolved</span></div>
          </div>
        </div>
        <div class="sim-pane">
          <span class="tag">MOD &amp; MODPACK INSTALL QUEUE</span>
          <h3>Background Multi-File Queue</h3>
          <p style="font-size:13px;color:#2B4C73">Install 10+ mods or a 150-mod CurseForge/Modrinth pack in the background while editing controls or skins.</p>
          <div style="padding-top:8px;border-top:1px solid rgba(2,132,199,0.22);display:grid;gap:6px">
            <div style="display:flex;justify-content:space-between;font-size:12.5px;font-weight:900"><span>Complementary Reimagined v5.3</span><span style="color:#0284C7">92%</span></div>
            <div class="sim-progress-track"><div class="sim-progress-fill" style="width:92%"></div></div>
            <div style="font-size:11.5px;color:#55789E">Target: Prism PvP 1.21.4 + Aurora Shaders</div>
          </div>
        </div>
      </div>`,

    vault: () => `
      <div class="sim-stage-body" style="grid-template-columns:1.1fr 0.9fr">
        <div class="sim-pane">
          <span class="tag">INSTANCE VAULT</span>
          <h3>Isolated Per-Instance Worlds, Mods &amp; Renderers</h3>
          <div>
            <div style="${rowStyle}"><b>💎 Prism PvP 1.21.4</b><span>Fabric · Java 21 · MobileGlues</span></div>
            <div style="${rowStyle}"><b>🔥 RLCraft Mobile</b><span>Forge 1.12.2 · Java 8 · GL4ES</span></div>
            <div style="${rowStyle};border-bottom:none"><b>⚡ Cobblemon 1.21.1</b><span>NeoForge · Java 21 · Vulkan Zink</span></div>
          </div>
        </div>
        <div class="sim-pane">
          <span class="tag">+ FORGE INSTANCE CREATOR</span>
          <h3>1-Click Version &amp; Loader Builder</h3>
          <p style="font-size:13px;color:#2B4C73">Every release from Minecraft 1.0 to 1.21.4+ &amp; Snapshots with automatic loader installation:</p>
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
          <p style="font-size:13px;color:#2B4C73">2.5× to 3.5× higher FPS on 4GB, 6GB &amp; 8GB phones. Auto-selects exact Minecraft &amp; Loader version.</p>
          <span class="sim-mini-btn" style="margin-top:auto;text-align:center">1-TAP INSTALL ✓</span>
        </div>
        <div class="sim-pane">
          <span class="tag">SHADERPACKS</span>
          <h3>🌈 Iris + Complementary</h3>
          <p style="font-size:13px;color:#2B4C73">Real-time water reflections, volumetric clouds &amp; dynamic shadows via MobileGlues OpenGL 4.x.</p>
          <span class="sim-mini-btn" style="margin-top:auto;text-align:center">1-TAP INSTALL ✓</span>
        </div>
        <div class="sim-pane">
          <span class="tag">FULL MODPACKS</span>
          <h3>📦 .mrpack &amp; CurseForge ZIP</h3>
          <p style="font-size:13px;color:#2B4C73">Search &amp; install complete modpacks with parallel multi-file download &amp; override extraction.</p>
          <span class="sim-mini-btn" style="margin-top:auto;text-align:center">BROWSE PACKS →</span>
        </div>
      </div>`,

    client: () => `
      <div class="sim-stage-body" style="grid-template-columns:1.1fr 0.9fr">
        <div class="sim-pane">
          <span class="tag">BUILT-IN FIRE CLIENT HUD</span>
          <h3>Competitive PvP &amp; FPS Overlay Suite</h3>
          <div class="pastel-grid-2x2">
            <div class="pastel-tile">FPS &amp; Frame-Time Pill</div>
            <div class="pastel-tile">RGB Keystrokes + CPS</div>
            <div class="pastel-tile">ToggleSprint &amp; Zoom</div>
            <div class="pastel-tile">Armor HUD &amp; Crosshair</div>
          </div>
        </div>
        <div class="sim-pane">
          <span class="tag">IN-GAME RECORDER &amp; MENU</span>
          <h3>🎥 1080p60 Screen + Internal Audio Recorder</h3>
          <p style="font-size:13px;color:#2B4C73">Tap the floating in-game FIRE LAUNCHER crystal flame button to record lag-free MP4 gameplay with internal game sound, live FPS counter, and instant log overlay.</p>
        </div>
      </div>`,

    controls: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">TOUCH &amp; GYRO STUDIO</span>
          <h3>11 Animated Control Themes + Custom Macros</h3>
          <p style="font-size:13px;color:#2B4C73">Drag, resize, set opacity, bind multi-key combos, customize virtual mouse cursor PNG/SVG, and enable precision Gyroscope aiming.</p>
          <div class="tag-row"><span class="tag">Aurora</span><span class="tag">Comet</span><span class="tag">Nebula</span><span class="tag">Galaxy</span><span class="tag">Stardust</span></div>
        </div>
        <div class="sim-pane">
          <span class="tag">GAMEPAD + KEYBOARD/MOUSE</span>
          <h3>Xbox, DualSense PS5 &amp; USB/BT Mouse Lock</h3>
          <p style="font-size:13px;color:#2B4C73">Full native pointer capture for USB OTG / Bluetooth mice &amp; keyboards plus customizable analog deadzones for Bluetooth controllers.</p>
        </div>
      </div>`,

    renderers: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">12 GRAPHICS RENDERERS</span>
          <h3>MobileGlues, Vulkan Zink, Turnip &amp; GL4ES</h3>
          <p style="font-size:13px;color:#2B4C73">Pick a global renderer or override per instance. Supports OpenGL 4.6 translation over Vulkan for modern Minecraft 1.21.4 + Iris Shaders.</p>
          <div class="tag-row"><span class="tag">MobileGlues</span><span class="tag">Vulkan Zink</span><span class="tag">Holy GL4ES</span><span class="tag">VirGL</span><span class="tag">ANGLE</span></div>
        </div>
        <div class="sim-pane">
          <span class="tag">CUSTOM GPU DRIVER IMPORTER</span>
          <h3>Import Adreno / Freedreno Turnip .ZIP Drivers</h3>
          <p style="font-size:13px;color:#2B4C73">Snapdragon users can import custom Mesa Turnip Vulkan drivers directly in Settings → Video for massive shader stability &amp; +40% extra FPS.</p>
        </div>
      </div>`,

    accounts: () => `
      <div class="sim-stage-body" style="grid-template-columns:1fr 1fr">
        <div class="sim-pane">
          <span class="tag">ACCOUNTS &amp; SKIN WARDROBE</span>
          <h3>Microsoft OAuth, Ely.by &amp; Offline Profiles</h3>
          <p style="font-size:13px;color:#2B4C73">Sign in securely with official Microsoft Xbox Live tokens, Ely.by skin servers, or instant Offline usernames — with full 3D Skin &amp; Cape preview.</p>
        </div>
        <div class="sim-pane">
          <span class="tag">FILES, CODE EDITOR &amp; LOGS</span>
          <h3>Built-In File Manager + Crash Log Analyzer</h3>
          <p style="font-size:13px;color:#2B4C73">Edit options.txt &amp; mod configs with line numbers &amp; search/replace, zip/unzip worlds, share crash logs in 1 tap, and show live status on Discord Rich Presence.</p>
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

  // Interactive RAM & FPS Performance Calculator
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

  // 1-Tap Copy for 4GB / 6GB / 8GB+ Best Settings Preset Cards
  document.querySelectorAll('[data-copy-preset]').forEach((btn) => {
    const orig = btn.textContent;
    btn.addEventListener('click', () => {
      const tier = btn.dataset.copyPreset;
      const flags = RAM_PROFILES[tier]?.jvm || RAM_PROFILES['6'].jvm;
      if (navigator.clipboard) {
        navigator.clipboard.writeText(flags);
        btn.textContent = `✓ Copied ${tier}GB Boost Flags!`;
        setTimeout(() => { btn.textContent = orig; }, 1800);
      }
    });
  });

  // Auto-resolve direct APK asset if a custom release is published on FIRE-LAUNCHER-
  fetch('https://api.github.com/repos/saifanhussain678-droid/FIRE-LAUNCHER-/releases/latest')
    .then((r) => (r.ok ? r.json() : null))
    .then((rel) => {
      if (!rel || !Array.isArray(rel.assets)) return;
      const apks = rel.assets.filter((a) => a.name && a.name.endsWith('.apk') && a.browser_download_url);
      if (!apks.length) return;
      const arm64 = apks.find((a) => a.name.includes('arm64')) || apks[0];
      const lite = apks.find((a) => a.name.includes('noruntime')) || apks[0];
      const univ = apks.find((a) => !a.name.includes('arm64') && !a.name.includes('armeabi') && !a.name.includes('noruntime')) || apks[0];
      document.querySelectorAll('[data-direct-apk="universal"]').forEach((a) => { a.href = univ.browser_download_url; });
      document.querySelectorAll('[data-direct-apk="arm64"]').forEach((a) => { a.href = arm64.browser_download_url; });
      document.querySelectorAll('[data-direct-apk="lite"]').forEach((a) => { a.href = lite.browser_download_url; });
    })
    .catch(() => {});

  // Interactive Real FPS Display Box Demo (Green / Yellow / Red + Settings ON/OFF)
  const fpsDemoState = { on: true, fps: 144, renderer: 'MobileGlues' };
  const liveBox = document.getElementById('liveRealFpsBox');
  const liveHidden = document.getElementById('liveRealFpsHiddenMsg');
  const liveBar = document.getElementById('liveRealFpsBar');
  const liveFpsVal = document.getElementById('liveRealFpsVal');
  const liveRenVal = document.getElementById('liveRealRendererVal');
  const liveMsVal = document.getElementById('liveRealMsVal');
  const btnOn = document.getElementById('fpsToggleOnBtn');
  const btnOff = document.getElementById('fpsToggleOffBtn');

  function colorForFps(fps) {
    if (fps >= 60) return '#22C55E'; // Green
    if (fps >= 30) return '#FACC15'; // Yellow
    return '#EF4444';                // Red
  }

  function renderRealFpsDemo() {
    if (!liveBox) return;
    if (!fpsDemoState.on) {
      liveBox.style.display = 'none';
      if (liveHidden) liveHidden.style.display = 'block';
      return;
    }
    liveBox.style.display = 'inline-flex';
    if (liveHidden) liveHidden.style.display = 'none';
    const col = colorForFps(fpsDemoState.fps);
    const ms = (1000 / Math.max(1, fpsDemoState.fps)).toFixed(1) + ' ms';
    liveBox.style.borderColor = col;
    if (liveBar) liveBar.style.background = col;
    if (liveFpsVal) {
      liveFpsVal.textContent = String(fpsDemoState.fps);
      liveFpsVal.style.color = col;
    }
    if (liveRenVal) liveRenVal.textContent = fpsDemoState.renderer;
    if (liveMsVal) {
      liveMsVal.textContent = ms;
      liveMsVal.style.color = col;
    }
  }

  if (btnOn && btnOff) {
    btnOn.addEventListener('click', () => {
      fpsDemoState.on = true;
      btnOn.classList.add('active');
      btnOff.classList.remove('active');
      renderRealFpsDemo();
    });
    btnOff.addEventListener('click', () => {
      fpsDemoState.on = false;
      btnOff.classList.add('active');
      btnOn.classList.remove('active');
      renderRealFpsDemo();
    });
  }

  document.querySelectorAll('[data-fps-demo]').forEach((btn) => {
    btn.addEventListener('click', () => {
      fpsDemoState.fps = parseInt(btn.dataset.fpsDemo, 10) || 60;
      document.querySelectorAll('[data-fps-demo]').forEach((b) => b.classList.toggle('active', b === btn));
      renderRealFpsDemo();
    });
  });

  document.querySelectorAll('[data-fps-renderer]').forEach((btn) => {
    btn.addEventListener('click', () => {
      fpsDemoState.renderer = btn.dataset.fpsRenderer || 'MobileGlues';
      document.querySelectorAll('[data-fps-renderer]').forEach((b) => b.classList.toggle('active', b === btn));
      renderRealFpsDemo();
    });
  });

  renderRealFpsDemo();
  updateCalculator();
})();
