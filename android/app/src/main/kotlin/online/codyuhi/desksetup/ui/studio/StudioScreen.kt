package online.codyuhi.desksetup.ui.studio

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import online.codyuhi.desksetup.data.ViewMode
import online.codyuhi.desksetup.domain.BudgetCsv
import online.codyuhi.desksetup.domain.CameraView
import online.codyuhi.desksetup.domain.Catalog
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.SetupJson
import online.codyuhi.desksetup.ui.components.CedarIconButton
import online.codyuhi.desksetup.ui.components.Lucide
import online.codyuhi.desksetup.ui.components.Segmented
import online.codyuhi.desksetup.ui.components.ThemeToggle
import online.codyuhi.desksetup.ui.scene.Blueprint2D
import online.codyuhi.desksetup.ui.scene.Scene3DCanvas
import online.codyuhi.desksetup.ui.theme.Cedar

/** The four destinations of the web's mobile bottom nav: Catalog, Desk, Budget, Actions. */
enum class Panel(val label: String, val icon: ImageVector) {
    Catalog("Catalog", Lucide.Package),
    Desk("Desk", Lucide.Table),
    Budget("Budget", Lucide.ChartColumn),
    Actions("Actions", Lucide.Menu),
}

/**
 * The one studio screen. Compact widths (Fold cover screen, S26+) get a top bar, the canvas and a
 * bottom bar whose items open bottom sheets, like the web mobile layout. Medium and wider (the
 * unfolded Fold) get a 72 dp icon rail and dismissible flyouts so the canvas keeps ≥ 85% of the width.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(vm: StudioViewModel, dark: Boolean) {
    val history = vm.history.collectAsStateWithLifecycle().value ?: return
    val draft by vm.draft.collectAsStateWithLifecycle()
    val selectedId by vm.selectedId.collectAsStateWithLifecycle()
    val camera by vm.camera.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val saved by vm.savedSetups.collectAsStateWithLifecycle()
    val setup = draft ?: history.current
    val viewMode = settings?.viewMode ?: ViewMode.ThreeD
    val wide = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val measurer = rememberTextMeasurer()
    val colors = Cedar.colors

    var panel by rememberSaveable { mutableStateOf<Panel?>(null) }
    var inspectorTab by rememberSaveable { mutableStateOf(InspectorTab.Desk) }
    var showSave by rememberSaveable { mutableStateOf(false) }
    var showClear by rememberSaveable { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vm) { vm.messages.collect { toast = it } }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2500)
            toast = null
        }
    }

    fun open(p: Panel) {
        if (p == Panel.Desk) inspectorTab = InspectorTab.Desk
        if (p == Panel.Budget) inspectorTab = InspectorTab.Budget
        panel = if (panel == p) null else p
    }

    val saveJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(SetupJson.export(vm.current ?: setup).toByteArray()) } }
            .onSuccess { Toast.makeText(context, "Saved JSON setup config!", Toast.LENGTH_SHORT).show() }
    }
    val saveCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(BudgetCsv.export(vm.current ?: setup).toByteArray()) } }
            .onSuccess { Toast.makeText(context, "Shopping list CSV exported!", Toast.LENGTH_SHORT).show() }
    }
    val openJson = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        vm.importJson(runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull())
    }

    val stamp = { System.currentTimeMillis() }
    val actions = StudioActions(
        setFinish = vm::setFinish,
        setSize = vm::setSize,
        setWidthLive = vm::setWidthLive,
        setDepthLive = vm::setDepthLive,
        setHeightLive = vm::setHeightLive,
        endEdit = vm::endEdit,
        rotate45 = {
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            vm.rotateSelected45()
        },
        delete = {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            vm.deleteSelected()
        },
        loadPreset = { vm.loadPreset(it); panel = null },
        requestSave = { showSave = true },
        loadSaved = { vm.loadSaved(it); panel = null },
        deleteSaved = vm::deleteSaved,
        shareSnapshot = {
            panel = null
            val shot = if (viewMode == ViewMode.ThreeD) {
                Exports.capture3D(view)
            } else {
                Exports.render2D(setup, colors.blueprint, measurer, density, view.width, view.height)
            }
            if (shot == null) {
                toast = "Snapshot unavailable; try again."
            } else {
                Exports.share(context, "desk-setup-${stamp()}.png", "image/png", Exports.png(Exports.withBadge(context, shot)), "Share snapshot")
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            }
        },
        saveJson = { saveJson.launch("desk-config-${stamp()}.json") },
        shareJson = {
            Exports.share(context, "desk-config-${stamp()}.json", "application/json", SetupJson.export(setup).toByteArray(), "Share setup config")
        },
        openJson = { openJson.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
        shareCsv = {
            Exports.share(context, "desk-budget-${stamp()}.csv", "text/csv", BudgetCsv.export(setup).toByteArray(), "Share shopping list")
        },
        saveCsv = { saveCsv.launch("desk-budget-${stamp()}.csv") },
        requestClear = { showClear = true },
    )

    BackHandler(enabled = panel != null || selectedId != null) {
        if (panel != null) panel = null else vm.select(null)
    }

    val canvas: @Composable (Modifier) -> Unit = { modifier ->
        StudioCanvas(vm, setup, selectedId, camera, viewMode, haptics::performHapticFeedback, modifier)
    }

    if (wide) {
        Row(Modifier.fillMaxSize().background(colors.canvas)) {
            StudioRail(panel, ::open, history.canUndo, history.canRedo, vm::undo, vm::redo, dark) { vm.toggleTheme(dark) }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                canvas(Modifier.fillMaxSize())
                CanvasOverlays(vm, setup, selectedId, viewMode, Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom)))
                androidx.compose.animation.AnimatedVisibility(
                    visible = panel != null,
                    enter = slideInHorizontally { -it / 3 } + fadeIn(),
                    exit = slideOutHorizontally { -it / 3 } + fadeOut(),
                ) {
                    // Flyout: overlays the canvas only while open, and is dismissed from the rail or its close button.
                    Surface(
                        color = colors.canvas,
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, colors.borderDistinct),
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                            .padding(8.dp)
                            .width(360.dp)
                            .fillMaxHeight(),
                    ) {
                        panel?.let { PanelContent(it, setup, selectedId, inspectorTab, { t -> inspectorTab = t }, saved, actions, vm::addItem) { panel = null } }
                    }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().background(colors.canvas)) {
            StudioTopBar(history.canUndo, history.canRedo, vm::undo, vm::redo, dark) { vm.toggleTheme(dark) }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                canvas(Modifier.fillMaxSize())
                CanvasOverlays(vm, setup, selectedId, viewMode, Modifier)
            }
            StudioBottomBar(panel, ::open)
        }
        if (panel != null) {
            ModalBottomSheet(
                onDismissRequest = { panel = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = panel != Panel.Desk),
                containerColor = colors.canvas,
                shape = MaterialTheme.shapes.large,
            ) {
                panel?.let {
                    // On phones, close the sheet after adding so the new item is visible on the desk.
                    val addAndClose = { id: String -> vm.addItem(id); panel = null }
                    PanelContent(it, setup, selectedId, inspectorTab, { t -> inspectorTab = t }, saved, actions, addAndClose, Modifier.heightIn(max = 640.dp)) { panel = null }
                }
            }
        }
    }

    // Toast, above the bars and the floating item bar.
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(bottom = if (wide) 96.dp else 160.dp), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = toast != null, enter = fadeIn(), exit = fadeOut()) {
            Surface(color = colors.textPrimary, shape = MaterialTheme.shapes.medium, shadowElevation = 6.dp) {
                Text(toast.orEmpty(), color = colors.surface, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }
    }

    if (showSave) SaveSetupDialog(onDismiss = { showSave = false }) { name -> vm.saveAs(name); showSave = false }
    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            title = { Text("Clear setup?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("This removes every item from the desk. You can undo it.") },
            confirmButton = {
                TextButton(onClick = { vm.clear(); showClear = false; panel = null }) { Text("Clear", color = colors.alert) }
            },
            dismissButton = { TextButton(onClick = { showClear = false }) { Text("Cancel", color = colors.textPrimary) } },
            containerColor = colors.surface,
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
private fun StudioCanvas(
    vm: StudioViewModel,
    setup: DeskSetup,
    selectedId: String?,
    camera: online.codyuhi.desksetup.domain.OrbitCamera,
    viewMode: ViewMode,
    haptic: (HapticFeedbackType) -> Unit,
    modifier: Modifier,
) {
    val colors = Cedar.colors
    val select = { id: String? ->
        if (id != null && id != vm.selectedId.value) haptic(HapticFeedbackType.ContextClick)
        vm.select(id)
    }
    if (viewMode == ViewMode.ThreeD) {
        Scene3DCanvas(setup, selectedId, camera, colors.scene, select, vm::moveItemLive, vm::rotateItemLive, vm::endEdit, vm::updateCamera, vm::setViewportAspect, modifier)
    } else {
        Blueprint2D(setup, selectedId, colors.blueprint, select, vm::moveItemLive, vm::rotateItemLive, vm::endEdit, modifier)
    }
}

/** View toggle and camera views (top), selected-item bar (bottom), drawn over either canvas. */
@Composable
private fun CanvasOverlays(vm: StudioViewModel, setup: DeskSetup, selectedId: String?, viewMode: ViewMode, modifier: Modifier) {
    val colors = Cedar.colors
    val haptics = LocalHapticFeedback.current
    Box(modifier.fillMaxSize().padding(10.dp)) {
        Segmented(
            options = listOf(ViewMode.ThreeD to "3D", ViewMode.TwoD to "2D"),
            selected = viewMode,
            onSelect = vm::setViewMode,
            icons = mapOf(ViewMode.ThreeD to Lucide.Box, ViewMode.TwoD to Lucide.LayoutGrid),
            modifier = Modifier.align(Alignment.TopStart),
        )
        if (viewMode == ViewMode.ThreeD) {
            Column(Modifier.align(Alignment.TopEnd), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CedarIconButton(Lucide.Video, "Perspective view", { vm.setCameraView(CameraView.Perspective) })
                CedarIconButton(Lucide.Box, "Isometric view", { vm.setCameraView(CameraView.Isometric) })
                CedarIconButton(Lucide.ArrowDownToLine, "Top view", { vm.setCameraView(CameraView.Top) })
                CedarIconButton(Lucide.Eye, "Front view", { vm.setCameraView(CameraView.Front) })
            }
        }
        val item = setup.item(selectedId)
        val cat = item?.let { Catalog.item(it.catalogId) }
        AnimatedVisibility(cat != null, Modifier.align(Alignment.BottomCenter), enter = fadeIn(), exit = fadeOut()) {
            Surface(
                color = colors.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, colors.borderDistinct),
                shadowElevation = 6.dp,
                modifier = Modifier.widthIn(max = 480.dp),
            ) {
                Row(Modifier.padding(start = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(categoryIcon(cat?.category ?: ""), null, Modifier.size(18.dp), tint = colors.brandAccent)
                    Text(
                        cat?.name.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp),
                    )
                    CedarIconButton(Lucide.RotateCw, "Rotate 45°", {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        vm.rotateSelected45()
                    }, outlined = false)
                    CedarIconButton(Lucide.Trash2, "Delete item", {
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        vm.deleteSelected()
                    }, outlined = false, tint = colors.alert)
                    CedarIconButton(Lucide.X, "Deselect", { vm.select(null) }, outlined = false)
                }
            }
        }
    }
}

