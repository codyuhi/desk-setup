package online.codyuhi.desksetup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupHistoryTest {
    private val a = DeskSetup(deskHeight = 70)
    private val b = DeskSetup(deskHeight = 80)
    private val c = DeskSetup(deskHeight = 90)

    @Test
    fun undoRedoWalkTheStack() {
        val h = SetupHistory.of(a).commit(b).commit(c)
        assertEquals(c, h.current)
        assertEquals(b, h.undo().current)
        assertEquals(a, h.undo().undo().current)
        assertFalse(h.undo().undo().canUndo)
        assertEquals(c, h.undo().redo().current)
        assertFalse(h.canRedo)
    }

    @Test
    fun identicalCommitIsSkipped() {
        val h = SetupHistory.of(a).commit(b)
        assertSame(h, h.commit(b))
    }

    @Test
    fun commitAfterUndoDropsRedoTail() {
        val h = SetupHistory.of(a).commit(b).undo().commit(c)
        assertEquals(listOf(a, c), h.snapshots)
        assertTrue(h.canUndo)
        assertFalse(h.canRedo)
    }

    @Test
    fun stackIsCapped() {
        var h = SetupHistory.of(DeskSetup(deskHeight = 65))
        repeat(150) { h = h.commit(DeskSetup(deskHeight = 66 + it)) }
        assertEquals(SetupHistory.MAX_SNAPSHOTS, h.snapshots.size)
        assertEquals(215, h.current.deskHeight)
    }

    @Test
    fun rotateQuarterTurnWrapsLikeWeb() {
        assertEquals(Math.PI / 4, rotatedQuarterTurn(0.0), 1e-12)
        assertEquals(0.0, rotatedQuarterTurn(7 * Math.PI / 4), 1e-12)
    }

    @Test
    fun moveRoundsLikeJavaScript() {
        val s = DeskSetup(placedItems = listOf(PlacedItem("i", "decor-coffee-mug", 0.0, 0.0)))
        val moved = s.moveItem("i", 2.5, -2.5).item("i")!!
        assertEquals(3.0, moved.x, 0.0)
        assertEquals(-2.0, moved.z, 0.0)
    }
}
