package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiAutoria(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", Autoria.APP_LABEL, Autoria.TABLE_NAME)

    companion object {
        val chave = "materia:autoria"
    }


    override fun sync(kwargs:Map<String, Any>): Int {
        val result = super.getList(kwargs)

        val mapAutores = Autor.importJsonArray(result["list"] as JsonArray, foreignKey = "autor") as HashMap<Int, Autor>

        val mapAutoria = Autoria.importJsonArray(result["list"] as JsonArray) as Map<Int, Autoria>

        val daoAutor = AppDataBase.getInstance(context).DaoAutor()
        val daoAutoria = AppDataBase.getInstance(context).DaoAutoria()
        val apagar = daoAutoria.loadAllByIds(result["deleted"] as IntArray)

        daoAutor.insertAll(ArrayList<Autor>(mapAutores.values))
        daoAutoria.delete(apagar)

        doAsync {
            mapAutores.forEach {entry ->
                if (entry.value.fotografia.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context, servico, entry.value.fotografia, entry.value.file_date_updated)
            }
        }
        try {
            daoAutoria.insertAll(ArrayList<Autoria>(mapAutoria.values))
        }
        catch (e: SQLiteConstraintException) {

            val lista = JsonArray()

            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            mapAutoria.forEach {
                lista.add(jsonApiMateriaLegislativa.getObject(it.value.materia))
            }
            jsonApiMateriaLegislativa.syncList(lista)


        }
        return mapAutoria.size
    }
}