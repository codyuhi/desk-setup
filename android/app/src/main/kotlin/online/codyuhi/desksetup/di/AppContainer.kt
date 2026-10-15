package online.codyuhi.desksetup.di

import android.content.Context
import androidx.room.Room
import online.codyuhi.desksetup.data.SettingsRepository
import online.codyuhi.desksetup.data.SetupRepository
import online.codyuhi.desksetup.data.db.AppDatabase

/** Manual constructor injection: one instance of each dependency for the whole app. */
class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "desksetup.db").build()

    val setupRepository = SetupRepository(database.setupDao())
    val settingsRepository = SettingsRepository(context)
}
