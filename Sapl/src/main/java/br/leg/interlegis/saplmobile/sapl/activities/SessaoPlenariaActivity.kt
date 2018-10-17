package br.leg.interlegis.saplmobile.sapl.activities

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import br.leg.interlegis.saplmobile.sapl.R
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.db.daos.sessao.DaoSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.support.Log
import kotlinx.android.synthetic.main.activity_sessao_plenaria.*

class SessaoPlenariaActivity : SaplBaseActivity() {
    var sessaoLiveData: LiveData<SessaoPlenaria>? = null
    var sessaoPlenaria: SessaoPlenaria? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessao_plenaria)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val uid = intent.getIntExtra("uid", 0)

        sessaoLiveData = SaplApplication.daoSessaoPlenaria!!.getLDSessao(uid)

        sessaoLiveData!!.observe( this,
            Observer<SessaoPlenaria> {
                update(it)
            })
    }
    fun update(sessao: SessaoPlenaria?) {
        sessaoPlenaria = sessao
        val titulos = SessaoPlenariaListActivity.titulo_sessao(this, sessao!!)
        toolbar.title = String.format("%s    (%s)",
                titulos["session_title"],
                titulos["session_date_extended"])

    }
}
