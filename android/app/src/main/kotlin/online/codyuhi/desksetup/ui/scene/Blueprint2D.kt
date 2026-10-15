package online.codyuhi.desksetup.ui.scene

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import online.codyuhi.desksetup.domain.Catalog
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.PlacedItem
import online.codyuhi.desksetup.ui.theme.BlueprintColors
import online.codyuhi.desksetup.ui.theme.CedarSans
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Blueprint view transform: [pxPerCm] scale and a pan [offset] of the desk centre from the canvas centre. */
data class BlueprintView(val pxPerCm: Float, val offset: Offset) {
    fun cmToPx(size: Size, x: Double, z: Double) =
        Offset(size.width / 2 + offset.x + x.toFloat() * pxPerCm, size.height / 2 + offset.y + z.toFloat() * pxPerCm)

    fun pxToCm(size: Size, p: Offset) = Pair(
        ((p.x - size.width / 2 - offset.x) / pxPerCm).toDouble(),
        ((p.y - size.height / 2 - offset.y) / pxPerCm).toDouble(),
    )
}

/** Fits the desk into ~78% of the canvas (room for the dimension labels), never above the web's 2.4 px/cm (in dp). */
fun fittedScale(size: Size, setup: DeskSetup, density: Float): Float =
    min(2.4f * density, min(size.width * 0.78f / setup.deskWidth, size.height * 0.7f / setup.deskDepth))

/** Flat items (desk pads) underneath, tall ones on top, so a pad never hides the keyboard on it. */
private fun DeskSetup.drawOrder(): List<PlacedItem> =
    placedItems.sortedBy { Catalog.item(it.catalogId)?.dimensions?.height ?: 0.0 }

/**
 * The 2D blueprint (port of app/src/canvas2d.js): drag items directly, drag empty space to pan,
 * pinch to zoom and twist two fingers to rotate the selected item in 15° steps.
 */
