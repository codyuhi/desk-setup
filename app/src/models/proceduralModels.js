import * as THREE from 'three';
import { FINISHES } from '../catalog.js';

export function getDeskTopMaterial(finishKey) {
  const finish = FINISHES[finishKey] || FINISHES.walnut;
  
  return new THREE.MeshStandardMaterial({
    color: finish.color,
    roughness: finish.roughness,
    metalness: finish.metalness,
    transparent: finish.transparent || false,
    opacity: finish.opacity !== undefined ? finish.opacity : 1.0,
  });
}

export function create3DItemMesh(itemData, customFinish = null, deskHeight = 75) {
  const group = new THREE.Group();
  group.name = itemData.id || 'item';
  group.castShadow = true;
  group.receiveShadow = true;

  const type = itemData.threeType;
  const finishKey = customFinish || itemData.defaultFinish || 'walnut';

  switch (type) {
    case 'deskStanding':
      buildStandingDesk(group, itemData, finishKey, deskHeight);
      break;
    case 'deskWood':
      buildWoodDesk(group, itemData, finishKey, deskHeight);
      break;
    case 'deskLShape':
      buildLShapeDesk(group, itemData, finishKey, deskHeight);
      break;
    case 'monitorUltrawide':
      buildUltrawideMonitor(group, itemData);
      break;
    case 'monitorDual27':
      buildDual27Monitor(group, itemData);
      break;
    case 'monitorVertical':
      buildVerticalMonitor(group, itemData);
      break;
    case 'monitorSuperUltrawide':
      buildSuperUltrawideMonitor(group, itemData);
      break;
    case 'pcTowerRGB':
      buildPCTowerRGB(group, itemData);
      break;
    case 'macStudio':
      buildMacStudio(group, itemData);
      break;
    case 'laptopOnStand':
      buildLaptopOnStand(group, itemData);
      break;
    case 'keyboardMech':
      buildKeyboard(group, itemData);
      break;
    case 'deskPad':
      buildDeskPad(group, itemData);
      break;
    case 'mouseErgo':
      buildMouse(group, itemData);
      break;
    case 'speakersPair':
      buildSpeakersPair(group, itemData);
      break;
    case 'headphonesStand':
      buildHeadphonesStand(group, itemData);
      break;
    case 'monitorLightBar':
      buildMonitorLightBar(group, itemData);
      break;
    case 'architectLamp':
      buildArchitectLamp(group, itemData);
      break;
    case 'rgbLedStrip':
      buildRgbLedStrip(group, itemData);
      break;
    case 'pottedPlant':
      buildPottedPlant(group, itemData);
      break;
    case 'pegboardWall':
      buildPegboard(group, itemData);
      break;
    case 'coffeeMug':
      buildCoffeeMug(group, itemData);
      break;
    default:
      buildGenericBox(group, itemData);
      break;
  }

  group.traverse((child) => {
    if (child.isMesh) {
      child.castShadow = true;
      child.receiveShadow = true;
    }
  });

  return group;
}

const SCALE = 0.01; // cm to meters

