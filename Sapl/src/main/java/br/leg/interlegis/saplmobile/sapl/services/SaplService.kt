package br.leg.interlegis.saplmobile.sapl.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.json.JsonApiTimeRefresh
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.support.Utils
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.reflect.full.declaredMemberProperties


class SaplService : Service() {

    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null

    private var interval_update : Long = 10000

    lateinit var jsonApiTimeRefresh: JsonApiTimeRefresh

    lateinit var managerDownloadFiles: ManagerDownloadFiles

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        instance = this

        val thread = HandlerThread("SaplThreadService", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        mServiceLooper = thread.getLooper()
        mServiceHandler = ServiceHandler(mServiceLooper!!)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        toast("Serviço iniciado!")

        jsonApiTimeRefresh = JsonApiTimeRefresh(this)
        managerDownloadFiles = ManagerDownloadFiles(this)

        val msg = mServiceHandler!!.obtainMessage()
        msg.arg1 = startId
        mServiceHandler!!.sendMessage(msg)
        return START_STICKY
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            try {
                mServiceHandler!!.post(object : Runnable {
                    override fun run() {
                        if (SaplApplication.isAnyActivityVisible()) {
                            if (!this@SaplService.running) {
                                Log.d("SAPL", "Timer: Iniciou serviço!")
                                this@SaplService.execute()
                            }
                            else {
                                Log.d("SAPL", "Timer: Serviço já em execução!")
                            }
                            delayed = true
                            mServiceHandler!!.postDelayed(this, this@SaplService.interval_update)
                        }
                        else {
                            delayed = false
                            Log.d("SAPL", "Minimizado, saindo e esperando sendMessage da interface sobre onResume!")
                        }
                    }
                })

            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }
        }
    }


    private fun execute() {
        this@SaplService.running = true
        var syncModules:  ArrayList<Pair<String, HashMap<String, Any>>>?
        try {
            syncModules = jsonApiTimeRefresh.sync_time_refresh()
            if (syncModules.isEmpty()) {
                //Log.d("SAPL", "SaplMobile está sincronizado com Servidor!")
                this@SaplService.running = false


                return
            }
        }
        catch (e:  Exception) {
            Log.d("SAPL", "Erro de Comunicação!")
            this@SaplService.running = false
            return
        }

        doAsync {
            try {
                Log.d("SAPL", "Sincronizando SaplMobile!")
                val json = JsonApi(this@SaplService)
                json.sync(syncModules)
                managerDownloadFiles.run()
                Log.d("SAPL", "Sincronizado!!!")
                this@SaplService.running = false
            }
            catch (e: Exception) {
                this@SaplService.running = false
            }
        }
    }

    @Volatile private var running: Boolean = false

    @Volatile private var delayed: Boolean = false

    companion object {

        var instance: SaplService? = null
            private set

        fun isRunning(): Boolean {
            return instance != null && !instance!!.running && !instance!!.delayed
        }
        fun isInstanceCreated(): Boolean {
            return instance != null
        }
        fun sendMessage(message: Message) {
            instance!!.mServiceHandler!!.sendMessage(message)
        }

        fun downloadFileLazy(relativeUrl: String, data:Date?): Boolean {
            if (instance == null)
                return false
            instance!!.managerDownloadFiles[relativeUrl] = relativeUrl to data
            return true
        }
    }

    class ManagerDownloadFiles(context: Context): ConcurrentSkipListMap<String, Pair<String, Date?>>() {
        var context: Context = context
        var retrofit: Retrofit? = null
        var servico: SaplRetrofitService? = null

        var running = false

        init {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                        .baseUrl(SettingsActivity.getStringPreference(context, "domain_casa_legislativa"))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            servico = this.retrofit!!.create(SaplRetrofitService::class.java)

            run()
        }

        fun run() {

            if (running)
                return

            doAsync {
                synchronized(running) {
                    running = true

                    while (this@ManagerDownloadFiles.isNotEmpty()) {
                        var entry = pollFirstEntry()
                        _run(entry.key, entry.value.second)
                    }
                    running = false
                }
            }
        }

        private fun _run(relativeUrl: String, data: Date?) {
            val fileDir = context?.filesDir
            val pathname: String = Utils.pathname(fileDir!!.absolutePath, relativeUrl)
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
                } else {
                    //Log.d("SAPL", "Precisa baixar")
                }
            }

            try {
                val call = servico?.downloadFile(relativeUrl)
                val response: ResponseBody? = call?.execute()!!.body()

                if (response != null) {
                    writeResponseBodyToDisk(response!!, pathname)
                    Log.d("SAPL", "Baixou... " + relativeUrl)
                }
            } catch (e: Exception) {
                Log.d("SAPL", e.stackTrace.contentToString())
            }
        }

        private fun writeResponseBodyToDisk(body: ResponseBody, pathname:String): Boolean {
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

        companion object {

            fun deleteFile(_context: Context, entities: List<SaplEntity>, attrs: ArrayList<String>) {

                val fileDir = _context?.filesDir

                entities.forEach {

                    val clazz = it.javaClass.kotlin
                    clazz.declaredMemberProperties.forEach { itAttr ->

                        attrs.forEach { attr ->
                            if (itAttr.name.equals(attr)) {
                                val pathname = Utils.pathname(fileDir!!.absolutePath, itAttr.get(it).toString())
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
        }
    }
}
