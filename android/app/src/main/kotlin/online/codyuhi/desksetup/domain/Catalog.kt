package online.codyuhi.desksetup.domain

// Hardware & furniture catalog, ported 1:1 from app/src/catalog.js. Keep the two in sync:
// ids, prices and dimensions are part of the shared JSON config format.

data class Category(val id: String, val name: String)

data class Finish(
    val key: String,
    val name: String,
    val color: Long,
    val roughness: Float,
    val metalness: Float,
    val opacity: Float = 1f,
)

/** Centimetres, like the web catalog. */
data class Dimensions(val width: Double, val depth: Double, val height: Double)

enum class ModelType {
    DeskBaseSetter, MonitorUltrawide, MonitorDual27, MonitorVertical, MonitorSuperUltrawide,
    PcTowerRgb, MacStudio, LaptopOnStand, KeyboardMech, DeskPad, MouseErgo, SpeakersPair,
    HeadphonesStand, MonitorLightBar, ArchitectLamp, RgbLedStrip, PottedPlant, PegboardWall, CoffeeMug,
}

data class CatalogItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val dimensions: Dimensions,
    val description: String,
    val model: ModelType,
    val defaultFinish: String? = null,
) {
    val isDeskBase: Boolean get() = model == ModelType.DeskBaseSetter
}

data class PresetItem(val catalogId: String, val x: Int, val z: Int, val rotation: Double = 0.0)

data class Preset(
    val id: String,
    val name: String,
    val description: String,
    val deskFinish: String,
    val deskHeight: Int,
    val items: List<PresetItem>,
)

object Catalog {
    val categories = listOf(
        Category("desk", "Desk Surfaces & Finishes"),
        Category("monitors", "Displays & Mounts"),
        Category("computers", "PC & Laptops"),
        Category("peripherals", "Keyboards & Mice"),
        Category("audio", "Audio & Acoustics"),
        Category("lighting", "Lighting & RGB"),
        Category("decor", "Decor & Storage"),
    )

    val finishes = linkedMapOf(
        "walnut" to Finish("walnut", "Dark Walnut", 0xFF4A3222, 0.4f, 0.05f),
        "oak" to Finish("oak", "Natural Oak", 0xFFD4B896, 0.5f, 0.05f),
        "black" to Finish("black", "Matte Obsidian", 0xFF26262A, 0.3f, 0.1f),
        "white" to Finish("white", "Alpine White", 0xFFF8FAFC, 0.2f, 0.05f),
        "glass" to Finish("glass", "Smoked Glass", 0xFF384856, 0.1f, 0.8f, opacity = 0.85f),
    )

    fun finish(key: String): Finish = finishes[key] ?: finishes.getValue("walnut")

