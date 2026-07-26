// Hardware & Furniture Catalog Data for DESKForge Studio

export const CATEGORIES = [
  { id: 'desk', name: 'Desk Surfaces & Finishes', icon: 'table' },
  { id: 'monitors', name: 'Displays & Mounts', icon: 'monitor' },
  { id: 'computers', name: 'PC & Laptops', icon: 'cpu' },
  { id: 'peripherals', name: 'Keyboards & Mice', icon: 'keyboard' },
  { id: 'audio', name: 'Audio & Acoustics', icon: 'volume-2' },
  { id: 'lighting', name: 'Lighting & RGB', icon: 'sun' },
  { id: 'decor', name: 'Decor & Storage', icon: 'sparkles' },
];

export const FINISHES = {
  walnut: { name: 'Dark Walnut', color: '#4a3222', roughness: 0.4, metalness: 0.05 },
  oak: { name: 'Natural Oak', color: '#d4b896', roughness: 0.5, metalness: 0.05 },
  black: { name: 'Matte Obsidian', color: '#26262a', roughness: 0.3, metalness: 0.1 },
  white: { name: 'Alpine White', color: '#f8fafc', roughness: 0.2, metalness: 0.05 },
  glass: { name: 'Smoked Glass', color: '#384856', roughness: 0.1, metalness: 0.8, opacity: 0.85, transparent: true },
};

