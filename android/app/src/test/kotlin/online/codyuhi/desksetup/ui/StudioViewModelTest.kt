package online.codyuhi.desksetup.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import online.codyuhi.desksetup.data.Settings
import online.codyuhi.desksetup.data.SettingsStore
import online.codyuhi.desksetup.data.SetupRepository
import online.codyuhi.desksetup.data.ThemeMode
import online.codyuhi.desksetup.data.ViewMode
import online.codyuhi.desksetup.data.db.SavedSetupEntity
import online.codyuhi.desksetup.data.db.SetupDao
import online.codyuhi.desksetup.data.db.WorkingSetupEntity
import online.codyuhi.desksetup.domain.SetupHistory
import online.codyuhi.desksetup.domain.SetupJson
import online.codyuhi.desksetup.ui.studio.StudioViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeDao : SetupDao {
        var working: WorkingSetupEntity? = null
        val saved = MutableStateFlow<List<SavedSetupEntity>>(emptyList())
        override suspend fun working() = working
        override suspend fun saveWorking(entity: WorkingSetupEntity) { working = entity }
        override fun savedSetups(): Flow<List<SavedSetupEntity>> = saved
        override suspend fun saved(id: Long) = saved.value.firstOrNull { it.id == id }
        override suspend fun insertSaved(entity: SavedSetupEntity): Long {
            val id = saved.value.size + 1L
            saved.value = saved.value + entity.copy(id = id)
            return id
        }
        override suspend fun deleteSaved(id: Long) { saved.value = saved.value.filterNot { it.id == id } }
    }

    private class FakeSettings : SettingsStore {
        val state = MutableStateFlow(Settings())
        override val settings = state.map { it }
        override suspend fun setThemeMode(mode: ThemeMode) { state.value = state.value.copy(themeMode = mode) }
        override suspend fun setViewMode(mode: ViewMode) { state.value = state.value.copy(viewMode = mode) }
    }

    private val dao = FakeDao()
    private val repo = SetupRepository(dao) { 0L }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm() = StudioViewModel(repo, FakeSettings())

    @Test
    fun firstLaunchStartsOnDeveloperPreset() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        val setup = vm.history.value!!.current
        assertEquals("walnut", setup.deskFinish)
        assertEquals(9, setup.placedItems.size)
    }

    @Test
    fun aDragIsOneUndoStepAndIsAutoSaved() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        val id = vm.history.value!!.current.placedItems.first().id
        vm.moveItemLive(id, 10.0, 0.0)
        vm.moveItemLive(id, 20.0, 5.0)
        vm.moveItemLive(id, 30.4, 6.6)
        assertEquals(1, vm.history.value!!.snapshots.size)
        vm.endEdit()
        assertNull(vm.draft.value)
        assertEquals(2, vm.history.value!!.snapshots.size)
        val moved = vm.history.value!!.current.item(id)!!
        assertEquals(30.0, moved.x, 0.0)
        assertEquals(7.0, moved.z, 0.0)

        advanceUntilIdle()
        val stored = SetupJson.run { dao.working!!.historyJson }
        assertTrue(stored.contains("\"x\":30.0"))

        vm.undo()
        assertEquals(0.0, vm.history.value!!.current.item(id)!!.x, 0.0)
    }

    @Test
    fun restartRestoresWorkingSetupAndUndoStack() = runTest(dispatcher) {
        val first = vm()
        advanceUntilIdle()
        first.setSize(200, 100)
        advanceUntilIdle()

        val second = vm()
        advanceUntilIdle()
        val h: SetupHistory = second.history.value!!
        assertEquals(200, h.current.deskWidth)
        assertTrue(h.canUndo)
    }

    @Test
    fun addingADeskBaseChangesFinishInsteadOfPlacingAnItem() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        val before = vm.history.value!!.current.placedItems.size
        vm.messages.test {
            vm.addItem("desk-oak-finish")
            assertEquals("Updated desk frame to Scandinavian Natural Oak Surface", awaitItem())
            vm.addItem("decor-coffee-mug")
            assertEquals("Added Ceramic Espresso Mug to desk!", awaitItem())
        }
        val setup = vm.history.value!!.current
        assertEquals("oak", setup.deskFinish)
        assertEquals(before + 1, setup.placedItems.size)
        assertNotNull(setup.item(vm.selectedId.value))
    }

    @Test
    fun savedSetupsRoundTrip() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        vm.loadPreset("preset-gamer")
        vm.saveAs("  Battle  ")
        advanceUntilIdle()
        val saved = vm.savedSetups.value.single()
        assertEquals("Battle", saved.name)
        vm.loadPreset("preset-minimalist")
        vm.loadSaved(saved.id)
        advanceUntilIdle()
        assertEquals("black", vm.history.value!!.current.deskFinish)
    }

    @Test
    fun badImportLeavesSetupAlone() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        val before = vm.history.value
        vm.importJson("{nope")
        assertEquals(before, vm.history.value)
    }
}
