import { state } from './state.js';
import { CATEGORIES, CATALOG_ITEMS, PRESET_SETUPS, FINISHES } from './catalog.js';
import { Scene3DManager } from './scene3d.js';
import { Canvas2DManager } from './canvas2d.js';
import { exportPNGSnapshot, exportJSONConfig, importJSONConfig, exportCSVBudget } from './exportUtils.js';
import { initThemeToggle } from './theme.js';

let scene3D = null;
let canvas2D = null;
let activeCategory = 'all';

document.addEventListener('DOMContentLoaded', () => {
  initUI();
  initMobileUI();
  initSceneManagers();
  renderCatalog();
  updateInspectorAndBudget();

  window.addEventListener('keydown', (e) => handleKeyboardShortcuts(e));
});

function initSceneManagers() {
  const container3D = document.getElementById('canvas3DContainer');
  const canvas2DElement = document.getElementById('canvas2d');

  initThemeToggle(document.getElementById('btnThemeToggle'));
  scene3D = new Scene3DManager(container3D);
  canvas2D = new Canvas2DManager(canvas2DElement);

  state.subscribe((appState, changeType) => {
    updateInspectorAndBudget();

    if (changeType === 'viewMode') {
      updateViewToggleUI(appState.viewMode);
    }
  });
}

function initUI() {
  const btn3D = document.getElementById('btnView3D');
  const btn2D = document.getElementById('btnView2D');

  btn3D?.addEventListener('click', () => setViewMode('3d'));
  btn2D?.addEventListener('click', () => setViewMode('2d'));

  const presetSelect = document.getElementById('presetSelect');
  presetSelect?.addEventListener('change', (e) => {
    if (e.target.value) {
      state.loadPreset(e.target.value);
      showToast('Loaded preset setup!');
      e.target.value = '';
    }
  });

  const sliderHeight = document.getElementById('sliderDeskHeight');
  const textHeight = document.getElementById('textDeskHeight');
  const sliderHeightMobile = document.getElementById('sliderDeskHeightMobile');
  const textHeightMobile = document.getElementById('textDeskHeightMobile');

  sliderHeight?.addEventListener('input', (e) => {
    const val = parseInt(e.target.value, 10);
    if (textHeight) textHeight.textContent = `${val}cm`;
    if (sliderHeightMobile) sliderHeightMobile.value = val;
    if (textHeightMobile) textHeightMobile.textContent = `${val}cm`;
    state.setDeskHeight(val);
  });

  sliderHeightMobile?.addEventListener('input', (e) => {
    const val = parseInt(e.target.value, 10);
    if (textHeightMobile) textHeightMobile.textContent = `${val}cm`;
    if (sliderHeight) sliderHeight.value = val;
    if (textHeight) textHeight.textContent = `${val}cm`;
    state.setDeskHeight(val);
  });

  const sliderWidth = document.getElementById('sliderDeskWidth');
  const textWidth = document.getElementById('textDeskWidth');
  const sliderDepth = document.getElementById('sliderDeskDepth');
  const textDepth = document.getElementById('textDeskDepth');

  sliderWidth?.addEventListener('input', (e) => {
    const val = parseInt(e.target.value, 10);
    if (textWidth) textWidth.textContent = `${val}cm`;
    state.setDeskDimensions(val, state.deskDepth);
  });

  sliderDepth?.addEventListener('input', (e) => {
    const val = parseInt(e.target.value, 10);
    if (textDepth) textDepth.textContent = `${val}cm`;
    state.setDeskDimensions(state.deskWidth, val);
  });

  const sizePills = document.querySelectorAll('.size-pill');
  sizePills.forEach((pill) => {
    pill.addEventListener('click', () => {
      sizePills.forEach((p) => p.classList.remove('active'));
      pill.classList.add('active');
      const w = parseInt(pill.getAttribute('data-w'), 10);
      const d = parseInt(pill.getAttribute('data-d'), 10);
      if (sliderWidth) sliderWidth.value = w;
      if (sliderDepth) sliderDepth.value = d;
      if (textWidth) textWidth.textContent = `${w}cm`;
      if (textDepth) textDepth.textContent = `${d}cm`;
      state.setDeskDimensions(w, d);
      showToast(`Desk resized to ${w}cm × ${d}cm`);
    });
  });

  const finishSwatches = document.querySelectorAll('.finish-swatch');
  finishSwatches.forEach((swatch) => {
    swatch.addEventListener('click', () => {
      finishSwatches.forEach((s) => s.classList.remove('active'));
      swatch.classList.add('active');
      const finishKey = swatch.getAttribute('data-finish');
      state.setDeskFinish(finishKey);
      showToast(`Applied ${FINISHES[finishKey].name} desk finish.`);
    });
  });

  document.getElementById('btnCamPerspective')?.addEventListener('click', () => scene3D?.setCameraView('perspective'));
  document.getElementById('btnCamIso')?.addEventListener('click', () => scene3D?.setCameraView('isometric'));
  document.getElementById('btnCamTop')?.addEventListener('click', () => scene3D?.setCameraView('top'));
  document.getElementById('btnCamFront')?.addEventListener('click', () => scene3D?.setCameraView('front'));

  const btnGizmoTranslate = document.getElementById('btnGizmoTranslate');
  const btnGizmoRotate = document.getElementById('btnGizmoRotate');

  btnGizmoTranslate?.addEventListener('click', () => {
    btnGizmoTranslate.classList.add('active');
    btnGizmoRotate?.classList.remove('active');
    if (scene3D) scene3D.setGizmoMode('translate');
    showToast('3D Drag Handle Mode activated');
  });

  btnGizmoRotate?.addEventListener('click', () => {
    btnGizmoRotate.classList.add('active');
    btnGizmoTranslate?.classList.remove('active');
    if (scene3D) scene3D.setGizmoMode('rotate');
    showToast('3D Rotate Ring Mode activated');
  });

  document.getElementById('btnUndo')?.addEventListener('click', () => {
    state.undo();
    showToast('Undo action');
  });

  document.getElementById('btnRedo')?.addEventListener('click', () => {
    state.redo();
    showToast('Redo action');
  });

  document.getElementById('btnDeleteItem')?.addEventListener('click', () => {
    if (state.selectedItemId) {
      state.deleteSelectedItem();
      showToast('Item deleted.');
    }
  });

  document.getElementById('btnClear')?.addEventListener('click', () => {
    if (confirm('Are you sure you want to clear your current setup?')) {
      state.clearSetup();
      showToast('Setup cleared.');
    }
  });

  document.getElementById('btnExportSnapshot')?.addEventListener('click', () => {
    triggerSnapshotExport();
  });

  document.getElementById('btnExportConfig')?.addEventListener('click', () => {
    exportJSONConfig();
    showToast('Saved JSON setup config!');
  });

  const fileInput = document.getElementById('fileInputJSON');
  document.getElementById('btnImportConfig')?.addEventListener('click', () => fileInput?.click());
  fileInput?.addEventListener('change', async (e) => {
    if (e.target.files.length > 0) {
      try {
        await importJSONConfig(e.target.files[0]);
        showToast('Successfully imported setup config!');
      } catch (err) {
        showToast('Error loading JSON file.');
      }
    }
  });

  document.getElementById('btnExportCSV')?.addEventListener('click', () => {
    exportCSVBudget();
    showToast('Shopping list CSV exported!');
  });

  renderCategoryPills();
}

