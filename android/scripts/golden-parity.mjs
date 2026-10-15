// Dumps budget/ergonomics golden values from the web app's own app/src/state.js for GoldenParityTest.
// Usage (from android/): node scripts/golden-parity.mjs .. > app/src/test/resources/golden/web-parity.json
const root = (await import('node:path')).resolve(process.argv[2] ?? '..');
const { state } = await import(`${root}/app/src/state.js`);
const { PRESET_SETUPS, CATALOG_ITEMS } = await import(`${root}/app/src/catalog.js`);
const cases = [];
const snap = (name) => cases.push({
  name,
  setup: { deskFinish: state.deskFinish, deskHeight: state.deskHeight, deskWidth: state.deskWidth, deskDepth: state.deskDepth,
    placedItems: state.placedItems.map(({ catalogId, x, z, rotation }) => ({ catalogId, x, z, rotation })) },
  budget: state.getCalculatedBudget(),
  ergonomics: state.getErgonomicAssessment(),
});
for (const p of PRESET_SETUPS) { state.loadPreset(p.id); snap(p.id); }
state.loadPreset('preset-gamer'); state.setDeskHeight(85); state.setDeskDimensions(200, 100); snap('gamer-tall-wide');
state.loadPreset('preset-minimalist'); state.setDeskHeight(66); state.setDeskDimensions(120, 60); state.setDeskFinish('glass'); snap('minimal-low-small-glass');
state.clearSetup(); snap('empty');
state.setDeskDimensions(999, 1); state.setDeskHeight(10); snap('clamped');
state.clearSetup(); state.setDeskHeight(74); state.setDeskDimensions(137, 73); state.setDeskFinish('white');
for (const c of CATALOG_ITEMS.filter((c) => !c.isDeskBase)) state.addItem(c.id, 1, 2, 0);
snap('everything-odd-size');
process.stdout.write(JSON.stringify(cases, null, 2) + '\n');
