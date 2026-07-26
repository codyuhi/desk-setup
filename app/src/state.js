// Central Application State for DESKForge Studio

import { PRESET_SETUPS, CATALOG_ITEMS, FINISHES } from './catalog.js';

class AppState {
  constructor() {
    this.deskFinish = 'walnut';
    this.deskHeight = 74; // cm
    this.deskWidth = 160; // cm (100 to 240)
    this.deskDepth = 80;  // cm (50 to 120)
    this.viewMode = '3d'; // '3d' | '2d'
    this.selectedItemId = null;

    this.placedItems = [];

    // History stack for Undo/Redo
    this.history = [];
    this.historyIndex = -1;

    this.listeners = [];

    this.loadPreset('preset-developer');
  }

  subscribe(listener) {
    this.listeners.push(listener);
  }

  notify(changeType = 'general') {
    this.listeners.forEach((fn) => fn(this, changeType));
  }

  saveHistoryState() {
    const snap = JSON.stringify({
      deskFinish: this.deskFinish,
      deskHeight: this.deskHeight,
      deskWidth: this.deskWidth,
      deskDepth: this.deskDepth,
      placedItems: this.placedItems,
    });

    if (this.historyIndex >= 0 && this.history[this.historyIndex] === snap) {
      return;
    }

    this.history = this.history.slice(0, this.historyIndex + 1);
    this.history.push(snap);
    this.historyIndex = this.history.length - 1;
  }

  undo() {
    if (this.historyIndex > 0) {
      this.historyIndex--;
      const snap = JSON.parse(this.history[this.historyIndex]);
      this.deskFinish = snap.deskFinish;
      this.deskHeight = snap.deskHeight;
      this.deskWidth = snap.deskWidth || 160;
      this.deskDepth = snap.deskDepth || 80;
      this.placedItems = snap.placedItems;
      this.selectedItemId = null;
      this.notify('undo');
    }
  }

  redo() {
    if (this.historyIndex < this.history.length - 1) {
      this.historyIndex++;
      const snap = JSON.parse(this.history[this.historyIndex]);
      this.deskFinish = snap.deskFinish;
      this.deskHeight = snap.deskHeight;
      this.deskWidth = snap.deskWidth || 160;
      this.deskDepth = snap.deskDepth || 80;
      this.placedItems = snap.placedItems;
      this.selectedItemId = null;
      this.notify('redo');
    }
  }

  setViewMode(mode) {
    this.viewMode = mode;
    this.notify('viewMode');
  }

  setDeskFinish(finishKey) {
    if (FINISHES[finishKey]) {
      this.deskFinish = finishKey;
      this.saveHistoryState();
      this.notify('deskFinish');
    }
  }

  setDeskHeight(heightCm) {
    this.deskHeight = Math.max(65, Math.min(120, heightCm));
    this.saveHistoryState();
    this.notify('deskHeight');
  }

  setDeskDimensions(widthCm, depthCm) {
    this.deskWidth = Math.max(100, Math.min(240, widthCm));
    this.deskDepth = Math.max(50, Math.min(120, depthCm));
    this.saveHistoryState();
    this.notify('deskDimensions');
  }

  addItem(catalogId, x = 0, z = 0, rotation = 0) {
    const catItem = CATALOG_ITEMS.find((i) => i.id === catalogId);
    if (!catItem) return false;

    if (catItem.isDeskBase) {
      this.setDeskFinish(catItem.defaultFinish);
      return false;
    }

    const newItem = {
      id: 'item_' + Date.now() + '_' + Math.floor(Math.random() * 1000),
      catalogId,
      x,
      z,
      rotation,
    };

    this.placedItems.push(newItem);
    this.selectedItemId = newItem.id;
    this.saveHistoryState();
    this.notify('addItem');
    return true;
  }

  selectItem(id) {
    this.selectedItemId = id;
    this.notify('selection');
  }

  updateItemPosition(id, x, z) {
    const item = this.placedItems.find((i) => i.id === id);
    if (item) {
      item.x = Math.round(x);
      item.z = Math.round(z);
      this.notify('updatePosition');
    }
  }