    val items = listOf(
        CatalogItem("desk-walnut-finish", "Solid Walnut Executive Surface", "desk", 649, Dimensions(160.0, 80.0, 75.0),
            "Rich dark walnut wood grain finish with bevel edges.", ModelType.DeskBaseSetter, "walnut"),
        CatalogItem("desk-oak-finish", "Scandinavian Natural Oak Surface", "desk", 549, Dimensions(160.0, 80.0, 75.0),
            "Warm natural light oak surface with rounded corners.", ModelType.DeskBaseSetter, "oak"),
        CatalogItem("desk-obsidian-finish", "Matte Obsidian Stealth Surface", "desk", 499, Dimensions(160.0, 80.0, 75.0),
            "Anti-fingerprint matte black finish designed for RGB setups.", ModelType.DeskBaseSetter, "black"),

        CatalogItem("monitor-34-ultrawide", "34\" Curved Ultrawide 144Hz", "monitors", 599, Dimensions(81.0, 22.0, 45.0),
            "UWQHD 1440p immersive curved panel with slim bezel.", ModelType.MonitorUltrawide),
        CatalogItem("monitor-27-4k-dual", "Dual 27\" 4K Colorist Setup", "monitors", 849, Dimensions(124.0, 20.0, 44.0),
            "Dual IPS 4K screens mounted on a gas-spring dual arm.", ModelType.MonitorDual27),
        CatalogItem("monitor-27-vertical", "27\" Vertical Coding Monitor", "monitors", 349, Dimensions(36.0, 18.0, 62.0),
            "Portrait orientation monitor optimized for code review & document reading.", ModelType.MonitorVertical),
        CatalogItem("monitor-49-super-ultrawide", "49\" Super Ultrawide 32:9", "monitors", 1199, Dimensions(119.0, 28.0, 52.0),
            "Replaces dual monitors with a seamless panoramic curve display.", ModelType.MonitorSuperUltrawide),

        CatalogItem("pc-rgb-gaming", "Custom Glass RGB PC Tower", "computers", 1899, Dimensions(23.0, 45.0, 46.0),
            "Dual tempered glass mid-tower with liquid cooling & ARGB fans.", ModelType.PcTowerRgb),
        CatalogItem("mac-studio-mini", "Aluminum Compact Workstation", "computers", 1999, Dimensions(20.0, 20.0, 10.0),
            "Ultra-compact silver anodized aluminum powerhouse desktop.", ModelType.MacStudio),
        CatalogItem("laptop-stand-open", "16\" Pro Laptop on Ergonomic Stand", "computers", 2499, Dimensions(36.0, 26.0, 22.0),
            "Slim metal laptop elevated on an angled vented aluminum riser.", ModelType.LaptopOnStand),

        CatalogItem("keyboard-mech-tkl", "Custom Mechanical Keyboard (TKL)", "peripherals", 189, Dimensions(36.0, 14.0, 3.5),
            "Gasket-mounted hot-swappable keyboard with custom keycaps & brass weight.", ModelType.KeyboardMech),
        CatalogItem("deskpad-felt-xl", "Merino Wool Felt Desk Pad (XL)", "peripherals", 49, Dimensions(90.0, 40.0, 0.5),
            "Premium anti-slip soft wool felt mat protecting your desk surface.", ModelType.DeskPad),
        CatalogItem("mouse-wireless-ergonomic", "Precision Wireless Ergonomic Mouse", "peripherals", 99, Dimensions(8.5, 12.5, 5.0),
            "Thumb rest wireless productivity mouse with silent magnetic scroll wheel.", ModelType.MouseErgo),

        CatalogItem("speakers-studio-pair", "5\" Active Studio Monitors (Pair)", "audio", 399, Dimensions(146.0, 22.0, 28.0),
            "Bi-amplified studio monitor speakers flanking the desk setup.", ModelType.SpeakersPair),
        CatalogItem("headphones-audiophile-stand", "Open-Back Headphones on Wood Stand", "audio", 379, Dimensions(14.0, 14.0, 26.0),
            "Reference planar magnetic headphones on an Omega wooden stand.", ModelType.HeadphonesStand),

        CatalogItem("light-monitor-bar", "Asymmetric Monitor Screen Bar Light", "lighting", 89, Dimensions(45.0, 9.0, 5.0),
            "Glares-free screen clip light bar with auto-dimming touch sensor.", ModelType.MonitorLightBar),
        CatalogItem("light-desk-lamp-gmatrix", "Architectural LED Swing-Arm Lamp", "lighting", 119, Dimensions(18.0, 50.0, 60.0),
            "Dual-joint articulated lamp with touch dimmer & color temperature dial.", ModelType.ArchitectLamp),
        CatalogItem("light-rgb-strip-backlight", "Smart Dynamic Ambient RGB LED Strip", "lighting", 45, Dimensions(160.0, 2.0, 1.0),
            "Rear desk edge RGB lighting synced with desktop themes.", ModelType.RgbLedStrip),

        CatalogItem("decor-monstera-plant", "Potted Monstera Deliciosa Plant", "decor", 35, Dimensions(28.0, 28.0, 38.0),
            "Natural vibrant green tropical foliage in a ceramic planter.", ModelType.PottedPlant),
        CatalogItem("decor-pegboard-wall", "Modular Wall Pegboard Organizer", "decor", 75, Dimensions(76.0, 6.0, 56.0),
            "Wall mounted board with wooden pegs, mini shelves & headphone hooks.", ModelType.PegboardWall),
        CatalogItem("decor-coffee-mug", "Ceramic Espresso Mug", "decor", 18, Dimensions(10.0, 10.0, 9.0),
            "Handcrafted matte ceramic coffee mug on a cork coaster.", ModelType.CoffeeMug),
    )

    private val byId = items.associateBy { it.id }

    fun item(id: String): CatalogItem? = byId[id]

    // The web preset names lead with an emoji; the app shows an icon instead.
    val presets = listOf(
        Preset(
            "preset-developer", "Senior Engineer Workstation",
            "Ultrawide display + vertical code review monitor, mechanical keyboard, & plant.", "walnut", 74,
            listOf(
                PresetItem("monitor-34-ultrawide", 0, -22),
                PresetItem("monitor-27-vertical", 56, -18, -0.3),
                PresetItem("laptop-stand-open", -56, 2, 0.4),
                PresetItem("keyboard-mech-tkl", -4, 12),
                PresetItem("mouse-wireless-ergonomic", 26, 14),
                PresetItem("deskpad-felt-xl", 2, 10),
                PresetItem("light-monitor-bar", 0, -22),
                PresetItem("decor-monstera-plant", 64, 18),
                PresetItem("decor-coffee-mug", -36, 22),
            ),
        ),
        Preset(
            "preset-gamer", "Battlestation RGB Gamer",
            "Dual monitors, RGB liquid cooled PC tower, studio speakers, & Purple LED glow.", "black", 72,
            listOf(
                PresetItem("monitor-27-4k-dual", 0, -20),
                PresetItem("speakers-studio-pair", 0, -18),
                PresetItem("pc-rgb-gaming", 50, 18),
                PresetItem("keyboard-mech-tkl", -4, 12),
                PresetItem("mouse-wireless-ergonomic", 24, 14),
                PresetItem("deskpad-felt-xl", 2, 10),
                PresetItem("headphones-audiophile-stand", -48, 18),
                PresetItem("light-rgb-strip-backlight", 0, -38),
            ),
        ),
        Preset(
            "preset-minimalist", "Scandinavian Minimalist",
            "Clean oak surface, Mac Studio, merino wool mat, single curved display, & lush greenery.", "oak", 74,
            listOf(
                PresetItem("monitor-34-ultrawide", 0, -18),
                PresetItem("mac-studio-mini", 54, -18),
                PresetItem("keyboard-mech-tkl", -2, 12),
                PresetItem("mouse-wireless-ergonomic", 26, 14),
                PresetItem("deskpad-felt-xl", 2, 10),
                PresetItem("light-desk-lamp-gmatrix", -56, -15),
                PresetItem("decor-monstera-plant", 56, 18),
            ),
        ),
    )

    /** Desk size quick picks shown as pills on the Desk panel (same as the web). */
    val sizePresets = listOf(120 to 60, 140 to 70, 160 to 80, 180 to 90, 200 to 100)
}
