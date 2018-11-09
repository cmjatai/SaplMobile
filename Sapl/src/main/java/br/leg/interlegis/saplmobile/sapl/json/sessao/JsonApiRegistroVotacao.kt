package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.ExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.OrdemDia
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.RegistroVotacao
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiMateriaLegislativa
import com.google.gson.JsonArray
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiRegistroVotacao(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {


    override val url = String.format("api/mobile/%s/%s/", RegistroVotacao.APP_LABEL, RegistroVotacao.TABLE_NAME)

    companion object {

        val chave = String.format("%s:%s", RegistroVotacao.APP_LABEL, RegistroVotacao.TABLE_NAME)
    }


    override fun syncList(list: Any?, deleted: IntArray?): Int {

        val daoRegistroVotacao = AppDataBase.getInstance(context).DaoRegistroVotacao()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoRegistroVotacao.loadAllByIds(deleted)
            daoRegistroVotacao.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val mapRegistroVotacao = RegistroVotacao.importJsonArray(list) as Map<Int, RegistroVotacao>

        val listaExpedienteMateria = JsonArray()
        val listaOrdemDia =JsonArray()
        val jsonApiExpedienteMateria = JsonApiExpedienteMateria(context, retrofit)
        val jsonApiOrdemDia = JsonApiOrdemDia(context, retrofit)

        mapRegistroVotacao.forEach {
            val registroVotacao = it.value

            try {
                daoRegistroVotacao.insert(registroVotacao)
            }
            catch (e: SQLiteConstraintException) {
                if (it.value.expediente != null) {
                    val daoExpedienteMateria =  AppDataBase.getInstance(context).DaoExpedienteMateria()
                    val existExpedienteMateria = daoExpedienteMateria.exists(it.value.expediente!!)

                    if (!existExpedienteMateria) {
                        val expedienteMateria = jsonApiExpedienteMateria.getObject(it.value.expediente!!)
                        listaExpedienteMateria.add(expedienteMateria)
                    }
                }
                if (it.value.ordem != null) {
                    val daoOrdemDia =  AppDataBase.getInstance(context).DaoOrdemDia()
                    val existOrdemDia = daoOrdemDia.exists(it.value.ordem!!)

                    if (!existOrdemDia) {
                        val ordemDia = jsonApiOrdemDia.getObject(it.value.ordem!!)
                        listaOrdemDia.add(ordemDia)
                    }
                }

                // não tentar o insert novamente pois, neste caso do registro de votação,
                // ou nos syncs abaixo o registro de votação será atualizado

                jsonApiOrdemDia.syncList(listaOrdemDia)
                jsonApiExpedienteMateria.syncList(listaExpedienteMateria)

            }
        }


        return mapRegistroVotacao.size
    }
}