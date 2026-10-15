package online.codyuhi.desksetup.ui.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import online.codyuhi.desksetup.data.SavedSetup
import online.codyuhi.desksetup.domain.Catalog
import online.codyuhi.desksetup.domain.CatalogItem
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.TipKind
import online.codyuhi.desksetup.domain.budget
import online.codyuhi.desksetup.domain.ergonomics
import online.codyuhi.desksetup.ui.components.CedarCard
import online.codyuhi.desksetup.ui.components.CedarIconButton
import online.codyuhi.desksetup.ui.components.CedarOutlinedButton
import online.codyuhi.desksetup.ui.components.CedarPill
import online.codyuhi.desksetup.ui.components.CedarPrimaryButton
import online.codyuhi.desksetup.ui.components.Lucide
import online.codyuhi.desksetup.ui.components.SectionLabel
import online.codyuhi.desksetup.ui.components.Segmented
import online.codyuhi.desksetup.ui.components.ValueRow
import online.codyuhi.desksetup.ui.theme.Cedar
import online.codyuhi.desksetup.ui.theme.Mono
import online.codyuhi.desksetup.ui.theme.MonoSmall
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt

fun categoryIcon(id: String): ImageVector = when (id) {
    "desk" -> Lucide.Table
    "monitors" -> Lucide.Monitor
    "computers" -> Lucide.Cpu
    "peripherals" -> Lucide.Keyboard
    "audio" -> Lucide.Volume2
    "lighting" -> Lucide.Lightbulb
    else -> Lucide.Box
}

fun money(v: Int): String = "$" + NumberFormat.getIntegerInstance(Locale.US).format(v)

// --- Catalog -----------------------------------------------------------------------------------

/** "Hardware & Furniture": category pills and catalog cards. */
@Composable
fun CatalogPanel(onAdd: (String) -> Unit, modifier: Modifier = Modifier) {
    var category by rememberSaveable { mutableStateOf("all") }
    val items = if (category == "all") Catalog.items else Catalog.items.filter { it.category == category }
    Column(modifier) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CedarPill("All Items", category == "all", { category = "all" })
            Catalog.categories.forEach { CedarPill(it.name, category == it.id, { category = it.id }) }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items, key = { it.id }) { CatalogCard(it, onAdd) }
        }
    }
}

@Composable
private fun CatalogCard(item: CatalogItem, onAdd: (String) -> Unit) {
    val c = Cedar.colors
    Surface(
        onClick = { onAdd(item.id) },
        color = c.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, c.borderSubtle),
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Add ${item.name}, ${money(item.price)}" },
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(categoryIcon(item.category), null, Modifier.padding(top = 2.dp).size(18.dp), tint = c.brandAccent)
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textEmphasis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                )
                Text(money(item.price), style = Mono, color = c.textPrimary)
            }
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val d = item.dimensions
                Text(
                    "${fmt(d.width)}×${fmt(d.depth)}×${fmt(d.height)}cm",
                    style = MonoSmall,
                    color = c.textSecondary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(c.inset)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                )
                Box(Modifier.weight(1f))
                Icon(Lucide.Plus, null, Modifier.size(16.dp), tint = c.brandAccent)
                Text(if (item.isDeskBase) "Apply" else "Add", style = MaterialTheme.typography.labelLarge, color = c.brandAccent)
            }
        }
    }
}

private fun fmt(v: Double) = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

// --- Inspector: Desk / Item / Budget -----------------------------------------------------------

enum class InspectorTab { Desk, Item, Budget }

