package br.leg.interlegis.saplmobile.sapl.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import br.leg.interlegis.saplmobile.sapl.R
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.services.SaplService

class SessaoPlenariaActivity : SaplBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessao_plenaria)
    }

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
