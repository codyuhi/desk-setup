package online.codyuhi.desksetup.ui.scene

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/** Rotation snapping for two-finger twists: 15° steps, like a ratchet. */
const val SNAP_STEP = PI / 12

fun snapAngle(radians: Double): Double = (radians / SNAP_STEP).roundToInt() * SNAP_STEP

/** What a two-finger frame did: centroid movement, scale factor and twist (radians, clockwise positive). */
data class MultiTouch(val pan: Offset, val zoom: Float, val twist: Double, val centroid: Offset)

/**
 * Callbacks for one gesture stream. A gesture is a tap, a one-finger drag (with an optional long
 * press first), or a multi-finger pinch/twist/pan; each starts with [onDown].
 */
interface CanvasGestureHandler {
    /** First finger down; return true to treat a one-finger drag as an item drag rather than a camera move. */
    fun onDown(position: Offset): Boolean
    fun onTap(position: Offset)
    fun onLongPress(position: Offset): Boolean = false
    fun onDrag(position: Offset, delta: Offset, itemDrag: Boolean)
    fun onMulti(frame: MultiTouch)
    fun onEnd()
}

/** One shared gesture loop for the 3D and 2D canvases. */
suspend fun PointerInputScope.detectCanvasGestures(handler: CanvasGestureHandler) {
    val slop = viewConfiguration.touchSlop
    val longPressMs = viewConfiguration.longPressTimeoutMillis
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var itemDrag = handler.onDown(down.position)
        val start = down.position
        val downAt = System.nanoTime() / 1_000_000
        var moved = false
        var multi = false
        var longPressed = false
        var last = down.position
        var lastPair: Pair<Offset, Offset>? = null
        while (true) {
            val waitingForLongPress = !moved && !multi && !longPressed
            val remaining = longPressMs - (System.nanoTime() / 1_000_000 - downAt)
            val event: PointerEvent? =
                if (waitingForLongPress) withTimeoutOrNull(remaining.coerceAtLeast(1)) { awaitPointerEvent() }
                else awaitPointerEvent()
            if (event == null) {
                // Finger held still: a long press can turn the drag into an item drag (it selects the item).
                longPressed = true
                if (handler.onLongPress(start)) itemDrag = true
                continue
            }
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) {
                if (!moved && !multi && !longPressed) handler.onTap(start)
                break
            }
            if (pressed.size >= 2) {
                multi = true
                val a = pressed[0].position
                val b = pressed[1].position
                lastPair?.let { (pa, pb) -> handler.onMulti(multiFrame(pa, pb, a, b)) }
                lastPair = a to b
                event.changes.forEach(PointerInputChange::consume)
                continue
            }
            lastPair = null
            if (multi) {
                // Lifting one finger of a pinch ends the gesture without an accidental drag.
                event.changes.forEach(PointerInputChange::consume)
                continue
            }
            val p = pressed[0]
            if (!moved && (p.position - start).getDistance() > slop) {
                moved = true
                last = p.position
            }
            if (moved && p.positionChanged()) {
                handler.onDrag(p.position, p.position - last, itemDrag)
                last = p.position
                p.consume()
            }
        }
        handler.onEnd()
    }
}

private fun multiFrame(pa: Offset, pb: Offset, a: Offset, b: Offset): MultiTouch {
    val prevC = (pa + pb) / 2f
    val c = (a + b) / 2f
    val prevD = hypot(pb.x - pa.x, pb.y - pa.y)
    val d = hypot(b.x - a.x, b.y - a.y)
    var twist = atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble()) - atan2((pb.y - pa.y).toDouble(), (pb.x - pa.x).toDouble())
    if (twist > PI) twist -= 2 * PI
    if (twist < -PI) twist += 2 * PI
    return MultiTouch(c - prevC, if (prevD > 1f && abs(d - prevD) > 0f) d / prevD else 1f, twist, c)
}