function initMobileUI() {
  const backdrop = document.getElementById('sheetBackdrop');
  const sidebarCatalog = document.getElementById('sidebarCatalog');
  const sidebarInspector = document.getElementById('sidebarInspector');
  const mobileMenuSheet = document.getElementById('mobileMenuSheet');

  const btnNavCatalog = document.getElementById('btnNavCatalog');
  const btnNavDesk = document.getElementById('btnNavDesk');
  const btnNavStats = document.getElementById('btnNavStats');
  const btnNavMenu = document.getElementById('btnNavMenu');
  const btnMobileMenu = document.getElementById('btnMobileMenu');

  const btnCloseCatalog = document.getElementById('btnCloseCatalog');
  const btnCloseInspector = document.getElementById('btnCloseInspector');
  const btnCloseMobileMenu = document.getElementById('btnCloseMobileMenu');

  function closeAllSheets() {
    sidebarCatalog?.classList.remove('open');
    sidebarInspector?.classList.remove('open');
    mobileMenuSheet?.classList.remove('open');
    backdrop?.classList.remove('active');

    btnNavCatalog?.classList.remove('active');
    btnNavDesk?.classList.remove('active');
    btnNavStats?.classList.remove('active');
    btnNavMenu?.classList.remove('active');
  }

  function openSheet(sheet, navBtn) {
    closeAllSheets();
    sheet?.classList.add('open');
    backdrop?.classList.add('active');
    navBtn?.classList.add('active');
  }

  backdrop?.addEventListener('click', closeAllSheets);
  btnCloseCatalog?.addEventListener('click', closeAllSheets);
  btnCloseInspector?.addEventListener('click', closeAllSheets);
  btnCloseMobileMenu?.addEventListener('click', closeAllSheets);

  btnNavCatalog?.addEventListener('click', () => {
    if (sidebarCatalog?.classList.contains('open')) {
      closeAllSheets();
    } else {
      openSheet(sidebarCatalog, btnNavCatalog);
    }
  });

  btnNavDesk?.addEventListener('click', () => {
    setInspectorTab('desk');
    if (sidebarInspector?.classList.contains('open') && sidebarInspector.getAttribute('data-tab') === 'desk') {
      closeAllSheets();
    } else {
      openSheet(sidebarInspector, btnNavDesk);
    }
  });

  btnNavStats?.addEventListener('click', () => {
    setInspectorTab('budget');
    if (sidebarInspector?.classList.contains('open') && sidebarInspector.getAttribute('data-tab') === 'budget') {
      closeAllSheets();
    } else {
      openSheet(sidebarInspector, btnNavStats);
    }
  });

  btnNavMenu?.addEventListener('click', () => {
    if (mobileMenuSheet?.classList.contains('open')) {
      closeAllSheets();
    } else {
      openSheet(mobileMenuSheet, btnNavMenu);
    }
  });

  btnMobileMenu?.addEventListener('click', () => {
    openSheet(mobileMenuSheet, btnNavMenu);
  });

  // Mobile Inspector Tabs
  function setInspectorTab(tabKey) {
    if (!sidebarInspector) return;
    sidebarInspector.setAttribute('data-tab', tabKey);
    const tabBtns = sidebarInspector.querySelectorAll('.tab-btn');
    tabBtns.forEach((btn) => {
      btn.classList.toggle('active', btn.getAttribute('data-tab') === tabKey);
    });
  }

  const inspectorTabs = document.querySelectorAll('.inspector-tabs .tab-btn');
  inspectorTabs.forEach((tabBtn) => {
    tabBtn.addEventListener('click', () => {
      const tab = tabBtn.getAttribute('data-tab');
      setInspectorTab(tab);
    });
  });

  // Default to desk tab on inspector
  if (sidebarInspector && !sidebarInspector.hasAttribute('data-tab')) {
    sidebarInspector.setAttribute('data-tab', 'desk');
  }

  // Mobile Menu Sheet Actions
  const presetSelectMobile = document.getElementById('presetSelectMobile');
  presetSelectMobile?.addEventListener('change', (e) => {
    if (e.target.value) {
      state.loadPreset(e.target.value);
      closeAllSheets();
      showToast('Loaded preset setup!');
      e.target.value = '';
    }
  });

  document.getElementById('btnMobileSnapshot')?.addEventListener('click', () => {
    closeAllSheets();
    triggerSnapshotExport();
  });

  document.getElementById('btnMobileExportConfig')?.addEventListener('click', () => {
    closeAllSheets();
    exportJSONConfig();
    showToast('Saved JSON setup config!');
  });

  const fileInput = document.getElementById('fileInputJSON');
  document.getElementById('btnMobileImportConfig')?.addEventListener('click', () => {
    closeAllSheets();
    fileInput?.click();
  });

  document.getElementById('btnMobileExportCSV')?.addEventListener('click', () => {
    closeAllSheets();
    exportCSVBudget();
    showToast('Shopping list CSV exported!');
  });

  document.getElementById('btnMobileClear')?.addEventListener('click', () => {
    closeAllSheets();
    document.getElementById('btnClear')?.click();
  });

  // Floating Item Actions
  document.getElementById('btnFloatingRotate')?.addEventListener('click', () => {
    if (state.selectedItemId) {
      const item = state.placedItems.find((i) => i.id === state.selectedItemId);
      if (item) {
        const newRot = (item.rotation + Math.PI / 4) % (Math.PI * 2);
        state.updateItemRotation(item.id, newRot);
        showToast('Rotated item 45°');
      }
    }
  });

  document.getElementById('btnFloatingDelete')?.addEventListener('click', () => {
    if (state.selectedItemId) {
      state.deleteSelectedItem();
      showToast('Item deleted.');
    }
  });

  document.getElementById('btnFloatingDeselect')?.addEventListener('click', () => {
    state.selectItem(null);
  });
}

