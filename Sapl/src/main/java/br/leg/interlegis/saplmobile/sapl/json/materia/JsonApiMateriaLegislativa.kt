package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.daos.materia.DaoMateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import br.leg.interlegis.saplmobile.sapl.support.Utils
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
        val result = super.get(kwargs)

        val listMaterias = ArrayList<MateriaLegislativa>()
        val mapAutores:HashMap<Int, Autor> = HashMap()

        (result["list"] as ArrayList<JsonObject>).forEach {
                listMaterias.add(MateriaLegislativa.importJsonObject(it) as MateriaLegislativa)
                mapAutores.putAll(Autor.importJsonArray(it.get("autores").asJsonArray) as HashMap<Int, Autor>)
        }

        val db = AppDataBase.getInstance(context!!)

        val daoMateria = db.DaoMateriaLegislativa()
        val apagar = daoMateria.loadAllByIds(result["deleted"] as IntArray)

        daoMateria.insertAll(listMaterias)
        daoMateria.delete(apagar)

        val daoAutor = db.DaoAutor()
        daoAutor.insertAll(ArrayList<Autor>(mapAutores.values))

        doAsync {
            mapAutores.forEach {
                if (it.value.fotografia.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context, servico, it.value.fotografia, it.value.file_date_updated)
            }

            listMaterias.forEach {
                if (it.texto_original.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context, servico, it.texto_original, it.file_date_updated)
            }
        }


        return listMaterias.size
    }
    fun deleteFiles( apagar: ArrayList<MateriaLegislativa>) {

    }


}