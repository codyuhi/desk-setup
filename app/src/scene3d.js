import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { TransformControls } from 'three/examples/jsm/controls/TransformControls.js';
import { state } from './state.js';
import { CATALOG_ITEMS } from './catalog.js';
import { create3DItemMesh } from './models/proceduralModels.js';

export class Scene3DManager {
  constructor(containerElement) {
    this.container = containerElement;
    this.itemMeshes = new Map();

    this.initScene();
    this.initLights();
    this.initRoom();
    this.initInteraction();

    state.subscribe((appState, changeType) => this.handleStateChange(appState, changeType));

    this.syncDeskAndItems();
    this.animate();

    window.addEventListener('resize', () => this.onWindowResize());
  }

  initScene() {
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0x1e2230);
    this.scene.fog = new THREE.FogExp2(0x1e2230, 0.04);

    const aspect = this.container.clientWidth / this.container.clientHeight;
    this.camera = new THREE.PerspectiveCamera(45, aspect, 0.1, 100);
    this.camera.position.set(0, 1.8, 2.5);

    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false, powerPreference: 'high-performance' });
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.35;

    this.container.appendChild(this.renderer.domElement);

    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.dampingFactor = 0.05;
    this.controls.maxPolarAngle = Math.PI / 2 - 0.02;
    this.controls.minDistance = 0.8;
    this.controls.maxDistance = 6.0;
    this.controls.target.set(0, 0.75, 0);
    this.controls.update();

    this.deskGroup = new THREE.Group();
    this.scene.add(this.deskGroup);
  }

  initLights() {
    this.ambientLight = new THREE.AmbientLight(0xffffff, 0.65);
    this.scene.add(this.ambientLight);

    this.hemiLight = new THREE.HemisphereLight(0xffffff, 0x475569, 1.1);
    this.scene.add(this.hemiLight);

    this.sunLight = new THREE.DirectionalLight(0xfff8f0, 2.2);
    this.sunLight.position.set(3.0, 5.0, 3.0);
    this.sunLight.castShadow = true;
    this.sunLight.shadow.mapSize.width = 2048;
    this.sunLight.shadow.mapSize.height = 2048;
    this.sunLight.shadow.camera.near = 0.5;
    this.sunLight.shadow.camera.far = 10;
    this.sunLight.shadow.camera.left = -2.5;
    this.sunLight.shadow.camera.right = 2.5;
    this.sunLight.shadow.camera.top = 2.5;
    this.sunLight.shadow.camera.bottom = -2.5;
    this.sunLight.shadow.bias = -0.0003;
    this.scene.add(this.sunLight);

    this.frontFill = new THREE.DirectionalLight(0xffffff, 0.85);
    this.frontFill.position.set(0, 3.0, 4.0);
    this.scene.add(this.frontFill);

    this.fillLight = new THREE.DirectionalLight(0x38bdf8, 0.6);
    this.fillLight.position.set(-3, 3, -3);
    this.scene.add(this.fillLight);

    this.rgbLight = new THREE.PointLight(0xc084fc, 2.5, 3.5);
    this.rgbLight.position.set(0, 0.8, -0.6);
    this.scene.add(this.rgbLight);
  }

  initRoom() {
    const floorGeo = new THREE.PlaneGeometry(12, 12);
    const floorMat = new THREE.MeshStandardMaterial({ color: 0x273042, roughness: 0.6, metalness: 0.1 });
    this.floor = new THREE.Mesh(floorGeo, floorMat);
    this.floor.rotation.x = -Math.PI / 2;
    this.floor.receiveShadow = true;
    this.scene.add(this.floor);

    this.gridHelper = new THREE.GridHelper(6, 30, 0x38bdf8, 0x334155);
    this.gridHelper.position.y = 0.001;
    this.scene.add(this.gridHelper);

    const wallGeo = new THREE.PlaneGeometry(12, 8);
    const wallMat = new THREE.MeshStandardMaterial({ color: 0x1e2636, roughness: 0.8 });
    this.wall = new THREE.Mesh(wallGeo, wallMat);
    this.wall.position.set(0, 4, -2.5);
    this.wall.receiveShadow = true;
    this.scene.add(this.wall);
  }

  initInteraction() {
    this.raycaster = new THREE.Raycaster();
    this.mouse = new THREE.Vector2();

    this.transformControls = new TransformControls(this.camera, this.renderer.domElement);
    this.transformControls.setMode('translate');
    this.transformControls.setSize(0.75);

    this.transformControls.addEventListener('dragging-changed', (event) => {
      this.controls.enabled = !event.value;
    });

    this.transformControls.addEventListener('objectChange', () => {
      const activeObj = this.transformControls.object;
      if (activeObj && state.selectedItemId) {
        const xCm = activeObj.position.x * 100;
        const zCm = activeObj.position.z * 100;
        state.updateItemPosition(state.selectedItemId, xCm, zCm);
      }
    });

    this.scene.add(this.transformControls.getHelper());

    this.renderer.domElement.addEventListener('pointerdown', (e) => this.onPointerDown(e));
  }

  onPointerDown(event) {
    if (this.transformControls.dragging) return;

    const rect = this.renderer.domElement.getBoundingClientRect();
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

    this.raycaster.setFromCamera(this.mouse, this.camera);

    const meshes = [];
    this.itemMeshes.forEach((meshGroup) => {
      meshGroup.traverse((child) => {
        if (child.isMesh) meshes.push(child);
      });
    });

    const intersects = this.raycaster.intersectObjects(meshes);
    if (intersects.length > 0) {
      let current = intersects[0].object;
      while (current.parent && !this.itemMeshes.has(current.name) && current.parent !== this.deskGroup) {
        current = current.parent;
      }
      if (current && this.itemMeshes.has(current.name)) {
        state.selectItem(current.name);
      }
    }
  }

  syncDeskAndItems() {
    if (this.transformControls) this.transformControls.detach();

    while (this.deskGroup.children.length > 0) {
      this.deskGroup.remove(this.deskGroup.children[0]);
    }
    this.itemMeshes.clear();

    const deskItemData = {
      id: 'desk-base',
      threeType: 'deskStanding',
      dimensions: { width: state.deskWidth, depth: state.deskDepth, height: state.deskHeight },
    };
    const deskMesh = create3DItemMesh(deskItemData, state.deskFinish, state.deskHeight);
    this.deskGroup.add(deskMesh);

    const deskTopY = state.deskHeight * 0.01;

    state.placedItems.forEach((item) => {
      const catItem = CATALOG_ITEMS.find((c) => c.id === item.catalogId);
      if (!catItem || catItem.isDeskBase || catItem.category === 'desk') return;

      const meshGroup = create3DItemMesh(catItem, item.finish || catItem.defaultFinish, state.deskHeight);
      meshGroup.name = item.id;

      const posX = item.x * 0.01;
      const posZ = item.z * 0.01;

      if (catItem.threeType === 'pegboardWall') {
        meshGroup.position.set(posX, deskTopY + 0.35, -0.42);
      } else {
        meshGroup.position.set(posX, deskTopY, posZ);
      }

      meshGroup.rotation.y = item.rotation;

      this.deskGroup.add(meshGroup);
      this.itemMeshes.set(item.id, meshGroup);
    });

    this.updateHighlighting();
  }

  updateHighlighting() {
    const selectedId = state.selectedItemId;

    if (selectedId && this.itemMeshes.has(selectedId)) {
      const selectedMesh = this.itemMeshes.get(selectedId);
      if (this.transformControls) {
        this.transformControls.attach(selectedMesh);
      }
    } else {
      if (this.transformControls) {
        this.transformControls.detach();
      }
    }

    this.itemMeshes.forEach((meshGroup, id) => {
      const isSelected = id === selectedId;

      meshGroup.traverse((child) => {
        if (child.isMesh && child.material) {
          if (isSelected) {
            child.material.emissive = child.material.emissive || new THREE.Color(0x000000);
            child.material.emissive.setHex(0x38bdf8);
            child.material.emissiveIntensity = 0.4;
          } else {
            if (child.material.emissive && !child.name.includes('RGB') && !child.name.includes('screen')) {
              child.material.emissive.setHex(0x000000);
              child.material.emissiveIntensity = 0;
            }
          }
        }
      });
    });
  }

  setGizmoMode(mode) {
    if (this.transformControls) {
      this.transformControls.setMode(mode);
    }
  }

  handleStateChange(appState, changeType) {
    if (changeType === 'selection') {
      this.updateHighlighting();
    } else if (changeType === 'updatePosition' || changeType === 'updateRotation') {
      if (state.selectedItemId && this.itemMeshes.has(state.selectedItemId)) {
        const mesh = this.itemMeshes.get(state.selectedItemId);
        const item = state.placedItems.find((i) => i.id === state.selectedItemId);
        if (item && mesh) {
          mesh.position.x = item.x * 0.01;
          mesh.position.z = item.z * 0.01;
          mesh.rotation.y = item.rotation;
        }
      }
      this.updateHighlighting();
    } else {
      this.syncDeskAndItems();
    }
  }

  setCameraView(viewName) {
    const targetY = (state.deskHeight * 0.01) / 2 + 0.4;
    this.controls.target.set(0, targetY, 0);

    switch (viewName) {
      case 'top':
        this.camera.position.set(0, 3.2, 0.01);
        break;
      case 'front':
        this.camera.position.set(0, targetY + 0.1, 2.2);
        break;
      case 'isometric':
        this.camera.position.set(2.2, 2.0, 2.2);
        break;
      case 'perspective':
      default:
        this.camera.position.set(0, 1.8, 2.5);
        break;
    }
    this.controls.update();
  }

  onWindowResize() {
    if (!this.container) return;
    const width = this.container.clientWidth;
    const height = this.container.clientHeight;

    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(width, height);
  }

  animate() {
    requestAnimationFrame(() => this.animate());

    this.controls.update();

    if (this.rgbLight) {
      const time = Date.now() * 0.001;
      this.rgbLight.intensity = 2.0 + Math.sin(time * 2) * 0.5;
    }

    this.renderer.render(this.scene, this.camera);
  }
}
