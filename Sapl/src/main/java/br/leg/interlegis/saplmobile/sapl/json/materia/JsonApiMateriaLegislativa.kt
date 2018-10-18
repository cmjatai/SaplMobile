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


class JsonApiMateriaLegislativa: JsonApiBaseAbstract() {

    override val url = "api/mobile/materialegislativa/"

    companion object {
        val chave = String.format("%s:%s", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)
    }

    override fun sync(_context: Context, _retrofit: Retrofit?, kwargs:Map<String, Any>): Int {
        context = _context
        retrofit = _retrofit
        servico = _retrofit?.create(SaplRetrofitService::class.java)

        val listMaterias = ArrayList<MateriaLegislativa>()
        val mapAutores:HashMap<Int, Autor> = HashMap()


        var response: SaplApiRestResponse? = null
        while (response == null || response.pagination!!.next_page != null) {

            response = call(response, kwargs)

            response.results?.forEach {
                val materia = MateriaLegislativa(
                    uid = it.get("id").asInt,
                    tipo = it.get("tipo").asString,
                    tipo_sigla = it.get("tipo_sigla").asString,
                    numero = it.get("numero").asInt,
                    ano = it.get("ano").asInt,
                    numero_protocolo = it.get("numero_protocolo").asInt,
                    data_apresentacao = Converters.df.parse(it.get("data_apresentacao").asString),
                    ementa = it.get("ementa").asString,
                    texto_original = it.get("texto_original").asString,
                    file_date_updated = if (it.get("file_date_updated").isJsonNull) null else Converters.dtf.parse(it.get("file_date_updated").asString)
                )
                listMaterias.add(materia)

                it.get("autores").asJsonArray.forEach { itAutor ->
                    val i = itAutor as JsonObject
                    val autor = Autor(
                        uid = i.get("id").asInt,
                        nome = i.get("nome").asString,
                        fotografia = i.get("fotografia").asString,
                        file_date_updated = if (i.get("file_date_updated").isJsonNull) null else Converters.dtf.parse(i.get("file_date_updated").asString)
                    )
                    mapAutores[autor.uid] = autor
                }
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
                    Utils.DownloadAndWriteFiles.run(context!!, servico, it.value.fotografia, it.value.file_date_updated)
            }

            listMaterias.forEach {
                if (it.texto_original.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context!!, servico, it.texto_original, it.file_date_updated)
            }


        }


        return listMaterias.size
    }
    fun deleteFiles( apagar: ArrayList<MateriaLegislativa>) {

    }


}