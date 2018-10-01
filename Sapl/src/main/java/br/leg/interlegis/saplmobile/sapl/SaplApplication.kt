package br.leg.interlegis.saplmobile.sapl

import android.app.Application
import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import org.jetbrains.anko.doAsync

class SaplApplication : Application() {

    companion object {
        var activitiesVisibles: HashSet<Context> = HashSet<Context>()

        fun isAnyActivityVisible(): Boolean {
            return activitiesVisibles.isNotEmpty()
        }

        fun activityResumed(context: Context) {
            activitiesVisibles.add(context)
        }

        fun activityPaused(context: Context) {
            activitiesVisibles.remove(context)
        }
    }


    override fun onCreate() {
        super.onCreate()

        doAsync {
            val database = AppDataBase.getInstance(context = this@SaplApplication)
        }
    }
}