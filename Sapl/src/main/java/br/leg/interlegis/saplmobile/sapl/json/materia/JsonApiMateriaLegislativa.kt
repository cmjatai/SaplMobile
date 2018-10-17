package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.MateriaLegislativaRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SessaoPlenariaRetrofitService
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

        val dao = AppDataBase.getInstance(context!!).DaoMateriaLegislativa()
        val apagar = dao.loadAllByIds(response.deleted!!)
        dao.insertAll(listMaterias)
        dao.delete(apagar)

        mapAutores.forEach {
            if (it.value.fotografia.isNotEmpty())
                checkDownloadFiles(retrofit, it.value.fotografia)
        }


        return listMaterias.size
    }
    fun deleteFiles( apagar: ArrayList<MateriaLegislativa>) {

    }
    fun checkDownloadFiles(retrofit: Retrofit?, relativeUrl: String) {

        if (!Utils.isExternalStorageWritable()) {
            return
        }

        val fileDir = context?.filesDir
        val pathname: String =String.format("%s/%s", fileDir?.absolutePath, relativeUrl).replace("//","/")

        val file = File(pathname)

        if (!file.exists()) {
            file.parentFile.mkdirs()
            Log.d("SAPL", pathname)
        }



        var servico: SaplRetrofitService = retrofit!!.create(SaplRetrofitService::class.java)

        val call = servico.downloadFile(relativeUrl)
            // checar se o arquivo existe
            // se arquivo existe - checar data
            // se arquivo não existe - fazer download

        val response: ResponseBody? = call.execute().body()

        writeResponseBodyToDisk(response, pathname)

    }

    private fun writeResponseBodyToDisk(body: ResponseBody?, pathname:String): Boolean {
        try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body!!.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(pathname)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream!!.write(fileReader, 0, read)

                    fileSizeDownloaded += read.toLong()

                    Log.d("SAPL", "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream!!.flush()

                return true
            } catch (e: IOException) {
                return false
            } finally {
                if (inputStream != null) {
                    inputStream!!.close()
                }

                if (outputStream != null) {
                    outputStream!!.close()
                }
            }
        } catch (e: IOException) {
            return false
        }

    }
}