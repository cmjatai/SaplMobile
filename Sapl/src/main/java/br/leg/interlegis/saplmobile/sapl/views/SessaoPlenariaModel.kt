package br.leg.interlegis.saplmobile.sapl.views

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.ExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.OrdemDia
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria

class SessaoPlenariaViewModel: ViewModel() {
    var uid_sessao: Int = 0
    var sessao: LiveData<SessaoPlenaria>? = null
        get() {
            if (field == null) {
                if (uid_sessao == 0)
                    return null

                val db = AppDataBase.getInstance(context = SaplApplication.instance!!)
                field = db.DaoSessaoPlenaria().getLDSessao(uid_sessao)
            }
            return field
        }

    var materias_do_expediente: LiveData<List<ExpedienteMateria>>? = null
        get() {
            if (field == null) {
                if (uid_sessao == 0)
                    return null

                val db = AppDataBase.getInstance(context = SaplApplication.instance!!)
                field = db.DaoExpedienteMateria().all_by_sessao(uid_sessao)
            }
            return field
        }

    var materias_da_ordemdia: LiveData<List<OrdemDia>>? = null
        get() {
            if (field == null) {
                if (uid_sessao == 0)
                    return null

                val db = AppDataBase.getInstance(context = SaplApplication.instance!!)
                field = db.DaoOrdemDia().all_by_sessao(uid_sessao)
            }
            return field
        }
}