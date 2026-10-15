package online.codyuhi.desksetup.ui.scene

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import online.codyuhi.desksetup.domain.Catalog
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.ModelType
import online.codyuhi.desksetup.domain.OrbitCamera
import online.codyuhi.desksetup.domain.Picking
import online.codyuhi.desksetup.domain.Vec3
import online.codyuhi.desksetup.domain.rayPlaneY
import online.codyuhi.desksetup.ui.theme.SceneColors
import kotlin.math.PI

/**
 * Interactive 3D view: one finger orbits (or drags the selected item along the desktop), a long
 * press grabs any item, two fingers pinch-zoom and pan the camera, and a two-finger twist turns
 * the selected item in 15° steps (or the camera when nothing is selected). Tap to select.
 */
@Composable
fun Scene3DCanvas(
    setup: DeskSetup,
    selectedId: String?,
    camera: OrbitCamera,
    colors: SceneColors,
    onSelect: (String?) -> Unit,
    onMoveLive: (String, Double, Double) -> Unit,
    onRotateLive: (String, Double) -> Unit,
    onEndEdit: () -> Unit,
    onCamera: ((OrbitCamera) -> OrbitCamera) -> Unit,
    onAspect: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val currentSetup by rememberUpdatedState(setup)
    val currentSelected by rememberUpdatedState(selectedId)
    val currentCamera by rememberUpdatedState(camera)
    val select by rememberUpdatedState(onSelect)
    val moveLive by rememberUpdatedState(onMoveLive)
    val rotateLive by rememberUpdatedState(onRotateLive)
    val endEdit by rememberUpdatedState(onEndEdit)
    val cameraUpdate by rememberUpdatedState(onCamera)

    val handler = remember {
        object : CanvasGestureHandler {
            var dragId: String? = null
            var grab = Vec3.ZERO
            var twistBase = 0.0
            var twistAccum = 0.0
            var lastSnap = 0.0
            var edited = false

            fun ray(p: Offset) = currentCamera.ray(p.x.toDouble(), p.y.toDouble(), viewport.width.toDouble(), viewport.height.toDouble())

            fun pick(p: Offset): String? {
                if (viewport.width == 0) return null
                val (o, d) = ray(p)
                return Picking.pick(currentSetup, o, d)
            }

            /** Point on the desktop plane under the finger. */
            fun deskPoint(p: Offset): Vec3? {
                val (o, d) = ray(p)
                return rayPlaneY(o, d, currentSetup.deskHeight * 0.01)
            }

            fun startDrag(id: String, p: Offset) {
                val item = currentSetup.item(id) ?: return
                val hit = deskPoint(p) ?: return
                dragId = id
                grab = hit - Vec3(item.x * 0.01, 0.0, item.z * 0.01)
            }

            override fun onDown(position: Offset): Boolean {
                twistAccum = 0.0
                lastSnap = 0.0
                twistBase = currentSetup.item(currentSelected)?.rotation ?: 0.0
                val hit = pick(position)
                if (hit != null && hit == currentSelected) {
                    startDrag(hit, position)
                    return dragId != null
                }
                return false
            }

            override fun onLongPress(position: Offset): Boolean {
                val hit = pick(position) ?: return false
                select(hit)
                startDrag(hit, position)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                twistBase = currentSetup.item(hit)?.rotation ?: 0.0
                return dragId != null
            }

            override fun onTap(position: Offset) = select(pick(position))

            override fun onDrag(position: Offset, delta: Offset, itemDrag: Boolean) {
                val id = dragId
                if (itemDrag && id != null) {
                    val hit = deskPoint(position) ?: return
                    val p = hit - grab
                    // Pegboards hang on the wall, so they only slide sideways.
                    val item = currentSetup.item(id) ?: return
                    val pegboard = Catalog.item(item.catalogId)?.model == ModelType.PegboardWall
                    moveLive(id, p.x * 100, if (pegboard) item.z else p.z * 100)
                    edited = true
                } else if (viewport.height > 0) {
                    val h = viewport.height.toDouble()
                    cameraUpdate { it.orbit(-2 * PI * delta.x / h, 2 * PI * delta.y / h) }
                }
            }

            override fun onMulti(frame: MultiTouch) {
                if (viewport.height == 0) return
                val h = viewport.height.toDouble()
                cameraUpdate { it.zoom(frame.zoom.toDouble()).pan(frame.pan.x / h, frame.pan.y / h) }
                val id = currentSelected
                if (id == null) {
                    cameraUpdate { it.orbit(-frame.twist, 0.0) }
                    return
                }
                twistAccum += frame.twist
                val snapped = snapAngle(twistAccum)
                if (snapped != lastSnap) {
                    lastSnap = snapped
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    // Seen from above, a clockwise twist is a negative turn around the vertical axis.
                    rotateLive(id, twistBase - snapped)
                    edited = true
                }
            }

            override fun onEnd() {
                if (edited) endEdit()
                edited = false
                dragId = null
            }
        }
    }

    Box(
        modifier.onSizeChanged {
            viewport = it
            if (it.height > 0) onAspect(it.width.toDouble() / it.height)
        },
    ) {
        DeskScene3D(setup, selectedId, camera, colors, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectCanvasGestures(handler) })
    }
}
