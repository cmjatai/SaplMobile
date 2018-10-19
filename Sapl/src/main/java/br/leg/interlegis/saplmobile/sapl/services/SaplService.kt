package br.leg.interlegis.saplmobile.sapl.services

import android.app.Service
import android.content.Intent
import android.os.*
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.support.Log
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.*


class SaplService : Service() {

    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null

    private var interval_update : Long = 10000

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
            syncModules = JsonApi(this@SaplService).sync_time_refresh()
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
                Log.d("SAPL", "Sincronizado!!!")
                this@SaplService.running = false // So tornar false quando ultima thread internas daqui terminarem
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
    }
}
