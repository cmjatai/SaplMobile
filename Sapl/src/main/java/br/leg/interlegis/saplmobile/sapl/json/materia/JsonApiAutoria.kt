package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiAutoria(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", Autoria.APP_LABEL, Autoria.TABLE_NAME)

    companion object {
        val chave = "materia:autoria"
    }


    override fun syncList(list:Any?, deleted: IntArray?): Int {

        val mapAutores = Autor.importJsonArray(list as JsonArray, foreignKey = "autor") as HashMap<Int, Autor>

        val mapAutoria = Autoria.importJsonArray(list) as Map<Int, Autoria>

        val daoAutor = AppDataBase.getInstance(context).DaoAutor()
        val daoAutoria = AppDataBase.getInstance(context).DaoAutoria()
        val apagar = daoAutoria.loadAllByIds(deleted as IntArray)

        daoAutor.insertAll(ArrayList<Autor>(mapAutores.values))
        daoAutoria.delete(apagar)

        doAsync {
            mapAutores.forEach {entry ->
                if (entry.value.fotografia.isNotEmpty())
                    Utils.ManageFiles.download(context, servico, entry.value.fotografia, entry.value.file_date_updated)
            }
        }


        try {
            daoAutoria.insertAll(ArrayList<Autoria>(mapAutoria.values))
        }
        catch (e: SQLiteConstraintException) {
            val lista = JsonArray()
            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            mapAutoria.values.forEach {
                try {
                    daoAutoria.insert(it)
                }
                catch (e: SQLiteConstraintException) {
                    lista.add(jsonApiMateriaLegislativa.getObject(it.materia))
                }
            }
            jsonApiMateriaLegislativa.syncList(lista)
            daoAutoria.insertAll(ArrayList<Autoria>(mapAutoria.values))
        }
        return mapAutoria.size
    }
}