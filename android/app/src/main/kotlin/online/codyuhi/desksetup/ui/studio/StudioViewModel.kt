package online.codyuhi.desksetup.ui.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import online.codyuhi.desksetup.data.SavedSetup
import online.codyuhi.desksetup.data.Settings
import online.codyuhi.desksetup.data.SettingsStore
import online.codyuhi.desksetup.data.SetupRepository
import online.codyuhi.desksetup.data.ThemeMode
import online.codyuhi.desksetup.data.ViewMode
import online.codyuhi.desksetup.domain.CameraView
import online.codyuhi.desksetup.domain.Catalog
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.OrbitCamera
import online.codyuhi.desksetup.domain.PlacedItem
import online.codyuhi.desksetup.domain.SetupHistory
import online.codyuhi.desksetup.domain.SetupJson
import online.codyuhi.desksetup.domain.rotatedQuarterTurn
import kotlin.math.PI

/**
 * Editor state for the single studio screen (the native port of app/src/state.js + main.js).
 *
 * Every committed change lands in [SetupHistory] and is auto-saved to Room. Continuous edits
 * (dragging an item, sliding the height) update a [draft] and commit once when the finger lifts,
 * so one drag is one undo step.
 */
class StudioViewModel(
    private val setups: SetupRepository,
    private val settingsRepo: SettingsStore,
) : ViewModel() {
    private val _history = MutableStateFlow<SetupHistory?>(null)
    val history: StateFlow<SetupHistory?> = _history.asStateFlow()

    private val _draft = MutableStateFlow<DeskSetup?>(null)
    /** In-progress edit shown instead of the committed setup while a finger is down. */
    val draft: StateFlow<DeskSetup?> = _draft.asStateFlow()

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    private val _camera = MutableStateFlow(OrbitCamera.initial())
    val camera: StateFlow<OrbitCamera> = _camera.asStateFlow()

    /** Width / height of the 3D canvas; changes when the Fold folds or the phone rotates. */
    private var aspect = 16.0 / 9
    private var cameraTouched = false

    val settings: StateFlow<Settings?> = settingsRepo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val savedSetups: StateFlow<List<SavedSetup>> = setups.savedSetups.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** One-shot toast messages (the web's showToast). */
    val messages = _messages.receiveAsFlow()

    private var cameraAnimation: Job? = null

    init {
        viewModelScope.launch {
            // Restore the last auto-saved setup; first launch starts on the Developer preset like the web.
            val restored = setups.loadWorking() ?: SetupHistory.of(DeskSetup.default)
            _history.value = restored
            if (!cameraTouched) _camera.value = OrbitCamera.initial(aspect, restored.current.deskWidth)
        }
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _history.filterNotNull().drop(1).debounce(250).collect { setups.saveWorking(it) }
        }
    }

    val current: DeskSetup? get() = _draft.value ?: _history.value?.current

    private fun commit(setup: DeskSetup) {
        _draft.value = null
        _history.update { it?.commit(setup) }
    }

    private fun toast(message: String) {
        _messages.trySend(message)
    }

    /** Writes the working setup now (called when the app goes to the background). */
    fun flush() {
        val h = _history.value ?: return
        viewModelScope.launch { setups.saveWorking(h) }
    }

    // --- Catalog -------------------------------------------------------------------------------

    fun addItem(catalogId: String) {
        val setup = current ?: return
        val cat = Catalog.item(catalogId) ?: return
        if (cat.isDeskBase) {
            commit(setup.withFinish(cat.defaultFinish ?: "walnut"))
            toast("Updated desk frame to ${cat.name}")
            return
        }
        val item = PlacedItem(DeskSetup.newItemId(), catalogId, 0.0, 0.0)
        commit(setup.copy(placedItems = setup.placedItems + item))
        _selectedId.value = item.id
        toast("Added ${cat.name} to desk!")
    }

    // --- Selection & item edits ----------------------------------------------------------------

    fun select(id: String?) {
        _selectedId.value = id?.takeIf { current?.item(it) != null }
    }

    fun moveItemLive(id: String, xCm: Double, zCm: Double) {
        val base = current ?: return
        _draft.value = base.moveItem(id, xCm.coerceIn(-150.0, 150.0), zCm.coerceIn(-120.0, 120.0))
    }

    fun rotateItemLive(id: String, radians: Double) {
        val base = current ?: return
        _draft.value = base.rotateItem(id, normalizeAngle(radians))
    }

    /** Commits the in-progress drag, twist or slider move as one undo step. */
    fun endEdit() {
        _draft.value?.let { commit(it) }
    }

    fun rotateSelected45() {
        val setup = current ?: return
        val item = setup.item(_selectedId.value) ?: return
        commit(setup.rotateItem(item.id, rotatedQuarterTurn(item.rotation)))
        toast("Rotated item 45°")
    }

    fun deleteSelected() {
        val setup = current ?: return
        val id = _selectedId.value ?: return
        commit(setup.removeItem(id))
        _selectedId.value = null
        toast("Item deleted.")
    }

    // --- Desk ----------------------------------------------------------------------------------

    fun setFinish(key: String) {
        val setup = current ?: return
        commit(setup.withFinish(key))
        toast("Applied ${Catalog.finish(key).name} desk finish.")
    }

    fun setHeightLive(cm: Int) {
        _draft.value = (current ?: return).withHeight(cm)
    }

    fun setWidthLive(cm: Int) {
        val s = current ?: return
        _draft.value = s.withDimensions(cm, s.deskDepth)
    }

    fun setDepthLive(cm: Int) {
        val s = current ?: return
        _draft.value = s.withDimensions(s.deskWidth, cm)
    }

    fun setSize(widthCm: Int, depthCm: Int) {
        commit((current ?: return).withDimensions(widthCm, depthCm))
        toast("Desk resized to ${widthCm}cm × ${depthCm}cm")
    }

    // --- History & setups ----------------------------------------------------------------------

    fun undo() {
        _draft.value = null
        _history.update { it?.undo() }
        _selectedId.value = null
        toast("Undo action")
    }

    fun redo() {
        _draft.value = null
        _history.update { it?.redo() }
        _selectedId.value = null
        toast("Redo action")
    }

    fun clear() {
        commit((current ?: return).copy(placedItems = emptyList()))
        _selectedId.value = null
        toast("Setup cleared.")
    }

    fun loadPreset(presetId: String) {
        val preset = Catalog.presets.firstOrNull { it.id == presetId } ?: return
        commit(DeskSetup.fromPreset(preset))
        _selectedId.value = null
        toast("Loaded preset setup!")
    }

    fun saveAs(name: String) {
        val setup = current ?: return
        val trimmed = name.trim().ifEmpty { "My desk setup" }
        viewModelScope.launch {
            setups.save(trimmed, setup)
            toast("Saved \"$trimmed\" to My setups")
        }
    }

    fun loadSaved(id: Long) {
        viewModelScope.launch {
            val setup = setups.load(id)
            if (setup == null) {
                toast("Couldn't open that setup.")
            } else {
                commit(setup)
                _selectedId.value = null
                toast("Loaded saved setup!")
            }
        }
    }

    fun deleteSaved(id: Long) {
        viewModelScope.launch { setups.delete(id) }
    }

    /** Imports a JSON config (from the file picker, a share or "Open with"). */
    fun importJson(text: String?) {
        val setup = text?.let { SetupJson.import(it, current ?: DeskSetup()) }
        if (setup == null) {
            toast("Error loading JSON file.")
        } else {
            commit(setup)
            _selectedId.value = null
            toast("Successfully imported setup config!")
        }
    }

    // --- View ----------------------------------------------------------------------------------

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch { settingsRepo.setViewMode(mode) }
    }

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch { settingsRepo.setThemeMode(if (currentlyDark) ThemeMode.Light else ThemeMode.Dark) }
    }

    fun updateCamera(transform: (OrbitCamera) -> OrbitCamera) {
        cameraAnimation?.cancel()
        cameraTouched = true
        _camera.update(transform)
    }

    /** Re-frames for a new canvas shape: the untouched default view refits, a moved camera keeps its pose. */
    fun setViewportAspect(value: Double) {
        if (value <= 0 || value == aspect) return
        aspect = value
        _camera.update {
            if (cameraTouched) it.copy(fovYDegrees = OrbitCamera.fovFor(value))
            else OrbitCamera.initial(value, current?.deskWidth ?: 160)
        }
    }

    /** Glides to one of the four preset views over ~450 ms. */
    fun setCameraView(view: CameraView) {
        val from = _camera.value
        val to = OrbitCamera.preset(view, current?.deskHeight ?: 74, aspect, current?.deskWidth ?: 160)
        // Take the short way round so the camera never spins a full turn.
        var dAz = (to.azimuth - from.azimuth) % (2 * PI)
        if (dAz > PI) dAz -= 2 * PI
        if (dAz < -PI) dAz += 2 * PI
        cameraAnimation?.cancel()
        cameraAnimation = viewModelScope.launch {
            val steps = 27
            for (i in 1..steps) {
                val t = i.toDouble() / steps
                val e = 1 - (1 - t) * (1 - t) * (1 - t) // ease-out cubic
                _camera.value = OrbitCamera(
                    target = from.target + (to.target - from.target) * e,
                    azimuth = from.azimuth + dAz * e,
                    elevation = from.elevation + (to.elevation - from.elevation) * e,
                    distance = from.distance + (to.distance - from.distance) * e,
                    fovYDegrees = to.fovYDegrees,
                )
                delay(16)
            }
        }
    }

    companion object {
        fun normalizeAngle(r: Double): Double {
            val twoPi = 2 * PI
            return ((r % twoPi) + twoPi) % twoPi
        }
    }
}
