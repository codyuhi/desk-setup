package online.codyuhi.desksetup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.PI

class GeometryTest {
    private fun assertVec(e: Vec3, a: Vec3, eps: Double = 1e-9) {
        assertEquals(e.x, a.x, eps); assertEquals(e.y, a.y, eps); assertEquals(e.z, a.z, eps)
    }

    @Test
    fun eulerFollowsThreeXyzOrder() {
        // Three.js: Rx · Ry · Rz applied to a vector, so Z rotates first.
        val r = Mat3.euler(Vec3(PI / 2, 0.0, PI / 2))
        assertVec(Vec3(0.0, 0.0, 1.0), r * Vec3(1.0, 0.0, 0.0))
    }

    @Test
    fun rayHitsBoxFromAbove() {
        val box = Aabb(Vec3(-1.0, 0.0, -1.0), Vec3(1.0, 1.0, 1.0))
        assertEquals(4.0, box.intersect(Vec3(0.0, 5.0, 0.0), Vec3(0.0, -1.0, 0.0))!!, 1e-9)
        assertNull(box.intersect(Vec3(3.0, 5.0, 0.0), Vec3(0.0, -1.0, 0.0)))
        assertNull(box.intersect(Vec3(0.0, 5.0, 0.0), Vec3(0.0, 1.0, 0.0)))
    }

    @Test
    fun keyboardBoundsMatchItsDimensions() {
        val b = Models.bounds(Models.item(Catalog.item("keyboard-mech-tkl")!!))
        assertEquals(0.36, b.size.x, 1e-6)
        assertEquals(0.14, b.size.z, 1e-6)
        assertEquals(0.0, b.min.y, 1e-9)
    }

    @Test
    fun everyCatalogModelHasFiniteBounds() = Catalog.items.forEach { item ->
        val b = Models.bounds(Models.item(item))
        listOf(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z).forEach { assert(it.isFinite()) { item.id } }
    }

    @Test
    fun pickingFindsTheTopmostItemUnderARay() {
        val setup = DeskSetup(
            placedItems = listOf(
                PlacedItem("pad", "deskpad-felt-xl", 0.0, 10.0),
                PlacedItem("kb", "keyboard-mech-tkl", 0.0, 10.0),
            ),
        )
        assertEquals("kb", Picking.pick(setup, Vec3(0.0, 3.0, 0.1), Vec3(0.0, -1.0, 0.0)))
        assertEquals("pad", Picking.pick(setup, Vec3(0.4, 3.0, 0.1), Vec3(0.0, -1.0, 0.0)))
        assertNull(Picking.pick(setup, Vec3(2.0, 3.0, 0.1), Vec3(0.0, -1.0, 0.0)))
    }

    @Test
    fun pickingRespectsItemRotation() {
        // A 36 × 14 cm keyboard turned 90° is 14 cm wide along X.
        val setup = DeskSetup(placedItems = listOf(PlacedItem("kb", "keyboard-mech-tkl", 0.0, 0.0, PI / 2)))
        assertNull(Picking.pick(setup, Vec3(0.15, 3.0, 0.0), Vec3(0.0, -1.0, 0.0)))
        assertNotNull(Picking.pick(setup, Vec3(0.0, 3.0, 0.15), Vec3(0.0, -1.0, 0.0)))
    }
}
