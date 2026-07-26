import confetti from 'canvas-confetti';
import { state } from './state.js';
import { CATALOG_ITEMS } from './catalog.js';

export function exportPNGSnapshot(canvasElement, title = 'My DESKForge Setup') {
  if (!canvasElement) return;

  const tempCanvas = document.createElement('canvas');
  tempCanvas.width = canvasElement.width;
  tempCanvas.height = canvasElement.height;
  const ctx = tempCanvas.getContext('2d');

  ctx.drawImage(canvasElement, 0, 0);

  ctx.save();
  const padding = 20;
  const badgeWidth = 260;
  const badgeHeight = 50;

  const x = tempCanvas.width - badgeWidth - padding;
  const y = tempCanvas.height - badgeHeight - padding;

  ctx.fillStyle = 'rgba(15, 23, 42, 0.85)';
  ctx.strokeStyle = '#38bdf8';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.roundRect(x, y, badgeWidth, badgeHeight, 10);
  ctx.fill();
  ctx.stroke();

  ctx.fillStyle = '#f8fafc';
  ctx.font = 'bold 16px Inter, sans-serif';
  ctx.fillText('DESKForge Studio', x + 16, y + 24);

  ctx.fillStyle = '#38bdf8';
  ctx.font = '12px Inter, sans-serif';
  ctx.fillText(title, x + 16, y + 40);
  ctx.restore();

  const link = document.createElement('a');
  link.download = `desk-setup-${Date.now()}.png`;
  link.href = tempCanvas.toDataURL('image/png');
  link.click();

  confetti({
    particleCount: 60,
    spread: 70,
    origin: { y: 0.8 },
  });
}

export function exportJSONConfig() {
  const data = {
    version: '1.0',
    createdAt: new Date().toISOString(),
    deskHeight: state.deskHeight,
    deskFinish: state.deskFinish,
    placedItems: state.placedItems,
    budget: state.getCalculatedBudget(),
  };

  const jsonStr = JSON.stringify(data, null, 2);
  const blob = new Blob([jsonStr], { type: 'application/json' });
  const link = document.createElement('a');
  link.download = `desk-config-${Date.now()}.json`;
  link.href = URL.createObjectURL(blob);
  link.click();
}

export function importJSONConfig(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target.result);
        if (data.placedItems && Array.isArray(data.placedItems)) {
          state.deskHeight = data.deskHeight || 74;
          state.deskFinish = data.deskFinish || 'walnut';
          state.placedItems = data.placedItems;
          state.selectedItemId = null;
          state.saveHistoryState();
          state.notify('import');
          resolve(true);
        } else {
          reject(new Error('Invalid desk configuration file.'));
        }
      } catch (err) {
        reject(err);
      }
    };
    reader.readAsText(file);
  });
}

export function exportCSVBudget() {
  const budget = state.getCalculatedBudget();
  let csvContent = 'data:text/csv;charset=utf-8,Item Name,Estimated Price (USD)\n';

  budget.breakdown.forEach((item) => {
    csvContent += `"${item.name}",$${item.price}\n`;
  });

  csvContent += `"\nTOTAL ESTIMATED SETUP COST","$${budget.totalPrice}"\n`;

  const encodedUri = encodeURI(csvContent);
  const link = document.createElement('a');
  link.setAttribute('href', encodedUri);
  link.setAttribute('download', `desk-budget-${Date.now()}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
