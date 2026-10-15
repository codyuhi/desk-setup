package online.codyuhi.desksetup.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** The setup being edited, with its undo stack (one row, id 0), stored as JSON. */
@Entity(tableName = "working_setup")
data class WorkingSetupEntity(
    @PrimaryKey val id: Int = 0,
    val historyJson: String,
    val updatedAt: Long,
)

/** A named setup the user saved to the "My setups" library. */
@Entity(tableName = "saved_setup")
data class SavedSetupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val setupJson: String,
    val totalPrice: Int,
    val itemCount: Int,
    val updatedAt: Long,
)

@Dao
interface SetupDao {
    @Query("SELECT * FROM working_setup WHERE id = 0")
    suspend fun working(): WorkingSetupEntity?

    @Upsert
    suspend fun saveWorking(entity: WorkingSetupEntity)

    @Query("SELECT * FROM saved_setup ORDER BY updatedAt DESC")
    fun savedSetups(): Flow<List<SavedSetupEntity>>

    @Query("SELECT * FROM saved_setup WHERE id = :id")
    suspend fun saved(id: Long): SavedSetupEntity?

    @Insert
    suspend fun insertSaved(entity: SavedSetupEntity): Long

    @Query("DELETE FROM saved_setup WHERE id = :id")
    suspend fun deleteSaved(id: Long)
}

@Database(entities = [WorkingSetupEntity::class, SavedSetupEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun setupDao(): SetupDao
}