@Composable
fun Blueprint2D(
    setup: DeskSetup,
    selectedId: String?,
    colors: BlueprintColors,
    onSelect: (String?) -> Unit,
    onMoveLive: (String, Double, Double) -> Unit,
    onRotateLive: (String, Double) -> Unit,
    onEndEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val haptics = LocalHapticFeedback.current
    val measurer = rememberTextMeasurer()
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var panX by rememberSaveable { mutableFloatStateOf(0f) }
    var panY by rememberSaveable { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val currentSetup by rememberUpdatedState(setup)
    val currentSelected by rememberUpdatedState(selectedId)
    val view = BlueprintView(fittedScale(canvasSize, setup, density) * zoom, Offset(panX, panY))
    val currentView by rememberUpdatedState(view)
    val select by rememberUpdatedState(onSelect)
    val moveLive by rememberUpdatedState(onMoveLive)
    val rotateLive by rememberUpdatedState(onRotateLive)
    val endEdit by rememberUpdatedState(onEndEdit)

    val handler = remember {
        object : CanvasGestureHandler {
            var dragId: String? = null
            var grab = Offset.Zero
            var twistBase = 0.0
            var twistAccum = 0.0
            var lastSnap = 0.0
            var edited = false

            override fun onDown(position: Offset): Boolean {
                val hit = hitTest(currentSetup, currentView, canvasSize, position)
                select(hit?.id)
                dragId = hit?.id
                if (hit != null) grab = position - currentView.cmToPx(canvasSize, hit.x, hit.z)
                twistAccum = 0.0
                twistBase = currentSetup.item(hit?.id ?: currentSelected)?.rotation ?: 0.0
                return hit != null
            }

            override fun onTap(position: Offset) = Unit

            override fun onDrag(position: Offset, delta: Offset, itemDrag: Boolean) {
                val id = dragId
                if (itemDrag && id != null) {
                    val (x, z) = currentView.pxToCm(canvasSize, position - grab)
                    moveLive(id, x, z)
                    edited = true
                } else {
                    panX += delta.x
                    panY += delta.y
                }
            }

            override fun onMulti(frame: MultiTouch) {
                zoom = (zoom * frame.zoom).coerceIn(0.4f, 5f)
                panX += frame.pan.x
                panY += frame.pan.y
                val id = currentSelected ?: return
                twistAccum += frame.twist
                val snapped = snapAngle(twistAccum)
                if (snapped != lastSnap) {
                    lastSnap = snapped
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    // The canvas y axis points down, so a clockwise twist is a positive rotation here.
                    rotateLive(id, twistBase + snapped)
                    edited = true
                }
            }

            override fun onEnd() {
                if (edited) endEdit()
                edited = false
                dragId = null
                lastSnap = 0.0
            }
        }
    }

    Canvas(
        modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(Unit) { detectCanvasGestures(handler) },
    ) {
        drawBlueprint(setup, selectedId, view, colors, measurer)
    }
}

private fun hitTest(setup: DeskSetup, view: BlueprintView, size: Size, p: Offset): PlacedItem? =
    setup.drawOrder().lastOrNull { item ->
        val cat = Catalog.item(item.catalogId)?.takeUnless { it.isDeskBase } ?: return@lastOrNull false
        val c = view.cmToPx(size, item.x, item.z)
        // Undo the item's rotation, then test against its footprint (at least 24 px for fingers).
        val dx = p.x - c.x
        val dy = p.y - c.y
        val lx = dx * cos(-item.rotation) - dy * sin(-item.rotation)
        val ly = dx * sin(-item.rotation) + dy * cos(-item.rotation)
        val hw = maxOf(cat.dimensions.width.toFloat() * view.pxPerCm, 48f) / 2
        val hd = maxOf(cat.dimensions.depth.toFloat() * view.pxPerCm, 48f) / 2
        lx in -hw..hw && ly in -hd..hd
    }

/** Draws the blueprint; shared by the live canvas and the PNG snapshot. */
fun DrawScope.drawBlueprint(
    setup: DeskSetup,
    selectedId: String?,
    view: BlueprintView,
    colors: BlueprintColors,
    measurer: TextMeasurer,
) {
    drawRect(colors.background)
    val center = view.cmToPx(size, 0.0, 0.0)
    // 20 cm grid, aligned to the desk centre.
    val step = 20 * view.pxPerCm
    if (step > 4) {
        var x = center.x % step
        while (x < size.width) {
            drawLine(colors.grid, Offset(x, 0f), Offset(x, size.height), 1f); x += step
        }
        var y = center.y % step
        while (y < size.height) {
            drawLine(colors.grid, Offset(0f, y), Offset(size.width, y), 1f); y += step
        }
    }

    val deskW = setup.deskWidth * view.pxPerCm
    val deskD = setup.deskDepth * view.pxPerCm
    val topLeft = Offset(center.x - deskW / 2, center.y - deskD / 2)
    drawRect(colors.grid.copy(alpha = 0.25f), topLeft + Offset(0f, 6f), Size(deskW, deskD))
    drawRect(colors.desk, topLeft, Size(deskW, deskD))
    drawRect(colors.accent, topLeft, Size(deskW, deskD), style = Stroke(2f * density))

    val label = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = colors.label)
    val widthText = measurer.measure("${setup.deskWidth} cm", label)
    drawText(widthText, topLeft = Offset(center.x - widthText.size.width / 2, topLeft.y - widthText.size.height - 8 * density))
    // Depth label reads bottom-to-top along the desk's left edge.
    val depthText = measurer.measure("${setup.deskDepth} cm", label)
    rotate(-90f, pivot = Offset(topLeft.x - 8 * density - depthText.size.height / 2, center.y)) {
        drawText(
            depthText,
            topLeft = Offset(topLeft.x - 8 * density - depthText.size.height / 2 - depthText.size.width / 2, center.y - depthText.size.height / 2),
        )
    }

    setup.drawOrder().forEach { item ->
        val cat = Catalog.item(item.catalogId)?.takeUnless { it.isDeskBase } ?: return@forEach
        val selected = item.id == selectedId
        val pos = view.cmToPx(size, item.x, item.z)
        val w = cat.dimensions.width.toFloat() * view.pxPerCm
        val d = cat.dimensions.depth.toFloat() * view.pxPerCm
        translate(pos.x, pos.y) {
            rotateRad(item.rotation.toFloat(), pivot = Offset.Zero) {
                val tl = Offset(-w / 2, -d / 2)
                drawRoundRect(if (selected) colors.selectFill else colors.item, tl, Size(w, d), CornerRadius(2f * density))
                drawRoundRect(
                    if (selected) colors.accent else colors.itemStroke, tl, Size(w, d), CornerRadius(2f * density),
                    style = Stroke((if (selected) 2.5f else 1.5f) * density),
                )
                val name = measurer.measure(
                    cat.name.substringBefore(' '),
                    TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.Medium, fontSize = 11.sp,
                        color = if (selected) colors.accent else colors.itemLabel),
                )
                drawText(name, topLeft = Offset(-name.size.width / 2f, -name.size.height / 2f))
            }
        }
    }
}
