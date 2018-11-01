package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiAnexada(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", Anexada.APP_LABEL, Anexada.TABLE_NAME)

    companion object {
        val chave = "materia:anexada"
    }


    override fun syncList(list: Any?, deleted: IntArray?): Int {

        val listaMaterias = JsonArray()

        val mapAnexada = Anexada.importJsonArray(list as JsonArray) as Map<Int, Anexada>

        val daoAnexada = AppDataBase.getInstance(context).DaoAnexada()
        val apagar = daoAnexada.loadAllByIds(deleted as IntArray)

        daoAnexada.delete(apagar)

        try {
            daoAnexada.insertAll(ArrayList<Anexada>(mapAnexada.values))
        }
        catch (e: SQLiteConstraintException) {

            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            mapAnexada.values.forEach {

                try {
                    daoAnexada.insert(it)
                }
                catch (e: SQLiteConstraintException) {
                    listaMaterias.add(jsonApiMateriaLegislativa.getObject(it.materia_principal))
                }
            }
            jsonApiMateriaLegislativa.syncList(listaMaterias)
        }
        return mapAnexada.size
    }
}