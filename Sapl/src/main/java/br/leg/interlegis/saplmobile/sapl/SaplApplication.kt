package br.leg.interlegis.saplmobile.sapl

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.support.Log
import org.jetbrains.anko.doAsync

class SaplApplication : Application() {

    companion object {
        var sessoesPlenarias: LiveData<List<SessaoPlenaria>>? = null
        var daoSessaoPlenaria: DaoSessaoPlenaria? = null


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
            val db = AppDataBase.getInstance(context = this@SaplApplication)
            daoSessaoPlenaria = db.DaoSessaoPlenaria()
            sessoesPlenarias = daoSessaoPlenaria?.all

            var teste = daoSessaoPlenaria?.all_test
            Log.d("SAPL", teste!!.size.toString())




        }
    }
}