export const CATALOG_ITEMS = [
  // --- DESK FINISH BASES ---
  {
    id: 'desk-walnut-finish',
    name: 'Solid Walnut Executive Surface',
    category: 'desk',
    price: 649,
    dimensions: { width: 160, depth: 80, height: 75 },
    defaultFinish: 'walnut',
    description: 'Rich dark walnut wood grain finish with bevel edges.',
    threeType: 'deskBaseSetter',
    isDeskBase: true
  },
  {
    id: 'desk-oak-finish',
    name: 'Scandinavian Natural Oak Surface',
    category: 'desk',
    price: 549,
    dimensions: { width: 160, depth: 80, height: 75 },
    defaultFinish: 'oak',
    description: 'Warm natural light oak surface with rounded corners.',
    threeType: 'deskBaseSetter',
    isDeskBase: true
  },
  {
    id: 'desk-obsidian-finish',
    name: 'Matte Obsidian Stealth Surface',
    category: 'desk',
    price: 499,
    dimensions: { width: 160, depth: 80, height: 75 },
    defaultFinish: 'black',
    description: 'Anti-fingerprint matte black finish designed for RGB setups.',
    threeType: 'deskBaseSetter',
    isDeskBase: true
  },

  // --- MONITORS ---
  {
    id: 'monitor-34-ultrawide',
    name: '34" Curved Ultrawide 144Hz',
    category: 'monitors',
    price: 599,
    dimensions: { width: 81, depth: 22, height: 45 },
    defaultColor: '#27272a',
    description: 'UWQHD 1440p immersive curved panel with slim bezel.',
    threeType: 'monitorUltrawide'
  },
  {
    id: 'monitor-27-4k-dual',
    name: 'Dual 27" 4K Colorist Setup',
    category: 'monitors',
    price: 849,
    dimensions: { width: 124, depth: 20, height: 44 },
    defaultColor: '#3f3f46',
    description: 'Dual IPS 4K screens mounted on a gas-spring dual arm.',
    threeType: 'monitorDual27'
  },
  {
    id: 'monitor-27-vertical',
    name: '27" Vertical Coding Monitor',
    category: 'monitors',
    price: 349,
    dimensions: { width: 36, depth: 18, height: 62 },
    defaultColor: '#27272a',
    description: 'Portrait orientation monitor optimized for code review & document reading.',
    threeType: 'monitorVertical'
  },
  {
    id: 'monitor-49-super-ultrawide',
    name: '49" Super Ultrawide 32:9',
    category: 'monitors',
    price: 1199,
    dimensions: { width: 119, depth: 28, height: 52 },
    defaultColor: '#18181b',
    description: 'Replaces dual monitors with a seamless panoramic curve display.',
    threeType: 'monitorSuperUltrawide'
  },

  // --- COMPUTERS ---
  {
    id: 'pc-rgb-gaming',
    name: 'Custom Glass RGB PC Tower',
    category: 'computers',
    price: 1899,
    dimensions: { width: 23, depth: 45, height: 46 },
    defaultColor: '#18181b',
    rgbGlow: '#00e5ff',
    description: 'Dual tempered glass mid-tower with liquid cooling & ARGB fans.',
    threeType: 'pcTowerRGB'
  },
  {
    id: 'mac-studio-mini',
    name: 'Aluminum Compact Workstation',
    category: 'computers',
    price: 1999,
    dimensions: { width: 20, depth: 20, height: 10 },
    defaultColor: '#e4e4e7',
    description: 'Ultra-compact silver anodized aluminum powerhouse desktop.',
    threeType: 'macStudio'
  },
  {
    id: 'laptop-stand-open',
    name: '16" Pro Laptop on Ergonomic Stand',
    category: 'computers',
    price: 2499,
    dimensions: { width: 36, depth: 26, height: 22 },
    defaultColor: '#cbd5e1',
    description: 'Slim metal laptop elevated on an angled vented aluminum riser.',
    threeType: 'laptopOnStand'
  },

  // --- PERIPHERALS ---
  {
    id: 'keyboard-mech-tkl',
    name: 'Custom Mechanical Keyboard (TKL)',
    category: 'peripherals',
    price: 189,
    dimensions: { width: 36, depth: 14, height: 3.5 },
    defaultColor: '#3f3f46',
    accentColor: '#f97316',
    description: 'Gasket-mounted hot-swappable keyboard with custom keycaps & brass weight.',
    threeType: 'keyboardMech'
  },
  {
    id: 'deskpad-felt-xl',
    name: 'Merino Wool Felt Desk Pad (XL)',
    category: 'peripherals',
    price: 49,
    dimensions: { width: 90, depth: 40, height: 0.5 },
    defaultColor: '#475569',
    description: 'Premium anti-slip soft wool felt mat protecting your desk surface.',
    threeType: 'deskPad'
  },
  {
    id: 'mouse-wireless-ergonomic',
    name: 'Precision Wireless Ergonomic Mouse',
    category: 'peripherals',
    price: 99,
    dimensions: { width: 8.5, depth: 12.5, height: 5 },
    defaultColor: '#27272a',
    description: 'Thumb rest wireless productivity mouse with silent magnetic scroll wheel.',
    threeType: 'mouseErgo'
  },

  // --- AUDIO ---
  {
    id: 'speakers-studio-pair',
    name: '5" Active Studio Monitors (Pair)',
    category: 'audio',
    price: 399,
    dimensions: { width: 146, depth: 22, height: 28 },
    defaultColor: '#27272a',
    coneColor: '#eab308',
    description: 'Bi-amplified studio monitor speakers flanking the desk setup.',
    threeType: 'speakersPair'
  },
  {
    id: 'headphones-audiophile-stand',
    name: 'Open-Back Headphones on Wood Stand',
    category: 'audio',
    price: 379,
    dimensions: { width: 14, depth: 14, height: 26 },
    defaultColor: '#52341d',
    description: 'Reference planar magnetic headphones on an Omega wooden stand.',
    threeType: 'headphonesStand'
  },

  // --- LIGHTING ---
  {
    id: 'light-monitor-bar',
    name: 'Asymmetric Monitor Screen Bar Light',
    category: 'lighting',
    price: 89,
    dimensions: { width: 45, depth: 9, height: 5 },
    defaultColor: '#27272a',
    lightColor: '#ffeaad',
    description: 'Glares-free screen clip light bar with auto-dimming touch sensor.',
    threeType: 'monitorLightBar'
  },
  {
    id: 'light-desk-lamp-gmatrix',
    name: 'Architectural LED Swing-Arm Lamp',
    category: 'lighting',
    price: 119,
    dimensions: { width: 18, depth: 50, height: 60 },
    defaultColor: '#3f3f46',
    lightColor: '#ffffff',
    description: 'Dual-joint articulated lamp with touch dimmer & color temperature dial.',
    threeType: 'architectLamp'
  },
  {
    id: 'light-rgb-strip-backlight',
    name: 'Smart Dynamic Ambient RGB LED Strip',
    category: 'lighting',
    price: 45,
    dimensions: { width: 160, depth: 2, height: 1 },
    defaultColor: '#c084fc',
    description: 'Rear desk edge RGB lighting synced with desktop themes.',
    threeType: 'rgbLedStrip'
  },

  // --- DECOR & STORAGE ---
  {
    id: 'decor-monstera-plant',
    name: 'Potted Monstera Deliciosa Plant',
    category: 'decor',
    price: 35,
    dimensions: { width: 28, depth: 28, height: 38 },
    defaultColor: '#16a34a',
    potColor: '#ffffff',
    description: 'Natural vibrant green tropical foliage in a ceramic planter.',
    threeType: 'pottedPlant'
  },
  {
    id: 'decor-pegboard-wall',
    name: 'Modular Wall Pegboard Organizer',
    category: 'decor',
    price: 75,
    dimensions: { width: 76, depth: 6, height: 56 },
    defaultColor: '#f8fafc',
    description: 'Wall mounted board with wooden pegs, mini shelves & headphone hooks.',
    threeType: 'pegboardWall'
  },
  {
    id: 'decor-coffee-mug',
    name: 'Ceramic Espresso Mug',
    category: 'decor',
    price: 18,
    dimensions: { width: 10, depth: 10, height: 9 },
    defaultColor: '#334155',
    description: 'Handcrafted matte ceramic coffee mug on a cork coaster.',
    threeType: 'coffeeMug'
  }
];

