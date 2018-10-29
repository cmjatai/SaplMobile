package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Retrofit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.jetbrains.anko.doAsync


class JsonApiMateriaLegislativa(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)


    companion object {
        val chave = String.format("%s:%s", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)
    }

    override fun sync(kwargs:Map<String, Any>): Int {
        val result = super.getList(kwargs)
        return syncList(result["list"], result["deleted"] as IntArray)
    }

    fun syncList(list:Any?, deleted: IntArray? = null): Int {

        val mapMaterias:HashMap<Int, MateriaLegislativa> = HashMap()
        val mapAnexada:HashMap<Int, Anexada> = HashMap()

        val mapAutores:HashMap<Int, Autor> = HashMap()
        val mapAutoria:HashMap<Int, Autoria> = HashMap()

        fun syncMateria(obj: JsonObject) {
            val mat = MateriaLegislativa.importJsonObject(obj)
            mapMaterias[mat.uid] = mat


            mapAutores.putAll(Autor.importJsonArray(obj.get("autoria").asJsonArray, foreignKey = "autor") as HashMap<Int, Autor>)
            mapAutoria.putAll(Autoria.importJsonArray(obj.get("autoria").asJsonArray) as HashMap<Int, Autoria>)
        }

        (list as JsonArray).forEach array@{ itMat ->

            syncMateria(itMat.asJsonObject)

            arrayOf("anexadas" to "materia_anexada", "anexo_de" to "materia_principal").forEach {
                if (itMat.asJsonObject.has(it.first)) {

                    mapAnexada.putAll(Anexada.importDeJsonArray(itMat.asJsonObject.get(it.first).asJsonArray) as HashMap<Int, Anexada>)
                    itMat.asJsonObject.getAsJsonArray(it.first).forEach { itAnexadas ->
                        syncMateria(itAnexadas.asJsonObject.getAsJsonObject(it.second))
                    }
                }
            }
        }

        val db = AppDataBase.getInstance(context)

        val daoMateria = db.DaoMateriaLegislativa()
        val daoAnexada = db.DaoAnexada()
        val daoAutor = db.DaoAutor()
        val daoAutoria = db.DaoAutoria()


        daoMateria.insertAll(ArrayList<MateriaLegislativa>(mapMaterias.values))
        daoAnexada.insertAll(ArrayList<Anexada>(mapAnexada.values))

        daoAutor.insertAll(ArrayList<Autor>(mapAutores.values))
        daoAutoria.insertAll(ArrayList<Autoria>(mapAutoria.values))

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoMateria.loadAllByIds(deleted)
            Utils.ManageFiles.deleteFile(context, apagar, arrayListOf("texto_original"))
            daoMateria.delete( apagar )
        }

        doAsync {
            mapAutores.forEach {
                if (it.value.fotografia.isNotEmpty())
                    Utils.ManageFiles.download(context, servico, it.value.fotografia, it.value.file_date_updated)
            }

            mapMaterias.forEach {
                if (it.value.texto_original.isNotEmpty())
                    Utils.ManageFiles.download(context, servico, it.value.texto_original, it.value.file_date_updated)
            }
        }

        return mapMaterias.size
    }
}