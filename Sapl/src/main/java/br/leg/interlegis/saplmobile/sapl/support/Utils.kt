package br.leg.interlegis.saplmobile.sapl.support

import android.content.Context
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.declaredMemberProperties


class Utils {

    companion object {
        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(state)
        }

        fun pathname(pathDir: String, relative_pathfile: String) =
                String.format("%s/%s", pathDir, relative_pathfile).replace("//", "/")
    }


    class ManageFiles {

        companion object {

            fun deleteFile(context: Context, entities: List<SaplEntity>, attrs: ArrayList<String>) {
                val fileDir = context?.filesDir

                entities.forEach {

                    val clazz = it.javaClass.kotlin
                    clazz.declaredMemberProperties.forEach { itAttr ->

                        attrs.forEach {attr ->
                            if (itAttr.name.equals(attr)) {
                                val pathname =  Utils.pathname(fileDir!!.absolutePath, itAttr.get(it).toString())
                                val fileBase = File(pathname)

                                fun delete(file: File) {
                                    val fileDir = file.parentFile
                                    file.delete()

                                    val lista = fileDir.listFiles()

                                    if (lista.isEmpty())
                                        delete(fileDir)
                                }

                                if (fileBase.exists()) {
                                    delete(fileBase)
                                }

                            }

                        }
                    }


                }

            }

            fun download(context: Context?, servico: SaplRetrofitService?, relativeUrl: String, data:Date?, async: Boolean = false) {

                if (!async)
                    _run(context, servico, relativeUrl, data)
                else
                    doAsync {
                        _run(context, servico, relativeUrl, data)
                    }
            }

            private fun _run(context: Context?, servico: SaplRetrofitService?, relativeUrl: String, data:Date?) {
                val fileDir = context?.filesDir
                val pathname: String = pathname(fileDir!!.absolutePath, relativeUrl)
                val file = File(pathname)

                if (!file.exists())
                    file.parentFile.mkdirs()
                else {

                    if (data != null) {
                        val tzLocal = TimeZone.getDefault()
                        var cal = Calendar.getInstance()
                        cal.timeInMillis = file.lastModified() - tzLocal.rawOffset
                        if (data.before(cal.time)) {
                            //Log.d("SAPL", "Não precisa baixar")
                            return
                        }
                    }
                    else {
                        //Log.d("SAPL", "Precisa baixar")
                    }
                }

                try {
                    val call = servico?.downloadFile(relativeUrl)
                    val response: ResponseBody? = call?.execute()!!.body()

                    writeResponseBodyToDisk(response, pathname)

                    Log.d("SAPL", "Baixou...")
                }
                catch (e: Exception) {
                    Log.d("SAPL", e.stackTrace.contentToString())
                }
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

                            //Log.d("SAPL", "file download: $fileSizeDownloaded of $fileSize")
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
    }
}