function buildStandingDesk(group, item, finishKey, deskHeight) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = deskHeight * SCALE;
  const thickness = 0.035;

  const topGeo = new THREE.BoxGeometry(w, thickness, d);
  const topMat = getDeskTopMaterial(finishKey);
  const topMesh = new THREE.Mesh(topGeo, topMat);
  topMesh.position.y = h - thickness / 2;
  topMesh.name = 'deskSurface';
  group.add(topMesh);

  const edgeGeo = new THREE.BoxGeometry(w + 0.004, thickness * 0.4, d + 0.004);
  const edgeMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.5 });
  const edgeMesh = new THREE.Mesh(edgeGeo, edgeMat);
  edgeMesh.position.y = h - thickness / 2;
  group.add(edgeMesh);

  const metalMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.3, metalness: 0.7 });
  const legRadius = 0.035;
  const legHeight = h - thickness;

  const legLeft = new THREE.Mesh(new THREE.BoxGeometry(legRadius * 2, legHeight, legRadius * 2.5), metalMat);
  legLeft.position.set(-w * 0.35, legHeight / 2, 0);
  group.add(legLeft);

  const legRight = new THREE.Mesh(new THREE.BoxGeometry(legRadius * 2, legHeight, legRadius * 2.5), metalMat);
  legRight.position.set(w * 0.35, legHeight / 2, 0);
  group.add(legRight);

  const footGeo = new THREE.BoxGeometry(0.08, 0.025, d * 0.85);
  const footLeft = new THREE.Mesh(footGeo, metalMat);
  footLeft.position.set(-w * 0.35, 0.0125, 0);
  group.add(footLeft);

  const footRight = new THREE.Mesh(footGeo, metalMat);
  footRight.position.set(w * 0.35, 0.0125, 0);
  group.add(footRight);

  const barGeo = new THREE.BoxGeometry(w * 0.7, 0.03, 0.04);
  const barMesh = new THREE.Mesh(barGeo, metalMat);
  barMesh.position.set(0, h - thickness - 0.02, 0);
  group.add(barMesh);
}

function buildWoodDesk(group, item, finishKey, deskHeight) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = deskHeight * SCALE;
  const thickness = 0.03;

  const topGeo = new THREE.BoxGeometry(w, thickness, d);
  const topMat = getDeskTopMaterial(finishKey);
  const topMesh = new THREE.Mesh(topGeo, topMat);
  topMesh.position.y = h - thickness / 2;
  topMesh.name = 'deskSurface';
  group.add(topMesh);

  const legMat = getDeskTopMaterial(finishKey);
  const legGeo = new THREE.CylinderGeometry(0.02, 0.012, h - thickness, 16);
  const insetX = w * 0.42;
  const insetZ = d * 0.38;
  const legHeight = h - thickness;

  const legPositions = [
    [-insetX, legHeight / 2, -insetZ],
    [insetX, legHeight / 2, -insetZ],
    [-insetX, legHeight / 2, insetZ],
    [insetX, legHeight / 2, insetZ],
  ];

  legPositions.forEach(([x, y, z]) => {
    const leg = new THREE.Mesh(legGeo, legMat);
    leg.position.set(x, y, z);
    group.add(leg);
  });
}

function buildLShapeDesk(group, item, finishKey, deskHeight) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = deskHeight * SCALE;
  const thickness = 0.035;

  const mainTopGeo = new THREE.BoxGeometry(w, thickness, d * 0.55);
  const topMat = getDeskTopMaterial(finishKey);
  const mainTop = new THREE.Mesh(mainTopGeo, topMat);
  mainTop.position.set(0, h - thickness / 2, -d * 0.2);
  mainTop.name = 'deskSurface';
  group.add(mainTop);

  const sideTopGeo = new THREE.BoxGeometry(w * 0.4, thickness, d * 0.5);
  const sideTop = new THREE.Mesh(sideTopGeo, topMat);
  sideTop.position.set(w * 0.3, h - thickness / 2, d * 0.25);
  group.add(sideTop);

  const metalMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.3, metalness: 0.8 });
  const legGeo = new THREE.BoxGeometry(0.05, h - thickness, 0.05);

  const leg1 = new THREE.Mesh(legGeo, metalMat);
  leg1.position.set(-w * 0.45, (h - thickness) / 2, -d * 0.4);
  group.add(leg1);

  const leg2 = new THREE.Mesh(legGeo, metalMat);
  leg2.position.set(w * 0.45, (h - thickness) / 2, -d * 0.4);
  group.add(leg2);

  const leg3 = new THREE.Mesh(legGeo, metalMat);
  leg3.position.set(-w * 0.45, (h - thickness) / 2, 0.05);
  group.add(leg3);

  const leg4 = new THREE.Mesh(legGeo, metalMat);
  leg4.position.set(w * 0.45, (h - thickness) / 2, d * 0.45);
  group.add(leg4);
}

