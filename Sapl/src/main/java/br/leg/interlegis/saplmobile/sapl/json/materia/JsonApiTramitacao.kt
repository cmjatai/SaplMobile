package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Tramitacao
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiTramitacao(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", Tramitacao.APP_LABEL, Tramitacao.TABLE_NAME)

    companion object {
        val chave = String.format("%s:%s", Tramitacao.APP_LABEL, Tramitacao.TABLE_NAME)
    }


    override fun syncList(list:Any?, deleted: IntArray?): Int {

        val mapTramitacao = Tramitacao.importJsonArray(list as JsonArray) as Map<Int, Tramitacao>

        val daoTramitacao = AppDataBase.getInstance(context).DaoTramitacao()
        val apagar = daoTramitacao.loadAllByIds(deleted as IntArray)

        daoTramitacao.delete(apagar)

        try {
            daoTramitacao.insertAll(ArrayList<Tramitacao>(mapTramitacao.values))
        }
        catch (e: SQLiteConstraintException) {
            val lista = JsonArray()
            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            mapTramitacao.values.forEach {
                try {
                    daoTramitacao.insert(it)
                }
                catch (e: SQLiteConstraintException) {
                    lista.add(jsonApiMateriaLegislativa.getObject(it.materia))
                }
            }
            jsonApiMateriaLegislativa.syncList(lista)
            daoTramitacao.insertAll(ArrayList<Tramitacao>(mapTramitacao.values))
        }
        return mapTramitacao.size
    }
}