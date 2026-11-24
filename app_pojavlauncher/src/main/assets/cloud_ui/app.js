(function () {
  const BUILTIN_SKINS = [
    "skins/skin_cloud_op.png",
    "skins/skin_prism_striker.png",
    "skins/skin_sakura_mage.png",
    "skins/skin_crystal_gold.png"
  ];

  const DEFAULT_VERSIONS = [
    { id: "1.21.4", type: "RELEASE" },
    { id: "1.21.3", type: "RELEASE" },
    { id: "1.21.1", type: "RELEASE" },
    { id: "1.20.6", type: "RELEASE" },
    { id: "1.20.4", type: "RELEASE" },
    { id: "1.20.2", type: "RELEASE" },
    { id: "1.20.1", type: "RELEASE" },
    { id: "1.19.4", type: "RELEASE" },
    { id: "1.18.2", type: "RELEASE" },
    { id: "1.17.1", type: "RELEASE" },
    { id: "1.16.5", type: "RELEASE" },
    { id: "1.12.2", type: "RELEASE" },
    { id: "1.8.9", type: "RELEASE" }
  ];

  const DEFAULT_SERVERS = [
    { name: "Hypixel Network", address: "mc.hypixel.net", mode: "SkyWars · BedWars · SkyBlock" },
    { name: "Minemen Club", address: "na.minemen.club", mode: "Competitive 1.8 / 1.20 PvP" },
    { name: "Hoplite", address: "hoplite.gg", mode: "1.20+ Battle Royale & Duels" },
    { name: "MCC Island", address: "play.mccisland.net", mode: "Modern Minigames · 1.21" }
  ];

  const state = {
    screen: "hq",
    settingsCat: "fps",
    forgeLoader: "VANILLA",
    forgeType: "RELEASE",
    selectedMcVer: "1.21.4",
    packKind: "shader",
    skinIdx: 0,
    viewer: null,
    data: {
      accountName: "Cloud_OP",
      accountType: "ONLINE ID",
      skinDataUrl: "",
      accounts: [],
      selectedInstance: "Prism PvP 1.21.4",
      selectedVersion: "1.21.4",
      instances: [],
      ramMb: 2816,
      maxRamMb: 8192,
      freeRamMb: 3200,
      renderer: "opengles2",
      rendererLabel: "MobileGlues (Vulkan 1.3 / OpenGL 4.6)",
      renderers: [
        { id: "opengles2", label: "MobileGlues (Vulkan 1.3 / OpenGL 4.6)" },
        { id: "vulkan_zink", label: "Mesa Zink (Vulkan Desktop GL)" },
        { id: "opengles3_ltw", label: "LTW GLES 3.2 Translator" }
      ],
      showRealFps: true,
      unlock120Hz: true,
      sustainedPerf: true,
      fullScreen: true,
      scaleFactor: 85,
      jvmArgs: "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=50",
      keystrokes: true,
      cpsCounter: true,
      toggleSprint: true,
      zoomHud: true,
      crosshair: false,
      preset: "6gb",
      versionName: "2.4.7",
      packageName: "net.kdt.pojavlaunch.cloud",
      taskActive: false,
      taskTitle: "",
      taskPct: 0
    },
    versions: DEFAULT_VERSIONS.slice(),
    servers: JSON.parse(localStorage.getItem("cloud_servers_v1") || "null") || DEFAULT_SERVERS
  };

  function bridge() {
    return window.CloudBridge || null;
  }

  function toast(msg) {
    const el = document.getElementById("cloud-toast");
    if (!el) return;
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(toast._t);
    toast._t = setTimeout(() => { el.style.display = "none"; }, 2400);
  }

  function esc(str) {
    return String(str == null ? "" : str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function currentSkinUrl() {
    if (state.data.skinDataUrl && state.data.skinDataUrl.startsWith("data:image/")) {
      return state.data.skinDataUrl;
    }
    return BUILTIN_SKINS[state.skinIdx % BUILTIN_SKINS.length];
  }

  function drawTopAvatar(skinSrc) {
    const c = document.getElementById("top-avatar-canvas");
    if (!c) return;
    const ctx = c.getContext("2d");
    const img = new Image();
    img.onload = function () {
      ctx.imageSmoothingEnabled = false;
      ctx.clearRect(0, 0, 32, 32);
      ctx.drawImage(img, 8, 8, 8, 8, 0, 0, 32, 32);
      ctx.drawImage(img, 40, 8, 8, 8, 0, 0, 32, 32);
    };
    img.src = skinSrc;
  }

  function init3DSkin() {
    const host = document.getElementById("hq-skin-host");
    if (!host) return;
    host.innerHTML = "";
    const w = Math.max(150, host.clientWidth || 200);
    const h = Math.max(180, host.clientHeight || 235);
    try {
      if (window.skinview3d && window.skinview3d.SkinViewer) {
        const canvas = document.createElement("canvas");
        const viewer = new window.skinview3d.SkinViewer({
          canvas: canvas,
          width: w,
          height: h,
          skin: currentSkinUrl()
        });
        viewer.pixelRatio = Math.min(window.devicePixelRatio || 1, 2);
        viewer.fov = 42;
        viewer.zoom = 0.86;
        if (window.skinview3d.IdleAnimation) {
          viewer.animation = new window.skinview3d.IdleAnimation();
        } else if (window.skinview3d.WalkingAnimation) {
          viewer.animation = new window.skinview3d.WalkingAnimation();
          viewer.animation.speed = 0.55;
        }
        viewer.playerObject.rotation.y = -0.42;
        host.appendChild(canvas);
        state.viewer = viewer;
        drawTopAvatar(currentSkinUrl());
        return;
      }
    } catch (e) {
      console.warn("WebGL skinview fallback:", e);
    }
    // Crisp 2D pixel skin fallback if WebGL is unavailable
    const fallback = document.createElement("canvas");
    fallback.width = 160;
    fallback.height = 220;
    fallback.style.height = "92%";
    fallback.style.imageRendering = "pixelated";
    host.appendChild(fallback);
    render2DSkinFallback(fallback, currentSkinUrl());
    drawTopAvatar(currentSkinUrl());
  }

  function render2DSkinFallback(canvas, src) {
    const ctx = canvas.getContext("2d");
    const img = new Image();
    img.onload = function () {
      ctx.imageSmoothingEnabled = false;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      // Head
      ctx.drawImage(img, 8, 8, 8, 8, 48, 14, 64, 64);
      ctx.drawImage(img, 40, 8, 8, 8, 46, 12, 68, 68);
      // Body
      ctx.drawImage(img, 20, 20, 8, 12, 48, 78, 64, 72);
      // Arms
      ctx.drawImage(img, 44, 20, 4, 12, 20, 78, 28, 72);
      ctx.drawImage(img, 36, 52, 4, 12, 112, 78, 28, 72);
      // Legs
      ctx.drawImage(img, 4, 20, 4, 12, 48, 150, 32, 64);
      ctx.drawImage(img, 20, 52, 4, 12, 80, 150, 32, 64);
    };
    img.src = src;
  }

  function updateSkinTexture() {
    const src = currentSkinUrl();
    drawTopAvatar(src);
    if (state.viewer) {
      try { state.viewer.loadSkin(src); } catch (e) {}
    } else {
      const fb = document.querySelector("#hq-skin-host canvas");
      if (fb) render2DSkinFallback(fb, src);
    }
  }

  function syncFromBridge() {
    const b = bridge();
    if (!b) {
      renderAll();
      return;
    }
    try {
      const raw = JSON.parse(b.getStateJson());
      Object.assign(state.data, raw);
      const vers = JSON.parse(b.getVersionsJson() || "[]");
      if (Array.isArray(vers) && vers.length > 0) {
        state.versions = vers;
      }
    } catch (e) {
      console.warn("syncFromBridge error:", e);
    }
    renderAll();
    updateSkinTexture();
  }

  function renderTopAndHQ() {
    const d = state.data;
    document.getElementById("top-account-name").textContent = d.accountName || "Cloud_OP";
    document.getElementById("top-account-mode").textContent = "· " + (d.accountType || "CLOUD ID");

    const instTitle = d.selectedInstance || "Cloud 1.21.4";
    const instVer = d.selectedVersion || "1.21.4";
    document.getElementById("top-inst-pill").textContent = instTitle + " · " + instVer;
    document.getElementById("hq-active-name").textContent = instTitle;
    document.getElementById("hq-active-ver").textContent = "Minecraft " + instVer + " · Ready to Ignite";

    const shortRenderer = (d.rendererLabel || "MobileGlues").split("(")[0].trim().toUpperCase();
    document.getElementById("hq-engine-status").textContent =
      "ENGINE READY · " + shortRenderer + " · " + d.ramMb + " MB RAM" + (d.unlock120Hz ? " · 120HZ" : "");

    // Top Downloader Bar
    const dlBar = document.getElementById("top-downloader");
    if (d.taskActive) {
      dlBar.classList.add("active");
      document.body.classList.add("has-dl");
      document.getElementById("dl-title").textContent = d.taskTitle || "Cloud Downloader Active...";
      const pct = Math.max(5, Math.min(100, d.taskPct || 45));
      document.getElementById("dl-fill").style.width = pct + "%";
      document.getElementById("dl-pct").textContent = pct + "%";
    } else {
      dlBar.classList.remove("active");
      document.body.classList.remove("has-dl");
    }

    // Instance Rack
    const rack = document.getElementById("hq-rack-list");
    const list = d.instances && d.instances.length ? d.instances : [];
    if (!list.length) {
      rack.innerHTML = `
        <div class="rack-item selected" onclick="CloudUI.quickCreateDefault()">
          <div class="rack-badge">+</div>
          <div class="rack-name">Tap to Create Minecraft 1.21.4 Instance</div>
          <span class="dot-green"></span>
        </div>`;
    } else {
      rack.innerHTML = list.map((it, idx) => `
        <div class="rack-item ${it.selected ? "selected" : ""}" onclick="CloudUI.selectInstance(${idx})">
          <div class="rack-badge">${esc((it.name || "C").slice(0, 2).toUpperCase())}</div>
          <div style="flex:1;min-width:0;">
            <div class="rack-name">${esc(it.name)}</div>
            <div style="font-size:9.5px;color:#64748b;font-weight:700;">${esc(it.versionId || "1.21.4")}</div>
          </div>
          ${it.selected ? '<span class="dot-green"></span>' : '<span style="color:#94a3b8;font-weight:900;">›</span>'}
        </div>
      `).join("");
    }
  }

  function renderVault() {
    const grid = document.getElementById("vault-grid");
    const list = state.data.instances || [];
    if (!list.length) {
      grid.innerHTML = `
        <div class="item-card">
          <div class="setting-title">No Instances Yet</div>
          <div class="setting-sub">Create a Vanilla, Fabric, Forge, NeoForge, or Quilt profile in 1 tap.</div>
          <div class="item-actions">
            <button class="mini-btn primary" onclick="CloudUI.quickCreateDefault()">+ Quick Create 1.21.4</button>
            <button class="mini-btn" onclick="CloudUI.go('forge')">Open + FORGE</button>
          </div>
        </div>`;
      return;
    }
    grid.innerHTML = list.map((it, idx) => `
      <div class="item-card" style="${it.selected ? 'border-color:#38bdf8;background:rgba(240,249,255,0.92);' : ''}">
        <div class="item-card-head">
          <div class="item-icon">${esc((it.name || "MC").slice(0, 2).toUpperCase())}</div>
          <div style="min-width:0;flex:1;">
            <div class="setting-title" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(it.name)}</div>
            <div class="setting-sub">${esc(it.versionId || "1.21.4")} ${it.selected ? "· ● ACTIVE" : ""}</div>
          </div>
        </div>
        <div class="item-actions">
          <button class="mini-btn primary" onclick="CloudUI.selectAndLaunch(${idx})">► LAUNCH</button>
          <button class="mini-btn" onclick="CloudUI.selectInstance(${idx})">SELECT</button>
          <button class="mini-btn" onclick="CloudUI.renameInstance(${idx})">RENAME</button>
          <button class="mini-btn" onclick="CloudUI.cloneInstance(${idx})">CLONE</button>
          <button class="mini-btn danger" onclick="CloudUI.deleteInstance(${idx})">DELETE</button>
        </div>
      </div>
    `).join("");
  }

  function renderForgeVersions() {
    const q = (document.getElementById("forge-search")?.value || "").trim().toLowerCase();
    const listEl = document.getElementById("forge-versions-list");
    const filtered = state.versions.filter(v => {
      if (state.forgeType === "SNAPSHOT") {
        if (v.type === "RELEASE") return false;
      } else {
        if (v.type && v.type !== "RELEASE") return false;
      }
      return !q || v.id.toLowerCase().includes(q);
    }).slice(0, 48);

    listEl.innerHTML = filtered.map(v => {
      const sel = v.id === state.selectedMcVer;
      return `
        <div class="item-card" style="cursor:pointer;${sel ? 'border-color:#c084fc;background:linear-gradient(135deg,rgba(224,242,254,0.95),rgba(252,231,243,0.92));' : ''}"
             onclick="CloudUI.pickMcVersion('${esc(v.id)}')">
          <div class="item-card-head">
            <div class="item-icon">⛏</div>
            <div>
              <div class="setting-title">Minecraft ${esc(v.id)}</div>
              <div class="setting-sub">${esc(state.forgeLoader)} · ${esc(v.type || "RELEASE")}</div>
            </div>
          </div>
        </div>`;
    }).join("");
  }

  function renderSettings() {
    const pane = document.getElementById("settings-main-pane");
    const d = state.data;
    const cat = state.settingsCat;

    document.querySelectorAll("[data-scat]").forEach(btn => {
      btn.classList.toggle("active", btn.getAttribute("data-scat") === cat);
    });

    if (cat === "fps" || cat === "video") {
      const rendererOpts = (d.renderers || []).map(r =>
        `<option value="${esc(r.id)}" ${r.id === d.renderer ? "selected" : ""}>${esc(r.label)}</option>`
      ).join("");
      const shortRend = (d.rendererLabel || "MobileGlues").split("(")[0].trim();

      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">CLOUD FPS BOOST PRESETS (1-TAP OPTIMIZER)</div>
            <div class="setting-sub">Auto-tunes RAM, Renderer, Resolution Scale &amp; G1GC Flags for your phone</div>
          </div>
          <div class="preset-group">
            <button class="preset-pill ${d.preset === '4gb' ? 'active' : ''}" onclick="CloudUI.applyPreset('4gb')">4GB</button>
            <button class="preset-pill ${d.preset === '6gb' ? 'active' : ''}" onclick="CloudUI.applyPreset('6gb')">6GB</button>
            <button class="preset-pill ${d.preset === '8gb' ? 'active' : ''}" onclick="CloudUI.applyPreset('8gb')">8GB+</button>
          </div>
        </div>

        <div class="setting-row">
          <div style="min-width:165px;">
            <div class="setting-title">Memory Allocation (RAM)</div>
            <div class="setting-sub">Free Device RAM: ${d.freeRamMb} MB</div>
          </div>
          <input type="range" class="prism-slider" min="512" max="${Math.max(2048, d.maxRamMb)}" step="64" value="${d.ramMb}"
                 oninput="document.getElementById('ram-val-lbl').textContent=this.value+' MB'"
                 onchange="CloudUI.setRam(this.value)">
          <div id="ram-val-lbl" style="font-weight:900;font-size:13px;min-width:74px;text-align:right;">${d.ramMb} MB</div>
        </div>

        <div class="setting-row">
          <div>
            <div class="setting-title">Show Real FPS Display</div>
            <div class="setting-sub">Green (60+) · Yellow (30-59) · Red (&lt;30)</div>
          </div>
          <div style="display:flex;align-items:center;gap:10px;">
            <div class="fps-preview-box">
              Fps - <span class="g">124</span> / Renderer - <span class="b">${esc(shortRend)}</span> / Ms - <span class="g">8.1 ms</span>
            </div>
            <div class="prism-switch ${d.showRealFps ? 'on' : ''}" onclick="CloudUI.togglePref('showRealFps')">
              <div class="knob"></div>
            </div>
          </div>
        </div>

        <div class="setting-row">
          <div>
            <div class="setting-title">Graphics Renderer</div>
            <div class="setting-sub">OpenGL / Vulkan backend engine for Minecraft</div>
          </div>
          <select class="prism-select" onchange="CloudUI.setRenderer(this.value)">
            ${rendererOpts}
          </select>
        </div>

        <div class="setting-row">
          <div>
            <div class="setting-title">Max Refresh Rate (120Hz Display Unlock)</div>
            <div class="setting-sub">Unlocks 90Hz / 120Hz / 144Hz high-refresh display mode</div>
          </div>
          <div class="prism-switch ${d.unlock120Hz ? 'on' : ''}" onclick="CloudUI.togglePref('unlock120Hz')">
            <div class="knob"></div>
          </div>
        </div>

        <div class="setting-row">
          <div style="min-width:165px;">
            <div class="setting-title">Resolution Scale (${d.scaleFactor}%)</div>
            <div class="setting-sub">Lower scale boosts FPS massively on 4GB/6GB GPUs</div>
          </div>
          <input type="range" class="prism-slider" min="35" max="100" step="5" value="${d.scaleFactor}"
                 onchange="CloudUI.setScale(this.value)">
          <div style="font-weight:900;font-size:13px;min-width:54px;text-align:right;">${d.scaleFactor}%</div>
        </div>

        <div class="setting-row">
          <div style="flex:1;">
            <div class="setting-title">JVM Launch Arguments</div>
            <input class="prism-input" style="width:100%;margin-top:5px;" value="${esc(d.jvmArgs)}"
                   onchange="CloudUI.setJvmArgs(this.value)">
          </div>
        </div>
      `;
    } else if (cat === "appearance") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">Prism Iridescent Light Theme</div>
            <div class="setting-sub">Signature Cloud Launcher Frosted White + Pastel Sky Aurora UI</div>
          </div>
          <span class="dl-badge">ACTIVE</span>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Edge-to-Edge Fullscreen Window</div>
            <div class="setting-sub">Draw seamlessly across camera cutout &amp; system bars</div>
          </div>
          <div class="prism-switch ${d.fullScreen ? 'on' : ''}" onclick="CloudUI.togglePref('fullScreen')">
            <div class="knob"></div>
          </div>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">3D Operator Studio Skin Preset</div>
            <div class="setting-sub">Cycle built-in HD Prism skins or equip your own .PNG</div>
          </div>
          <div style="display:flex;gap:6px;">
            <button class="mini-btn" onclick="CloudUI.cycleSkin()">NEXT PRESET</button>
            <button class="mini-btn primary" onclick="CloudUI.pickSkinFile()">IMPORT .PNG</button>
          </div>
        </div>
      `;
    } else if (cat === "controls") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">Customize Touch Controls Rig</div>
            <div class="setting-sub">Open visual layout editor for WASD, Attack, Jump, GUI &amp; Macros</div>
          </div>
          <button class="mini-btn primary" onclick="CloudUI.openControlsRig()">OPEN EDITOR</button>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Hardware Controller &amp; Gamepad Remapper</div>
            <div class="setting-sub">Configure Bluetooth or USB-C gamepads</div>
          </div>
          <button class="mini-btn primary" onclick="CloudUI.openGamepad()">MAP GAMEPAD</button>
        </div>
      `;
    } else if (cat === "client") {
      pane.innerHTML = buildClientModulesHtml();
    } else {
      pane.innerHTML = `
        <div class="setting-row">
          <div style="display:flex;align-items:center;gap:12px;">
            <div class="brand-logo-box" style="width:48px;height:48px;"><img src="logo.png" alt="Cloud Launcher"></div>
            <div>
              <div class="setting-title">CLOUD LAUNCHER · Official Prism Edition</div>
              <div class="setting-sub">Version ${esc(d.versionName)} · Package ${esc(d.packageName)}</div>
            </div>
          </div>
          <span class="dl-badge">VERIFIED</span>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Standalone Android Package Architecture</div>
            <div class="setting-sub">Installs cleanly alongside any other launcher with independent settings, Real FPS Display HUD, and Prism Light UI.</div>
          </div>
        </div>
      `;
    }
  }

  function buildClientModulesHtml() {
    const d = state.data;
    const mods = [
      ["showRealFps", "Real FPS Display Box (Top-Left)", "Single box showing Fps, Renderer & Ms with Green / Yellow / Red dynamic colors"],
      ["keystrokes", "RGB Keystrokes Overlay", "Shows live W / A / S / D / LMB / RMB badges in gameplay"],
      ["cpsCounter", "Live CPS Counter", "Displays real-time clicks-per-second inside the HUD"],
      ["toggleSprint", "Toggle Sprint Lock", "1-tap sprint indicator for competitive PvP"],
      ["zoomHud", "Quick Optic Zoom Pill", "Instant C-key zoom button on the gameplay HUD"],
      ["crosshair", "Precision PvP Crosshair", "Crisp center crosshair overlay"]
    ];
    return mods.map(([key, title, sub]) => `
      <div class="setting-row">
        <div>
          <div class="setting-title">${esc(title)}</div>
          <div class="setting-sub">${esc(sub)}</div>
        </div>
        <div class="prism-switch ${d[key] ? 'on' : ''}" onclick="CloudUI.togglePref('${key}')">
          <div class="knob"></div>
        </div>
      </div>
    `).join("");
  }

  function renderClientAndAccounts() {
    document.getElementById("client-modules-list").innerHTML = buildClientModulesHtml();
    const listEl = document.getElementById("accounts-list");
    const accs = state.data.accounts || [];
    if (!accs.length) {
      listEl.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">No Saved Account Yet</div>
            <div class="setting-sub">Sign in with Microsoft or enter an Offline username above to play immediately!</div>
          </div>
        </div>`;
      return;
    }
    listEl.innerHTML = accs.map((a, idx) => `
      <div class="setting-row" style="${a.selected ? 'border-color:#38bdf8;background:rgba(240,249,255,0.92);' : ''}">
        <div>
          <div class="setting-title">${esc(a.username)} ${a.selected ? '· <span style="color:#10b981;">ACTIVE</span>' : ''}</div>
          <div class="setting-sub">${esc(a.type)}</div>
        </div>
        <div style="display:flex;gap:5px;">
          ${!a.selected ? `<button class="mini-btn primary" onclick="CloudUI.selectAccount(${idx})">USE</button>` : ''}
          <button class="mini-btn danger" onclick="CloudUI.deleteAccount(${idx})">✕</button>
        </div>
      </div>
    `).join("");
  }

  function renderRealmsAndCrew() {
    const rg = document.getElementById("realms-grid");
    rg.innerHTML = state.servers.map((s, idx) => `
      <div class="item-card">
        <div>
          <div class="setting-title">🌐 ${esc(s.name)}</div>
          <div class="setting-sub">${esc(s.address)} · ${esc(s.mode || "Multiplayer")}</div>
        </div>
        <div class="item-actions">
          <button class="mini-btn primary" onclick="CloudUI.joinServer('${esc(s.address)}')">► JOIN SERVER</button>
          ${idx >= DEFAULT_SERVERS.length ? `<button class="mini-btn danger" onclick="CloudUI.removeServer(${idx})">DELETE</button>` : ''}
        </div>
      </div>
    `).join("");

    const cc = document.getElementById("crew-cards");
    const tag = "CLOUD-" + Math.abs((state.data.accountName || "OP").split("").reduce((a, c) => a * 31 + c.charCodeAt(0), 7) % 9000 + 1000);
    cc.innerHTML = `
      <div class="item-card">
        <div class="setting-title">🪪 Your Cloud Squad ID</div>
        <div class="setting-sub">Operator: <b>${esc(state.data.accountName)}</b> · Invite Code: <b>${tag}</b></div>
        <button class="mini-btn primary" onclick="navigator.clipboard?.writeText('${tag}');CloudUI.toast('Copied Squad Code ${tag}!')">COPY INVITE CODE</button>
      </div>
      <div class="item-card">
        <div class="setting-title">⚡ Direct LAN / Party Connect</div>
        <div class="setting-sub">Jump straight into any friend's server IP with your active instance</div>
        <button class="mini-btn primary" onclick="CloudUI.go('realms')">OPEN REALMS HUB</button>
      </div>
    `;
  }

  async function searchModrinth(projectType, query, targetGridId, subfolder) {
    const grid = document.getElementById(targetGridId);
    grid.innerHTML = `<div class="setting-row"><div class="setting-title">Searching Modrinth ${projectType}s...</div></div>`;
    try {
      const facets = encodeURIComponent(JSON.stringify([[`project_type:${projectType}`]]));
      const url = `https://api.modrinth.com/v2/search?query=${encodeURIComponent(query || "")}&limit=16&facets=${facets}`;
      const res = await fetch(url);
      const data = await res.json();
      const hits = data.hits || [];
      if (!hits.length) {
        grid.innerHTML = `<div class="setting-row"><div class="setting-title">No results found for "${esc(query)}"</div></div>`;
        return;
      }
      grid.innerHTML = hits.map(h => `
        <div class="item-card">
          <div class="item-card-head">
            ${h.icon_url ? `<img class="item-icon" src="${esc(h.icon_url)}" alt="">` : `<div class="item-icon">🧊</div>`}
            <div style="min-width:0;flex:1;">
              <div class="setting-title" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(h.title)}</div>
              <div class="setting-sub">⬇ ${(h.downloads || 0).toLocaleString()} · by ${esc(h.author || "Creator")}</div>
            </div>
          </div>
          <div class="setting-sub" style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">${esc(h.description || "")}</div>
          <div class="item-actions">
            <button class="mini-btn primary" onclick="CloudUI.installModrinthProject('${esc(h.project_id)}', '${esc(h.title)}', '${subfolder}')">⬇ INSTALL TO INSTANCE</button>
          </div>
        </div>
      `).join("");
    } catch (e) {
      grid.innerHTML = `
        <div class="item-card">
          <div class="setting-title">Offline / Network Restricted</div>
          <div class="setting-sub">Connect to Wi-Fi / Mobile Data to browse live Modrinth ${esc(projectType)}s, or use RUN .JAR /Installed Mods.</div>
        </div>`;
    }
  }

  function renderAll() {
    renderTopAndHQ();
    renderVault();
    renderForgeVersions();
    renderSettings();
    renderClientAndAccounts();
    renderRealmsAndCrew();
  }

  window.CloudUI = {
    toast,
    syncFromNative: syncFromBridge,
    updateDownloadProgress: function (active, title, pct) {
      state.data.taskActive = !!active;
      state.data.taskTitle = title || "";
      state.data.taskPct = pct || 0;
      renderTopAndHQ();
    },
    onBackPressed: function () {
      if (state.screen !== "hq") {
        this.go("hq");
        return true;
      }
      return false;
    },
    go: function (scr) {
      state.screen = scr;
      document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
      const target = document.getElementById("screen-" + scr);
      if (target) target.classList.add("active");

      document.querySelectorAll(".dock-btn").forEach(b => {
        const d = b.getAttribute("data-dock");
        b.classList.toggle("active", d === scr || (scr === "forge" && d === "forge2"));
      });
      document.querySelectorAll(".h-pill").forEach(b => {
        b.classList.toggle("active", b.getAttribute("data-nav") === scr);
      });

      if (scr === "armory" && !document.getElementById("armory-grid").children.length) {
        this.searchMods();
      } else if (scr === "packs" && !document.getElementById("packs-grid").children.length) {
        this.searchPacks();
      } else if (scr === "files") {
        this.browseSub("");
      } else if (scr === "console") {
        this.loadConsole();
      } else if (scr === "hq") {
        setTimeout(() => { if (!state.viewer) init3DSkin(); }, 40);
      }
    },
    setCenterMode: function (mode) {
      document.getElementById("tab-reactor").classList.toggle("active", mode === "reactor");
      document.getElementById("tab-deck").classList.toggle("active", mode === "deck");
      document.getElementById("reactor-split-view").style.display = mode === "reactor" ? "grid" : "none";
      document.getElementById("command-deck-view").style.display = mode === "deck" ? "flex" : "none";
    },
    cycleSkin: function () {
      state.data.skinDataUrl = "";
      state.skinIdx = (state.skinIdx + 1) % BUILTIN_SKINS.length;
      updateSkinTexture();
      toast("Equipped Cloud Wardrobe Skin #" + (state.skinIdx + 1));
    },
    pickSkinFile: function () {
      if (bridge()) bridge().pickSkinFile();
    },
    launchGame: function () {
      if (bridge()) {
        const msg = bridge().launchGame();
        if (msg) toast(msg);
      } else {
        toast("Launching " + state.data.selectedInstance + "...");
      }
    },
    selectInstance: function (idx) {
      if (bridge()) bridge().selectInstance(idx);
      syncFromBridge();
      toast("Selected instance!");
    },
    selectAndLaunch: function (idx) {
      if (bridge()) {
        bridge().selectInstance(idx);
        const msg = bridge().launchGame();
        if (msg) toast(msg);
      }
    },
    quickCreateDefault: function () {
      if (bridge()) {
        bridge().createInstance("VANILLA", "1.21.4", "Cloud 1.21.4");
        setTimeout(syncFromBridge, 400);
        toast("Created Cloud 1.21.4!");
      }
    },
    renameInstance: function (idx) {
      const cur = (state.data.instances[idx] || {}).name || "";
      const next = prompt("New Instance Name:", cur);
      if (next && next.trim() && bridge()) {
        bridge().renameInstance(idx, next.trim());
        syncFromBridge();
      }
    },
    cloneInstance: function (idx) {
      const cur = (state.data.instances[idx] || {}).name || "Instance";
      const next = prompt("Clone Name:", cur + " Copy");
      if (next && next.trim() && bridge()) {
        bridge().cloneInstance(idx, next.trim());
        setTimeout(syncFromBridge, 400);
      }
    },
    deleteInstance: function (idx) {
      const cur = (state.data.instances[idx] || {}).name || "this instance";
      if (confirm("Delete " + cur + "?")) {
        if (bridge()) bridge().deleteInstance(idx);
        syncFromBridge();
      }
    },
    setForgeLoader: function (loader, vtype) {
      state.forgeLoader = loader;
      state.forgeType = vtype;
      document.querySelectorAll("#forge-loader-rail .rail-btn").forEach(b => {
        const key = vtype === "SNAPSHOT" ? "SNAPSHOT" : loader;
        b.classList.toggle("active", b.getAttribute("data-loader") === key);
      });
      const prefix = loader === "VANILLA" ? "Cloud" : ("Cloud " + loader.charAt(0) + loader.slice(1).toLowerCase());
      document.getElementById("forge-inst-name").value = prefix + " " + state.selectedMcVer;
      renderForgeVersions();
    },
    pickMcVersion: function (ver) {
      state.selectedMcVer = ver;
      const prefix = state.forgeLoader === "VANILLA" ? "Cloud" : ("Cloud " + state.forgeLoader.charAt(0) + state.forgeLoader.slice(1).toLowerCase());
      document.getElementById("forge-inst-name").value = prefix + " " + ver;
      renderForgeVersions();
    },
    installSelectedVersion: function () {
      const name = (document.getElementById("forge-inst-name").value || ("Cloud " + state.selectedMcVer)).trim();
      if (bridge()) {
        toast("Installing " + name + "...");
        bridge().createInstance(state.forgeLoader, state.selectedMcVer, name);
      }
    },
    searchMods: function () {
      const q = document.getElementById("armory-query").value || "";
      searchModrinth("mod", q, "armory-grid", "mods");
    },
    showInstalledMods: function () {
      if (!bridge()) return;
      const items = JSON.parse(bridge().listFilesJson("mods") || "[]");
      const grid = document.getElementById("armory-grid");
      if (!items.length) {
        grid.innerHTML = `<div class="item-card"><div class="setting-title">No .JAR mods installed in active instance yet</div><div class="setting-sub">Search Modrinth above to install mods in 1 tap!</div></div>`;
        return;
      }
      grid.innerHTML = items.map(f => `
        <div class="item-card">
          <div class="setting-title">🧩 ${esc(f.name)}</div>
          <div class="setting-sub">${esc(f.size)}</div>
          <div class="item-actions">
            <button class="mini-btn danger" onclick="CloudUI.deleteModFile('${esc(f.name)}')">DELETE</button>
          </div>
        </div>
      `).join("");
    },
    deleteModFile: function (name) {
      if (bridge()) {
        bridge().deleteRelativeFile("mods/" + name);
        this.showInstalledMods();
      }
    },
    setPackKind: function (kind) {
      state.packKind = kind;
      document.getElementById("pack-tab-shader").classList.toggle("primary", kind === "shader");
      document.getElementById("pack-tab-rp").classList.toggle("primary", kind === "resourcepack");
      this.searchPacks();
    },
    searchPacks: function () {
      const q = document.getElementById("packs-query").value || "";
      const folder = state.packKind === "shader" ? "shaderpacks" : "resourcepacks";
      searchModrinth(state.packKind, q, "packs-grid", folder);
    },
    installModrinthProject: async function (projectId, title, subfolder) {
      try {
        toast("Resolving " + title + "...");
        const res = await fetch(`https://api.modrinth.com/v2/project/${encodeURIComponent(projectId)}/version`);
        const versions = await res.json();
        if (!Array.isArray(versions) || !versions.length) {
          toast("No downloadable file found");
          return;
        }
        const mcVer = state.data.selectedVersion || "1.21.4";
        const match = versions.find(v => (v.game_versions || []).includes(mcVer)) || versions[0];
        const file = (match.files || [])[0];
        if (!file || !file.url) {
          toast("No file URL available");
          return;
        }
        if (bridge()) {
          bridge().downloadUrlToSubfolder(file.url, file.filename, subfolder);
          toast("Downloading " + file.filename + " -> " + subfolder + "/");
        }
      } catch (e) {
        toast("Download error: " + e.message);
      }
    },
    setSettingsCat: function (cat) {
      state.settingsCat = cat;
      renderSettings();
    },
    applyPreset: function (p) {
      if (bridge()) bridge().applyPreset(p);
      syncFromBridge();
      toast("Applied " + p.toUpperCase() + " Cloud FPS Boost Preset!");
    },
    setRam: function (mb) {
      if (bridge()) bridge().setRamMb(parseInt(mb, 10));
      syncFromBridge();
      toast("RAM Allocation set to " + mb + " MB");
    },
    setRenderer: function (id) {
      if (bridge()) bridge().setRenderer(id);
      syncFromBridge();
      toast("Renderer updated!");
    },
    setScale: function (pct) {
      if (bridge()) bridge().setIntPref("resolutionRatio", parseInt(pct, 10));
      syncFromBridge();
      toast("Resolution Scale: " + pct + "%");
    },
    setJvmArgs: function (args) {
      if (bridge()) bridge().setStringPref("javaArgs", args);
      syncFromBridge();
      toast("Saved JVM Arguments");
    },
    togglePref: function (key) {
      state.data[key] = !state.data[key];
      if (bridge()) bridge().setToggle(key, !!state.data[key]);
      syncFromBridge();
    },
    microsoftLogin: function () {
      if (bridge()) bridge().microsoftLogin();
    },
    createOffline: function () {
      const inp = document.getElementById("offline-username");
      const name = (inp.value || "").trim();
      if (!name) {
        toast("Enter an Offline username first!");
        return;
      }
      if (bridge()) {
        bridge().createOfflineAccount(name);
        inp.value = "";
        setTimeout(syncFromBridge, 250);
        toast("Signed in as " + name + "!");
      }
    },
    selectAccount: function (idx) {
      if (bridge()) bridge().selectAccount(idx);
      syncFromBridge();
    },
    deleteAccount: function (idx) {
      if (bridge()) bridge().deleteAccount(idx);
      syncFromBridge();
    },
    addServer: function () {
      const n = (document.getElementById("srv-name").value || "").trim();
      const a = (document.getElementById("srv-addr").value || "").trim();
      if (!a) { toast("Enter a server IP/domain"); return; }
      state.servers.push({ name: n || a, address: a, mode: "Custom Realm" });
      localStorage.setItem("cloud_servers_v1", JSON.stringify(state.servers));
      document.getElementById("srv-name").value = "";
      document.getElementById("srv-addr").value = "";
      renderRealmsAndCrew();
    },
    removeServer: function (idx) {
      state.servers.splice(idx, 1);
      localStorage.setItem("cloud_servers_v1", JSON.stringify(state.servers));
      renderRealmsAndCrew();
    },
    joinServer: function (addr) {
      if (bridge()) {
        const msg = bridge().joinServer(addr);
        if (msg) toast(msg);
      }
    },
    openControlsRig: function () {
      if (bridge()) bridge().openControlsRig();
    },
    openGamepad: function () {
      if (bridge()) bridge().openGamepad();
    },
    runJarInstaller: function () {
      if (bridge()) bridge().runJarInstaller();
    },
    clearCache: function () {
      if (bridge()) bridge().clearCache();
      toast("Cleared metadata & temp cache!");
    },
    openDiscord: function () {
      if (bridge()) bridge().openUrl("https://discord.gg/RS7q9KaCm6");
    },
    browseSub: function (sub) {
      document.getElementById("files-current-path").textContent = ".minecraft/" + (sub || "");
      const el = document.getElementById("files-list");
      if (!bridge()) return;
      const items = JSON.parse(bridge().listFilesJson(sub) || "[]");
      if (!items.length) {
        el.innerHTML = `<div class="setting-row"><div class="setting-title">Folder is empty</div></div>`;
        return;
      }
      el.innerHTML = items.map(f => `
        <div class="setting-row" style="padding:7px 12px;">
          <div style="display:flex;align-items:center;gap:8px;">
            <span>${f.isDir ? "📁" : "📄"}</span>
            <div>
              <div class="setting-title" style="font-size:12px;">${esc(f.name)}</div>
              <div class="setting-sub">${esc(f.size)}</div>
            </div>
          </div>
          ${f.isDir
            ? `<button class="mini-btn" onclick="CloudUI.browseSub('${esc((sub ? sub + '/' : '') + f.name)}')">OPEN</button>`
            : `<button class="mini-btn danger" onclick="CloudUI.deleteFileInExplorer('${esc(sub)}', '${esc(f.name)}')">DELETE</button>`}
        </div>
      `).join("");
    },
    deleteFileInExplorer: function (sub, name) {
      if (confirm("Delete " + name + "?") && bridge()) {
        bridge().deleteRelativeFile((sub ? sub + "/" : "") + name);
        this.browseSub(sub);
      }
    },
    openNativeFolder: function () {
      if (bridge()) bridge().openGameFolder();
    },
    loadConsole: function () {
      if (bridge()) {
        document.getElementById("console-output").textContent = bridge().getConsoleLog() || "No log output yet.";
      }
    },
    shareLogs: function () {
      if (bridge()) bridge().shareLogs();
    },
    refreshAll: function () {
      if (bridge()) bridge().reloadBackend();
      syncFromBridge();
      toast("Refreshed instances & accounts");
    }
  };

  window.addEventListener("DOMContentLoaded", function () {
    syncFromBridge();
    setTimeout(init3DSkin, 80);
    setInterval(function () {
      const b = bridge();
      if (!b) return;
      try {
        const p = JSON.parse(b.getProgressJson());
        if (p.active !== state.data.taskActive || p.pct !== state.data.taskPct || p.title !== state.data.taskTitle) {
          window.CloudUI.updateDownloadProgress(p.active, p.title, p.pct);
        }
      } catch (e) {}
    }, 800);
  });
})();