function buildUltrawideMonitor(group, item) {
  const w = item.dimensions.width * SCALE;
  const h = item.dimensions.height * SCALE;

  const plasticMat = new THREE.MeshStandardMaterial({ color: 0x27272a, roughness: 0.3, metalness: 0.5 });
  const screenMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, roughness: 0.1, metalness: 0.8, emissive: 0x0ea5e9, emissiveIntensity: 0.25 });
  const metalMat = new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.2, metalness: 0.9 });

  const caseMesh = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.03), plasticMat);
  caseMesh.position.y = h / 2 + 0.15;
  group.add(caseMesh);

  const screenMesh = new THREE.Mesh(new THREE.PlaneGeometry(w - 0.015, h - 0.015), screenMat);
  screenMesh.position.set(0, h / 2 + 0.15, 0.016);
  group.add(screenMesh);

  const colMesh = new THREE.Mesh(new THREE.CylinderGeometry(0.015, 0.02, h * 0.8, 16), metalMat);
  colMesh.position.set(0, h * 0.35, -0.04);
  group.add(colMesh);

  const baseMesh = new THREE.Mesh(new THREE.BoxGeometry(w * 0.35, 0.01, 0.22), metalMat);
  baseMesh.position.set(0, 0.005, 0);
  group.add(baseMesh);
}

function buildDual27Monitor(group, item) {
  const w = (item.dimensions.width / 2) * SCALE;
  const h = item.dimensions.height * SCALE;

  const plasticMat = new THREE.MeshStandardMaterial({ color: 0x27272a, roughness: 0.4 });
  const screenMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, emissive: 0x0284c7, emissiveIntensity: 0.3 });
  const metalMat = new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.3, metalness: 0.8 });

  const leftGroup = new THREE.Group();
  leftGroup.position.set(-w * 0.52, h / 2 + 0.15, 0);
  leftGroup.rotation.y = 0.15;

  const screenLeft = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.025), plasticMat);
  const displayLeft = new THREE.Mesh(new THREE.PlaneGeometry(w - 0.01, h - 0.01), screenMat);
  displayLeft.position.z = 0.013;
  leftGroup.add(screenLeft, displayLeft);
  group.add(leftGroup);

  const rightGroup = new THREE.Group();
  rightGroup.position.set(w * 0.52, h / 2 + 0.15, 0);
  rightGroup.rotation.y = -0.15;

  const screenRight = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.025), plasticMat);
  const displayRight = new THREE.Mesh(new THREE.PlaneGeometry(w - 0.01, h - 0.01), screenMat);
  displayRight.position.z = 0.013;
  rightGroup.add(screenRight, displayRight);
  group.add(rightGroup);

  const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, h * 0.9, 16), metalMat);
  centerPole.position.set(0, h * 0.45, -0.06);
  group.add(centerPole);
}

function buildVerticalMonitor(group, item) {
  const w = item.dimensions.width * SCALE;
  const h = item.dimensions.height * SCALE;

  const plasticMat = new THREE.MeshStandardMaterial({ color: 0x27272a, roughness: 0.4 });
  const screenMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, emissive: 0x0ea5e9, emissiveIntensity: 0.25 });
  const metalMat = new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.2, metalness: 0.9 });

  const caseMesh = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.025), plasticMat);
  caseMesh.position.y = h / 2 + 0.1;
  group.add(caseMesh);

  const screenMesh = new THREE.Mesh(new THREE.PlaneGeometry(w - 0.012, h - 0.012), screenMat);
  screenMesh.position.set(0, h / 2 + 0.1, 0.013);
  group.add(screenMesh);

  const colMesh = new THREE.Mesh(new THREE.CylinderGeometry(0.016, 0.018, h * 0.7, 16), metalMat);
  colMesh.position.set(0, h * 0.35, -0.04);
  group.add(colMesh);

  const baseMesh = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.01, 0.18), metalMat);
  baseMesh.position.set(0, 0.005, 0);
  group.add(baseMesh);
}