function triggerSnapshotExport() {
  const targetCanvas = state.viewMode === '2d'
    ? document.getElementById('canvas2d')
    : document.getElementById('canvas3DContainer')?.querySelector('canvas');

  if (targetCanvas) {
    exportPNGSnapshot(targetCanvas);
    showToast('Snapshot PNG exported!');
  }
}

function setViewMode(mode) {
  state.setViewMode(mode);
  updateViewToggleUI(mode);
}

function updateViewToggleUI(mode) {
  const container3D = document.getElementById('canvas3DContainer');
  const canvas2DElement = document.getElementById('canvas2d');
  const btn3D = document.getElementById('btnView3D');
  const btn2D = document.getElementById('btnView2D');

  if (mode === '2d') {
    if (container3D) container3D.style.display = 'none';
    if (canvas2DElement) canvas2DElement.style.display = 'block';
    btn3D?.classList.remove('active');
    btn2D?.classList.add('active');
    if (canvas2D) canvas2D.resize();
  } else {
    if (container3D) container3D.style.display = 'block';
    if (canvas2DElement) canvas2DElement.style.display = 'none';
    btn3D?.classList.add('active');
    btn2D?.classList.remove('active');
    if (scene3D) scene3D.onWindowResize();
  }
}

function renderCategoryPills() {
  const container = document.getElementById('categoryPills');
  if (!container) return;
  container.innerHTML = `<button class="category-pill active" data-cat="all">All Items</button>`;

  CATEGORIES.forEach((cat) => {
    const pill = document.createElement('button');
    pill.className = 'category-pill';
    pill.setAttribute('data-cat', cat.id);
    pill.textContent = cat.name;
    pill.addEventListener('click', () => {
      document.querySelectorAll('.category-pill').forEach((p) => p.classList.remove('active'));
      pill.classList.add('active');
      activeCategory = cat.id;
      renderCatalog();
    });
    container.appendChild(pill);
  });
}