  updateItemRotation(id, rotationRad) {
    const item = this.placedItems.find((i) => i.id === id);
    if (item) {
      item.rotation = rotationRad;
      this.saveHistoryState();
      this.notify('updateRotation');
    }
  }

  deleteSelectedItem() {
    if (!this.selectedItemId) return;
    this.placedItems = this.placedItems.filter((i) => i.id !== this.selectedItemId);
    this.selectedItemId = null;
    this.saveHistoryState();
    this.notify('deleteItem');
  }

  clearSetup() {
    this.placedItems = [];
    this.selectedItemId = null;
    this.saveHistoryState();
    this.notify('clear');
  }

  loadPreset(presetId) {
    const preset = PRESET_SETUPS.find((p) => p.id === presetId);
    if (!preset) return;

    this.deskFinish = preset.deskFinish || 'walnut';
    this.deskHeight = preset.deskHeight || 74;
    this.deskWidth = preset.deskWidth || 160;
    this.deskDepth = preset.deskDepth || 80;
    this.placedItems = preset.items.map((i, idx) => ({
      id: `preset_item_${idx}_${Date.now()}`,
      catalogId: i.catalogId,
      x: i.x,
      z: i.z,
      rotation: i.rotation || 0,
    }));
    this.selectedItemId = null;

    this.saveHistoryState();
    this.notify('loadPreset');
  }

  getCalculatedBudget() {
    let basePrice = 499;
    if (this.deskFinish === 'walnut') basePrice = 649;
    if (this.deskFinish === 'oak') basePrice = 549;

    // Adjust desk base price proportionally to surface area (m^2)
    const areaSqM = (this.deskWidth * this.deskDepth) / (160 * 80);
    const calculatedDeskPrice = Math.round(basePrice * areaSqM);

    const breakdown = [
      {
        name: `Desk Frame & Surface (${FINISHES[this.deskFinish]?.name || 'Walnut'} - ${this.deskWidth}×${this.deskDepth}cm)`,
        price: calculatedDeskPrice,
      },
    ];

    let totalPrice = calculatedDeskPrice;

    this.placedItems.forEach((placed) => {
      const cat = CATALOG_ITEMS.find((c) => c.id === placed.catalogId);
      if (cat) {
        breakdown.push({ name: cat.name, price: cat.price });
        totalPrice += cat.price;
      }
    });

    return { totalPrice, breakdown };
  }

  getErgonomicAssessment() {
    let score = 100;
    const tips = [];

    const hasMonitor = this.placedItems.some((i) => {
      const cat = CATALOG_ITEMS.find((c) => c.id === i.catalogId);
      return cat?.category === 'monitors';
    });

    const hasKeyboard = this.placedItems.some((i) => i.catalogId === 'keyboard-mech-tkl');
    const hasLaptopStand = this.placedItems.some((i) => i.catalogId === 'laptop-stand-open');
    const hasLightBar = this.placedItems.some((i) => i.catalogId === 'light-monitor-bar');

    if (this.deskHeight > 80) {
      score -= 10;
      tips.push('⚠️ Desk height is above 80cm; consider adding a footrest or adjusting chair height.');
    }
    if (this.deskHeight < 68) {
      score -= 10;
      tips.push('⚠️ Desk height is low; ensure sufficient leg clearance.');
    }
    if (!hasMonitor && !hasLaptopStand) {
      score -= 20;
      tips.push('⚠️ No elevated display detected. Looking down causes neck strain.');
    }
    if (!hasKeyboard) {
      score -= 15;
      tips.push('💡 Add an ergonomic keyboard positioned 15-20cm from desk edge.');
    }
    if (hasLightBar) {
      score += 5;
      tips.push('✨ Monitor light bar reduces eye fatigue during evening work.');
    }

    if (tips.length === 0) {
      tips.push('✅ Ideal ergonomic alignment! Elbows at 90°, eyes aligned with top third of screen.');
    }

    return { score: Math.max(40, Math.min(100, score)), tips };
  }
}

export const state = new AppState();
