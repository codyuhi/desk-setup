// Generated from lucide 1.27.0 (ISC licence) so the app uses the same
// icons as the web client. Regenerate with android/scripts/lucide2kt.mjs; do not edit by hand.
package online.codyuhi.desksetup.ui.components

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private fun lucide(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach {
            addPath(
                pathData = addPathNodes(it),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object Lucide {
    val Package: ImageVector by lazy {
        lucide(
            "package",
            "M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z",
            "M12 22V12",
            "M3.29 7L12 12L20.71 7",
            "m7.5 4.27 9 5.15",
        )
    }
    val Table: ImageVector by lazy {
        lucide(
            "table",
            "M12 3v18",
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z",
            "M3 9h18",
            "M3 15h18",
        )
    }
    val ChartColumn: ImageVector by lazy {
        lucide(
            "chart-column",
            "M3 3v16a2 2 0 0 0 2 2h16",
            "M18 17V9",
            "M13 17V5",
            "M8 17v-3",
        )
    }
    val Menu: ImageVector by lazy {
        lucide(
            "menu",
            "M4 5h16",
            "M4 12h16",
            "M4 19h16",
        )
    }
    val Sun: ImageVector by lazy {
        lucide(
            "sun",
            "M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0",
            "M12 2v2",
            "M12 20v2",
            "m4.93 4.93 1.41 1.41",
            "m17.66 17.66 1.41 1.41",
            "M2 12h2",
            "M20 12h2",
            "m6.34 17.66-1.41 1.41",
            "m19.07 4.93-1.41 1.41",
        )
    }
    val Moon: ImageVector by lazy {
        lucide(
            "moon",
            "M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401",
        )
    }
    val Undo2: ImageVector by lazy {
        lucide(
            "undo-2",
            "M9 14 4 9l5-5",
            "M4 9h10.5a5.5 5.5 0 0 1 5.5 5.5a5.5 5.5 0 0 1-5.5 5.5H11",
        )
    }
    val Redo2: ImageVector by lazy {
        lucide(
            "redo-2",
            "m15 14 5-5-5-5",
            "M20 9H9.5A5.5 5.5 0 0 0 4 14.5A5.5 5.5 0 0 0 9.5 20H13",
        )
    }
    val Trash2: ImageVector by lazy {
        lucide(
            "trash-2",
            "M10 11v6",
            "M14 11v6",
            "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6",
            "M3 6h18",
            "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2",
        )
    }
    val RotateCw: ImageVector by lazy {
        lucide(
            "rotate-cw",
            "M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8",
            "M21 3v5h-5",
        )
    }
    val X: ImageVector by lazy {
        lucide(
            "x",
            "M18 6 6 18",
            "m6 6 12 12",
        )
    }
    val Camera: ImageVector by lazy {
        lucide(
            "camera",
            "M13.997 4a2 2 0 0 1 1.76 1.05l.486.9A2 2 0 0 0 18.003 7H20a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.997a2 2 0 0 0 1.759-1.048l.489-.904A2 2 0 0 1 10.004 4z",
            "M9 13a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
        )
    }
    val Share2: ImageVector by lazy {
        lucide(
            "share-2",
            "M15 5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M15 19a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
            "M8.59 13.51L15.42 17.49",
            "M15.41 6.51L8.59 10.49",
        )
    }
    val FolderOpen: ImageVector by lazy {
        lucide(
            "folder-open",
            "m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2",
        )
    }
    val Save: ImageVector by lazy {
        lucide(
            "save",
            "M15.2 3a2 2 0 0 1 1.4.6l3.8 3.8a2 2 0 0 1 .6 1.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
            "M17 21v-7a1 1 0 0 0-1-1H8a1 1 0 0 0-1 1v7",
            "M7 3v4a1 1 0 0 0 1 1h7",
        )
    }
    val Plus: ImageVector by lazy {
        lucide(
            "plus",
            "M5 12h14",
            "M12 5v14",
        )
    }
    val Monitor: ImageVector by lazy {
        lucide(
            "monitor",
            "M4 3h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z",
            "M8 21L16 21",
            "M12 17L12 21",
        )
    }
    val Cpu: ImageVector by lazy {
        lucide(
            "cpu",
            "M12 20v2",
            "M12 2v2",
            "M17 20v2",
            "M17 2v2",
            "M2 12h2",
            "M2 17h2",
            "M2 7h2",
            "M20 12h2",
            "M20 17h2",
            "M20 7h2",
            "M7 20v2",
            "M7 2v2",
            "M6 4h12a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2Z",
            "M9 8h6a1 1 0 0 1 1 1v6a1 1 0 0 1 -1 1h-6a1 1 0 0 1 -1 -1v-6a1 1 0 0 1 1 -1Z",
        )
    }
    val Keyboard: ImageVector by lazy {
        lucide(
            "keyboard",
            "M10 8h.01",
            "M12 12h.01",
            "M14 8h.01",
            "M16 12h.01",
            "M18 8h.01",
            "M6 8h.01",
            "M7 16h10",
            "M8 12h.01",
            "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2Z",
        )
    }
    val Volume2: ImageVector by lazy {
        lucide(
            "volume-2",
            "M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z",
            "M16 9a5 5 0 0 1 0 6",
            "M19.364 18.364a9 9 0 0 0 0-12.728",
        )
    }
    val Lightbulb: ImageVector by lazy {
        lucide(
            "lightbulb",
            "M15 14c.2-1 .7-1.7 1.5-2.5 1-.9 1.5-2.2 1.5-3.5A6 6 0 0 0 6 8c0 1 .2 2.2 1.5 3.5.7.7 1.3 1.5 1.5 2.5",
            "M9 18h6",
            "M10 22h4",
        )
    }
    val Box: ImageVector by lazy {
        lucide(
            "box",
            "M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z",
            "m3.3 7 8.7 5 8.7-5",
            "M12 22V12",
        )
    }
    val Video: ImageVector by lazy {
        lucide(
            "video",
            "m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5",
            "M4 6h10a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2Z",
        )
    }
    val Eye: ImageVector by lazy {
        lucide(
            "eye",
            "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0",
            "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0",
        )
    }
    val ArrowDownToLine: ImageVector by lazy {
        lucide(
            "arrow-down-to-line",
            "M12 17V3",
            "m6 11 6 6 6-6",
            "M19 21H5",
        )
    }
    val RulerDimensionLine: ImageVector by lazy {
        lucide(
            "ruler-dimension-line",
            "M10 15v-3",
            "M14 15v-3",
            "M18 15v-3",
            "M2 8V4",
            "M22 6H2",
            "M22 8V4",
            "M6 15v-3",
            "M4 12h16a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-4a2 2 0 0 1 2 -2Z",
        )
    }
    val LayoutGrid: ImageVector by lazy {
        lucide(
            "layout-grid",
            "M4 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z",
            "M15 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z",
            "M15 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z",
            "M4 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z",
        )
    }
    val Check: ImageVector by lazy {
        lucide(
            "check",
            "M20 6 9 17l-5-5",
        )
    }
    val TriangleAlert: ImageVector by lazy {
        lucide(
            "triangle-alert",
            "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",
            "M12 9v4",
            "M12 17h.01",
        )
    }
    val CircleCheck: ImageVector by lazy {
        lucide(
            "circle-check",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0",
            "m9 12 2 2 4-4",
        )
    }
    val ThumbsUp: ImageVector by lazy {
        lucide(
            "thumbs-up",
            "M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88Z",
            "M7 10v12",
        )
    }
    val Sheet: ImageVector by lazy {
        lucide(
            "sheet",
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z",
            "M3 9L21 9",
            "M3 15L21 15",
            "M9 9L9 21",
            "M15 9L15 21",
        )
    }
    val Image: ImageVector by lazy {
        lucide(
            "image",
            "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z",
            "M7 9a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
            "m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21",
        )
    }
    val BookmarkPlus: ImageVector by lazy {
        lucide(
            "bookmark-plus",
            "M12 7v6",
            "M15 10H9",
            "M17 3a2 2 0 0 1 2 2v15a1 1 0 0 1-1.496.868l-4.512-2.578a2 2 0 0 0-1.984 0l-4.512 2.578A1 1 0 0 1 5 20V5a2 2 0 0 1 2-2z",
        )
    }
    val Library: ImageVector by lazy {
        lucide(
            "library",
            "m16 6 4 14",
            "M12 6v14",
            "M8 8v12",
            "M4 4v16",
        )
    }
    val Hand: ImageVector by lazy {
        lucide(
            "hand",
            "M18 11V6a2 2 0 0 0-2-2a2 2 0 0 0-2 2",
            "M14 10V4a2 2 0 0 0-2-2a2 2 0 0 0-2 2v2",
            "M10 10.5V6a2 2 0 0 0-2-2a2 2 0 0 0-2 2v8",
            "M18 8a2 2 0 1 1 4 0v6a8 8 0 0 1-8 8h-2c-2.8 0-4.5-.86-5.99-2.34l-3.6-3.6a2 2 0 0 1 2.83-2.82L7 15",
        )
    }
    val Rotate3d: ImageVector by lazy {
        lucide(
            "rotate-3d",
            "m15.194 13.707 3.814 1.86-1.86 3.814",
            "M16.47214 7.52786 A 5 10 0 1 0 13 21.79796",
            "M21.79796 11 A 10 5 0 1 0 19 15.57071",
        )
    }
    val LampDesk: ImageVector by lazy {
        lucide(
            "lamp-desk",
            "M10.293 2.293a1 1 0 0 1 1.414 0l2.5 2.5 5.994 1.227a1 1 0 0 1 .506 1.687l-7 7a1 1 0 0 1-1.687-.506l-1.227-5.994-2.5-2.5a1 1 0 0 1 0-1.414z",
            "m14.207 4.793-3.414 3.414",
            "M3 20a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v1a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z",
            "m9.086 6.5-4.793 4.793a1 1 0 0 0-.18 1.17L7 18",
        )
    }
    val Info: ImageVector by lazy {
        lucide(
            "info",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0",
            "M12 16v-4",
            "M12 8h.01",
        )
    }
}