function renderCatalog() {
  const list = document.getElementById('catalogList');
  if (!list) return;
  list.innerHTML = '';

  const filtered = activeCategory === 'all'
    ? CATALOG_ITEMS
    : CATALOG_ITEMS.filter((i) => i.category === activeCategory);

  filtered.forEach((item) => {
    const card = document.createElement('div');
    card.className = 'catalog-card';

    card.innerHTML = `
      <div class="card-top">
        <span class="card-name">${item.name}</span>
        <span class="card-price">$${item.price}</span>
      </div>
      <div class="card-desc">${item.description}</div>
      <div class="card-foot">
        <span class="badge-dim">${item.dimensions.width}×${item.dimensions.depth}×${item.dimensions.height}cm</span>
        <span style="color: var(--accent-cyan); font-weight: 600;">+ Add</span>
      </div>
    `;

    card.addEventListener('click', () => {
      const added = state.addItem(item.id);
      if (added) {
        showToast(`Added ${item.name} to desk!`);
      } else {
        showToast(`Updated desk frame to ${item.name}`);
      }
    });

    list.appendChild(card);
  });
}

function handleKeyboardShortcuts(e) {
  if (e.key === 'r' || e.key === 'R') {
    if (state.selectedItemId) {
      const item = state.placedItems.find((i) => i.id === state.selectedItemId);
      if (item) {
        const newRot = (item.rotation + Math.PI / 4) % (Math.PI * 2);
        state.updateItemRotation(item.id, newRot);
        showToast('Rotated item 45°');
      }
    }
  }
  if (e.key === 'Delete' || e.key === 'Backspace') {
    if (state.selectedItemId && document.activeElement.tagName !== 'INPUT') {
      state.deleteSelectedItem();
      showToast('Item deleted.');
    }
  }
  if ((e.metaKey || e.ctrlKey) && e.key === 'z') {
    if (e.shiftKey) state.redo();
    else state.undo();
  }
  if ((e.metaKey || e.ctrlKey) && e.key === 'y') {
    state.redo();
  }
  if (e.key === 'Escape') {
    state.selectItem(null);
  }
}

