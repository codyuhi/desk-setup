// 2D Interactive Blueprint Manager for DESKForge Studio

import { state } from './state.js';
import { CATALOG_ITEMS } from './catalog.js';
import { cssVar } from './theme.js';

export class Canvas2DManager {
  constructor(canvasElement) {
    this.canvas = canvasElement;
    this.ctx = this.canvas.getContext('2d');

    this.scale = 2.4; // pixels per cm
    this.offsetX = 0;
    this.offsetY = 0;

    this.isDragging = false;
    this.draggedItemId = null;
    this.dragStartPos = { x: 0, z: 0 };

    this.resize();
    window.addEventListener('resize', () => this.resize());

    state.subscribe(() => this.draw());
    window.addEventListener('themechange', () => this.draw());

    this.initEvents();
  }

  resize() {
    if (!this.canvas.parentElement) return;
    this.canvas.width = this.canvas.parentElement.clientWidth;
    this.canvas.height = this.canvas.parentElement.clientHeight;
    this.draw();
  }

  initEvents() {
    this.canvas.addEventListener('pointerdown', (e) => this.onPointerDown(e));
    this.canvas.addEventListener('pointermove', (e) => this.onPointerMove(e));
    this.canvas.addEventListener('pointerup', (e) => this.onPointerUp(e));
    this.canvas.addEventListener('pointercancel', (e) => this.onPointerUp(e));
  }

  getDeskCenter() {
    return {
      x: this.canvas.width / 2,
      y: this.canvas.height / 2,
    };
  }

  cmToPx(cmX, cmZ) {
    const center = this.getDeskCenter();
    return {
      x: center.x + cmX * this.scale,
      y: center.y + cmZ * this.scale,
    };
  }

  pxToCm(pxX, pxY) {
    const center = this.getDeskCenter();
    return {
      x: Math.round((pxX - center.x) / this.scale),
      z: Math.round((pxY - center.y) / this.scale),
    };
  }

