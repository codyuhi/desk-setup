// 2D Interactive Blueprint Manager for DESKForge Studio

import { state } from './state.js';
import { CATALOG_ITEMS } from './catalog.js';

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

    this.initEvents();
  }

  resize() {
    if (!this.canvas.parentElement) return;
    this.canvas.width = this.canvas.parentElement.clientWidth;
    this.canvas.height = this.canvas.parentElement.clientHeight;
    this.draw();
  }

  initEvents() {
    this.canvas.addEventListener('mousedown', (e) => this.onMouseDown(e));
    this.canvas.addEventListener('mousemove', (e) => this.onMouseMove(e));
    this.canvas.addEventListener('mouseup', () => this.onMouseUp());
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
    this.ctx.fillStyle = '#262f40';
    this.ctx.strokeStyle = '#38bdf8';
    this.ctx.lineWidth = 2;

    this.ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';
    this.ctx.shadowBlur = 20;

    this.ctx.fillRect(center.x - deskWpx / 2, center.y - deskDpx / 2, deskWpx, deskDpx);
    this.ctx.strokeRect(center.x - deskWpx / 2, center.y - deskDpx / 2, deskWpx, deskDpx);
    this.ctx.restore();

    // Draw dimension tags for desk width and depth
    this.ctx.save();
    this.ctx.fillStyle = '#94a3b8';
    this.ctx.font = '500 12px Inter, sans-serif';

    // Width label above desk
    this.ctx.textAlign = 'center';
    this.ctx.fillText(`${state.deskWidth} cm`, center.x, center.y - deskDpx / 2 - 12);

    // Depth label left of desk
    this.ctx.textAlign = 'right';
    this.ctx.fillText(`${state.deskDepth} cm`, center.x - deskWpx / 2 - 12, center.y + 4);
    this.ctx.restore();

    // Draw placed items
    state.placedItems.forEach((item) => {
      const cat = CATALOG_ITEMS.find((c) => c.id === item.catalogId);
      if (!cat || cat.isDeskBase) return;

      const pos = this.cmToPx(item.x, item.z);
      const isSelected = item.id === state.selectedItemId;

      const itemW = (cat.dimensions.width || 20) * this.scale;
      const itemD = (cat.dimensions.depth || 20) * this.scale;

      this.ctx.save();
      this.ctx.translate(pos.x, pos.y);
      this.ctx.rotate(item.rotation);

      this.ctx.fillStyle = isSelected ? 'rgba(56, 189, 248, 0.25)' : 'rgba(30, 41, 59, 0.85)';
      this.ctx.strokeStyle = isSelected ? '#38bdf8' : '#64748b';
      this.ctx.lineWidth = isSelected ? 2.5 : 1.5;

      this.ctx.fillRect(-itemW / 2, -itemD / 2, itemW, itemD);
      this.ctx.strokeRect(-itemW / 2, -itemD / 2, itemW, itemD);

      this.ctx.fillStyle = isSelected ? '#38bdf8' : '#cbd5e1';
      this.ctx.font = '500 11px Inter, sans-serif';
      this.ctx.textAlign = 'center';
      this.ctx.textBaseline = 'middle';

      const shortName = cat.name.split(' ')[0];
      this.ctx.fillText(shortName, 0, 0);

      this.ctx.restore();
    });
  }

  drawGrid() {
    const w = this.canvas.width;
    const h = this.canvas.height;
    const gridSize = 20 * this.scale; // 20cm grid lines

    this.ctx.save();
    this.ctx.strokeStyle = 'rgba(51, 65, 85, 0.35)';
    this.ctx.lineWidth = 1;

    for (let x = 0; x < w; x += gridSize) {
      this.ctx.beginPath();
      this.ctx.moveTo(x, 0);
      this.ctx.lineTo(x, h);
      this.ctx.stroke();
    }

    for (let y = 0; y < h; y += gridSize) {
      this.ctx.beginPath();
      this.ctx.moveTo(0, y);
      this.ctx.lineTo(w, y);
      this.ctx.stroke();
    }

    this.ctx.restore();
  }

  onMouseDown(event) {
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
    }
  }

  onMouseMove(event) {
    if (!this.isDragging || !this.draggedItemId) return;

    const rect = this.canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    const cmPos = this.pxToCm(mouseX, mouseY);
    state.updateItemPosition(this.draggedItemId, cmPos.x, cmPos.z);
  }

  onMouseUp() {
    this.isDragging = false;
    this.draggedItemId = null;
  }
}
