package br.leg.interlegis.saplmobile.sapl

import android.app.Application
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import org.jetbrains.anko.doAsync

class SaplApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        doAsync {
            val database = AppDataBase.getInstance(context = this@SaplApplication)
        }
    }
}