@Composable
fun InspectorPanel(
    tab: InspectorTab,
    onTab: (InspectorTab) -> Unit,
    setup: DeskSetup,
    selectedId: String?,
    actions: StudioActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Segmented(
            options = listOf(InspectorTab.Desk to "Desk", InspectorTab.Item to "Item", InspectorTab.Budget to "Budget"),
            selected = tab,
            onSelect = onTab,
            icons = mapOf(InspectorTab.Desk to Lucide.Table, InspectorTab.Item to Lucide.Package, InspectorTab.Budget to Lucide.ChartColumn),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (tab) {
                InspectorTab.Desk -> DeskTab(setup, actions)
                InspectorTab.Item -> ItemTab(setup, selectedId, actions)
                InspectorTab.Budget -> BudgetTab(setup, actions)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeskTab(setup: DeskSetup, actions: StudioActions) {
    val c = Cedar.colors
    SectionLabel("Desk Surface Finish")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Catalog.finishes.values.forEach { f ->
            val active = f.key == setup.deskFinish
            Column(
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .semantics { selected = active; role = Role.RadioButton }
                    .clickable { actions.setFinish(f.key) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(f.color.toInt()))
                        .border(if (active) 3.dp else 1.dp, if (active) c.brandAccent else c.borderDistinct, CircleShape),
                )
                Text(f.name, style = MaterialTheme.typography.bodySmall, color = if (active) c.textEmphasis else c.textSecondary)
            }
        }
    }

    SectionLabel("Desk Surface Dimensions")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Catalog.sizePresets.forEach { (w, d) ->
            CedarPill("$w×$d", setup.deskWidth == w && setup.deskDepth == d, { actions.setSize(w, d) })
        }
    }
    CmSlider("Width", setup.deskWidth, DeskSetup.MIN_WIDTH..DeskSetup.MAX_WIDTH, actions.setWidthLive, actions.endEdit)
    CmSlider("Depth", setup.deskDepth, DeskSetup.MIN_DEPTH..DeskSetup.MAX_DEPTH, actions.setDepthLive, actions.endEdit)
    CmSlider("Height", setup.deskHeight, DeskSetup.MIN_HEIGHT..DeskSetup.MAX_HEIGHT, actions.setHeightLive, actions.endEdit)
}

@Composable
private fun CmSlider(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit, onDone: () -> Unit) {
    val c = Cedar.colors
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$label:", style = MaterialTheme.typography.labelLarge, color = c.textPrimary, modifier = Modifier.weight(1f))
            Text("${value}cm", style = Mono, color = c.textEmphasis)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            onValueChangeFinished = onDone,
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = c.brand,
                activeTrackColor = c.brand,
                inactiveTrackColor = c.hover,
            ),
            modifier = Modifier.semantics { contentDescription = "Desk $label, $value centimetres" },
        )
    }
}

@Composable
private fun ItemTab(setup: DeskSetup, selectedId: String?, actions: StudioActions) {
    val c = Cedar.colors
    SectionLabel("Selected Item")
    val item = setup.item(selectedId)
    val cat = item?.let { Catalog.item(it.catalogId) }
    if (item == null || cat == null) {
        Text(
            "Select any item in 3D or 2D view to adjust position, rotation, and finishes.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
        )
        Text(
            "Tip: tap to select, drag a selected item to move it, and twist two fingers to rotate it.",
            style = MaterialTheme.typography.bodySmall,
            color = c.textMuted,
        )
        return
    }
    CedarCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(categoryIcon(cat.category), null, Modifier.size(18.dp), tint = c.brandAccent)
            Text(cat.name, style = MaterialTheme.typography.titleLarge, color = c.textEmphasis, modifier = Modifier.padding(start = 8.dp))
        }
        ValueRow("Category", Catalog.categories.firstOrNull { it.id == cat.category }?.name ?: cat.category)
        ValueRow("Position X", "${item.x.roundToInt()}cm")
        ValueRow("Position Z", "${item.z.roundToInt()}cm")
        ValueRow("Rotation", "${(item.rotation * 180 / PI).roundToInt()}°")
        ValueRow("Price", money(cat.price))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CedarOutlinedButton("Rotate 45°", actions.rotate45, Modifier.weight(1f), icon = Lucide.RotateCw)
        CedarOutlinedButton("Remove Item", actions.delete, Modifier.weight(1f), icon = Lucide.Trash2, danger = true)
    }
}

