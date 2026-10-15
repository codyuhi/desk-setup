package online.codyuhi.desksetup.domain

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.floor
import kotlin.random.Random

// Pure setup model and rules, ported from app/src/state.js. Positions are centimetres from the
// desk centre (x right, z towards the user); rotation is radians around the vertical axis.

@Serializable
data class PlacedItem(
    val id: String,
    val catalogId: String,
    val x: Double,
    val z: Double,
    val rotation: Double = 0.0,
)

@Serializable
data class DeskSetup(
    val deskFinish: String = "walnut",
    val deskHeight: Int = 74,
    val deskWidth: Int = 160,
    val deskDepth: Int = 80,
    val placedItems: List<PlacedItem> = emptyList(),
) {
    fun item(id: String?): PlacedItem? = placedItems.firstOrNull { it.id == id }

    fun withHeight(cm: Int) = copy(deskHeight = cm.coerceIn(MIN_HEIGHT, MAX_HEIGHT))

    fun withDimensions(widthCm: Int, depthCm: Int) = copy(
        deskWidth = widthCm.coerceIn(MIN_WIDTH, MAX_WIDTH),
        deskDepth = depthCm.coerceIn(MIN_DEPTH, MAX_DEPTH),
    )

    fun withFinish(key: String) = if (key in Catalog.finishes) copy(deskFinish = key) else this

    fun moveItem(id: String, x: Double, z: Double) = mapItem(id) { it.copy(x = jsRound(x), z = jsRound(z)) }

    fun rotateItem(id: String, radians: Double) = mapItem(id) { it.copy(rotation = radians) }

    fun removeItem(id: String) = copy(placedItems = placedItems.filterNot { it.id == id })

    private fun mapItem(id: String, f: (PlacedItem) -> PlacedItem) =
        copy(placedItems = placedItems.map { if (it.id == id) f(it) else it })

    companion object {
        const val MIN_HEIGHT = 65
        const val MAX_HEIGHT = 120
        const val MIN_WIDTH = 100
        const val MAX_WIDTH = 240
        const val MIN_DEPTH = 50
        const val MAX_DEPTH = 120

        fun fromPreset(preset: Preset, idSeed: Long = System.currentTimeMillis()) = DeskSetup(
            deskFinish = preset.deskFinish,
            deskHeight = preset.deskHeight,
            placedItems = preset.items.mapIndexed { i, p ->
                PlacedItem("preset_item_${i}_$idSeed", p.catalogId, p.x.toDouble(), p.z.toDouble(), p.rotation)
            },
        )

        val default: DeskSetup get() = fromPreset(Catalog.presets.first())

        fun newItemId(now: Long = System.currentTimeMillis(), random: Random = Random) =
            "item_${now}_${random.nextInt(1000)}"
    }
}

/** JavaScript's Math.round: halves round towards +infinity. */
fun jsRound(v: Double): Double = floor(v + 0.5)

/** Rotate by 45° and wrap like the web's `(rotation + PI / 4) % (2 * PI)`. */
fun rotatedQuarterTurn(rotation: Double): Double = (rotation + PI / 4) % (2 * PI)

data class BudgetLine(val name: String, val price: Int)

data class Budget(val totalPrice: Int, val breakdown: List<BudgetLine>)

fun DeskSetup.budget(): Budget {
    val basePrice = when (deskFinish) {
        "walnut" -> 649
        "oak" -> 549
        else -> 499
    }
    // Desk base price scales with surface area relative to the standard 160×80 cm top.
    val areaRatio = (deskWidth * deskDepth).toDouble() / (160 * 80)
    val deskPrice = jsRound(basePrice * areaRatio).toInt()
    val finishName = Catalog.finishes[deskFinish]?.name ?: "Walnut"
    val lines = mutableListOf(BudgetLine("Desk Frame & Surface ($finishName - ${deskWidth}×${deskDepth}cm)", deskPrice))
    placedItems.forEach { placed -> Catalog.item(placed.catalogId)?.let { lines += BudgetLine(it.name, it.price) } }
    return Budget(lines.sumOf { it.price }, lines)
}

enum class TipKind { Warning, Idea, Positive, Ideal }

data class ErgoTip(val kind: TipKind, val text: String)

data class Ergonomics(val score: Int, val tips: List<ErgoTip>)

fun DeskSetup.ergonomics(): Ergonomics {
    var score = 100
    val tips = mutableListOf<ErgoTip>()
    val hasMonitor = placedItems.any { Catalog.item(it.catalogId)?.category == "monitors" }
    val hasKeyboard = placedItems.any { it.catalogId == "keyboard-mech-tkl" }
    val hasLaptopStand = placedItems.any { it.catalogId == "laptop-stand-open" }
    val hasLightBar = placedItems.any { it.catalogId == "light-monitor-bar" }

    if (deskHeight > 80) {
        score -= 10
        tips += ErgoTip(TipKind.Warning, "Desk height is above 80cm; consider adding a footrest or adjusting chair height.")
    }
    if (deskHeight < 68) {
        score -= 10
        tips += ErgoTip(TipKind.Warning, "Desk height is low; ensure sufficient leg clearance.")
    }
    if (!hasMonitor && !hasLaptopStand) {
        score -= 20
        tips += ErgoTip(TipKind.Warning, "No elevated display detected. Looking down causes neck strain.")
    }
    if (!hasKeyboard) {
        score -= 15
        tips += ErgoTip(TipKind.Idea, "Add an ergonomic keyboard positioned 15-20cm from desk edge.")
    }
    if (hasLightBar) {
        score += 5
        tips += ErgoTip(TipKind.Positive, "Monitor light bar reduces eye fatigue during evening work.")
    }
    if (tips.isEmpty()) {
        tips += ErgoTip(TipKind.Ideal, "Ideal ergonomic alignment! Elbows at 90°, eyes aligned with top third of screen.")
    }
    return Ergonomics(score.coerceIn(40, 100), tips)
}
