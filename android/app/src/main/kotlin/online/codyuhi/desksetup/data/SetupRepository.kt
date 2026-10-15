package online.codyuhi.desksetup.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import online.codyuhi.desksetup.data.db.SavedSetupEntity
import online.codyuhi.desksetup.data.db.SetupDao
import online.codyuhi.desksetup.data.db.WorkingSetupEntity
import online.codyuhi.desksetup.domain.AppJson
import online.codyuhi.desksetup.domain.DeskSetup
import online.codyuhi.desksetup.domain.SetupHistory
import online.codyuhi.desksetup.domain.budget

data class SavedSetup(
    val id: Long,
    val name: String,
    val totalPrice: Int,
    val itemCount: Int,
    val updatedAt: Long,
)

/** Room is the source of truth: the working setup auto-saves on every change, offline. */
class SetupRepository(private val dao: SetupDao, private val clock: () -> Long = System::currentTimeMillis) {
    /** The last working setup and undo stack, or null on first launch (or if the row is unreadable). */
    suspend fun loadWorking(): SetupHistory? = dao.working()?.let {
        runCatching { AppJson.decodeFromString(SetupHistory.serializer(), it.historyJson) }.getOrNull()
            ?.takeIf { h -> h.snapshots.isNotEmpty() && h.index in h.snapshots.indices }
    }

    suspend fun saveWorking(history: SetupHistory) = dao.saveWorking(
        WorkingSetupEntity(historyJson = AppJson.encodeToString(SetupHistory.serializer(), history), updatedAt = clock()),
    )

    val savedSetups: Flow<List<SavedSetup>> = dao.savedSetups().map { rows ->
        rows.map { SavedSetup(it.id, it.name, it.totalPrice, it.itemCount, it.updatedAt) }
    }

    suspend fun save(name: String, setup: DeskSetup): Long = dao.insertSaved(
        SavedSetupEntity(
            name = name,
            setupJson = AppJson.encodeToString(DeskSetup.serializer(), setup),
            totalPrice = setup.budget().totalPrice,
            itemCount = setup.placedItems.size,
            updatedAt = clock(),
        ),
    )

    suspend fun load(id: Long): DeskSetup? = dao.saved(id)?.let {
        runCatching { AppJson.decodeFromString(DeskSetup.serializer(), it.setupJson) }.getOrNull()
    }

    suspend fun delete(id: Long) = dao.deleteSaved(id)
}
