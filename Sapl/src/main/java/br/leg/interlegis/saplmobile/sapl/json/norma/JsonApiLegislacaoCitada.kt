package br.leg.interlegis.saplmobile.sapl.json.norma

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.DocumentoAcessorio
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.LegislacaoCitada
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiMateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiLegislacaoCitada(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", LegislacaoCitada.APP_LABEL, LegislacaoCitada.TABLE_NAME)

    companion object {
        val chave = String.format("%s:%s", LegislacaoCitada.APP_LABEL, LegislacaoCitada.TABLE_NAME)
    }


    override fun syncList(list:Any?, deleted: IntArray?): Int {

        val daoLC = AppDataBase.getInstance(context).DaoLegislacaoCitada()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoLC.loadAllByIds(deleted)
            daoLC.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val listaMaterias = JsonArray()
        val listaNormas = JsonArray()

        val mapLC = LegislacaoCitada.importJsonArray(list) as Map<Int, LegislacaoCitada>

        try {
            daoLC.insertAll(ArrayList<LegislacaoCitada>(mapLC.values))
        }
        catch (e: SQLiteConstraintException) {

            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            val jsonApiNormaJuridica = JsonApiNormaJuridica(context, retrofit)

            mapLC.values.forEach {
                try {
                    daoLC.insert(it)
                }
                catch (e: SQLiteConstraintException) {
                    listaMaterias.add(jsonApiMateriaLegislativa.getObject(it.materia))
                    listaNormas.add(jsonApiNormaJuridica.getObject(it.norma))
                }
            }
            jsonApiMateriaLegislativa.syncList(listaMaterias)
            jsonApiNormaJuridica.syncList(listaMaterias)
            daoLC.insertAll(ArrayList<LegislacaoCitada>(mapLC.values))
        }

        return mapLC.size
    }
}