  draw() {
    const w = this.canvas.width;
    const h = this.canvas.height;
    if (w === 0 || h === 0) return;

    this.ctx.clearRect(0, 0, w, h);

    // Draw background grid
    this.drawGrid();

    // Draw Desk Top surface rect using dynamic state.deskWidth and state.deskDepth
    const center = this.getDeskCenter();
    const deskWpx = state.deskWidth * this.scale;
    const deskDpx = state.deskDepth * this.scale;

    this.ctx.save();
    this.ctx.fillStyle = cssVar('--blueprint-desk');
    this.ctx.strokeStyle = cssVar('--blueprint-accent');
    this.ctx.lineWidth = 2;

    this.ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';
    this.ctx.shadowBlur = 20;

    this.ctx.fillRect(center.x - deskWpx / 2, center.y - deskDpx / 2, deskWpx, deskDpx);
    this.ctx.strokeRect(center.x - deskWpx / 2, center.y - deskDpx / 2, deskWpx, deskDpx);
    this.ctx.restore();

    // Draw dimension tags for desk width and depth
    this.ctx.save();
    this.ctx.fillStyle = cssVar('--blueprint-label');
    this.ctx.font = "500 12px 'Plus Jakarta Sans', sans-serif";

    // Width label above desk
    this.ctx.textAlign = 'center';
    this.ctx.fillText(`${state.deskWidth} cm`, center.x, center.y - deskDpx / 2 - 12);

    // Depth label left of desk
    this.ctx.textAlign = 'right';
    this.ctx.fillText(`${state.deskDepth} cm`, center.x - deskWpx / 2 - 12, center.y + 4);
    this.ctx.restore();

    // Draw under-desk standing frame clearance overlay if enabled
    if (state.showUnderDeskFrame) {
      this.drawUnderDeskFrame(center, deskWpx, deskDpx);
    }

    // Draw placed items (flat deskpads and under-desk items first, then others)
    const sortedItems = [...state.placedItems].sort((a, b) => {
      const catA = CATALOG_ITEMS.find((c) => c.id === a.catalogId);
      const catB = CATALOG_ITEMS.find((c) => c.id === b.catalogId);
      const heightA = catA?.underDesk ? -10 : (catA?.dimensions?.height || 0);
      const heightB = catB?.underDesk ? -10 : (catB?.dimensions?.height || 0);
      return heightA - heightB;
    });

    sortedItems.forEach((item) => {
      const cat = CATALOG_ITEMS.find((c) => c.id === item.catalogId);
      if (!cat || cat.isDeskBase) return;

      const pos = this.cmToPx(item.x, item.z);
      const isSelected = item.id === state.selectedItemId;

      const itemW = (cat.dimensions.width || 20) * this.scale;
      const itemD = (cat.dimensions.depth || 20) * this.scale;

      this.ctx.save();
      this.ctx.translate(pos.x, pos.y);
      this.ctx.rotate(item.rotation);

      if (cat.underDesk) {
        // Under-desk mounting accessory
        this.ctx.fillStyle = isSelected ? 'rgba(56, 189, 248, 0.28)' : 'rgba(56, 189, 248, 0.16)';
        this.ctx.strokeStyle = isSelected ? '#0284c7' : '#38bdf8';
        this.ctx.lineWidth = isSelected ? 2.5 : 1.5;
        this.ctx.setLineDash([4, 3]);

        this.ctx.fillRect(-itemW / 2, -itemD / 2, itemW, itemD);
        this.ctx.strokeRect(-itemW / 2, -itemD / 2, itemW, itemD);
        this.ctx.setLineDash([]);

        this.ctx.fillStyle = isSelected ? '#0284c7' : '#0369a1';
        this.ctx.font = "600 10px 'Plus Jakarta Sans', sans-serif";
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        const shortName = cat.name.split(' ')[0];
        this.ctx.fillText(`${shortName} [Under]`, 0, 0);
      } else {
        this.ctx.fillStyle = isSelected ? cssVar('--blueprint-select-fill') : cssVar('--blueprint-item');
        this.ctx.strokeStyle = isSelected ? cssVar('--blueprint-accent') : cssVar('--blueprint-item-stroke');
        this.ctx.lineWidth = isSelected ? 2.5 : 1.5;

        this.ctx.fillRect(-itemW / 2, -itemD / 2, itemW, itemD);
        this.ctx.strokeRect(-itemW / 2, -itemD / 2, itemW, itemD);

        this.ctx.fillStyle = isSelected ? cssVar('--blueprint-accent') : cssVar('--blueprint-item-label');
        this.ctx.font = "500 11px 'Plus Jakarta Sans', sans-serif";
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        const shortName = cat.name.split(' ')[0];
        this.ctx.fillText(shortName, 0, 0);
      }

      this.ctx.restore();

      // Render clamp indicators & collision warnings for monitor arms & clamped items
      const collision = state.checkClampCollision(item);
      if (collision && (state.showUnderDeskFrame || isSelected || collision.collides)) {
        this.drawClampIndicator(item, cat, collision);
      }
    });
  }

  drawClampIndicator(item, cat, collision) {
    const depth = cat.dimensions?.depth || 20;
    const clampPos = this.cmToPx(collision.clampX, collision.clampZ);
    const clampW = 10 * this.scale;
    const clampD = 6 * this.scale;

    this.ctx.save();
    this.ctx.translate(clampPos.x, clampPos.y);
    this.ctx.rotate(item.rotation);

    if (collision.collides) {
      // Red collision alert
      this.ctx.fillStyle = 'rgba(239, 68, 68, 0.4)';
      this.ctx.strokeStyle = '#ef4444';
      this.ctx.lineWidth = 2.5;

      this.ctx.fillRect(-clampW / 2, -clampD / 2, clampW, clampD);
      this.ctx.strokeRect(-clampW / 2, -clampD / 2, clampW, clampD);

      // Warning text badge
      this.ctx.setLineDash([]);
      this.ctx.fillStyle = '#ef4444';
      this.ctx.font = "700 9px 'Plus Jakarta Sans', sans-serif";
      this.ctx.textAlign = 'center';
      this.ctx.textBaseline = 'bottom';
      this.ctx.fillText('⚠️ CLAMP COLLISION', 0, -clampD / 2 - 2);
    } else {
      // Clear green indicator
      this.ctx.fillStyle = 'rgba(16, 185, 129, 0.25)';
      this.ctx.strokeStyle = '#10b981';
      this.ctx.lineWidth = 1.5;

      this.ctx.fillRect(-clampW / 2, -clampD / 2, clampW, clampD);
      this.ctx.strokeRect(-clampW / 2, -clampD / 2, clampW, clampD);

      this.ctx.fillStyle = '#059669';
      this.ctx.font = "600 9px 'Plus Jakarta Sans', sans-serif";
      this.ctx.textAlign = 'center';
      this.ctx.textBaseline = 'bottom';
      this.ctx.fillText('✓ Clamp OK', 0, -clampD / 2 - 2);
    }

    this.ctx.restore();
  }

