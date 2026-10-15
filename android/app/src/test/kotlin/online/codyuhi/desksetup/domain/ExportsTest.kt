package online.codyuhi.desksetup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExportsTest {
    private val setup = DeskSetup.fromPreset(Catalog.presets[1], idSeed = 1).withDimensions(180, 90)

    @Test
    fun jsonRoundTrips() {
        val json = SetupJson.export(setup, Instant.parse("2026-10-15T12:00:00Z"))
        assertTrue(json.contains("\"version\": \"1.0\""))
        assertTrue(json.contains("\"totalPrice\": ${setup.budget().totalPrice}"))
        assertEquals(setup, SetupJson.import(json, DeskSetup()))
    }

    @Test
    fun webExportWithoutDimensionsKeepsCurrentSize() {
        // Shape of app/src/exportUtils.js exportJSONConfig.
        val web = """
            {"version":"1.0","createdAt":"2026-10-01T00:00:00.000Z","deskHeight":72,"deskFinish":"black",
             "placedItems":[{"id":"item_1","catalogId":"keyboard-mech-tkl","x":-4,"z":12,"rotation":0}],
             "budget":{"totalPrice":0,"breakdown":[]}}
        """.trimIndent()
        val imported = SetupJson.import(web, DeskSetup().withDimensions(200, 100))!!
        assertEquals(72, imported.deskHeight)
        assertEquals("black", imported.deskFinish)
        assertEquals(200, imported.deskWidth)
        assertEquals(100, imported.deskDepth)
        assertEquals(PlacedItem("item_1", "keyboard-mech-tkl", -4.0, 12.0, 0.0), imported.placedItems.single())
    }

    @Test
    fun missingFieldsFallBackLikeWeb() {
        val imported = SetupJson.import("""{"placedItems":[]}""", DeskSetup())!!
        assertEquals(74, imported.deskHeight)
        assertEquals("walnut", imported.deskFinish)
    }

    @Test
    fun garbageIsRejected() {
        assertNull(SetupJson.import("not json", DeskSetup()))
        assertNull(SetupJson.import("""{"deskHeight":70}""", DeskSetup()))
        assertNull(SetupJson.import("""[1,2]""", DeskSetup()))
    }

    @Test
    fun csvEscapesQuotesAndTotals() {
        val csv = BudgetCsv.export(DeskSetup(placedItems = listOf(PlacedItem("m", "monitor-34-ultrawide", 0.0, 0.0))))
        val lines = csv.trimEnd().lines()
        assertEquals("Item Name,Estimated Price (USD)", lines[0])
        assertEquals("\"34\"\" Curved Ultrawide 144Hz\",\$599", lines[2])
        assertEquals("\"TOTAL ESTIMATED SETUP COST\",\"\$1248\"", lines.last())
    }
}
