package br.leg.interlegis.saplmobile.sapl.support

import android.content.Context
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import br.leg.interlegis.saplmobile.sapl.json.interfaces.DownloadService
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class Utils {

    companion object {
        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(state)
        }

        fun pathname(pathDir: String, relative_pathfile: String) =
                String.format("%s/%s", pathDir, relative_pathfile).replace("//", "/")
    }


    class DownloadAndWriteFiles {

        companion object {

            fun run(context: Context?, retrofit: Retrofit?, relativeUrl: String, data:Date?, async: Boolean = false) {

                if (!async)
                    _run(context, retrofit, relativeUrl, data)
                else
                    doAsync {
                        _run(context, retrofit, relativeUrl, data)
                    }
            }

            private fun _run(context: Context?,retrofit: Retrofit?, relativeUrl: String, data:Date?) {
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
                            Log.d("SAPL", "Não precisa baixar")
                            return
                        }
                    }
                    else {
                        Log.d("SAPL", "Precisa baixar")
                    }
                }

                try {
                    val servico: DownloadService = retrofit!!.create(DownloadService::class.java)
                    val call = servico.downloadFile(relativeUrl)
                    val response: ResponseBody? = call.execute().body()

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
    }
}
