package br.leg.interlegis.saplmobile.sapl

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.daos.base.DaoAutor
import br.leg.interlegis.saplmobile.sapl.db.daos.sessao.DaoSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.support.Log
import org.jetbrains.anko.doAsync

class SaplApplication : Application() {

    companion object {
        val DEBUG = true

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

            if (DEBUG) {
                // Apaga Autoria em Cascata
                db.DaoAutor().delete(db.DaoAutor().all_direct)

                // Apaga em Cascata:
                // Anexada,
                // DocAcessorios,
                // LegislacaoCitada
                // Tramitacao
                // OrdemDia
                // ExpedienteMateria
                // RegistroVotacao
                db.DaoMateriaLegislativa().delete(db.DaoMateriaLegislativa().all_direct)

                // Apaga em cascata
                // LegislacaoCitada
                db.DaoNormaJuridica().delete(db.DaoNormaJuridica().all_direct)

                // Apaga em Cascata:
                // OrdemDia
                // ExpedienteMateria
                // RegistroVotacao
                db.DaoSessaoPlenaria().delete(db.DaoSessaoPlenaria().all_direct)

                db.DaoTimeRefresh().delete(db.DaoTimeRefresh().all)
            }

            daoSessaoPlenaria = db.DaoSessaoPlenaria()
            sessoesPlenarias = daoSessaoPlenaria?.all
        }
    }
}