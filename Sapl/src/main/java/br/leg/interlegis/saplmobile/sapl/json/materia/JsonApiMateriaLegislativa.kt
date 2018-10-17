package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import android.os.Environment
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.AutorRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.support.Utils
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import okhttp3.ResponseBody



class JsonApiMateriaLegislativa: JsonApiInterface {

    var servico: AutorRetrofitService? = null
    var context: Context? = null

    companion object {
        val chave = "materia:materialegislativa"
    }

    override fun sync(_context: Context, retrofit: Retrofit?, kwargs:Map<String, Any>): Int {

        servico = retrofit?.create(AutorRetrofitService::class.java)
        context = _context
        var response: SaplApiRestResponse? = null

        val listAutor = ArrayList<Autor>()


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

            for (item in response.results!!) {
                val autor = Autor(
                        uid = item.get("id").asInt,
                        nome = item.get("nome").asString,
                        fotografia = item.get("fotografia").asString,
                        file_date_updated = if (item.get("file_date_updated").isJsonNull) null else Converters.df.parse(item.get("file_date_updated").asString)
                )
                listAutor.add(autor)
                Log.d("SAPL", autor.nome)
            }
        }

        val dao = AppDataBase.getInstance(context!!).DaoAutor()
        val apagar = dao.loadAllByIds(response.deleted!!)
        dao.insertAll(listAutor)
        dao.delete(apagar)

        checkDownloadFiles(retrofit, listAutor)


        return listAutor.size
    }
    fun deleteFiles( apagar: ArrayList<Autor>) {

    }
    fun checkDownloadFiles(retrofit: Retrofit?, listAutor: ArrayList<Autor>) {

        if (!Utils.isExternalStorageWritable()) {
            return
        }

        val fileDir = context?.filesDir


        listAutor.forEach itAutor@ {
            if (it.fotografia.equals(""))
                return@itAutor

            var pathname: String =String.format("%s/%s", fileDir?.absolutePath, it.fotografia)
            val file = File(pathname)
            if (!file.exists())
                Log.d("SAPL", pathname)



            // checar se o arquivo existe
            // se arquivo existe - checar data
            // se arquivo não existe - fazer download


            /*val call = downloadService.downloadFileWithDynamicUrlSync(fileUrl)

            call.enqueue(object : Callback<ResponseBody>() {
                fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccess()) {
                        Log.d(TAG, "server contacted and has file")

                        val writtenToDisk = writeResponseBodyToDisk(response.body())

                        Log.d(TAG, "file download was a success? $writtenToDisk")
                    } else {
                        Log.d(TAG, "server contact failed")
                    }
                }

                fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "error")
                }
            })*/
        }

    }
/*
    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        try {
            // todo change the file location/name according to your needs
            val futureStudioIconFile = File(getExternalFilesDir(null) + File.separator + "Future Studio Icon.png")

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream!!.write(fileReader, 0, read)

                    fileSizeDownloaded += read.toLong()

                    Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
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

    }*/
}