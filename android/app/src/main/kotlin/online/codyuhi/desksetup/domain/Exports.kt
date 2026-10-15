package online.codyuhi.desksetup.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

private val PrettyJson = Json(AppJson) { prettyPrint = true }

@Serializable
private data class BudgetLineJson(val name: String, val price: Int)

@Serializable
private data class BudgetJson(val totalPrice: Int, val breakdown: List<BudgetLineJson>)

/** Same shape as the web's exportJSONConfig, plus deskWidth/deskDepth (ignored by the web importer). */
@Serializable
private data class ConfigJson(
    val version: String,
    val createdAt: String,
    val deskHeight: Int,
    val deskWidth: Int,
    val deskDepth: Int,
    val deskFinish: String,
    val placedItems: List<PlacedItem>,
    val budget: BudgetJson,
)

object SetupJson {
    fun export(setup: DeskSetup, now: Instant = Instant.now()): String {
        val budget = setup.budget()
        val config = ConfigJson(
            version = "1.0",
            createdAt = now.toString(),
            deskHeight = setup.deskHeight,
            deskWidth = setup.deskWidth,
            deskDepth = setup.deskDepth,
            deskFinish = setup.deskFinish,
            placedItems = setup.placedItems,
            budget = BudgetJson(budget.totalPrice, budget.breakdown.map { BudgetLineJson(it.name, it.price) }),
        )
        return PrettyJson.encodeToString(ConfigJson.serializer(), config)
    }

    /**
     * Parses a config exported by the web app or this app. Like the web importer it needs a
     * `placedItems` array and falls back to 74 cm / walnut; width and depth keep [current] when
     * the file has none (web exports never do). Returns null for anything that isn't a config.
     */
    fun import(text: String, current: DeskSetup): DeskSetup? {
        val root = runCatching { AppJson.parseToJsonElement(text) as? JsonObject }.getOrNull() ?: return null
        val items = root["placedItems"] as? JsonArray ?: return null
        val placed = runCatching { items.map { AppJson.decodeFromJsonElement<PlacedItem>(it) } }.getOrNull() ?: return null
        fun num(key: String) = runCatching { root[key]?.jsonPrimitive?.doubleOrNull }.getOrNull()?.toInt()
        val finish = runCatching { root["deskFinish"]?.jsonPrimitive?.contentOrNull }.getOrNull()
        return DeskSetup(
            deskFinish = finish?.takeIf { it in Catalog.finishes } ?: "walnut",
            deskHeight = num("deskHeight")?.takeIf { it > 0 } ?: 74,
            deskWidth = num("deskWidth") ?: current.deskWidth,
            deskDepth = num("deskDepth") ?: current.deskDepth,
            placedItems = placed,
        ).let { it.withHeight(it.deskHeight).withDimensions(it.deskWidth, it.deskDepth) }
    }
}

object BudgetCsv {
    /** The web's shopping list CSV, with quotes inside names escaped so 34" monitors stay one cell. */
    fun export(setup: DeskSetup): String {
        val budget = setup.budget()
        return buildString {
            append("Item Name,Estimated Price (USD)\n")
            budget.breakdown.forEach { append(quote(it.name)).append(",$").append(it.price).append('\n') }
            append('\n')
            append(quote("TOTAL ESTIMATED SETUP COST")).append(',').append(quote("$${budget.totalPrice}")).append('\n')
        }
    }

    private fun quote(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
}