@Composable
private fun PanelContent(
    panel: Panel,
    setup: DeskSetup,
    selectedId: String?,
    inspectorTab: InspectorTab,
    onTab: (InspectorTab) -> Unit,
    saved: List<online.codyuhi.desksetup.data.SavedSetup>,
    actions: StudioActions,
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    val colors = Cedar.colors
    Column(modifier.imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                when (panel) {
                    Panel.Catalog -> "Hardware & Furniture"
                    Panel.Desk, Panel.Budget -> "Inspector"
                    Panel.Actions -> "Studio Actions & Presets"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textEmphasis,
                modifier = Modifier.weight(1f),
            )
            CedarIconButton(Lucide.X, "Close", onClose, outlined = false)
        }
        when (panel) {
            Panel.Catalog -> CatalogPanel(onAdd = onAdd)
            Panel.Desk, Panel.Budget -> InspectorPanel(inspectorTab, onTab, setup, selectedId, actions)
            Panel.Actions -> ActionsPanel(saved, actions)
        }
    }
}

@Composable
private fun StudioTopBar(canUndo: Boolean, canRedo: Boolean, onUndo: () -> Unit, onRedo: () -> Unit, dark: Boolean, onTheme: () -> Unit) {
    val colors = Cedar.colors
    Surface(color = colors.surface, border = BorderStroke(1.dp, colors.borderSubtle)) {
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)))
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(Modifier.size(28.dp))
            Text(
                "DESKForge Studio",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textEmphasis,
                maxLines = 1,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            CedarIconButton(Lucide.Undo2, "Undo", onUndo, enabled = canUndo, outlined = false)
            CedarIconButton(Lucide.Redo2, "Redo", onRedo, enabled = canRedo, outlined = false)
            ThemeToggle(dark, onTheme)
        }
    }
}

