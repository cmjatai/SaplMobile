package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.MateriaLegislativaRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.interfaces.DownloadService
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonObject
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import java.io.*


class JsonApiMateriaLegislativa: JsonApiBaseAbstract() {

    var retrofit: Retrofit? = null

    companion object {
        val chave = String.format("%s:%s", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)
    }

    override fun sync(_context: Context, _retrofit: Retrofit?, kwargs:Map<String, Any>): Int {
        context = _context
        retrofit = _retrofit

        var servico: MateriaLegislativaRetrofitService? = _retrofit?.create(MateriaLegislativaRetrofitService::class.java)
        var response: SaplApiRestResponse? = null

        val listMaterias = ArrayList<MateriaLegislativa>()
        val mapAutores:HashMap<Int, Autor> = HashMap()


        while (response == null || response.pagination!!.next_page != null) {
            var dmin = if (kwargs["data_inicio"] is Date) Converters.dtf.format(kwargs["data_inicio"] as Date) else null
            var dmax = if (kwargs["data_fim"] is Date) Converters.dtf.format(kwargs["data_fim"] as Date) else null

            var tipo_update = "sync"
            if (kwargs.get("tipo_update") is String) {
                tipo_update = kwargs.get("tipo_update").toString()
            }

            val call = servico?.list(
                    format = "json",
                    page = if (response == null) 1 else response.pagination!!.next_page!!,
                    tipo_update = tipo_update,
                        // Tipo sync = filtro com base nas datas de alteração
                        // Tipo get = filtro com base nas datas da sessão plenária
                        // Tipo last_items = uma pagina só com os ultimos dados da listagem
                        // Tipo first_items = uma página só com os primeiros dados da listagem
                        // Tipo get_initial = uma página com os últimos dados do servidor
                    data_min = dmin,
                    data_max = dmax
            )

            response = call?.execute()!!.body()!!

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
                    Utils.DownloadAndWriteFiles.run(context!!, retrofit, it.value.fotografia, it.value.file_date_updated)
            }

            listMaterias.forEach {
                if (it.texto_original.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context!!, retrofit, it.texto_original, it.file_date_updated)
            }


        }


        return listMaterias.size
    }
    fun deleteFiles( apagar: ArrayList<MateriaLegislativa>) {

    }


}