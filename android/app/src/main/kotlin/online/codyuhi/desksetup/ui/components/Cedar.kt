package online.codyuhi.desksetup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import online.codyuhi.desksetup.ui.theme.Cedar
import online.codyuhi.desksetup.ui.theme.Eyebrow

@Composable
fun CedarPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = Cedar.colors.brand, contentColor = Color.White),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, Modifier.size(18.dp))
            Box(Modifier.size(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CedarOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    danger: Boolean = false,
    enabled: Boolean = true,
) {
    val content = if (danger) Cedar.colors.alert else Cedar.colors.textPrimary
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, if (danger) Cedar.colors.alert.copy(alpha = 0.45f) else Cedar.colors.borderDistinct),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Cedar.colors.surface, contentColor = content),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, Modifier.size(18.dp))
            Box(Modifier.size(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** 48 dp icon button with a Cedar outline, used for toolbar actions and the theme toggle. */
@Composable
fun CedarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = true,
    tint: Color = Cedar.colors.textPrimary,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .padding(4.dp)
            .then(
                if (outlined) Modifier.border(1.dp, Cedar.colors.borderDistinct, MaterialTheme.shapes.small)
                else Modifier,
            ),
        shape = MaterialTheme.shapes.small,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (outlined) Cedar.colors.surface else Color.Transparent,
            contentColor = tint,
            disabledContentColor = Cedar.colors.textMuted.copy(alpha = 0.5f),
            disabledContainerColor = if (outlined) Cedar.colors.surface else Color.Transparent,
        ),
    ) {
        Icon(icon, contentDescription, Modifier.size(20.dp))
    }
}

/** Moon in light mode, sun in dark mode, like the web toggle. Shown on every screen. */
@Composable
fun ThemeToggle(dark: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    CedarIconButton(
        icon = if (dark) Lucide.Sun else Lucide.Moon,
        contentDescription = if (dark) "Switch to light theme" else "Switch to dark theme",
        onClick = onToggle,
        modifier = modifier,
        tint = if (dark) Cedar.colors.ochre else Cedar.colors.textPrimary,
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = Eyebrow, color = Cedar.colors.textSecondary, modifier = modifier)
}

@Composable
fun CedarCard(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(14.dp), content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier,
        color = Cedar.colors.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, Cedar.colors.borderSubtle),
    ) {
        Column(Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

/** Selectable pill (category filter, desk size): Spruce when active. */
@Composable
fun CedarPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Cedar.colors
    Box(
        modifier
            .heightIn(min = 40.dp)
            .clip(CircleShape)
            .background(if (selected) c.brand else c.inset)
            .border(1.dp, if (selected) c.brand else c.borderSubtle, CircleShape)
            .semantics { this.selected = selected; role = Role.Tab }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else c.textPrimary, maxLines = 1)
    }
}

/** Two-option segmented control (3D / 2D, inspector tabs). */
@Composable
fun <T> Segmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    icons: Map<T, ImageVector> = emptyMap(),
) {
    val c = Cedar.colors
    Row(
        modifier
            .clip(MaterialTheme.shapes.small)
            .background(c.inset)
            .border(1.dp, c.borderSubtle, MaterialTheme.shapes.small)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Row(
                Modifier
                    .heightIn(min = 42.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(if (active) c.brand else Color.Transparent)
                    .semantics { this.selected = active; role = Role.Tab }
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                icons[value]?.let { Icon(it, null, Modifier.size(16.dp), tint = if (active) Color.White else c.textSecondary) }
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) Color.White else c.textPrimary)
            }
        }
    }
}

/** Label + value row used in budget and inspector lists. */
@Composable
fun ValueRow(label: String, value: String, modifier: Modifier = Modifier, emphasis: Boolean = false) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Cedar.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (emphasis) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
            color = Cedar.colors.textPrimary,
        )
    }
}
