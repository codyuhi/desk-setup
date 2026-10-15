package online.codyuhi.desksetup.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Budget and ergonomics must match the web exactly. The fixture was produced by running the web's
 * own app/src/state.js under Node (see android/README.md → "Parity fixture").
 */
class GoldenParityTest {
    @Serializable data class GItem(val catalogId: String, val x: Double, val z: Double, val rotation: Double = 0.0)
    @Serializable data class GSetup(val deskFinish: String, val deskHeight: Int, val deskWidth: Int, val deskDepth: Int, val placedItems: List<GItem>)
    @Serializable data class GLine(val name: String, val price: Int)
    @Serializable data class GBudget(val totalPrice: Int, val breakdown: List<GLine>)
    @Serializable data class GErgo(val score: Int, val tips: List<String>)
    @Serializable data class GCase(val name: String, val setup: GSetup, val budget: GBudget, val ergonomics: GErgo)

    private val cases: List<GCase> = Json { ignoreUnknownKeys = true }.decodeFromString(
        javaClass.getResource("/golden/web-parity.json")!!.readText(),
    )

    private fun GSetup.toDesk() = DeskSetup(
        deskFinish, deskHeight, deskWidth, deskDepth,
        placedItems.mapIndexed { i, it -> PlacedItem("g$i", it.catalogId, it.x, it.z, it.rotation) },
    )

    /** The web prefixes tips with an emoji ("⚠️ ", "💡 ", …); the app shows an icon instead. */
    private fun stripEmoji(tip: String) = tip.substringAfter(' ')

    @Test
    fun fixtureCoversEveryScenario() = assertEquals(8, cases.size)

    @Test
    fun budgetMatchesWeb() = cases.forEach { c ->
        val budget = c.setup.toDesk().budget()
        assertEquals(c.name, c.budget.totalPrice, budget.totalPrice)
        assertEquals(c.name, c.budget.breakdown.map { it.name to it.price }, budget.breakdown.map { it.name to it.price })
    }

    @Test
    fun ergonomicsMatchesWeb() = cases.forEach { c ->
        val ergo = c.setup.toDesk().ergonomics()
        assertEquals(c.name, c.ergonomics.score, ergo.score)
        assertEquals(c.name, c.ergonomics.tips.map(::stripEmoji), ergo.tips.map { it.text })
    }

    @Test
    fun clampingMatchesWeb() {
        val clamped = cases.single { it.name == "clamped" }.setup
        val native = DeskSetup().withDimensions(999, 1).withHeight(10)
        assertEquals(clamped.deskWidth, native.deskWidth)
        assertEquals(clamped.deskDepth, native.deskDepth)
        assertEquals(clamped.deskHeight, native.deskHeight)
    }

    @Test
    fun presetsMatchWeb() = Catalog.presets.forEach { p ->
        val web = cases.single { it.name == p.id }.setup
        val native = DeskSetup.fromPreset(p)
        assertEquals(web.deskFinish, native.deskFinish)
        assertEquals(web.deskHeight, native.deskHeight)
        assertEquals(web.deskWidth, native.deskWidth)
        assertEquals(web.placedItems.map { Triple(it.catalogId, it.x, it.z) }, native.placedItems.map { Triple(it.catalogId, it.x, it.z) })
    }
}
