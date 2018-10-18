package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
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

    override val url = "api/mobile/materialegislativa/"

    companion object {
        val chave = String.format("%s:%s", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)
    }

    override fun sync(kwargs:Map<String, Any>): Int {
        servico = retrofit.create(SaplRetrofitService::class.java)

        val listMaterias = ArrayList<MateriaLegislativa>()
        val mapAutores:HashMap<Int, Autor> = HashMap()

        var response: SaplApiRestResponse? = null
        while (response == null || response.pagination!!.next_page != null) {

            response = call(response, kwargs)
            response.results?.forEach {
                listMaterias.add(MateriaLegislativa.parse(it))
                mapAutores.putAll(Autor.parseList(it.get("autores").asJsonArray))
            }
        }

        val db = AppDataBase.getInstance(context!!)

        val daoMateria = db.DaoMateriaLegislativa()
        val apagar = daoMateria.loadAllByIds(response.deleted!!)

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