@Composable
private fun StudioBottomBar(panel: Panel?, onOpen: (Panel) -> Unit) {
    val colors = Cedar.colors
    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
        Panel.entries.forEach { p ->
            NavigationBarItem(
                selected = panel == p,
                onClick = { onOpen(p) },
                icon = { Icon(p.icon, null) },
                label = { Text(p.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.brandAccent,
                    selectedTextColor = colors.brandAccent,
                    indicatorColor = colors.inset,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
            )
        }
    }
}

@Composable
private fun StudioRail(
    panel: Panel?,
    onOpen: (Panel) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    dark: Boolean,
    onTheme: () -> Unit,
) {
    val colors = Cedar.colors
    NavigationRail(
        containerColor = colors.surface,
        modifier = Modifier.width(72.dp),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start),
        header = { BrandMark(Modifier.padding(vertical = 8.dp).size(32.dp)) },
    ) {
        Panel.entries.forEach { p ->
            NavigationRailItem(
                selected = panel == p,
                onClick = { onOpen(p) },
                icon = { Icon(p.icon, null) },
                label = { Text(p.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.brandAccent,
                    selectedTextColor = colors.brandAccent,
                    indicatorColor = colors.inset,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
            )
        }
        Box(Modifier.weight(1f))
        CedarIconButton(Lucide.Undo2, "Undo", onUndo, enabled = canUndo, outlined = false)
        CedarIconButton(Lucide.Redo2, "Redo", onRedo, enabled = canRedo, outlined = false)
        ThemeToggle(dark, onTheme, Modifier.padding(bottom = 8.dp))
    }
}

/** The favicon emblem (a monitor on a timber desk), drawn in the current theme's colors. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    val c = Cedar.colors
    val fg = if (c.dark) Color(0xFFF4F2ED) else Color(0xFF1F513F)
    val desk = if (c.dark) Color(0xFFC08552) else Color(0xFF8B5A2B)
    val glow = if (c.dark) Color(0xFFD89F28) else Color(0xFFB8860B)
    Canvas(modifier) {
        val u = size.minDimension / 64f
        fun r(x: Float, y: Float, w: Float, h: Float) = Offset(x * u, y * u) to Size(w * u, h * u)
        r(12f, 7f, 40f, 27f).let { (o, s) -> drawRoundRect(fg, o, s, CornerRadius(4 * u), style = Stroke(5 * u)) }
        r(20f, 16f, 14f, 4.5f).let { (o, s) -> drawRoundRect(glow, o, s, CornerRadius(2.25f * u)) }
        r(28.5f, 34f, 7f, 10f).let { (o, s) -> drawRect(fg, o, s) }
        r(4f, 44f, 56f, 7f).let { (o, s) -> drawRoundRect(desk, o, s, CornerRadius(3.5f * u)) }
        r(9f, 51f, 5f, 10f).let { (o, s) -> drawRoundRect(fg, o, s, CornerRadius(2 * u)) }
        r(50f, 51f, 5f, 10f).let { (o, s) -> drawRoundRect(fg, o, s, CornerRadius(2 * u)) }
    }
}

@Composable
private fun SaveSetupDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val colors = Cedar.colors
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save setup", style = MaterialTheme.typography.headlineSmall) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                singleLine = true,
                label = { Text("Setup name") },
                placeholder = { Text("e.g. Home office v2") },
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.brandAccent,
                    unfocusedBorderColor = colors.borderDistinct,
                    focusedLabelColor = colors.brandAccent,
                    cursorColor = colors.brandAccent,
                ),
            )
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text("Save", color = colors.brandAccent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textPrimary) } },
        containerColor = colors.surface,
        shape = MaterialTheme.shapes.large,
    )
}