function updateInspectorAndBudget() {
  const inspectorDetails = document.getElementById('selectedItemDetails');
  const floatingBar = document.getElementById('selectedItemFloatingBar');
  const floatingTitle = document.getElementById('floatingItemTitle');

  if (state.selectedItemId) {
    const selected = state.placedItems.find((i) => i.id === state.selectedItemId);
    const cat = CATALOG_ITEMS.find((c) => c.id === selected?.catalogId);

    if (cat && selected) {
      if (inspectorDetails) {
        inspectorDetails.innerHTML = `
          <div style="font-weight: 600; color: var(--text-main); margin-bottom: 4px;">${cat.name}</div>
          <div style="margin-bottom: 8px;">Category: <span style="color: var(--accent-cyan);">${cat.category}</span></div>
          <div style="display: flex; gap: 12px; margin-bottom: 8px;">
            <div>X: <strong style="color: var(--text-main);">${selected.x}cm</strong></div>
            <div>Z: <strong style="color: var(--text-main);">${selected.z}cm</strong></div>
            <div>Rot: <strong style="color: var(--text-main);">${Math.round((selected.rotation * 180) / Math.PI)}°</strong></div>
          </div>
          <button id="btnInspectorDelete" class="btn" style="width: 100%; color: var(--danger); border-color: color-mix(in srgb, var(--danger) 35%, transparent);">
            Remove Item
          </button>
        `;

        document.getElementById('btnInspectorDelete')?.addEventListener('click', () => {
          state.deleteSelectedItem();
          showToast('Item removed.');
        });
      }

      // Show floating item action bar
      if (floatingBar) {
        floatingBar.style.display = 'flex';
        if (floatingTitle) floatingTitle.textContent = cat.name;
      }
    }
  } else {
    if (inspectorDetails) {
      inspectorDetails.innerHTML = `
        <div style="font-size: 12px; color: var(--text-muted); text-align: center; padding: 12px 0;">
          Click any object in 3D or 2D view to inspect dimensions & position.
        </div>
      `;
    }
    if (floatingBar) {
      floatingBar.style.display = 'none';
    }
  }

  // Update desk sliders if changed from outside (presets, undo/redo)
  const sliderHeight = document.getElementById('sliderDeskHeight');
  const textHeight = document.getElementById('textDeskHeight');
  const sliderHeightMobile = document.getElementById('sliderDeskHeightMobile');
  const textHeightMobile = document.getElementById('textDeskHeightMobile');
  if (sliderHeight && sliderHeight.value != state.deskHeight) sliderHeight.value = state.deskHeight;
  if (sliderHeightMobile && sliderHeightMobile.value != state.deskHeight) sliderHeightMobile.value = state.deskHeight;
  if (textHeight) textHeight.textContent = `${state.deskHeight}cm`;
  if (textHeightMobile) textHeightMobile.textContent = `${state.deskHeight}cm`;

  const sliderWidth = document.getElementById('sliderDeskWidth');
  const textWidth = document.getElementById('textDeskWidth');
  if (sliderWidth && sliderWidth.value != state.deskWidth) sliderWidth.value = state.deskWidth;
  if (textWidth) textWidth.textContent = `${state.deskWidth}cm`;

  const sliderDepth = document.getElementById('sliderDeskDepth');
  const textDepth = document.getElementById('textDeskDepth');
  if (sliderDepth && sliderDepth.value != state.deskDepth) sliderDepth.value = state.deskDepth;
  if (textDepth) textDepth.textContent = `${state.deskDepth}cm`;

  const ergo = state.getErgonomicAssessment();
  const ergoScoreVal = document.getElementById('ergoScoreValue');
  if (ergoScoreVal) ergoScoreVal.textContent = `${ergo.score}%`;

  const ergoTipsContainer = document.getElementById('ergoTipsList');
  if (ergoTipsContainer) {
    ergoTipsContainer.innerHTML = ergo.tips.map((tip) => `<div>${tip}</div>`).join('');
  }

  const budget = state.getCalculatedBudget();
  const budgetListContainer = document.getElementById('budgetItemsList');
  if (budgetListContainer) {
    budgetListContainer.innerHTML = budget.breakdown
      .map(
        (item) => `
        <div class="budget-item">
          <span>${item.name}</span>
          <span style="font-weight: 600;">$${item.price}</span>
        </div>
      `
      )
      .join('');
  }

  const textTotalCost = document.getElementById('textTotalCost');
  if (textTotalCost) {
    textTotalCost.textContent = `$${budget.totalPrice.toLocaleString()}`;
  }
}

function showToast(message) {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = message;
  toast.classList.add('show');

  setTimeout(() => {
    toast.classList.remove('show');
  }, 2500);
}
