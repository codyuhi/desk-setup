package online.codyuhi.desksetup

import android.app.Application
import online.codyuhi.desksetup.di.AppContainer

class DeskSetupApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