function buildSuperUltrawideMonitor(group, item) {
  const w = item.dimensions.width * SCALE;
  const h = item.dimensions.height * SCALE;

  const plasticMat = new THREE.MeshStandardMaterial({ color: 0x18181b, roughness: 0.3, metalness: 0.7 });
  const screenMat = new THREE.MeshStandardMaterial({ color: 0x0f172a, emissive: 0x38bdf8, emissiveIntensity: 0.3 });
  const metalMat = new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.2, metalness: 0.9 });

  const caseMesh = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.03), plasticMat);
  caseMesh.position.y = h / 2 + 0.15;
  group.add(caseMesh);

  const screenMesh = new THREE.Mesh(new THREE.PlaneGeometry(w - 0.02, h - 0.02), screenMat);
  screenMesh.position.set(0, h / 2 + 0.15, 0.016);
  group.add(screenMesh);

  const colMesh = new THREE.Mesh(new THREE.BoxGeometry(0.04, h * 0.7, 0.04), metalMat);
  colMesh.position.set(0, h * 0.35, -0.05);
  group.add(colMesh);

  const baseMesh = new THREE.Mesh(new THREE.BoxGeometry(w * 0.3, 0.012, 0.24), metalMat);
  baseMesh.position.set(0, 0.006, 0);
  group.add(baseMesh);
}

function buildPCTowerRGB(group, item) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = item.dimensions.height * SCALE;

  const chassisMat = new THREE.MeshStandardMaterial({ color: 0x27272a, roughness: 0.3, metalness: 0.7 });
  const glassMat = new THREE.MeshStandardMaterial({ color: 0x38bdf8, roughness: 0.05, metalness: 0.9, transparent: true, opacity: 0.35 });
  const rgbMat = new THREE.MeshStandardMaterial({ color: 0x00e5ff, emissive: 0x00e5ff, emissiveIntensity: 2.5 });

  const chassis = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), chassisMat);
  chassis.position.y = h / 2;
  group.add(chassis);

  const glass = new THREE.Mesh(new THREE.PlaneGeometry(d - 0.04, h - 0.04), glassMat);
  glass.position.set(-w / 2 - 0.001, h / 2, 0);
  glass.rotation.y = -Math.PI / 2;
  group.add(glass);

  for (let i = 0; i < 3; i++) {
    const fanRing = new THREE.Mesh(new THREE.TorusGeometry(0.05, 0.006, 8, 24), rgbMat);
    fanRing.position.set(0, h * 0.25 + i * 0.12, d / 2 + 0.001);
    group.add(fanRing);
  }

  const rgbLight = new THREE.PointLight(0x00e5ff, 2.0, 1.2);
  rgbLight.position.set(0, h * 0.5, 0);
  group.add(rgbLight);
}

function buildMacStudio(group, item) {
  const w = item.dimensions.width * SCALE;
  const h = item.dimensions.height * SCALE;

  const alumMat = new THREE.MeshStandardMaterial({ color: 0xe2e8f0, roughness: 0.2, metalness: 0.85 });
  const blackMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.5 });

  const box = new THREE.Mesh(new THREE.BoxGeometry(w, h, w), alumMat);
  box.position.y = h / 2;
  group.add(box);

  const bottomRing = new THREE.Mesh(new THREE.CylinderGeometry(w * 0.45, w * 0.45, 0.008, 32), blackMat);
  bottomRing.position.y = 0.004;
  group.add(bottomRing);
}

