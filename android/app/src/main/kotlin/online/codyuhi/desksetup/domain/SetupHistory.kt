package online.codyuhi.desksetup.domain

import kotlinx.serialization.Serializable

/**
 * Undo/redo stack, mirroring `saveHistoryState`/`undo`/`redo` in app/src/state.js: identical
 * consecutive snapshots are skipped and a new commit drops the redo tail. Unlike the web, the
 * whole stack is persisted, so undo survives an app restart.
 */
@Serializable
data class SetupHistory(
    val snapshots: List<DeskSetup>,
    val index: Int,
) {
    val current: DeskSetup get() = snapshots[index]
    val canUndo: Boolean get() = index > 0
    val canRedo: Boolean get() = index < snapshots.lastIndex

    fun commit(setup: DeskSetup): SetupHistory {
        if (setup == current) return this
        val kept = snapshots.subList(0, index + 1) + setup
        val trimmed = if (kept.size > MAX_SNAPSHOTS) kept.takeLast(MAX_SNAPSHOTS) else kept
        return SetupHistory(trimmed, trimmed.lastIndex)
    }

    fun undo() = if (canUndo) copy(index = index - 1) else this

    fun redo() = if (canRedo) copy(index = index + 1) else this

    companion object {
        const val MAX_SNAPSHOTS = 100

        fun of(setup: DeskSetup) = SetupHistory(listOf(setup), 0)
    }
}
