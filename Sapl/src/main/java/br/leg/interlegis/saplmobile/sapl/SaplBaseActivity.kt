package br.leg.interlegis.saplmobile.sapl

import android.os.Message
import android.support.v7.app.AppCompatActivity
import br.leg.interlegis.saplmobile.sapl.services.SaplService

abstract class SaplBaseActivity: AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        SaplApplication.activityResumed(this)

        if (SaplService.isRunning()) {
            val message = Message()
            message.arg1 = 1
            message.arg2 = 2
            SaplService.sendMessage(message)
        }
    }

    override fun onPause() {
        super.onPause()
        SaplApplication.activityPaused(this)
    }

}