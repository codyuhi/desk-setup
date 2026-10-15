package online.codyuhi.desksetup.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { System, Light, Dark }

enum class ViewMode { ThreeD, TwoD }

data class Settings(
    val themeMode: ThemeMode = ThemeMode.System,
    val viewMode: ViewMode = ViewMode.ThreeD,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsStore {
    val settings: Flow<Settings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setViewMode(mode: ViewMode)
}

/** User preferences; the theme key mirrors the web's localStorage `deskforge_theme`. */
class SettingsRepository(private val context: Context) : SettingsStore {
    private object Keys {
        val theme = stringPreferencesKey("deskforge_theme")
        val viewMode = stringPreferencesKey("deskforge_view_mode")
    }

    override val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            themeMode = p[Keys.theme]?.let { v -> ThemeMode.entries.firstOrNull { it.name == v } } ?: ThemeMode.System,
            viewMode = p[Keys.viewMode]?.let { v -> ViewMode.entries.firstOrNull { it.name == v } } ?: ViewMode.ThreeD,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = mode.name }
    }

    override suspend fun setViewMode(mode: ViewMode) {
        context.dataStore.edit { it[Keys.viewMode] = mode.name }
    }
}