  drawUnderDeskFrame(center, deskWpx, deskDpx) {
    const legSpanX = state.deskWidth * 0.35 * this.scale;
    const bracketHalfD = state.deskDepth * 0.35 * this.scale;
    const bracketW = 10 * this.scale;
    const columnW = 8 * this.scale;
    const columnD = 10 * this.scale;
    const beamH = 8 * this.scale;

    this.ctx.save();

    // 1. Central Crossbar Beam Zone
    this.ctx.fillStyle = 'rgba(245, 158, 11, 0.14)';
    this.ctx.strokeStyle = 'rgba(217, 119, 6, 0.8)';
    this.ctx.lineWidth = 1.5;
    this.ctx.setLineDash([4, 4]);

    const beamW = legSpanX * 2;
    this.ctx.fillRect(center.x - legSpanX, center.y - beamH / 2, beamW, beamH);
    this.ctx.strokeRect(center.x - legSpanX, center.y - beamH / 2, beamW, beamH);

    this.ctx.setLineDash([]);
    this.ctx.fillStyle = 'rgba(180, 83, 9, 0.95)';
    this.ctx.font = "600 10px 'Plus Jakarta Sans', sans-serif";
    this.ctx.textAlign = 'center';
    this.ctx.textBaseline = 'middle';
    this.ctx.fillText('CROSSBAR BEAM (NO CLAMP ZONE)', center.x, center.y);

    // 2. Leg Brackets and Motor Columns
    const drawLeg = (legCenterX, sideLabel) => {
      // Bracket zone
      this.ctx.fillStyle = 'rgba(245, 158, 11, 0.08)';
      this.ctx.strokeStyle = 'rgba(245, 158, 11, 0.6)';
      this.ctx.setLineDash([3, 3]);
      this.ctx.fillRect(legCenterX - bracketW / 2, center.y - bracketHalfD, bracketW, bracketHalfD * 2);
      this.ctx.strokeRect(legCenterX - bracketW / 2, center.y - bracketHalfD, bracketW, bracketHalfD * 2);

      // Motor column (solid exclusion)
      this.ctx.fillStyle = 'rgba(239, 68, 68, 0.2)';
      this.ctx.strokeStyle = 'rgba(220, 38, 38, 0.85)';
      this.ctx.setLineDash([]);
      this.ctx.fillRect(legCenterX - columnW / 2, center.y - columnD / 2, columnW, columnD);
      this.ctx.strokeRect(legCenterX - columnW / 2, center.y - columnD / 2, columnW, columnD);

      this.ctx.fillStyle = 'rgba(185, 28, 28, 0.95)';
      this.ctx.font = "600 9px 'Plus Jakarta Sans', sans-serif";
      this.ctx.fillText(`${sideLabel} Motor Column`, legCenterX, center.y + columnD / 2 + 10);
    };

    drawLeg(center.x - legSpanX, 'Left');
    drawLeg(center.x + legSpanX, 'Right');

    // 3. Rear Edge Clamp Clearance Zone (8cm strip along rear desk edge)
    const rearZoneD = 8 * this.scale;
    const rearTopY = center.y - deskDpx / 2;
    this.ctx.fillStyle = 'rgba(16, 185, 129, 0.1)';
    this.ctx.strokeStyle = 'rgba(16, 185, 129, 0.6)';
    this.ctx.setLineDash([5, 3]);
    this.ctx.fillRect(center.x - deskWpx / 2, rearTopY, deskWpx, rearZoneD);
    this.ctx.strokeRect(center.x - deskWpx / 2, rearTopY, deskWpx, rearZoneD);

    this.ctx.setLineDash([]);
    this.ctx.fillStyle = 'rgba(5, 150, 105, 0.95)';
    this.ctx.font = "600 10px 'Plus Jakarta Sans', sans-serif";
    this.ctx.fillText('REAR CLAMP CLEARANCE ZONE (8cm)', center.x, rearTopY + rearZoneD / 2);

    this.ctx.restore();
  }

