package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.ExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.RegistroVotacao
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiMateriaLegislativa
import com.google.gson.JsonArray
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiExpedienteMateria(context:Context, retrofit: Retrofit?): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", ExpedienteMateria.APP_LABEL, ExpedienteMateria.TABLE_NAME)

    companion object {

        val chave = String.format("%s:%s", ExpedienteMateria.APP_LABEL, ExpedienteMateria.TABLE_NAME)
    }

    override fun syncList(list: Any?, deleted: IntArray?): Int {

        val daoExpMat = AppDataBase.getInstance(context).DaoExpedienteMateria()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoExpMat.loadAllByIds(deleted)
            daoExpMat.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val listaExpedienteMateria = ArrayList<ExpedienteMateria>()
        val mapVotacao:HashMap<Int, RegistroVotacao> = HashMap()

        list.forEach {
            listaExpedienteMateria.add(ExpedienteMateria.importJsonObject(it.asJsonObject))
            mapVotacao.putAll(RegistroVotacao.importJsonArray(it.asJsonObject.get("votacao").asJsonArray) as HashMap<Int, RegistroVotacao>)
        }

        try {
            daoExpMat.insertAll(listaExpedienteMateria)
        }
        catch (e: SQLiteConstraintException) {
            val daoSessao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
            val daoMateria =  AppDataBase.getInstance(context).DaoMateriaLegislativa()

            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            val jsonApiSessaoPlenaria = JsonApiSessaoPlenaria(context, retrofit)


            val listaMaterias = JsonArray()
            val listaSessoes = JsonArray()
            listaExpedienteMateria.forEach {
                val existSessaoPlenaria = daoSessao.exists(it.sessao_plenaria)
                val existMateriaLegislativa = daoMateria.exists(it.materia)

                if (!existMateriaLegislativa) {
                    listaMaterias.add(jsonApiMateriaLegislativa.getObject(it.materia))
                }

                if (!existSessaoPlenaria) {
                    listaSessoes.add(jsonApiSessaoPlenaria.getObject(it.sessao_plenaria))
                }
            }

            jsonApiMateriaLegislativa.syncList(listaMaterias)
            jsonApiSessaoPlenaria.syncList(listaSessoes)

            daoExpMat.insertAll(listaExpedienteMateria)
        }

        if (mapVotacao.isNotEmpty()) {
            val daoRegistroVotacao = AppDataBase.getInstance(context).DaoRegistroVotacao()
            daoRegistroVotacao.insertAll(ArrayList(mapVotacao.values))
        }

        return listaExpedienteMateria.size
    }


    fun allBySessao(sessaoId:Int) {
        val kwargs = HashMap<String, Any>()
        kwargs["tipo_update"] = "get"
        kwargs["fk_name"] = "sessao_plenaria"
        kwargs["fk_pk"] = sessaoId
        sync(kwargs)
    }
}