function buildLaptopOnStand(group, item) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const screenH = d * 0.8;

  const alumMat = new THREE.MeshStandardMaterial({ color: 0xcbd5e1, roughness: 0.2, metalness: 0.85 });
  const screenMat = new THREE.MeshStandardMaterial({ color: 0x0f172a, emissive: 0x38bdf8, emissiveIntensity: 0.3 });
  const kbMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.6 });

  const standGroup = new THREE.Group();
  standGroup.position.y = 0.09;
  standGroup.rotation.x = 0.2;

  const standMat = new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.2, metalness: 0.9 });
  const legLeft = new THREE.Mesh(new THREE.BoxGeometry(0.02, 0.09, d * 0.8), standMat);
  legLeft.position.set(-w * 0.35, -0.045, 0);
  const legRight = legLeft.clone();
  legRight.position.x = w * 0.35;
  standGroup.add(legLeft, legRight);

  const deck = new THREE.Mesh(new THREE.BoxGeometry(w, 0.012, d), alumMat);
  deck.position.y = 0.006;
  standGroup.add(deck);

  const kb = new THREE.Mesh(new THREE.PlaneGeometry(w * 0.88, d * 0.52), kbMat);
  kb.rotation.x = -Math.PI / 2;
  kb.position.set(0, 0.013, -d * 0.12);
  standGroup.add(kb);

  const pad = new THREE.Mesh(new THREE.PlaneGeometry(w * 0.32, d * 0.28), alumMat);
  pad.rotation.x = -Math.PI / 2;
  pad.position.set(0, 0.013, d * 0.28);
  standGroup.add(pad);

  const lidHinge = new THREE.Group();
  lidHinge.position.set(0, 0.012, -d / 2);
  lidHinge.rotation.x = -0.3;

  const lidShell = new THREE.Mesh(new THREE.BoxGeometry(w, screenH, 0.006), alumMat);
  lidShell.position.set(0, screenH / 2, 0);
  lidHinge.add(lidShell);

  const screenPanel = new THREE.Mesh(new THREE.PlaneGeometry(w - 0.02, screenH - 0.02), screenMat);
  screenPanel.position.set(0, screenH / 2, 0.004);
  lidHinge.add(screenPanel);

  standGroup.add(lidHinge);
  group.add(standGroup);
}

function buildKeyboard(group, item) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = item.dimensions.height * SCALE;

  const caseMat = new THREE.MeshStandardMaterial({ color: 0x3f3f46, roughness: 0.3, metalness: 0.5 });
  const keyMat = new THREE.MeshStandardMaterial({ color: 0xf8fafc, roughness: 0.4 });
  const accentMat = new THREE.MeshStandardMaterial({ color: 0xf97316, roughness: 0.4 });

  const base = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), caseMat);
  base.position.y = h / 2;
  group.add(base);

  const rows = 5;
  const cols = 14;
  const kw = (w * 0.9) / cols;
  const kd = (d * 0.85) / rows;

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const isAccent = (r === 0 && c === 0) || (r === 2 && c === cols - 1);
      const kMesh = new THREE.Mesh(
        new THREE.BoxGeometry(kw * 0.88, 0.008, kd * 0.88),
        isAccent ? accentMat : keyMat
      );
      const x = -w * 0.42 + c * kw;
      const z = -d * 0.38 + r * kd;
      kMesh.position.set(x, h + 0.004, z);
      group.add(kMesh);
    }
  }
}

function buildDeskPad(group, item) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = item.dimensions.height * SCALE;

  const padMat = new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.8, metalness: 0.0 });
  const pad = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), padMat);
  pad.position.y = h / 2;
  group.add(pad);
}

function buildMouse(group, item) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = item.dimensions.height * SCALE;

  const mouseMat = new THREE.MeshStandardMaterial({ color: 0x27272a, roughness: 0.3, metalness: 0.3 });
  const wheelMat = new THREE.MeshStandardMaterial({ color: 0xcbd5e1, roughness: 0.2, metalness: 0.9 });

  const body = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mouseMat);
  body.position.y = h / 2;
  group.add(body);

  const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.004, 0.004, 0.012, 16), wheelMat);
  wheel.rotation.z = Math.PI / 2;
  wheel.position.set(0, h + 0.002, -d * 0.25);
  group.add(wheel);
}

function buildSpeakersPair(group, item) {
  const spkWidth = 0.16;
  const d = item.dimensions.depth * SCALE;
  const h = item.dimensions.height * SCALE;

  const cabinetMat = new THREE.MeshStandardMaterial({ color: 0x27272a, roughness: 0.5 });
  const coneMat = new THREE.MeshStandardMaterial({ color: 0xeab308, roughness: 0.3, metalness: 0.6 });

  const halfSpread = (item.dimensions.width / 2) * SCALE;

  const leftSpk = new THREE.Group();
  leftSpk.position.x = -halfSpread;

  const cabL = new THREE.Mesh(new THREE.BoxGeometry(spkWidth, h, d), cabinetMat);
  cabL.position.y = h / 2;
  const coneL = new THREE.Mesh(new THREE.CylinderGeometry(spkWidth * 0.35, spkWidth * 0.35, 0.01, 24), coneMat);
  coneL.rotation.x = Math.PI / 2;
  coneL.position.set(0, h * 0.45, d / 2 + 0.001);
  leftSpk.add(cabL, coneL);
  group.add(leftSpk);

  const rightSpk = new THREE.Group();
  rightSpk.position.x = halfSpread;

  const cabR = cabL.clone();
  const coneR = coneL.clone();
  rightSpk.add(cabR, coneR);
  group.add(rightSpk);
}

