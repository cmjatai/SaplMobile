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

class JsonApiOrdemDia(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {


    override val url = String.format("api/mobile/%s/%s/", OrdemDia.APP_LABEL, OrdemDia.TABLE_NAME)

    companion object {

        val chave = String.format("%s:%s", OrdemDia.APP_LABEL, OrdemDia.TABLE_NAME)
    }


    override fun syncList(list: Any?, deleted: IntArray?): Int {

        val daoOrdemDia = AppDataBase.getInstance(context).DaoOrdemDia()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoOrdemDia.loadAllByIds(deleted)
            daoOrdemDia.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val listaOrdemDia = ArrayList<OrdemDia>()
        val mapVotacao:HashMap<Int, RegistroVotacao> = HashMap()

        list.forEach {
            listaOrdemDia.add(OrdemDia.importJsonObject(it.asJsonObject))
            mapVotacao.putAll(RegistroVotacao.importJsonArray(it.asJsonObject.get("votacao").asJsonArray) as HashMap<Int, RegistroVotacao>)
        }

        try {
            daoOrdemDia.insertAll(listaOrdemDia)
        }
        catch (e: SQLiteConstraintException) {
            val daoSessao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
            val daoMateria =  AppDataBase.getInstance(context).DaoMateriaLegislativa()

            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            val jsonApiSessaoPlenaria = JsonApiSessaoPlenaria(context, retrofit)

            val listaMaterias = JsonArray()
            val listaSessoes = JsonArray()
            listaOrdemDia.forEach {
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

            daoOrdemDia.insertAll(listaOrdemDia)
        }

        if (mapVotacao.isNotEmpty()) {
            val daoRegistroVotacao = AppDataBase.getInstance(context).DaoRegistroVotacao()
            daoRegistroVotacao.insertAll(ArrayList(mapVotacao.values))
        }
        return listaOrdemDia.size
    }
}