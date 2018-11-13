package br.leg.interlegis.saplmobile.sapl.activities

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import br.leg.interlegis.saplmobile.sapl.R
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.daos.od.DaoExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.daos.sessao.DaoSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.ExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.OrdemDia
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.json.sessao.JsonApiExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.json.sessao.JsonApiOrdemDia
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.views.SessaoPlenariaViewModel
import kotlinx.android.synthetic.main.activity_sessao_plenaria.*
import org.jetbrains.anko.doAsync
import java.io.File

class SessaoPlenariaActivity : SaplBaseActivity() {

    val db = AppDataBase.getInstance(context = this)

    val grid = ArrayList<SaplEntity>()

    var rootView: View? = null

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessao_plenaria)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val uid = intent.getIntExtra("uid", 0)

        var sessaoModel = ViewModelProviders.of(
                this).get(SessaoPlenariaViewModel::class.java)

        sessaoModel.uid_sessao = uid

        doAsync {

            sessaoModel.materias_do_expediente!!.observe(this@SessaoPlenariaActivity,
                    Observer<List<ExpedienteMateria>> {
                        if (it != null)
                            Log.d("SAPL", it.size.toString())
                    }
            )
            sessaoModel.materias_da_ordemdia!!.observe(this@SessaoPlenariaActivity,
                    Observer<List<OrdemDia>> {
                        if (it != null)
                            Log.d("SAPL", it.size.toString())
                    }
            )
            sessaoModel.sessao!!.observe(this@SessaoPlenariaActivity,
                    Observer<SessaoPlenaria> {
                        if (it != null)
                            update(it)
                    }
            )

            var jsonApiExpedienteMateria = JsonApiExpedienteMateria(this@SessaoPlenariaActivity, null)
            jsonApiExpedienteMateria.allBySessao(sessaoModel.uid_sessao)


            var jsonApiOrdemDia= JsonApiOrdemDia(this@SessaoPlenariaActivity, null)
            jsonApiOrdemDia.allBySessao(sessaoModel.uid_sessao)



        }


        /*doAsync {
            var autor = AppDataBase.getInstance(this@SessaoPlenariaActivity).DaoAutor().getAutor(2)
            val pathname: String =String.format("%s/%s", filesDir?.absolutePath, autor.fotografia).replace("//","/")
            var f = File(pathname)
            if (f.exists()) {
                var bitmap = BitmapFactory.decodeFile(f.absolutePath)
                image_test.setImageBitmap(bitmap)
                image_test.contentDescription = autor.nome
            }
        }*/

        /*viewManager = LinearLayoutManager(this)
        viewAdapter = PartesSessaoAdapter()

        container.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }*/
    }

    fun update(sessao: SessaoPlenaria) {
        val titulos = SessaoPlenariaListActivity.titulo_sessao(this, sessao)
        toolbar.title = String.format("%s (%s)",
                titulos["session_title"],
                titulos["session_date_extended"])
    }


    class PartesSessaoAdapter(private val grid_adapter: ArrayList<ArrayList<SaplEntity>>) :
            RecyclerView.Adapter<PartesSessaoAdapter.SaplEntityHolder>() {

        inner class SaplEntityHolder(val _view: View) : RecyclerView.ViewHolder(_view) {
            var item: SaplEntity? = null
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SaplEntityHolder {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getItemCount(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onBindViewHolder(p0: SaplEntityHolder, p1: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
