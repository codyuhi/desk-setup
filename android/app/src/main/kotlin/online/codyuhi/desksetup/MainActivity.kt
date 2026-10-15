package online.codyuhi.desksetup

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.codyuhi.desksetup.data.ThemeMode
import online.codyuhi.desksetup.ui.studio.StudioScreen
import online.codyuhi.desksetup.ui.studio.StudioViewModel
import online.codyuhi.desksetup.ui.theme.CedarDark
import online.codyuhi.desksetup.ui.theme.CedarLight
import online.codyuhi.desksetup.ui.theme.CedarTheme

class MainActivity : ComponentActivity() {
    /** JSON text handed to the app through "Open with" or the share sheet, waiting for the ViewModel. */
    private val incomingJson = MutableStateFlow<String?>(null)
    private var studio: StudioViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        val container = (application as DeskSetupApplication).container
        var ready = false
        splash.setKeepOnScreenCondition { !ready }
        if (savedInstanceState == null) handleIntent(intent)

        setContent {
            val vm: StudioViewModel = viewModel(
                factory = viewModelFactory { initializer { StudioViewModel(container.setupRepository, container.settingsRepository) } },
            )
            studio = vm
            val settings = vm.settings.collectAsStateWithLifecycle().value
            val history = vm.history.collectAsStateWithLifecycle().value
            if (settings != null && history != null) ready = true
            val dark = when (settings?.themeMode ?: ThemeMode.System) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            LaunchedEffect(dark) {
                val bars = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            val pending = incomingJson.collectAsStateWithLifecycle().value
            LaunchedEffect(pending, history != null) {
                if (pending != null && history != null) {
                    vm.importJson(pending)
                    incomingJson.value = null
                }
            }
            CedarTheme(if (dark) CedarDark else CedarLight) {
                StudioScreen(vm, dark)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        studio?.flush()
    }

    private fun handleIntent(intent: Intent?) {
        val uri: Uri = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            else -> null
        } ?: return
        lifecycleScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
            }
            incomingJson.value = text ?: ""
        }
    }
}