@Composable
private fun BudgetTab(setup: DeskSetup, actions: StudioActions) {
    val c = Cedar.colors
    val ergo = setup.ergonomics()
    val budget = setup.budget()
    SectionLabel("Ergonomic Posture Diagnostic")
    CedarCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${ergo.score}%", style = MaterialTheme.typography.headlineMedium, color = c.textEmphasis)
            val (label, color) = when {
                ergo.score >= 90 -> "Optimal Workspace" to c.successText
                ergo.score >= 70 -> "Good Workspace" to c.warningText
                else -> "Needs Attention" to c.errorText
            }
            Text(label, style = MaterialTheme.typography.titleSmall, color = color, modifier = Modifier.padding(start = 12.dp))
        }
        ergo.tips.forEach { tip ->
            val (icon, tint) = when (tip.kind) {
                TipKind.Warning -> Lucide.TriangleAlert to c.warningText
                TipKind.Idea -> Lucide.Lightbulb to c.infoText
                TipKind.Positive -> Lucide.ThumbsUp to c.successText
                TipKind.Ideal -> Lucide.CircleCheck to c.successText
            }
            Row {
                Icon(icon, null, Modifier.padding(top = 2.dp).size(16.dp), tint = tint)
                Text(tip.text, style = MaterialTheme.typography.bodySmall, color = c.textPrimary, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
    SectionLabel("Hardware & Budget")
    CedarCard {
        budget.breakdown.forEach { line ->
            Row {
                Text(line.name, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, modifier = Modifier.weight(1f))
                Text(money(line.price), style = MonoSmall, color = c.textPrimary, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = 1.dp).background(c.borderSubtle))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("Total Estimated", Modifier.weight(1f))
            Text(money(budget.totalPrice), style = MaterialTheme.typography.headlineSmall, color = c.textEmphasis)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CedarOutlinedButton("Share CSV", actions.shareCsv, Modifier.weight(1f), icon = Lucide.Share2)
        CedarOutlinedButton("Save CSV", actions.saveCsv, Modifier.weight(1f), icon = Lucide.Sheet)
    }
}

// --- Actions & presets -------------------------------------------------------------------------

@Composable
fun ActionsPanel(
    savedSetups: List<SavedSetup>,
    actions: StudioActions,
    modifier: Modifier = Modifier,
) {
    val c = Cedar.colors
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionLabel("Workspace Preset")
        Catalog.presets.forEach { preset ->
            Surface(
                onClick = { actions.loadPreset(preset.id) },
                color = c.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, c.borderSubtle),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.LampDesk, null, Modifier.size(20.dp), tint = c.brandAccent)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text(preset.name, style = MaterialTheme.typography.titleSmall, color = c.textEmphasis)
                        Text(preset.description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                }
            }
        }

        SectionLabel("My Setups", Modifier.padding(top = 8.dp))
        CedarPrimaryButton("Save current setup", actions.requestSave, Modifier.fillMaxWidth(), icon = Lucide.BookmarkPlus)
        if (savedSetups.isEmpty()) {
            Text("Saved setups appear here and stay on this phone.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }
        savedSetups.forEach { saved ->
            Surface(
                onClick = { actions.loadSaved(saved.id) },
                color = c.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, c.borderSubtle),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Library, null, Modifier.size(18.dp), tint = c.brandAccent)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 10.dp)) {
                        Text(saved.name, style = MaterialTheme.typography.titleSmall, color = c.textEmphasis, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${saved.itemCount} items · ${money(saved.totalPrice)}", style = MonoSmall, color = c.textSecondary)
                    }
                    CedarIconButton(Lucide.Trash2, "Delete ${saved.name}", { actions.deleteSaved(saved.id) }, outlined = false, tint = c.alert)
                }
            }
        }

        SectionLabel("Snapshot & Exports", Modifier.padding(top = 8.dp))
        CedarOutlinedButton("Share Snapshot PNG", actions.shareSnapshot, Modifier.fillMaxWidth(), icon = Lucide.Camera)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CedarOutlinedButton("Save JSON", actions.saveJson, Modifier.weight(1f), icon = Lucide.Save)
            CedarOutlinedButton("Share JSON", actions.shareJson, Modifier.weight(1f), icon = Lucide.Share2)
        }
        CedarOutlinedButton("Open JSON Config", actions.openJson, Modifier.fillMaxWidth(), icon = Lucide.FolderOpen)
        CedarOutlinedButton("Share Budget CSV", actions.shareCsv, Modifier.fillMaxWidth(), icon = Lucide.Sheet)
        CedarOutlinedButton("Clear / Reset Setup", actions.requestClear, Modifier.fillMaxWidth(), icon = Lucide.Trash2, danger = true)
    }
}

/** Every user action the panels can trigger, wired up once in [StudioScreen]. */
class StudioActions(
    val setFinish: (String) -> Unit,
    val setSize: (Int, Int) -> Unit,
    val setWidthLive: (Int) -> Unit,
    val setDepthLive: (Int) -> Unit,
    val setHeightLive: (Int) -> Unit,
    val endEdit: () -> Unit,
    val rotate45: () -> Unit,
    val delete: () -> Unit,
    val loadPreset: (String) -> Unit,
    val requestSave: () -> Unit,
    val loadSaved: (Long) -> Unit,
    val deleteSaved: (Long) -> Unit,
    val shareSnapshot: () -> Unit,
    val saveJson: () -> Unit,
    val shareJson: () -> Unit,
    val openJson: () -> Unit,
    val shareCsv: () -> Unit,
    val saveCsv: () -> Unit,
    val requestClear: () -> Unit,
)
