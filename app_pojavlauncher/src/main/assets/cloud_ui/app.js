(function () {
  const bridge = () => (typeof window.CloudBridge !== "undefined" ? window.CloudBridge : null);

  const PRESET_SKINS = [
    { name: "Prism Steve", url: "skins/steve.png", slim: false },
    { name: "Prism Alex", url: "skins/alex.png", slim: true }
  ];

  const FALLBACK_FABRIC_BUILDS = [
    { id: "0.16.10", label: "Fabric Loader 0.16.10", stable: true },
    { id: "0.16.9", label: "Fabric Loader 0.16.9", stable: true },
    { id: "0.16.7", label: "Fabric Loader 0.16.7", stable: true },
    { id: "0.16.5", label: "Fabric Loader 0.16.5", stable: true },
    { id: "0.15.11", label: "Fabric Loader 0.15.11", stable: true },
    { id: "0.14.25", label: "Fabric Loader 0.14.25", stable: true }
  ];

  const FALLBACK_QUILT_BUILDS = [
    { id: "0.27.1", label: "Quilt Loader 0.27.1", stable: true },
    { id: "0.26.4", label: "Quilt Loader 0.26.4", stable: true },
    { id: "0.25.0", label: "Quilt Loader 0.25.0", stable: true }
  ];

  const state = {
    screen: "hq",
    settingsCat: "fps",
    versionFilter: "RELEASE",
    selectedMcVersion: "1.21.4",
    modloader: "FABRIC",
    selectedLoaderBuild: "0.16.10",
    loaderBuilds: FALLBACK_FABRIC_BUILDS.slice(),
    loadingBuilds: false,
    slimModel: false,
    skinWalking: true,
    packKind: "shader",
    skinIdx: 0,
    customSkinData: localStorage.getItem("cloud_custom_skin") || "",
    preset: localStorage.getItem("cloud_fps_preset") || "6gb",
    accounts: [],
    selectedAccount: null,
    instances: [],
    selectedInstance: null,
    versions: [],
    settings: {
      ramMb: 2816,
      maxRamMb: 8192,
      renderer: "opengles2",
      renderers: [
        { id: "opengles2", name: "MobileGlues (Vulkan 1.3 / OpenGL 4.6)" },
        { id: "vulkan_zink", name: "Mesa Zink (Vulkan Desktop GL)" },
        { id: "opengles3_ltw", name: "LTW GLES 3.2 Translator" }
      ],
      resolutionScale: 85,
      sustainedPerf: true,
      jvmArgs: "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=40",
      fpsBoxEnabled: true,
      keystrokes: true,
      cpsCounter: true,
      toggleSprint: true,
      zoomOverlay: true,
      crosshair: false
    },
    servers: JSON.parse(localStorage.getItem("cloud_servers") || JSON.stringify([
      { name: "Hypixel Network", addr: "mc.hypixel.net", tag: "Official Account · Minigames" },
      { name: "MCC Island", addr: "play.mccisland.net", tag: "1.20+ · Fast Arenas" },
      { name: "Hoplite", addr: "hoplite.gg", tag: "1.20+ · Battle Royale" },
      { name: "PvP Legacy", addr: "play.pvplegacy.net", tag: "1.20+ · Duels" }
    ])),
    musicSongs: [],
    musicLastQuery: "Top Hindi & Global Hits",
    musicLoading: false,
    musicSystemEnabled: localStorage.getItem("cloud_music_enabled") !== "0",
    musicStatus: {
      playing: false,
      preparing: false,
      songId: "",
      title: "",
      artist: "",
      artwork: ""
    },
    cloudClient: {
      selectedVersion: "1.21.11",
      installing: false,
      installingVersion: "",
      progress: 100,
      status: "CLOUD CLIENT INSTALLER · Auto-Installing Sodium, Lithium, FPS Display + 6 Boost Mods for 1.21.11",
      hudDrawerOpen: false,
      versions: [
        "26.3", "26.2", "26.1.2", "26.1.1", "26.1",
        "1.21.11", "1.21.10", "1.21.9", "1.21.8", "1.21.7",
        "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1"
      ].map(v => ({ version: v, name: "Cloud Client " + v, installed: false, active: false, modsCount: 0 })),
      mods: [
        { key: "sodium", title: "Sodium (+300% FPS)" },
        { key: "lithium", title: "Lithium (Physics & Tick Boost)" },
        { key: "fpsdisplay", title: "FPS Display (Live FPS HUD)" },
        { key: "ferrite-core", title: "FerriteCore (-45% RAM)" },
        { key: "immediatelyfast", title: "ImmediatelyFast" },
        { key: "entityculling", title: "EntityCulling" },
        { key: "moreculling", title: "MoreCulling" },
        { key: "dynamic-fps", title: "Dynamic FPS" },
        { key: "fabric-api", title: "Fabric API" }
      ]
    }
  };

  let hqSkinViewer = null;
  let accSkinViewer = null;
  let webFallbackAudio = null;
  let musicSearchTimer = null;

  function toast(msg) {
    const el = document.getElementById("cloud-toast");
    if (!el) return;
    el.textContent = msg;
    el.style.display = "block";
    clearTimeout(toast._t);
    toast._t = setTimeout(() => { el.style.display = "none"; }, 2800);
  }

  function safeParse(raw, fallback) {
    try { return raw ? JSON.parse(raw) : fallback; } catch (_) { return fallback; }
  }

  function refreshStateFromNative() {
    const nb = bridge();
    if (!nb) {
      if (!state.versions.length) {
        state.versions = [
          { id: "1.21.4", type: "RELEASE" },
          { id: "1.21.3", type: "RELEASE" },
          { id: "1.21.1", type: "RELEASE" },
          { id: "1.20.6", type: "RELEASE" },
          { id: "1.20.4", type: "RELEASE" },
          { id: "1.20.1", type: "RELEASE" },
          { id: "1.19.4", type: "RELEASE" },
          { id: "1.18.2", type: "RELEASE" },
          { id: "1.16.5", type: "RELEASE" },
          { id: "1.12.2", type: "RELEASE" },
          { id: "1.8.9", type: "RELEASE" },
          { id: "25w09b", type: "SNAPSHOT" },
          { id: "24w45a", type: "SNAPSHOT" }
        ];
      }
      if (!state.instances.length) {
        state.instances = [
          { index: 0, name: "Fabric 1.21.4", versionId: "fabric-loader-0.16.10-1.21.4", selected: true }
        ];
        state.selectedInstance = state.instances[0];
      }
      if (!state.accounts.length) {
        state.accounts = [{ index: 0, username: "Cloud_OP", isMicrosoft: false, selected: true }];
        state.selectedAccount = state.accounts[0];
      }
      return;
    }

    const snap = safeParse(nb.getStateJson(), {});
    if (Array.isArray(snap.accounts)) {
      state.accounts = snap.accounts.map((a, idx) => ({
        index: typeof a.index === "number" ? a.index : idx,
        username: a.username || "Cloud_OP",
        isMicrosoft: !!(a.isMicrosoft || String(a.type || "").toUpperCase().includes("MSA") || String(a.type || "").toUpperCase().includes("MICROSOFT")),
        selected: !!a.selected
      }));
    }
    state.selectedAccount = state.accounts.find(a => a.selected) || state.accounts[0] || null;

    if (snap.skinDataUrl) {
      state.customSkinData = snap.skinDataUrl;
    }

    if (Array.isArray(snap.instances)) {
      state.instances = snap.instances.map((inst, idx) => ({
        index: typeof inst.index === "number" ? inst.index : idx,
        name: inst.name || "Instance",
        versionId: inst.versionId || "1.21.4",
        isCloudClient: !!(inst.isCloudClient || String(inst.name || "").toLowerCase().startsWith("cloud client")),
        selected: !!inst.selected
      }));
    }
    state.selectedInstance = state.instances.find(i => i.selected) || state.instances[0] || null;

    if (nb.getCloudClientCatalogJson) {
      const cat = safeParse(nb.getCloudClientCatalogJson(), null);
      if (cat && typeof cat === "object") {
        if (Array.isArray(cat.versions) && cat.versions.length) {
          state.cloudClient.versions = cat.versions;
          const actVer = cat.versions.find(v => v.active);
          if (actVer && !state.cloudClient._userPicked) {
            state.cloudClient.selectedVersion = actVer.version;
          }
        }
        if (Array.isArray(cat.mods) && cat.mods.length) {
          state.cloudClient.mods = cat.mods;
        }
        state.cloudClient.installing = !!cat.installing;
        if (cat.installingVersion) state.cloudClient.installingVersion = cat.installingVersion;
        if (cat.installing && cat.status) state.cloudClient.status = cat.status;
        if (typeof cat.progress === "number" && cat.installing) state.cloudClient.progress = cat.progress;
      }
    }

    const vers = safeParse(nb.getVersionsJson(), []);
    if (Array.isArray(vers) && vers.length) {
      state.versions = vers;
    }

    if (typeof snap.ramMb === "number") state.settings.ramMb = snap.ramMb;
    if (typeof snap.maxRamMb === "number") state.settings.maxRamMb = snap.maxRamMb;
    if (snap.renderer) state.settings.renderer = snap.renderer;
    if (Array.isArray(snap.renderers) && snap.renderers.length) {
      state.settings.renderers = snap.renderers.map(r => ({ id: r.id, name: r.name || r.label || r.id }));
    }
    if (typeof snap.scaleFactor === "number") state.settings.resolutionScale = snap.scaleFactor;
    if (typeof snap.sustainedPerf === "boolean") state.settings.sustainedPerf = snap.sustainedPerf;
    if (typeof snap.jvmArgs === "string") state.settings.jvmArgs = snap.jvmArgs;
    if (typeof snap.showRealFps === "boolean") state.settings.fpsBoxEnabled = snap.showRealFps;
    if (typeof snap.keystrokes === "boolean") state.settings.keystrokes = snap.keystrokes;
    if (typeof snap.cpsCounter === "boolean") state.settings.cpsCounter = snap.cpsCounter;
    if (typeof snap.toggleSprint === "boolean") state.settings.toggleSprint = snap.toggleSprint;
    if (typeof snap.zoomHud === "boolean") state.settings.zoomOverlay = snap.zoomHud;
    if (typeof snap.crosshair === "boolean") state.settings.crosshair = snap.crosshair;
    if (snap.preset) state.preset = snap.preset;
  }

  function activeSkinUrl() {
    return state.customSkinData || PRESET_SKINS[state.skinIdx % PRESET_SKINS.length].url;
  }

  function createOrUpdateSkinViewer(hostId, currentViewer, w, h) {
    const host = document.getElementById(hostId);
    if (!host) return null;
    if (typeof window.skinview3d !== "undefined") {
      try {
        if (!currentViewer) {
          host.innerHTML = "";
          const canvas = document.createElement("canvas");
          host.appendChild(canvas);
          currentViewer = new window.skinview3d.SkinViewer({
            canvas: canvas,
            width: w,
            height: h,
            skin: activeSkinUrl(),
            model: state.slimModel ? "slim" : "default"
          });
          currentViewer.fov = 48;
          currentViewer.zoom = 0.88;
          currentViewer.autoRotate = true;
          currentViewer.autoRotateSpeed = 0.55;
          if (window.skinview3d.WalkingAnimation) {
            currentViewer.animation = new window.skinview3d.WalkingAnimation();
            currentViewer.animation.speed = 0.55;
          }
        } else {
          currentViewer.loadSkin(activeSkinUrl(), { model: state.slimModel ? "slim" : "default" });
          currentViewer.animationPaused = !state.skinWalking;
        }
        return currentViewer;
      } catch (_) {}
    }
    host.innerHTML = `<img src="${activeSkinUrl()}" style="height:135px;image-rendering:pixelated;">`;
    return null;
  }

  function init3DSkins() {
    hqSkinViewer = createOrUpdateSkinViewer("hq-skin-host", hqSkinViewer, 205, 225);
    if (state.screen === "accounts") {
      accSkinViewer = createOrUpdateSkinViewer("acc-skin-host", accSkinViewer, 210, 235);
    }
    renderTopAvatar();
  }

  function renderTopAvatar() {
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
    img.src = activeSkinUrl();
  }

  function renderHeaderAndHQ() {
    const accName = state.selectedAccount ? state.selectedAccount.username : "Cloud_OP";
    const accMode = state.selectedAccount
      ? (state.selectedAccount.isMicrosoft ? "· MICROSOFT ID" : "· OFFLINE ID")
      : "· TAP TO SIGN IN";
    document.getElementById("top-account-name").textContent = accName;
    document.getElementById("top-account-mode").textContent = accMode;

    const subMap = {
      hq: "",
      forge: "· VERSION STUDIO",
      accounts: "· ACCOUNT STUDIO",
      music: "· MUSIC SYSTEM",
      vault: "· INSTANCE VAULT",
      armory: "· MOD ARMORY",
      packs: "· SHADERS & PACKS",
      client: "· CLOUD CLIENT",
      config: "· SETTINGS"
    };
    document.getElementById("header-sub-tag").textContent = subMap[state.screen] || "";

    const instName = state.selectedInstance ? state.selectedInstance.name : "Install Version";
    const instVer = state.selectedInstance ? state.selectedInstance.versionId : "Tap + FORGE to create";
    const selIsCC = !!(state.selectedInstance && (state.selectedInstance.isCloudClient || String(instName).toLowerCase().startsWith("cloud client")));
    const ccLogoHtml = selIsCC ? `<img src="logo.png" class="inline-cloud-logo" alt="">` : "";
    document.getElementById("top-inst-pill").innerHTML = ccLogoHtml + escapeHtml(instName);
    document.getElementById("hq-active-name").innerHTML = ccLogoHtml + escapeHtml(instName);
    document.getElementById("hq-active-ver").textContent = (selIsCC ? "Cloud Client · " : "Minecraft ") + instVer + " · Ready to Launch";

    const rObj = (state.settings.renderers || []).find(r => r.id === state.settings.renderer);
    const rShort = rObj ? rObj.name.split("(")[0].trim().toUpperCase() : "MOBILEGLUES";
    document.getElementById("hq-engine-status").textContent =
      `ENGINE READY · ${rShort} · ${state.settings.ramMb}MB RAM`;

    document.querySelectorAll(".preset-pill").forEach(b => {
      b.classList.toggle("active", b.dataset.preset === state.preset);
    });

    const rack = document.getElementById("hq-rack-list");
    if (rack) {
      if (!state.instances.length) {
        rack.innerHTML = `<div style="font-size:11px;color:#64748b;padding:8px;">No instances yet — tap <b>CLIENT</b> or <b>+ INSTALL VERSION</b>!</div>`;
      } else {
        rack.innerHTML = state.instances.map((inst, idx) => {
          const isCC = !!(inst.isCloudClient || String(inst.name || "").toLowerCase().startsWith("cloud client"));
          return `
          <div class="rack-item ${inst.selected ? "selected" : ""}" onclick="CloudUI.selectInstance(${idx})">
            <div class="rack-badge" style="${isCC ? "background:linear-gradient(135deg,#e0f2fe,#ede9fe);padding:2px;" : ""}">
              ${isCC ? `<img src="logo.png" alt="" style="width:20px;height:20px;object-fit:contain;">` : escapeHtml((inst.name || "M").slice(0, 2).toUpperCase())}
            </div>
            <div class="rack-name">
              ${isCC ? `<img src="logo.png" class="inline-cloud-logo" alt="">` : ""}${escapeHtml(inst.name)}
              ${isCC ? `<span class="cc-badge" style="font-size:8px;padding:1px 6px;margin-left:4px;">CLOUD CLIENT</span>` : `<span style="color:#64748b;font-weight:600;font-size:10px;">(${escapeHtml(inst.versionId)})</span>`}
            </div>
            <span style="font-size:10px;font-weight:800;color:#0284c7;">${inst.selected ? "ACTIVE" : "SELECT"}</span>
          </div>
        `;
        }).join("");
      }
    }
  }

  function updateDefaultInstanceName() {
    const inp = document.getElementById("forge-inst-name");
    if (!inp) return;
    const prefixMap = {
      VANILLA: "Minecraft",
      FABRIC: "Fabric",
      FORGE: "Forge",
      NEOFORGE: "NeoForge",
      QUILT: "Quilt"
    };
    const nextName = `${prefixMap[state.modloader] || state.modloader} ${state.selectedMcVersion}`;
    inp.value = nextName;
    const dirEl = document.getElementById("vs-dir-preview");
    if (dirEl) dirEl.textContent = `📁 /CloudLauncher/instances/${nextName}`;

    const minor = parseInt((state.selectedMcVersion.split(".")[1] || "21"), 10);
    const jChip = document.getElementById("vs-java-chip");
    if (jChip) jChip.textContent = minor >= 20 ? "☕ Java 21" : (minor >= 17 ? "☕ Java 17" : "☕ Java 8");
  }

  async function loadModloaderBuilds() {
    const loader = state.modloader;
    const mcVer = state.selectedMcVersion;

    if (loader === "VANILLA") {
      state.loaderBuilds = [{ id: "vanilla", label: `Official Mojang ${mcVer} (Pure Vanilla)`, stable: true }];
      state.selectedLoaderBuild = "vanilla";
      renderLoaderBuildsOnly();
      return;
    }

    if (loader === "FABRIC") {
      state.loaderBuilds = FALLBACK_FABRIC_BUILDS.slice();
      state.selectedLoaderBuild = state.loaderBuilds[0].id;
      renderLoaderBuildsOnly();
    } else if (loader === "QUILT") {
      state.loaderBuilds = FALLBACK_QUILT_BUILDS.slice();
      state.selectedLoaderBuild = state.loaderBuilds[0].id;
      renderLoaderBuildsOnly();
    } else {
      state.loadingBuilds = true;
      state.loaderBuilds = [];
      renderLoaderBuildsOnly();
    }

    const nb = bridge();
    if (nb && nb.fetchLoaderVersionsAsync) {
      nb.fetchLoaderVersionsAsync(loader, mcVer);
    }

    try {
      if (loader === "FABRIC") {
        const res = await fetch(`https://meta.fabricmc.net/v2/versions/loader/${encodeURIComponent(mcVer)}`);
        if (res.ok) {
          const arr = await res.json();
          if (Array.isArray(arr) && arr.length) {
            state.loaderBuilds = arr.slice(0, 28).map(x => ({
              id: x.loader.version,
              label: `Fabric Loader ${x.loader.version}`,
              stable: x.loader.stable !== false
            }));
            state.selectedLoaderBuild = (state.loaderBuilds.find(b => b.stable) || state.loaderBuilds[0]).id;
          }
        }
      } else if (loader === "QUILT") {
        const res = await fetch(`https://meta.quiltmc.org/v3/versions/loader/${encodeURIComponent(mcVer)}`);
        if (res.ok) {
          const arr = await res.json();
          if (Array.isArray(arr) && arr.length) {
            state.loaderBuilds = arr.slice(0, 24).map(x => ({
              id: x.loader.version,
              label: `Quilt Loader ${x.loader.version}`,
              stable: !String(x.loader.version).includes("beta")
            }));
            state.selectedLoaderBuild = state.loaderBuilds[0].id;
          }
        }
      }
    } catch (_) {}

    state.loadingBuilds = false;
    renderLoaderBuildsOnly();
  }

  function renderLoaderBuildsOnly() {
    const heading = document.getElementById("vs-loader-ver-heading");
    const listEl = document.getElementById("vs-loader-ver-list");
    if (!heading || !listEl) return;

    const titleMap = {
      VANILLA: "🟩 OFFICIAL MOJANG RUNTIME",
      FABRIC: "🛡 FABRIC LOADER VERSION",
      FORGE: "⚒ FORGE LOADER VERSION",
      NEOFORGE: "🦊 NEOFORGE LOADER VERSION",
      QUILT: "🟪 QUILT LOADER VERSION"
    };
    heading.textContent = titleMap[state.modloader] || "LOADER VERSION";

    if (state.loadingBuilds && !state.loaderBuilds.length) {
      listEl.innerHTML = `<div style="padding:10px;font-size:11.5px;font-weight:700;color:#2563eb;">Fetching ${state.modloader} versions for ${escapeHtml(state.selectedMcVersion)}...</div>`;
      return;
    }

    if (!state.loaderBuilds.length) {
      listEl.innerHTML = `<div style="padding:10px;font-size:11px;color:#64748b;">No ${state.modloader} build published for Minecraft ${escapeHtml(state.selectedMcVersion)}. Pick 1.21.4, 1.21.1, or 1.20.1, or choose Fabric / Vanilla.</div>`;
      return;
    }

    listEl.innerHTML = state.loaderBuilds.map(b => {
      const sel = b.id === state.selectedLoaderBuild;
      return `
        <div class="ver-row ${sel ? "selected" : ""}" onclick="CloudUI.pickLoaderBuild('${escapeAttr(b.id)}')">
          <div class="ver-row-left">
            <span style="font-size:12px;">${escapeHtml(b.label || b.id)}</span>
            ${b.stable ? `<span class="tag-pill-blue">STABLE</span>` : ""}
          </div>
          ${sel ? `<span class="check-circle">✓</span>` : `<span style="color:#94a3b8;font-weight:900;">›</span>`}
        </div>
      `;
    }).join("");
  }

  function renderVersionStudio() {
    const rSel = document.getElementById("vs-renderer-select");
    if (rSel) {
      rSel.innerHTML = (state.settings.renderers || []).map(r =>
        `<option value="${escapeAttr(r.id)}" ${r.id === state.settings.renderer ? "selected" : ""}>${escapeHtml(r.name)}</option>`
      ).join("");
    }
    const ramSel = document.getElementById("vs-ram-select");
    if (ramSel) {
      ramSel.value = state.settings.ramMb >= 3800 ? "4096" : (state.settings.ramMb >= 2400 ? "2816" : "1792");
    }

    document.querySelectorAll("#vs-loader-tiles .loader-tile").forEach(tile => {
      const isAct = tile.dataset.ldr === state.modloader;
      tile.classList.toggle("active", isAct);
      const chip = tile.querySelector(".loader-active-chip");
      if (isAct && !chip) {
        const s = document.createElement("span");
        s.className = "loader-active-chip";
        s.textContent = "ACTIVE";
        tile.appendChild(s);
      } else if (!isAct && chip) {
        chip.remove();
      }
    });

    document.getElementById("vtype-release")?.classList.toggle("active", state.versionFilter === "RELEASE");
    document.getElementById("vtype-snapshot")?.classList.toggle("active", state.versionFilter === "SNAPSHOT");

    const q = (document.getElementById("forge-search")?.value || "").trim().toLowerCase();
    const list = state.versions.filter(v => {
      const matchType = v.type === state.versionFilter || (state.versionFilter === "RELEASE" && v.type === "INSTALLED");
      return matchType && (!q || v.id.toLowerCase().includes(q));
    });

    const mcHost = document.getElementById("vs-mc-list");
    if (mcHost) {
      mcHost.innerHTML = list.slice(0, 120).map(v => {
        const sel = v.id === state.selectedMcVersion;
        return `
          <div class="ver-row ${sel ? "selected" : ""}" onclick="CloudUI.pickMcVersion('${escapeAttr(v.id)}')">
            <div class="ver-row-left">
              <span>🟩</span>
              <span>${escapeHtml(v.id)}</span>
            </div>
            <div style="display:flex;align-items:center;gap:6px;">
              ${sel ? `<span class="tag-pill-blue">SELECTED</span><span class="check-circle">✓</span>` : `<span style="color:#94a3b8;font-weight:900;">›</span>`}
            </div>
          </div>
        `;
      }).join("");
    }

    renderLoaderBuildsOnly();
  }

  function renderAccountStudio() {
    const countEl = document.getElementById("saved-acc-count");
    if (countEl) countEl.textContent = `${state.accounts.length} Saved`;

    document.getElementById("model-classic")?.classList.toggle("active", !state.slimModel);
    document.getElementById("model-slim")?.classList.toggle("active", state.slimModel);

    const accHost = document.getElementById("accounts-list");
    if (accHost) {
      if (!state.accounts.length) {
        accHost.innerHTML = `<div style="padding:14px;border-radius:14px;background:rgba(255,255,255,0.85);font-size:11.5px;color:#475569;">No accounts added yet. Sign in with <b>Microsoft</b> or enter a username under <b>Create Offline Account</b> on the left!</div>`;
      } else {
        accHost.innerHTML = state.accounts.map((a, idx) => `
          <div class="saved-acc-card ${a.selected ? "active" : ""}">
            <div class="acc-face-box">${escapeHtml((a.username || "P").slice(0, 2).toUpperCase())}</div>
            <div style="flex:1;min-width:0;">
              <div style="display:flex;align-items:center;gap:6px;">
                <span style="font-size:13px;font-weight:900;color:#0f172a;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${escapeHtml(a.username)}</span>
                ${a.selected ? `<span class="badge-active-green">ACTIVE</span>` : ""}
              </div>
              <div style="font-size:10px;font-weight:700;color:#64748b;margin-top:2px;">
                ${a.isMicrosoft ? "👑 Microsoft Account" : "👤 Offline Account"}
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:5px;">
              ${a.selected ? "" : `<button class="mini-btn" onclick="CloudUI.selectAcc(${idx})">⇄ SWITCH</button>`}
              <button class="mini-btn danger" onclick="CloudUI.removeAcc(${idx})">✕</button>
            </div>
          </div>
        `).join("");
      }
    }

    accSkinViewer = createOrUpdateSkinViewer("acc-skin-host", accSkinViewer, 210, 235);
  }

  function renderVault() {
    const grid = document.getElementById("vault-grid");
    if (!grid) return;
    if (!state.instances.length) {
      grid.innerHTML = `<div class="item-card"><div class="setting-title">No Instances Created Yet</div><div class="setting-sub">Open CLOUD CLIENT or Version Studio (+ FORGE) to install Minecraft!</div><button class="mini-btn primary" onclick="CloudUI.go('client')">OPEN CLOUD CLIENT</button></div>`;
      return;
    }
    grid.innerHTML = state.instances.map((inst, idx) => {
      const isCC = !!(inst.isCloudClient || String(inst.name || "").toLowerCase().startsWith("cloud client"));
      return `
      <div class="item-card" style="${inst.selected ? "border:2px solid #38bdf8;" : ""}">
        <div class="item-card-head">
          ${isCC
            ? `<div class="item-icon" style="background:linear-gradient(135deg,#e0f2fe,#ede9fe);padding:4px;"><img src="logo.png" alt="Cloud" style="width:100%;height:100%;object-fit:contain;"></div>`
            : `<div class="item-icon">${escapeHtml((inst.name || "M").slice(0, 2).toUpperCase())}</div>`
          }
          <div style="min-width:0;flex:1;">
            <div class="setting-title" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
              ${isCC ? `<img src="logo.png" class="inline-cloud-logo" alt="">` : ""}${escapeHtml(inst.name)}
            </div>
            <div class="setting-sub">
              ${isCC ? `<span class="cc-badge" style="font-size:8.5px;padding:1px 7px;margin-right:4px;">CLOUD CLIENT</span>` : ""}Version: ${escapeHtml(inst.versionId)}
            </div>
          </div>
        </div>
        <div class="item-actions">
          <button class="mini-btn primary" onclick="CloudUI.selectAndLaunch(${idx})">▶ PLAY</button>
          <button class="mini-btn" onclick="CloudUI.selectInstance(${idx})">${inst.selected ? "✓ ACTIVE" : "SET ACTIVE"}</button>
          <button class="mini-btn" onclick="CloudUI.go('armory')">⚔ MODS</button>
          <button class="mini-btn danger" onclick="CloudUI.deleteInstance(${idx})">DELETE</button>
        </div>
      </div>
    `;
    }).join("");
  }

  function renderSettings() {
    document.querySelectorAll(".rail-btn").forEach(b => {
      b.classList.toggle("active", b.dataset.scat === state.settingsCat);
    });
    const pane = document.getElementById("settings-main-pane");
    if (!pane) return;
    const s = state.settings;
    const rObj = (s.renderers || []).find(r => r.id === s.renderer);
    const rTag = rObj ? rObj.name.split(" ")[0] : "MobileGlues";

    if (state.settingsCat === "fps") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">1-Tap FPS Boost Presets</div>
            <div class="setting-sub">Tuned RAM + Renderer + G1GC flags for 4GB, 6GB, and 8GB+ phones</div>
          </div>
          <div class="preset-group">
            <button class="preset-pill ${state.preset === "4gb" ? "active" : ""}" onclick="CloudUI.applyPreset('4gb')">4GB</button>
            <button class="preset-pill ${state.preset === "6gb" ? "active" : ""}" onclick="CloudUI.applyPreset('6gb')">6GB</button>
            <button class="preset-pill ${state.preset === "8gb" ? "active" : ""}" onclick="CloudUI.applyPreset('8gb')">8GB+</button>
          </div>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Show Real FPS Display Box</div>
            <div class="setting-sub">Green (60+) / Yellow (30-59) / Red (&lt;30) live in-game telemetry box</div>
          </div>
          <div style="display:flex;align-items:center;gap:10px;">
            <div class="fps-preview-box">Fps - <span class="g">124</span> | Renderer - <span class="b">${escapeHtml(rTag)}</span> | Ms - <span class="g">8.1</span></div>
            <div class="prism-switch ${s.fpsBoxEnabled ? "on" : ""}" onclick="CloudUI.toggleFpsBox()"><div class="knob"></div></div>
          </div>
        </div>
        <div class="setting-row" style="flex-direction:column;align-items:stretch;">
          <div style="display:flex;justify-content:space-between;">
            <span class="setting-title">Allocated Java Heap RAM</span>
            <span class="setting-title" id="ram-val-lbl" style="color:#7c3aed;">${s.ramMb} MB</span>
          </div>
          <input type="range" class="prism-slider" min="1024" max="${Math.max(4096, s.maxRamMb || 8192)}" step="128" value="${s.ramMb}" oninput="CloudUI.setRam(this.value)">
        </div>
        <div class="setting-row" style="flex-direction:column;align-items:stretch;">
          <span class="setting-title">Custom JVM Flags</span>
          <input class="prism-input" value="${escapeAttr(s.jvmArgs || "")}" onchange="CloudUI.setJvmArgs(this.value)">
        </div>
      `;
    } else if (state.settingsCat === "video") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">Graphics Renderer Engine</div>
            <div class="setting-sub">Choose MobileGlues, LTW, or Vulkan Zink</div>
          </div>
          <select class="prism-select" onchange="CloudUI.setRenderer(this.value)">
            ${(s.renderers || []).map(r => `<option value="${escapeAttr(r.id)}" ${r.id === s.renderer ? "selected" : ""}>${escapeHtml(r.name)}</option>`).join("")}
          </select>
        </div>
        <div class="setting-row" style="flex-direction:column;align-items:stretch;">
          <div style="display:flex;justify-content:space-between;">
            <span class="setting-title">3D Resolution Scale</span>
            <span class="setting-title" id="res-val-lbl" style="color:#0284c7;">${s.resolutionScale}%</span>
          </div>
          <input type="range" class="prism-slider" min="50" max="100" step="5" value="${s.resolutionScale}" oninput="CloudUI.setResolution(this.value)">
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Sustained Performance Mode</div>
            <div class="setting-sub">Prevent thermal throttling during extended sessions</div>
          </div>
          <div class="prism-switch ${s.sustainedPerf ? "on" : ""}" onclick="CloudUI.toggleSustained()"><div class="knob"></div></div>
        </div>
      `;
    } else if (state.settingsCat === "appearance") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">Prism Light Crystal Glass UI</div>
            <div class="setting-sub">Iridescent white, sky-blue, lavender, and soft pink frosted theme active</div>
          </div>
          <span class="mini-btn primary">ACTIVE</span>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Top Cloud Downloader Bar</div>
            <div class="setting-sub">Preview the relocated top game-asset progress indicator</div>
          </div>
          <button class="mini-btn" onclick="CloudUI.previewDownloader()">TEST BAR</button>
        </div>
      `;
    } else if (state.settingsCat === "controls") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">Touch Controls Rig Editor</div>
            <div class="setting-sub">Customize on-screen buttons, scale, opacity, and mouse sensitivity</div>
          </div>
          <button class="mini-btn primary" onclick="CloudUI.openControlsRig()">OPEN EDITOR</button>
        </div>
        <div class="setting-row">
          <div>
            <div class="setting-title">Controller &amp; Gamepad Remapper</div>
            <div class="setting-sub">Configure Bluetooth or USB-C controllers</div>
          </div>
          <button class="mini-btn primary" onclick="CloudUI.openGamepad()">MAP GAMEPAD</button>
        </div>
      `;
    } else if (state.settingsCat === "client") {
      pane.innerHTML = `
        <div class="setting-row">
          <div>
            <div class="setting-title">Cloud Client PvP HUD Suite</div>
            <div class="setting-sub">Manage Real FPS Box, Keystrokes, CPS, Toggle Sprint, and Crosshair</div>
          </div>
          <button class="mini-btn primary" onclick="CloudUI.go('client')">OPEN CLIENT HUB</button>
        </div>
      `;
    } else {
      pane.innerHTML = `
        <div class="setting-row">
          <div style="display:flex;align-items:center;gap:12px;">
            <img src="logo.png" style="width:48px;height:48px;border-radius:12px;">
            <div>
              <div class="setting-title">Cloud Launcher · Prism Light Edition</div>
              <div class="setting-sub">Standalone Android Java Edition Launcher · Version Studio &amp; Account Studio</div>
            </div>
          </div>
        </div>
      `;
    }
  }

  function renderClientModules() {
    const cc = state.cloudClient;
    const selVer = cc.selectedVersion || "1.21.11";
    const verListEl = document.getElementById("cc-versions-list");
    const modListEl = document.getElementById("cc-mods-list");
    const modHost = document.getElementById("client-modules-list");
    const statusEl = document.getElementById("cc-installer-status");
    const fillEl = document.getElementById("cc-installer-fill");
    const ctaTextEl = document.getElementById("cc-cta-text");

    const curVerObj = (cc.versions || []).find(v => v.version === selVer) || { version: selVer, installed: false, modsCount: 0 };

    if (statusEl) {
      if (cc.installing) {
        statusEl.textContent = cc.status || `CLOUD CLIENT INSTALLER · Installing Cloud Client ${selVer}...`;
      } else if (curVerObj.installed) {
        statusEl.textContent = `CLOUD CLIENT READY · Cloud Client ${selVer} Installed (${curVerObj.modsCount || 9} FPS Boost Mods Active on Launch)`;
      } else {
        statusEl.textContent = `CLOUD CLIENT INSTALLER · Auto-Installing Sodium, Lithium, FPS Display + 6 Boost Mods for ${selVer}`;
      }
    }
    if (fillEl) {
      fillEl.style.width = (cc.installing ? Math.max(8, Math.min(100, cc.progress || 25)) : 100) + "%";
    }
    if (ctaTextEl) {
      if (cc.installing && cc.installingVersion === selVer) {
        ctaTextEl.textContent = `INSTALLING CLOUD CLIENT ${selVer}... (${Math.round(cc.progress || 15)}%)`;
      } else if (curVerObj.installed) {
        ctaTextEl.textContent = `LAUNCH CLOUD CLIENT ${selVer} (MODS ACTIVE)`;
      } else {
        ctaTextEl.textContent = `INSTALL & LAUNCH CLOUD CLIENT ${selVer}`;
      }
    }

    if (verListEl) {
      verListEl.innerHTML = (cc.versions || []).map(v => {
        const isSel = v.version === selVer;
        return `
          <div class="cc-version-row ${isSel ? "selected" : ""}" onclick="CloudUI.selectCloudClientVersion('${escapeAttr(v.version)}')">
            <div class="cc-ver-left">
              <img class="cc-ver-logo" src="logo.png" alt="Cloud Client">
              <span class="cc-ver-title">Cloud Client ${escapeHtml(v.version)}</span>
              ${isSel ? `<span class="cc-selected-tag">[SELECTED <span class="cc-green-dot">✓</span>]</span>` : ""}
              ${v.installed ? `<span class="cc-installed-tag">✓ INSTALLED</span>` : ""}
            </div>
            <span class="cc-badge">CLOUD CLIENT</span>
          </div>
        `;
      }).join("");
    }

    if (modListEl && modHost) {
      modListEl.style.display = cc.hudDrawerOpen ? "none" : "flex";
      modHost.style.display = cc.hudDrawerOpen ? "flex" : "none";
      modListEl.innerHTML = (cc.mods || []).map(m => `
        <div class="cc-mod-pill">
          <div class="cc-mod-left">
            <span class="cc-mod-check">✓</span>
            <span class="cc-mod-name">${escapeHtml(m.title)}</span>
          </div>
          <span class="cc-mod-tag">${curVerObj.installed ? "ACTIVE ✓" : "AUTO-INSTALL"}</span>
        </div>
      `).join("");
    }

    if (!modHost) return;
    const mods = [
      { key: "fpsBoxEnabled", nKey: "showRealFps", name: "Real FPS Display Box", sub: "Fps - | Renderer - | Ms - (Green / Yellow / Red)" },
      { key: "keystrokes", nKey: "keystrokes", name: "RGB Keystrokes HUD", sub: "Live WASD + LMB/RMB overlay" },
      { key: "cpsCounter", nKey: "cpsCounter", name: "Live CPS Counter", sub: "Real-time left & right click speed" },
      { key: "toggleSprint", nKey: "toggleSprint", name: "Toggle Sprint Lock", sub: "1-tap sprint lock for PvP combos" },
      { key: "zoomOverlay", nKey: "zoomHud", name: "OptiFine-Style Quick Zoom", sub: "Smooth cinematic zoom keybind" },
      { key: "crosshair", nKey: "crosshair", name: "Precision Crosshair", sub: "High-contrast custom reticle" }
    ];
    modHost.innerHTML = mods.map(m => `
      <div class="setting-row">
        <div>
          <div class="setting-title">${m.name}</div>
          <div class="setting-sub">${m.sub}</div>
        </div>
        <div class="prism-switch ${state.settings[m.key] ? "on" : ""}" onclick="CloudUI.toggleClientMod('${m.key}','${m.nKey}')"><div class="knob"></div></div>
      </div>
    `).join("");
  }

  async function searchModrinth(projectType, query, targetGridId) {
    const grid = document.getElementById(targetGridId);
    if (!grid) return;
    grid.innerHTML = `<div class="setting-title" style="padding:12px;">Searching Modrinth ${projectType}s...</div>`;
    try {
      const facets = encodeURIComponent(JSON.stringify([[`project_type:${projectType}`]]));
      const url = `https://api.modrinth.com/v2/search?query=${encodeURIComponent(query || "")}&limit=16&facets=${facets}`;
      const res = await fetch(url);
      const data = await res.json();
      const hits = data.hits || [];
      if (!hits.length) {
        grid.innerHTML = `<div class="setting-sub" style="padding:12px;">No results found on Modrinth.</div>`;
        return;
      }
      grid.innerHTML = hits.map(h => `
        <div class="item-card">
          <div class="item-card-head">
            ${h.icon_url ? `<img class="item-icon" src="${escapeAttr(h.icon_url)}">` : `<div class="item-icon">⚡</div>`}
            <div style="min-width:0;">
              <div class="setting-title" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(h.title)}</div>
              <div class="setting-sub">${formatNum(h.downloads)} downloads</div>
            </div>
          </div>
          <div class="setting-sub" style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">${escapeHtml(h.description || "")}</div>
          <div class="item-actions">
            <button class="mini-btn primary" onclick="CloudUI.installFromModrinth('${escapeAttr(h.project_id)}','${escapeAttr(projectType)}','${escapeAttr(h.title)}')">⬇ INSTALL TO INSTANCE</button>
          </div>
        </div>
      `).join("");
    } catch (_) {
      grid.innerHTML = `<div class="setting-sub" style="padding:12px;">Connect to the internet to browse live Modrinth ${projectType}s, or tap INSTALLED (.JAR) to manage local files.</div>`;
    }
  }

  function renderRealmsAndCrew() {
    const rGrid = document.getElementById("realms-grid");
    if (rGrid) {
      rGrid.innerHTML = state.servers.map((s, idx) => `
        <div class="item-card">
          <div class="item-card-head">
            <div class="item-icon">🌐</div>
            <div style="min-width:0;">
              <div class="setting-title">${escapeHtml(s.name)}</div>
              <div class="setting-sub">${escapeHtml(s.addr)}</div>
            </div>
          </div>
          <div class="setting-sub">${escapeHtml(s.tag || "Multiplayer Server")}</div>
          <div class="item-actions">
            <button class="mini-btn primary" onclick="CloudUI.quickJoin('${escapeAttr(s.addr)}')">⚡ QUICK JOIN</button>
            <button class="mini-btn danger" onclick="CloudUI.removeServer(${idx})">REMOVE</button>
          </div>
        </div>
      `).join("");
    }

    const cGrid = document.getElementById("crew-cards");
    if (cGrid) {
      const code = "CLOUD-" + ((state.selectedAccount?.username || "OP").toUpperCase().slice(0, 6));
      cGrid.innerHTML = `
        <div class="item-card">
          <div class="setting-title">🪪 Squad Invite Code: ${escapeHtml(code)}</div>
          <div class="setting-sub">Copy your Cloud Squad Tag &amp; active server IP to invite friends</div>
          <button class="mini-btn primary" onclick="CloudUI.copyInvite('${escapeAttr(code)}')">COPY SQUAD INVITE</button>
        </div>
        <div class="item-card">
          <div class="setting-title">🌐 Direct Multiplayer Realms</div>
          <div class="setting-sub">Jump straight into your saved Java servers</div>
          <button class="mini-btn primary" onclick="CloudUI.go('realms')">OPEN REALMS</button>
        </div>
      `;
    }
  }

  function formatDur(sec) {
    const n = parseInt(sec, 10) || 0;
    if (n <= 0) return "";
    const m = Math.floor(n / 60);
    const s = n % 60;
    return `${m}:${s < 10 ? "0" : ""}${s}`;
  }

  const WHITE_SPEAKER_SVG = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" fill="#ffffff"></polygon><path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path><path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path></svg>`;

  function renderMusicSection() {
    const sw = document.getElementById("header-music-sys-sw");
    const swLbl = document.getElementById("header-music-sys-lbl");
    if (sw && swLbl) {
      sw.classList.toggle("on", !!state.musicSystemEnabled);
      swLbl.textContent = state.musicSystemEnabled ? "ON" : "OFF";
    }

    const st = state.musicStatus || {};
    const host = document.getElementById("music-songs-list");
    if (!host) return;

    if (!state.musicSystemEnabled) {
      host.innerHTML = `
        <div class="music-song-row">
          <div class="music-song-photo">${WHITE_SPEAKER_SVG}</div>
          <div class="music-song-box">
            <div class="music-song-info">
              <div class="music-song-title">MUSIC SYSTEM IS OFF</div>
              <div class="music-song-sub">Tap "Music System" toggle in the top header to turn ON background music</div>
            </div>
            <div class="music-song-controls">
              <button class="music-play-btn" onclick="CloudUI.toggleMusicSystem()">TURN ON</button>
            </div>
          </div>
        </div>
      `;
      return;
    }

    if (state.musicLoading && !state.musicSongs.length) {
      host.innerHTML = `
        <div class="music-song-row">
          <div class="music-song-photo">${WHITE_SPEAKER_SVG}</div>
          <div class="music-song-box">
            <div class="music-song-info">
              <div class="music-song-title">LOADING SONGS...</div>
              <div class="music-song-sub">Fetching full song catalog</div>
            </div>
          </div>
        </div>
      `;
      return;
    }

    if (!state.musicSongs.length) {
      host.innerHTML = `
        <div class="music-song-row">
          <div class="music-song-photo">${WHITE_SPEAKER_SVG}</div>
          <div class="music-song-box">
            <div class="music-song-info">
              <div class="music-song-title">NO SONGS FOUND</div>
              <div class="music-song-sub">Search another song or artist above, or tap + Import MP3</div>
            </div>
          </div>
        </div>
      `;
      return;
    }

    host.innerHTML = state.musicSongs.map((song, idx) => {
      const isThisPlaying = !!(st.songId && st.songId === song.id && (st.playing || st.preparing));
      const durTxt = formatDur(song.durationSec);
      const subParts = [];
      if (song.artist) subParts.push(song.artist);
      if (durTxt) subParts.push(durTxt);

      const photoHtml = song.photo
        ? `<img class="music-song-photo" src="${escapeAttr(song.photo)}" alt="" onerror="this.outerHTML='<div class=\\'music-song-photo\\'>${WHITE_SPEAKER_SVG.replace(/"/g, "&quot;")}</div>'">`
        : `<div class="music-song-photo">${WHITE_SPEAKER_SVG}</div>`;

      return `
        <div class="music-song-row ${isThisPlaying ? "playing" : ""}">
          ${photoHtml}
          <div class="music-song-box">
            ${isThisPlaying ? `<span class="playing-bg-pill">${st.preparing ? "LOADING SONG..." : "PLAYING IN BACKGROUND"}</span>` : ""}
            <div class="music-song-info">
              <div class="music-song-title">${escapeHtml(song.title || "Song")}</div>
              <div class="music-song-sub">${escapeHtml(subParts.join(" · ") || "Ready to Play")}</div>
            </div>
            <div class="music-song-controls">
              <button class="music-play-btn ${isThisPlaying ? "active" : ""}" onclick="CloudUI.playSongByIndex(${idx})">
                ▶ ${isThisPlaying ? "PLAYING" : "PLAY"}
              </button>
              <button class="music-stop-btn" onclick="CloudUI.stopSong()">
                ▣ STOP
              </button>
            </div>
          </div>
        </div>
      `;
    }).join("");
  }

  async function loadMusicCatalog(query) {
    const q = (query || "").trim() || "Top Hindi & Global Hits";
    state.musicLastQuery = q;
    state.musicLoading = true;
    renderMusicSection();

    const nb = bridge();
    if (nb && nb.searchMusicAsync) {
      nb.searchMusicAsync(q);
    }

    if (q === "__DEVICE__") return;

    // Parallel WebView search fallback so songs populate ultra-fast in both APK & browser
    try {
      const enc = encodeURIComponent(q);
      let loaded = [];

      // Try full-length JioSaavn JSON mirror first
      try {
        const r1 = await fetch(`https://saavn.sumit.co/api/search/songs?query=${enc}&limit=35`);
        if (r1.ok) {
          const j1 = await r1.json();
          const res = j1 && j1.data && j1.data.results;
          if (Array.isArray(res) && res.length) {
            loaded = res.map((it, idx) => {
              const dls = Array.isArray(it.downloadUrl) ? it.downloadUrl : [];
              const best = dls.length ? dls[dls.length - 1].url : "";
              const imgs = Array.isArray(it.image) ? it.image : [];
              const photo = imgs.length ? imgs[imgs.length - 1].url : "";
              const prim = it.artists && Array.isArray(it.artists.primary) ? it.artists.primary : [];
              const artist = prim.length ? prim[0].name : "";
              return {
                id: "saavn_" + (it.id || idx),
                title: it.name || "Song",
                artist: artist || "",
                photo: photo || "",
                streamUrl: best || "",
                durationSec: it.duration || 0
              };
            }).filter(x => !!x.streamUrl);
          }
        }
      } catch (_) {}

      // Fallback to Apple iTunes global catalog if mirror didn't respond
      if (!loaded.length) {
        const r2 = await fetch(`https://itunes.apple.com/search?term=${enc}&media=music&entity=song&limit=38&country=IN`);
        if (r2.ok) {
          const j2 = await r2.json();
          const res2 = j2 && j2.results;
          if (Array.isArray(res2) && res2.length) {
            loaded = res2.filter(x => !!x.previewUrl).map((it, idx) => ({
              id: "it_" + (it.trackId || idx),
              title: it.trackName || "Song",
              artist: it.artistName || "",
              photo: String(it.artworkUrl100 || "").replace("100x100bb", "400x400bb"),
              streamUrl: it.previewUrl,
              durationSec: Math.round((it.trackTimeMillis || 0) / 1000)
            }));
          }
        }
      }

      if (loaded.length && state.musicLastQuery === q && !state.musicSongs.length) {
        state.musicSongs = loaded;
        state.musicLoading = false;
        renderMusicSection();
      }
    } catch (_) {
      state.musicLoading = false;
      renderMusicSection();
    }
  }

  function escapeHtml(s) {
    return String(s || "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  }
  function escapeAttr(s) { return escapeHtml(s); }
  function formatNum(n) {
    if (!n) return "0";
    if (n >= 1e6) return (n / 1e6).toFixed(1) + "M";
    if (n >= 1e3) return (n / 1e3).toFixed(1) + "K";
    return String(n);
  }

  window.CloudUI = {
    toast,

    syncFromNative() {
      refreshStateFromNative();
      const nb = bridge();
      if (nb && nb.getMusicStatusJson) {
        const ms = safeParse(nb.getMusicStatusJson(), null);
        if (ms) state.musicStatus = ms;
      }
      renderHeaderAndHQ();
      init3DSkins();
      if (state.screen === "vault") renderVault();
      if (state.screen === "forge") renderVersionStudio();
      if (state.screen === "accounts") renderAccountStudio();
      if (state.screen === "config") renderSettings();
      if (state.screen === "client") renderClientModules();
      if (state.screen === "music") renderMusicSection();
    },

    onBackPressed() {
      if (state.screen !== "hq") {
        this.go("hq");
        return true;
      }
      return false;
    },

    go(screen) {
      state.screen = screen;
      document.body.classList.toggle("on-music-screen", screen === "music");
      document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
      const target = document.getElementById("screen-" + screen);
      if (target) target.classList.add("active");

      document.querySelectorAll(".dock-btn").forEach(b => {
        b.classList.toggle("active", b.dataset.dock === screen);
      });
      document.querySelectorAll(".h-pill").forEach(b => {
        b.classList.toggle("active", b.dataset.nav === screen);
      });

      refreshStateFromNative();
      renderHeaderAndHQ();
      if (screen === "vault") renderVault();
      if (screen === "forge") {
        updateDefaultInstanceName();
        renderVersionStudio();
        loadModloaderBuilds();
      }
      if (screen === "accounts") renderAccountStudio();
      if (screen === "config") renderSettings();
      if (screen === "client") renderClientModules();
      if (screen === "music") {
        renderMusicSection();
        if (!state.musicSongs.length) {
          loadMusicCatalog(state.musicLastQuery || "Top Hindi & Global Hits");
        }
      }
      if (screen === "armory" && !document.getElementById("armory-grid").children.length) {
        searchModrinth("mod", "", "armory-grid");
      }
      if (screen === "packs" && !document.getElementById("packs-grid").children.length) {
        searchModrinth(state.packKind, "", "packs-grid");
      }
      if (screen === "realms" || screen === "crew") renderRealmsAndCrew();
      if (screen === "files") this.browseSub("");
      if (screen === "console") this.loadConsole();
    },

    setCenterMode(mode) {
      document.getElementById("tab-reactor").classList.toggle("active", mode === "reactor");
      document.getElementById("tab-deck").classList.toggle("active", mode === "deck");
      document.getElementById("reactor-split-view").style.display = mode === "reactor" ? "grid" : "none";
      document.getElementById("command-deck-view").style.display = mode === "deck" ? "flex" : "none";
    },

    cycleSkin() {
      state.customSkinData = "";
      localStorage.removeItem("cloud_custom_skin");
      state.skinIdx = (state.skinIdx + 1) % PRESET_SKINS.length;
      const p = PRESET_SKINS[state.skinIdx];
      state.slimModel = !!p.slim;
      init3DSkins();
      renderAccountStudio();
      toast("Equipped " + p.name);
    },

    setPlayerModel(slim) {
      state.slimModel = !!slim;
      renderAccountStudio();
      init3DSkins();
    },

    toggleSkinWalk() {
      state.skinWalking = !state.skinWalking;
      init3DSkins();
    },

    pickSkinFile() {
      const nb = bridge();
      if (nb && nb.pickSkinFile) {
        nb.pickSkinFile();
        return;
      }
      const inp = document.createElement("input");
      inp.type = "file";
      inp.accept = "image/png";
      inp.onchange = e => {
        const f = e.target.files && e.target.files[0];
        if (!f) return;
        const reader = new FileReader();
        reader.onload = ev => {
          state.customSkinData = ev.target.result;
          localStorage.setItem("cloud_custom_skin", state.customSkinData);
          init3DSkins();
          toast("Custom .PNG Skin Loaded!");
        };
        reader.readAsDataURL(f);
      };
      inp.click();
    },

    launchGame() {
      const nb = bridge();
      if (nb) {
        const res = nb.launchGame();
        if (res) toast(res);
      } else {
        toast("Launching " + (state.selectedInstance?.name || "Minecraft") + "...");
      }
    },

    selectInstance(idx) {
      const nb = bridge();
      if (nb) nb.selectInstance(Number(idx) || 0);
      setTimeout(() => this.syncFromNative(), 80);
    },

    selectAndLaunch(idx) {
      const nb = bridge();
      if (nb) {
        nb.selectInstance(Number(idx) || 0);
        setTimeout(() => nb.launchGame(), 100);
      }
    },

    deleteInstance(idx) {
      const nb = bridge();
      if (nb) nb.deleteInstance(Number(idx) || 0);
      setTimeout(() => this.syncFromNative(), 100);
      toast("Instance removed");
    },

    setVersionFilter(vf) {
      state.versionFilter = vf;
      renderVersionStudio();
    },

    pickMcVersion(verId) {
      state.selectedMcVersion = verId;
      updateDefaultInstanceName();
      renderVersionStudio();
      loadModloaderBuilds();
    },

    pickModloader(ldr) {
      state.modloader = ldr;
      updateDefaultInstanceName();
      renderVersionStudio();
      loadModloaderBuilds();
    },

    pickLoaderBuild(buildId) {
      state.selectedLoaderBuild = buildId;
      renderLoaderBuildsOnly();
    },

    refreshLoaderBuilds() {
      loadModloaderBuilds();
      toast("Refreshing " + state.modloader + " versions...");
    },

    onLoaderVersionsLoaded(loader, mcVer, list) {
      if (loader !== state.modloader || mcVer !== state.selectedMcVersion) return;
      state.loadingBuilds = false;
      if (Array.isArray(list) && list.length) {
        state.loaderBuilds = list;
        const preferred = list.find(x => x.id === state.selectedLoaderBuild) || list.find(x => x.stable) || list[0];
        state.selectedLoaderBuild = preferred.id;
      }
      renderLoaderBuildsOnly();
    },

    installSelectedVersion() {
      const name = (document.getElementById("forge-inst-name")?.value || "").trim()
        || `${state.modloader} ${state.selectedMcVersion}`;
      const buildId = state.selectedLoaderBuild || "";

      if (state.modloader !== "VANILLA" && !buildId) {
        toast("Please pick a " + state.modloader + " loader version first");
        return;
      }

      const nb = bridge();
      if (nb) {
        nb.createInstanceWithBuild(state.modloader, state.selectedMcVersion, buildId, name);
        toast(`Installing ${name}...`);
      } else {
        this.previewDownloader(`Installing ${name} (${buildId || "Vanilla"})...`);
        state.instances.unshift({ index: 0, name, versionId: state.selectedMcVersion, selected: true });
        this.go("hq");
      }
    },

    onInstanceCreated(ok, msg) {
      this.syncFromNative();
      toast(msg || (ok ? "Instance installed & ready!" : "Install finished"));
      if (ok) this.go("hq");
    },

    microsoftLogin() {
      const nb = bridge();
      if (nb) nb.microsoftLogin();
      else toast("Opening Microsoft Xbox Live Sign-In...");
    },

    createOffline() {
      const inp = document.getElementById("offline-username");
      const name = (inp?.value || "").trim();
      if (!name) {
        toast("Enter a player username first!");
        inp?.focus();
        return;
      }
      const nb = bridge();
      if (nb) {
        nb.createOfflineAccount(name);
      } else {
        state.accounts.forEach(a => a.selected = false);
        state.accounts.unshift({ index: 0, username: name, isMicrosoft: false, selected: true });
      }
      if (inp) inp.value = "";
      setTimeout(() => {
        this.syncFromNative();
        toast("Signed in as " + name + "!");
      }, 90);
    },

    selectAcc(idx) {
      const nb = bridge();
      if (nb) nb.selectAccount(Number(idx) || 0);
      setTimeout(() => {
        this.syncFromNative();
        toast("Switched active account");
      }, 90);
    },

    removeAcc(idx) {
      const nb = bridge();
      if (nb) nb.deleteAccount(Number(idx) || 0);
      setTimeout(() => {
        this.syncFromNative();
        toast("Account removed");
      }, 90);
    },

    setSettingsCat(cat) {
      state.settingsCat = cat;
      renderSettings();
    },

    applyPreset(tier) {
      state.preset = tier;
      localStorage.setItem("cloud_fps_preset", tier);
      const nb = bridge();
      if (nb) nb.applyPreset(tier);
      setTimeout(() => {
        this.syncFromNative();
        toast("Applied " + tier.toUpperCase() + " FPS Boost Preset!");
      }, 80);
    },

    toggleFpsBox() {
      state.settings.fpsBoxEnabled = !state.settings.fpsBoxEnabled;
      const nb = bridge();
      if (nb) nb.setToggle("showRealFps", state.settings.fpsBoxEnabled);
      renderSettings();
      toast("Real FPS Display Box: " + (state.settings.fpsBoxEnabled ? "ON" : "OFF"));
    },

    setRam(val) {
      state.settings.ramMb = parseInt(val, 10) || 2048;
      const lbl = document.getElementById("ram-val-lbl");
      if (lbl) lbl.textContent = state.settings.ramMb + " MB";
      const nb = bridge();
      if (nb) nb.setRamMb(state.settings.ramMb);
      renderHeaderAndHQ();
    },

    setRenderer(id) {
      state.settings.renderer = id;
      const nb = bridge();
      if (nb) nb.setRenderer(id);
      renderHeaderAndHQ();
      renderSettings();
    },

    setResolution(val) {
      state.settings.resolutionScale = parseInt(val, 10) || 100;
      const lbl = document.getElementById("res-val-lbl");
      if (lbl) lbl.textContent = state.settings.resolutionScale + "%";
      const nb = bridge();
      if (nb) nb.setIntPref("resolutionRatio", state.settings.resolutionScale);
    },

    toggleSustained() {
      state.settings.sustainedPerf = !state.settings.sustainedPerf;
      const nb = bridge();
      if (nb) nb.setToggle("sustainedPerformance", state.settings.sustainedPerf);
      renderSettings();
    },

    setJvmArgs(val) {
      state.settings.jvmArgs = val;
      const nb = bridge();
      if (nb) nb.setStringPref("javaArgs", val);
      toast("Saved JVM arguments");
    },

    toggleClientMod(key, nativeKey) {
      state.settings[key] = !state.settings[key];
      const nb = bridge();
      if (nb) nb.setToggle(nativeKey || key, !!state.settings[key]);
      renderClientModules();
    },

    searchMods() {
      const q = document.getElementById("armory-query")?.value || "";
      searchModrinth("mod", q, "armory-grid");
    },

    showInstalledMods() {
      const grid = document.getElementById("armory-grid");
      if (!grid) return;
      const nb = bridge();
      const list = nb ? safeParse(nb.listFilesJson("mods"), []) : [];
      if (!list.length) {
        grid.innerHTML = `<div class="setting-sub" style="padding:12px;">No local .jar mods installed in this instance yet. Search Modrinth above to install!</div>`;
        return;
      }
      grid.innerHTML = list.map(f => `
        <div class="item-card">
          <div class="setting-title">${escapeHtml(f.name)}</div>
          <div class="setting-sub">${escapeHtml(String(f.size || ""))}</div>
          <div class="item-actions">
            <button class="mini-btn danger" onclick="CloudUI.deleteModJar('${encodeURIComponent(f.name)}')">DELETE</button>
          </div>
        </div>
      `).join("");
    },

    deleteModJar(encName) {
      const nb = bridge();
      if (nb) nb.deleteRelativeFile("mods/" + decodeURIComponent(encName));
      this.showInstalledMods();
    },

    setPackKind(kind) {
      state.packKind = kind;
      document.getElementById("pack-tab-shader").classList.toggle("primary", kind === "shader");
      document.getElementById("pack-tab-rp").classList.toggle("primary", kind === "resourcepack");
      this.searchPacks();
    },

    searchPacks() {
      const q = document.getElementById("packs-query")?.value || "";
      searchModrinth(state.packKind, q, "packs-grid");
    },

    async installFromModrinth(projectId, kind, title) {
      toast("Fetching " + title + "...");
      try {
        const res = await fetch(`https://api.modrinth.com/v2/project/${encodeURIComponent(projectId)}/version`);
        const vers = await res.json();
        const first = Array.isArray(vers) && vers[0];
        const file = first && first.files && first.files[0];
        if (!file || !file.url) {
          toast("No downloadable file found");
          return;
        }
        const folder = kind === "shader" ? "shaderpacks" : (kind === "resourcepack" ? "resourcepacks" : "mods");
        const nb = bridge();
        if (nb) {
          nb.downloadUrlToSubfolder(file.url, file.filename, folder);
        } else {
          this.previewDownloader("Downloading " + file.filename + "...");
        }
      } catch (_) {
        toast("Download failed — check connection");
      }
    },

    addServer() {
      const name = (document.getElementById("srv-name")?.value || "").trim();
      const addr = (document.getElementById("srv-addr")?.value || "").trim();
      if (!addr) { toast("Enter server IP address"); return; }
      state.servers.unshift({ name: name || addr, addr, tag: "Saved Server" });
      localStorage.setItem("cloud_servers", JSON.stringify(state.servers));
      renderRealmsAndCrew();
      toast("Added " + (name || addr));
    },

    removeServer(idx) {
      state.servers.splice(idx, 1);
      localStorage.setItem("cloud_servers", JSON.stringify(state.servers));
      renderRealmsAndCrew();
    },

    quickJoin(addr) {
      const nb = bridge();
      if (nb) nb.joinServer(addr);
      else toast("Joining " + addr + "...");
    },

    copyInvite(code) {
      if (navigator.clipboard) navigator.clipboard.writeText(code);
      toast("Copied Squad Code: " + code);
    },

    openControlsRig() {
      const nb = bridge();
      if (nb) nb.openControlsRig();
      else toast("Opening Touch Controls Rig...");
    },

    runJarInstaller() {
      const nb = bridge();
      if (nb) nb.runJarInstaller();
      else toast("Select .JAR Mod Installer...");
    },

    openGamepad() {
      const nb = bridge();
      if (nb) nb.openGamepad();
      else toast("Opening Gamepad Mapper...");
    },

    shareLogs() {
      const nb = bridge();
      if (nb) nb.shareLogs();
      else toast("Sharing latestlog.txt...");
    },

    openDiscord() {
      const nb = bridge();
      if (nb) nb.openUrl("https://discord.gg/minecraft");
    },

    clearCache() {
      const nb = bridge();
      if (nb) nb.clearCache();
      toast("Cache cleared!");
    },

    browseSub(sub) {
      document.getElementById("files-current-path").textContent = ".minecraft/" + (sub || "");
      const host = document.getElementById("files-list");
      if (!host) return;
      const nb = bridge();
      const items = nb ? safeParse(nb.listFilesJson(sub), []) : [
        { name: "mods", isDir: true, size: "Folder" },
        { name: "shaderpacks", isDir: true, size: "Folder" },
        { name: "options.txt", isDir: false, size: "4 KB" }
      ];
      if (!items.length) {
        host.innerHTML = `<div class="setting-sub" style="padding:10px;">Folder is empty.</div>`;
        return;
      }
      host.innerHTML = items.map(f => `
        <div class="setting-row" style="padding:8px 12px;">
          <div style="font-size:12px;font-weight:700;">${f.isDir ? "📁" : "📄"} ${escapeHtml(f.name)}</div>
          <div style="font-size:11px;color:#64748b;">${escapeHtml(String(f.size || ""))}</div>
        </div>
      `).join("");
    },

    openNativeFolder() {
      const nb = bridge();
      if (nb) nb.openGameFolder();
    },

    loadConsole() {
      const pre = document.getElementById("console-output");
      const nb = bridge();
      if (pre && nb) {
        pre.textContent = nb.getConsoleLog() || "Cloud Launcher Ready.";
      }
    },

    refreshAll() {
      this.syncFromNative();
    },

    // ===================================================================
    // MUSIC SECTION HANDLERS (Search + Play in Background + Stop + Auto-End)
    // ===================================================================
    onMusicQueryInput(val) {
      clearTimeout(musicSearchTimer);
      const q = (val || "").trim();
      document.querySelectorAll("#music-chips-bar .music-chip").forEach(c => c.classList.remove("active"));
      musicSearchTimer = setTimeout(() => {
        state.musicSongs = [];
        loadMusicCatalog(q || "Top Hindi & Global Hits");
      }, 280);
    },

    searchMusicNow() {
      clearTimeout(musicSearchTimer);
      const q = (document.getElementById("music-search-input")?.value || "").trim();
      state.musicSongs = [];
      loadMusicCatalog(q || "Top Hindi & Global Hits");
    },

    selectMusicCategory(btn, query) {
      document.querySelectorAll("#music-chips-bar .music-chip").forEach(c => c.classList.remove("active"));
      if (btn) btn.classList.add("active");
      const inp = document.getElementById("music-search-input");
      if (inp && query !== "__DEVICE__") inp.value = "";
      state.musicSongs = [];
      loadMusicCatalog(query);
    },

    onMusicSearchResults(query, arr) {
      if (query !== state.musicLastQuery) return;
      state.musicLoading = false;
      if (Array.isArray(arr) && arr.length) {
        state.musicSongs = arr;
      }
      renderMusicSection();
    },

    toggleMusicSystem() {
      state.musicSystemEnabled = !state.musicSystemEnabled;
      localStorage.setItem("cloud_music_enabled", state.musicSystemEnabled ? "1" : "0");
      if (!state.musicSystemEnabled) {
        this.stopSong();
        toast("Music System: OFF");
      } else {
        renderMusicSection();
        if (!state.musicSongs.length) {
          loadMusicCatalog(state.musicLastQuery || "Top Hindi & Global Hits");
        }
        toast("Music System: ON");
      }
    },

    playSongByIndex(idx) {
      if (!state.musicSystemEnabled) {
        toast("Turn ON Music System first");
        return;
      }
      const song = state.musicSongs[idx];
      if (!song || !song.streamUrl) {
        toast("Song stream unavailable");
        return;
      }

      if (webFallbackAudio) {
        try {
          webFallbackAudio.pause();
          webFallbackAudio.src = "";
        } catch (_) {}
        webFallbackAudio = null;
      }

      const nb = bridge();
      if (nb && nb.playMusic) {
        state.musicStatus = {
          playing: true,
          preparing: true,
          songId: song.id,
          title: song.title || "Song",
          artist: song.artist || "",
          artwork: song.photo || ""
        };
        renderMusicSection();
        nb.playMusic(song.id || String(idx), song.title || "Song", song.artist || "", song.photo || "", song.streamUrl);
        toast("Playing: " + (song.title || "Song"));
        return;
      }

      // Browser fallback (non-looping — automatically ends when song finishes!)
      try {
        if (webFallbackAudio) {
          webFallbackAudio.pause();
          webFallbackAudio.src = "";
          webFallbackAudio = null;
        }
        const audio = new Audio(song.streamUrl);
        audio.loop = false;
        webFallbackAudio = audio;
        state.musicStatus = {
          playing: true,
          preparing: false,
          songId: song.id,
          title: song.title || "Song",
          artist: song.artist || "",
          artwork: song.photo || ""
        };
        renderMusicSection();
        audio.onended = () => {
          state.musicStatus = { playing: false, preparing: false, songId: "", title: "", artist: "", artwork: "" };
          renderMusicSection();
          toast("Song ended");
        };
        audio.play().catch(() => {});
        toast("Playing: " + (song.title || "Song"));
      } catch (_) {}
    },

    stopSong() {
      const nb = bridge();
      if (nb && nb.stopMusic) {
        nb.stopMusic();
      }
      if (webFallbackAudio) {
        try {
          webFallbackAudio.pause();
          webFallbackAudio.currentTime = 0;
        } catch (_) {}
        webFallbackAudio = null;
      }
      state.musicStatus = { playing: false, preparing: false, songId: "", title: "", artist: "", artwork: "" };
      renderMusicSection();
      toast("Music stopped");
    },

    onMusicStateUpdate(st) {
      if (st && typeof st === "object") {
        // Don't overwrite active webFallbackAudio if fallback is currently playing
        if (!webFallbackAudio || st.playing || st.preparing) {
          state.musicStatus = st;
        }
      }
      renderMusicSection();
    },

    playWebAudioFallback(songId, url) {
      if (!state.musicSystemEnabled || !url) return;
      try {
        if (webFallbackAudio) {
          webFallbackAudio.pause();
          webFallbackAudio.src = "";
        }
        const matched = state.musicSongs.find(s => s.id === songId);
        const audio = new Audio(url);
        audio.loop = false;
        audio.volume = 1.0;
        webFallbackAudio = audio;
        state.musicStatus = {
          playing: true,
          preparing: false,
          songId: songId,
          title: matched ? matched.title : "Song",
          artist: matched ? matched.artist : "",
          artwork: matched ? matched.photo : ""
        };
        renderMusicSection();
        audio.onended = () => {
          webFallbackAudio = null;
          state.musicStatus = { playing: false, preparing: false, songId: "", title: "", artist: "", artwork: "" };
          renderMusicSection();
        };
        audio.play().catch(() => {});
      } catch (_) {}
    },

    pickLocalSongFile() {
      const inp = document.createElement("input");
      inp.type = "file";
      inp.accept = "audio/*";
      inp.onchange = e => {
        const f = e.target.files && e.target.files[0];
        if (!f) return;
        const objUrl = URL.createObjectURL(f);
        const cleanTitle = f.name.replace(/\.[^.]+$/, "");
        const added = {
          id: "picked_" + Date.now(),
          title: cleanTitle,
          artist: "Local Audio File",
          photo: "",
          streamUrl: objUrl,
          durationSec: 0
        };
        state.musicSongs.unshift(added);
        renderMusicSection();
        // Play local blob via HTML5 audio or data URI
        if (webFallbackAudio) {
          try { webFallbackAudio.pause(); } catch (_) {}
        }
        const a = new Audio(objUrl);
        a.loop = false;
        webFallbackAudio = a;
        state.musicStatus = { playing: true, preparing: false, songId: added.id, title: cleanTitle, artist: "Local Audio", artwork: "" };
        renderMusicSection();
        a.onended = () => {
          state.musicStatus = { playing: false, preparing: false, songId: "", title: "", artist: "", artwork: "" };
          renderMusicSection();
        };
        a.play().catch(() => {});
        toast("Playing " + cleanTitle);
      };
      inp.click();
    },

    previewDownloader(customLabel) {
      this.onDownloadProgress(true, customLabel || "Downloading Minecraft 1.21.4 Assets...", 64);
      setTimeout(() => this.onDownloadProgress(false, "", 100), 3200);
    },

    syncState() {
      this.syncFromNative();
    },

    selectCloudClientVersion(ver) {
      state.cloudClient.selectedVersion = ver;
      state.cloudClient._userPicked = true;
      const nb = bridge();
      const vObj = (state.cloudClient.versions || []).find(v => v.version === ver);
      if (vObj && vObj.installed && nb && nb.selectCloudClient) {
        nb.selectCloudClient(ver);
      }
      renderClientModules();
    },

    toggleClientHudDrawer() {
      state.cloudClient.hudDrawerOpen = !state.cloudClient.hudDrawerOpen;
      renderClientModules();
    },

    launchSelectedCloudClient() {
      const ver = state.cloudClient.selectedVersion || "1.21.11";
      const vObj = (state.cloudClient.versions || []).find(v => v.version === ver);
      const nb = bridge();
      if (vObj && vObj.installed && nb && nb.selectCloudClient) {
        nb.selectCloudClient(ver);
        this.launchGame();
      } else {
        this.installAndLaunchCloudClient(true);
      }
    },

    installAndLaunchCloudClient(autoLaunch) {
      const ver = state.cloudClient.selectedVersion || "1.21.11";
      if (state.cloudClient.installing) {
        toast("Cloud Client installer is already running...");
        return;
      }
      const vObj = (state.cloudClient.versions || []).find(v => v.version === ver);
      const nb = bridge();
      if (autoLaunch && vObj && vObj.installed && nb && nb.selectCloudClient) {
        nb.selectCloudClient(ver);
        toast("Launching Cloud Client " + ver + " with FPS Boost Mods!");
        this.launchGame();
        return;
      }

      state.cloudClient.installing = true;
      state.cloudClient.installingVersion = ver;
      state.cloudClient.progress = 8;
      state.cloudClient.status = `CLOUD CLIENT INSTALLER · Auto-Installing Sodium, Lithium, FPS Display + 6 Boost Mods for ${ver}...`;
      renderClientModules();

      if (nb && nb.installCloudClient) {
        toast((autoLaunch ? "Installing & Launching " : "Installing ") + "Cloud Client " + ver + "...");
        nb.installCloudClient(ver, !!autoLaunch);
        return;
      }

      // Browser preview simulation
      let p = 15;
      const timer = setInterval(() => {
        p += 25;
        if (p >= 100) {
          clearInterval(timer);
          state.cloudClient.installing = false;
          state.cloudClient.progress = 100;
          if (vObj) { vObj.installed = true; vObj.modsCount = 9; }
          state.instances.unshift({
            index: 0,
            name: "Cloud Client " + ver,
            versionId: "fabric-loader-0.19.5-" + ver,
            isCloudClient: true,
            selected: true
          });
          state.selectedInstance = state.instances[0];
          renderHeaderAndHQ();
          renderClientModules();
          toast("Cloud Client " + ver + " Installed with 9 FPS Boost Mods!");
        } else {
          state.cloudClient.progress = p;
          renderClientModules();
        }
      }, 350);
    },

    onCloudClientProgress(active, text, pct, ver) {
      state.cloudClient.installing = !!active;
      if (ver) state.cloudClient.installingVersion = ver;
      if (text) state.cloudClient.status = text;
      if (typeof pct === "number") state.cloudClient.progress = pct;
      if (!active) {
        refreshStateFromNative();
        renderHeaderAndHQ();
        if (text) toast(text);
      }
      if (state.screen === "client") {
        renderClientModules();
      }
    },

    onDownloadProgress(active, label, pct) {
      const bar = document.getElementById("top-downloader");
      if (!bar) return;
      bar.classList.toggle("active", !!active);
      document.body.classList.toggle("has-dl", !!active);
      if (active) {
        document.getElementById("dl-title").textContent = label || "Cloud Downloader Active...";
        const clamped = Math.max(4, Math.min(100, Math.round(pct || 0)));
        document.getElementById("dl-fill").style.width = clamped + "%";
        document.getElementById("dl-pct").textContent = clamped + "%";
      }
    }
  };

  // Poll native task progress every 900ms so the relocated Top CLOUD DOWNLOADER bar tracks all asset downloads!
  setInterval(() => {
    const nb = bridge();
    if (!nb || !nb.getProgressJson) return;
    const p = safeParse(nb.getProgressJson(), null);
    if (p) window.CloudUI.onDownloadProgress(!!p.active, p.title, p.pct);
  }, 900);

  document.addEventListener("DOMContentLoaded", () => {
    const instInp = document.getElementById("forge-inst-name");
    if (instInp) {
      instInp.addEventListener("input", () => {
        const d = document.getElementById("vs-dir-preview");
        if (d) d.textContent = "📁 /CloudLauncher/instances/" + (instInp.value || "Instance");
      });
    }
    refreshStateFromNative();
    renderHeaderAndHQ();
    init3DSkins();
    setTimeout(() => loadMusicCatalog("Top Hindi & Global Hits"), 200);
  });
})();
