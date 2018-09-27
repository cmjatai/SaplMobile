package br.leg.interlegis.saplmobile.sapl.services

import android.app.ActivityManager
import android.app.Service
import android.arch.lifecycle.LiveData
import android.content.Intent
import android.os.*
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.ChaveValor
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.support.Log
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

import br.leg.interlegis.saplmobile.R
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoChaveValor



class SaplService : Service() {

    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null

    private var interval_update : Long = 7000

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        toast("Serviço iniciado!")
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
                        if (SaplApplication.isActivityVisible()) {
                            if (!this@SaplService.running) {
                                Log.d("SAPL", "Timer: Iniciou serviço!")
                                this@SaplService.execute()
                            }
                            else {
                                Log.d("SAPL", "Timer: Serviço já em execução!")
                            }
                            mServiceHandler!!.postDelayed(this, this@SaplService.interval_update)
                        }
                        else {
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

    private fun isUpdated(): Boolean {
        try {
            val json = JsonApi(this@SaplService)
            val lgrt = json.get_last_global_refresh_time()
            var cv = DaoChaveValor?.get_or_create(this@SaplService,"last_global_refresh_time", lgrt)

            if (cv.valor == lgrt) {
                return true
            }
            else {
                cv.valor = lgrt
                val list = ArrayList<ChaveValor>()
                list.add(cv)
                AppDataBase.getInstance(this@SaplService).DaoChaveValor().insertAll(list)
                return false
            }
        }
        catch (e: Exception) {
            toast("Erro de Comunicação com o Servidor na Internet")
            return true
        }
    }

    private fun execute() {
        this@SaplService.running = true
        /*if (isUpdated()) {
            Log.d("SAPL", "SaplMobile está sincronizado com Servidor!")
            this@SaplService.running = false
            return
        }
        Log.d("SAPL", "Servidor foi atualizado... sincronizando SaplMobile!")*/
        doAsync {

            Log.d("SAPL", "Sincronizando SaplMobile!")
            val json = JsonApi(this@SaplService)
            json.sync()
            Log.d("SAPL", "Sincronizado!!!")

            Thread.sleep(5000)
            this@SaplService.running = false // So tornar false quando ultima thread internas daqui terminarem
        }
    }

    @Volatile private var running: Boolean = false

    companion object {
        var instance: SaplService? = null
            private set
        fun isRunning(): Boolean {
            return instance != null && !instance!!.running
        }
        fun sendMessage(message: Message) {
            instance!!.mServiceHandler!!.sendMessage(message)
        }
    }
}