export const PRESET_SETUPS = [
  {
    id: 'preset-developer',
    name: '👨‍💻 Senior Engineer Workstation',
    description: 'Ultrawide display + vertical code review monitor, mechanical keyboard, & plant.',
    deskFinish: 'walnut',
    deskHeight: 74,
    items: [
      { catalogId: 'monitor-34-ultrawide', x: 0, z: -22, rotation: 0 },
      { catalogId: 'monitor-27-vertical', x: 56, z: -18, rotation: -0.3 },
      { catalogId: 'laptop-stand-open', x: -56, z: 2, rotation: 0.4 },
      { catalogId: 'keyboard-mech-tkl', x: -4, z: 12, rotation: 0 },
      { catalogId: 'mouse-wireless-ergonomic', x: 26, z: 14, rotation: 0 },
      { catalogId: 'deskpad-felt-xl', x: 2, z: 10, rotation: 0 },
      { catalogId: 'light-monitor-bar', x: 0, z: -22, rotation: 0 },
      { catalogId: 'decor-monstera-plant', x: 64, z: 18, rotation: 0 },
      { catalogId: 'decor-coffee-mug', x: -36, z: 22, rotation: 0 },
    ]
  },
  {
    id: 'preset-gamer',
    name: '🎮 Battlestation RGB Gamer',
    description: 'Dual monitors, RGB liquid cooled PC tower, studio speakers, & Purple LED glow.',
    deskFinish: 'black',
    deskHeight: 72,
    items: [
      { catalogId: 'monitor-27-4k-dual', x: 0, z: -20, rotation: 0 },
      { catalogId: 'speakers-studio-pair', x: 0, z: -18, rotation: 0 },
      { catalogId: 'pc-rgb-gaming', x: 50, z: 18, rotation: 0 },
      { catalogId: 'keyboard-mech-tkl', x: -4, z: 12, rotation: 0 },
      { catalogId: 'mouse-wireless-ergonomic', x: 24, z: 14, rotation: 0 },
      { catalogId: 'deskpad-felt-xl', x: 2, z: 10, rotation: 0 },
      { catalogId: 'headphones-audiophile-stand', x: -48, z: 18, rotation: 0 },
      { catalogId: 'light-rgb-strip-backlight', x: 0, z: -38, rotation: 0 },
    ]
  },
  {
    id: 'preset-minimalist',
    name: '🌿 Scandinavian Minimalist',
    description: 'Clean oak surface, Mac Studio, merino wool mat, single curved display, & lush greenery.',
    deskFinish: 'oak',
    deskHeight: 74,
    items: [
      { catalogId: 'monitor-34-ultrawide', x: 0, z: -18, rotation: 0 },
      { catalogId: 'mac-studio-mini', x: 54, z: -18, rotation: 0 },
      { catalogId: 'keyboard-mech-tkl', x: -2, z: 12, rotation: 0 },
      { catalogId: 'mouse-wireless-ergonomic', x: 26, z: 14, rotation: 0 },
      { catalogId: 'deskpad-felt-xl', x: 2, z: 10, rotation: 0 },
      { catalogId: 'light-desk-lamp-gmatrix', x: -56, z: -15, rotation: 0 },
      { catalogId: 'decor-monstera-plant', x: 56, z: 18, rotation: 0 },
    ]
  }
];