  drawGrid() {
    const w = this.canvas.width;
    const h = this.canvas.height;
    const center = this.getDeskCenter();

    this.ctx.save();

    // When snapToGrid is enabled, render 5cm sub-grid lines
    if (state.snapToGrid) {
      const step5cm = 5 * this.scale;
      this.ctx.strokeStyle = cssVar('--blueprint-grid');
      this.ctx.globalAlpha = 0.45;
      this.ctx.lineWidth = 0.5;

      const startX = center.x % step5cm;
      for (let x = startX; x < w; x += step5cm) {
        this.ctx.beginPath();
        this.ctx.moveTo(x, 0);
        this.ctx.lineTo(x, h);
        this.ctx.stroke();
      }

      const startY = center.y % step5cm;
      for (let y = startY; y < h; y += step5cm) {
        this.ctx.beginPath();
        this.ctx.moveTo(0, y);
        this.ctx.lineTo(w, y);
        this.ctx.stroke();
      }
      this.ctx.globalAlpha = 1.0;
    }

    const gridSize = 20 * this.scale; // 20cm grid lines
    this.ctx.strokeStyle = cssVar('--blueprint-grid');
    this.ctx.lineWidth = 1;

    const startMajorX = center.x % gridSize;
    for (let x = startMajorX; x < w; x += gridSize) {
      this.ctx.beginPath();
      this.ctx.moveTo(x, 0);
      this.ctx.lineTo(x, h);
      this.ctx.stroke();
    }

    const startMajorY = center.y % gridSize;
    for (let y = startMajorY; y < h; y += gridSize) {
      this.ctx.beginPath();
      this.ctx.moveTo(0, y);
      this.ctx.lineTo(w, y);
      this.ctx.stroke();
    }

    this.ctx.restore();
  }

  onPointerDown(event) {
    const rect = this.canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    let clickedItemId = null;

    for (let i = state.placedItems.length - 1; i >= 0; i--) {
      const item = state.placedItems[i];
      const cat = CATALOG_ITEMS.find((c) => c.id === item.catalogId);
      if (!cat || cat.isDeskBase) continue;

      const pos = this.cmToPx(item.x, item.z);
      const halfW = ((cat.dimensions.width || 20) * this.scale) / 2;
      const halfD = ((cat.dimensions.depth || 20) * this.scale) / 2;

      if (
        mouseX >= pos.x - halfW &&
        mouseX <= pos.x + halfW &&
        mouseY >= pos.y - halfD &&
        mouseY <= pos.y + halfD
      ) {
        clickedItemId = item.id;
        break;
      }
    }

    state.selectItem(clickedItemId);

    if (clickedItemId) {
      this.isDragging = true;
      this.draggedItemId = clickedItemId;
      try {
        this.canvas.setPointerCapture(event.pointerId);
      } catch (err) {
        // Fallback if not supported
      }
    }
  }

  onPointerMove(event) {
    if (!this.isDragging || !this.draggedItemId) return;

    const rect = this.canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    let cmPos = this.pxToCm(mouseX, mouseY);
    if (state.snapToGrid) {
      cmPos.x = Math.round(cmPos.x / 5) * 5;
      cmPos.z = Math.round(cmPos.z / 5) * 5;
    }
    state.updateItemPosition(this.draggedItemId, cmPos.x, cmPos.z);
  }

  onPointerUp(event) {
    if (this.isDragging && event?.pointerId) {
      try {
        this.canvas.releasePointerCapture(event.pointerId);
      } catch (err) {
        // Fallback
      }
    }
    this.isDragging = false;
    this.draggedItemId = null;
  }
}