function buildHeadphonesStand(group, item) {
  const h = item.dimensions.height * SCALE;
  const woodMat = new THREE.MeshStandardMaterial({ color: 0x78350f, roughness: 0.5 });
  const padMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.8 });

  const arch = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.07, h, 16), woodMat);
  arch.position.y = h / 2;
  group.add(arch);

  const phoneGroup = new THREE.Group();
  phoneGroup.position.y = h * 0.75;

  const earL = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 0.03, 24), padMat);
  earL.rotation.z = Math.PI / 2;
  earL.position.x = -0.07;

  const earR = earL.clone();
  earR.position.x = 0.07;
  phoneGroup.add(earL, earR);

  group.add(phoneGroup);
}

function buildMonitorLightBar(group, item) {
  const w = item.dimensions.width * SCALE;
  const mat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.3, metalness: 0.8 });

  const bar = new THREE.Mesh(new THREE.CylinderGeometry(0.008, 0.008, w, 16), mat);
  bar.rotation.z = Math.PI / 2;
  bar.position.set(0, 0.48, -0.02);
  group.add(bar);

  const light = new THREE.SpotLight(0xfffaed, 2.5, 1.5, Math.PI / 4, 0.5);
  light.position.set(0, 0.47, 0.02);
  light.target.position.set(0, 0, 0.15);
  group.add(light, light.target);
}

function buildArchitectLamp(group, item) {
  const metalMat = new THREE.MeshStandardMaterial({ color: 0x384252, roughness: 0.3, metalness: 0.8 });
  const shadeMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, roughness: 0.4 });
  const bulbMat = new THREE.MeshBasicMaterial({ color: 0xfff4d6 });

  // 1. Heavy Base Plate sitting flat on desk surface
  const base = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.08, 0.015, 32), metalMat);
  base.position.y = 0.0075;
  group.add(base);

  // 2. Lower Articulated Arm leaning forward/inward (+X, +Z) towards the work surface
  const lowerArm = new THREE.Mesh(new THREE.CylinderGeometry(0.006, 0.006, 0.28, 16), metalMat);
  lowerArm.position.set(0.06, 0.13, 0.06);
  lowerArm.rotation.z = -0.4; // lean towards center
  lowerArm.rotation.x = 0.3;  // lean forward towards user
  group.add(lowerArm);

  // Middle Elbow Swivel Joint
  const elbowJoint = new THREE.Mesh(new THREE.SphereGeometry(0.014, 16, 16), metalMat);
  elbowJoint.position.set(0.12, 0.25, 0.12);
  group.add(elbowJoint);

  // 3. Upper Cantilever Arm angling forward toward desk center
  const upperArm = new THREE.Mesh(new THREE.CylinderGeometry(0.006, 0.006, 0.26, 16), metalMat);
  upperArm.position.set(0.18, 0.35, 0.18);
  upperArm.rotation.z = -0.6;
  upperArm.rotation.x = 0.4;
  group.add(upperArm);

  // 4. Conical Lamp Hood (Shade)
  const shadeGroup = new THREE.Group();
  shadeGroup.position.set(0.24, 0.42, 0.24);
  // Point cone hood DOWNWARD and INWARD (-X, +Z, -Y) towards desk surface center
  shadeGroup.rotation.set(0.8, -0.6, 0.5);

  const shadeCone = new THREE.Mesh(new THREE.ConeGeometry(0.08, 0.12, 24, 1, true), shadeMat);
  shadeCone.position.y = -0.06;
  shadeGroup.add(shadeCone);

  // Emissive Light Bulb inside shade
  const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.02, 16, 16), bulbMat);
  bulb.position.y = -0.04;
  shadeGroup.add(bulb);

  // 5. Realistic Warm SpotLight casting a cone of light onto the desk surface
  const lampLight = new THREE.SpotLight(0xfff4d6, 3.5, 2.0, Math.PI / 3, 0.4);
  lampLight.position.set(0.24, 0.40, 0.24);
  lampLight.target.position.set(0.0, 0.0, 0.1); // Target center of desk surface
  group.add(lampLight, lampLight.target);

  group.add(shadeGroup);
}

