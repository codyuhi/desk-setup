package online.codyuhi.desksetup.ui.studio

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import online.codyuhi.desksetup.R
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.ui.scene.BlueprintView
import online.codyuhi.desksetup.ui.scene.drawBlueprint
import online.codyuhi.desksetup.ui.scene.fittedScale
import online.codyuhi.desksetup.ui.theme.BlueprintColors
import java.io.File

/** Share-sheet and file exports. Files go to cache/exports and leave the app only through FileProvider. */
object Exports {
    fun share(context: Context, fileName: String, mime: String, bytes: ByteArray, title: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        // Keep only the latest few exports around.
        dir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(5)?.forEach { it.delete() }
        val file = File(dir, fileName).apply { writeBytes(bytes) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, fileName)
            clipData = ClipData.newRawUri(fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, title))
    }

    fun png(bitmap: Bitmap): ByteArray = java.io.ByteArrayOutputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        it.toByteArray()
    }

    /** Grabs the current 3D frame from SceneView's TextureView, or null if it isn't on screen. */
    fun capture3D(root: View): Bitmap? = findTextureView(root.rootView)?.takeIf { it.isAvailable }?.bitmap

    private fun findTextureView(v: View): TextureView? = when (v) {
        is TextureView -> v
        is ViewGroup -> (0 until v.childCount).firstNotNullOfOrNull { findTextureView(v.getChildAt(it)) }
        else -> null
    }

    /** Renders the blueprint off-screen at [width]×[height] px, framed like the live view's default. */
    fun render2D(
        setup: DeskSetup,
        colors: BlueprintColors,
        measurer: TextMeasurer,
        density: Density,
        width: Int,
        height: Int,
    ): Bitmap {
        val image = ImageBitmap(width, height)
        val size = Size(width.toFloat(), height.toFloat())
        val view = BlueprintView(fittedScale(size, setup, density.density), androidx.compose.ui.geometry.Offset.Zero)
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image), size) {
            drawBlueprint(setup, null, view, colors, measurer)
        }
        return image.asAndroidBitmap()
    }

    /** Adds the web's fixed "DESKForge Studio" badge (Cedar Night label) in the bottom-right corner. */
    fun withBadge(context: Context, source: Bitmap, title: String = "My DESKForge Setup"): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val d = context.resources.displayMetrics.density
        val canvas = android.graphics.Canvas(out)
        val pad = 20 * d
        val w = 260 * d
        val h = 50 * d
        val rect = RectF(out.width - w - pad, out.height - h - pad, out.width - pad, out.height - pad)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE0161614.toInt() }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF48A97C.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2 * d
        }
        canvas.drawRoundRect(rect, 6 * d, 6 * d, fill)
        canvas.drawRoundRect(rect, 6 * d, 6 * d, stroke)
        val serif = ResourcesCompat.getFont(context, R.font.source_serif_4_bold)
        val sans = ResourcesCompat.getFont(context, R.font.inter_regular)
        canvas.drawText("DESKForge Studio", rect.left + 16 * d, rect.top + 24 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF4F2ED.toInt()
            textSize = 16 * d
            typeface = serif
        })
        canvas.drawText(title, rect.left + 16 * d, rect.top + 40 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF48A97C.toInt()
            textSize = 12 * d
            typeface = sans
        })
        return out
    }
}