function buildRgbLedStrip(group, item) {
  const rgbLight = new THREE.PointLight(0xc084fc, 3.5, 2.0);
  rgbLight.position.set(0, 0.7, -0.4);
  group.add(rgbLight);
}

function buildPottedPlant(group, item) {
  const potR = (item.dimensions.width / 2) * SCALE;
  const potH = (item.dimensions.height * 0.4) * SCALE;

  const potMat = new THREE.MeshStandardMaterial({ color: 0xf8fafc, roughness: 0.3 });
  const leafMat = new THREE.MeshStandardMaterial({ color: 0x16a34a, roughness: 0.5 });
  const soilMat = new THREE.MeshStandardMaterial({ color: 0x331f10, roughness: 0.9 });

  const pot = new THREE.Mesh(new THREE.CylinderGeometry(potR, potR * 0.75, potH, 24), potMat);
  pot.position.y = potH / 2;
  group.add(pot);

  const soil = new THREE.Mesh(new THREE.CylinderGeometry(potR * 0.9, potR * 0.9, 0.01, 24), soilMat);
  soil.position.y = potH;
  group.add(soil);

  for (let i = 0; i < 5; i++) {
    const leaf = new THREE.Mesh(new THREE.SphereGeometry(0.08, 8, 8), leafMat);
    leaf.scale.set(1.2, 0.1, 1.5);
    const angle = (i / 5) * Math.PI * 2;
    leaf.position.set(Math.cos(angle) * 0.07, potH + 0.12, Math.sin(angle) * 0.07);
    leaf.rotation.set(0.3, angle, 0.4);
    group.add(leaf);
  }
}

function buildPegboard(group, item) {
  const w = item.dimensions.width * SCALE;
  const h = item.dimensions.height * SCALE;

  const boardMat = new THREE.MeshStandardMaterial({ color: 0xf8fafc, roughness: 0.6 });
  const shelfMat = new THREE.MeshStandardMaterial({ color: 0xe2e8f0, roughness: 0.4 });

  const board = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.015), boardMat);
  board.position.set(0, h / 2 + 0.5, -0.42);
  group.add(board);

  const shelf = new THREE.Mesh(new THREE.BoxGeometry(w * 0.5, 0.015, 0.08), shelfMat);
  shelf.position.set(0, h * 0.4 + 0.5, -0.38);
  group.add(shelf);
}

function buildCoffeeMug(group, item) {
  const r = (item.dimensions.width / 2) * SCALE;
  const h = item.dimensions.height * SCALE;

  const mugMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.4 });
  const coffeeMat = new THREE.MeshStandardMaterial({ color: 0x451a03, roughness: 0.1 });

  const mug = new THREE.Mesh(new THREE.CylinderGeometry(r, r * 0.85, h, 24), mugMat);
  mug.position.y = h / 2;
  group.add(mug);

  const liquid = new THREE.Mesh(new THREE.CylinderGeometry(r * 0.88, r * 0.88, 0.005, 24), coffeeMat);
  liquid.position.y = h * 0.85;
  group.add(liquid);
}

function buildGenericBox(group, item) {
  const w = item.dimensions.width * SCALE;
  const d = item.dimensions.depth * SCALE;
  const h = item.dimensions.height * SCALE;

  const mat = new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.5 });
  const box = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
  box.position.y = h / 2;
  group.add(box);
}
