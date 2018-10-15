package br.leg.interlegis.saplmobile.sapl.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AutoCompleteTextView
import android.widget.TextView

import br.leg.interlegis.saplmobile.sapl.R
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.json.JsonApiSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.views.SessaoPlenariaListViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_sessao_plenaria_list.*
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.*
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.view.*
import kotlinx.android.synthetic.main.item_sessao_plenaria.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.dimen
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.onPageChangeListener
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SessaoPlenariaListActivity : SaplBaseActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var screen_list_sessao_plenaria: String = "10"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessao_plenaria_list)

        screen_list_sessao_plenaria = SettingsActivity.getStringPreference(this, "screen_list_sessao_plenaria")



        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        container.setPageTransformer(true, ZoomOutPageTransformer())

        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixel: Int) {}
            override fun onPageSelected(p0: Int) {

                var item = this@SessaoPlenariaListActivity
                var sections = item.mSectionsPagerAdapter
                if (sections!!.count - p0 == 1) {
                    doAsync {
                        val dataFim = sections.sessoes!![sections.sessoes!!.size-1].data_inicio
                        val json = JsonApi(this@SessaoPlenariaListActivity)
                        json.get_sessao_sessao_plenaria(dataFim = dataFim)
                    }
                }
            }
        })

        var sessaoModel = ViewModelProviders.of(
                this).get(SessaoPlenariaListViewModel::class.java)

        sessaoModel.sessoes?.observe(this,
            Observer<List<SessaoPlenaria>> {sessoes ->
                if (sessoes != null) {
                    mSectionsPagerAdapter?.sessoes = sessoes
                    mSectionsPagerAdapter?.notifyDataSetChanged()
                }
            })
    }
    class ZoomOutPageTransformer : ViewPager.PageTransformer {

        private val MIN_SCALE = 0.5f
        private val MIN_ALPHA = 0.5f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
        var grid: ArrayList<ArrayList<SessaoPlenaria>> = ArrayList()
        var sessoes: List<SessaoPlenaria>? = null
        set(value) {
            grid.clear()
            value?.forEach value@{ sessao ->
                var flagInsert = false
                grid.forEach grid@{

                    if (this@SessaoPlenariaListActivity.screen_list_sessao_plenaria == "10") {
                        if (it[0].data_inicio!!.year == sessao.data_inicio!!.year &&
                                it[0].data_inicio!!.month == sessao.data_inicio!!.month) {
                            it.add(sessao)
                            flagInsert = true
                            return@grid
                        }
                    }
                    else if (this@SessaoPlenariaListActivity.screen_list_sessao_plenaria == "20") {
                        if (it[0].data_inicio!!.year == sessao.data_inicio!!.year && it[0].data_inicio!!.month >= 6 && sessao.data_inicio!!.month >= 6) {
                            it.add(sessao)
                            flagInsert = true
                            return@grid
                        }
                        else if (it[0].data_inicio!!.year == sessao.data_inicio!!.year && it[0].data_inicio!!.month < 6 && sessao.data_inicio!!.month < 6) {
                            it.add(sessao)
                            flagInsert = true
                            return@grid
                        }
                    }
                    else if (this@SessaoPlenariaListActivity.screen_list_sessao_plenaria == "30") {
                        if (it[0].data_inicio!!.year == sessao.data_inicio!!.year) {
                            it.add(sessao)
                            flagInsert = true
                            return@grid
                        }
                    }

                }
                if (!flagInsert) {
                    var lista = ArrayList<SessaoPlenaria>()
                    lista.add(sessao)
                    grid.add(lista)
                }
            }
            field = value
        }
        val mFragments: WeakHashMap<Int, Fragment> = WeakHashMap()

        override fun notifyDataSetChanged() {
            for (position in mFragments.keys) {
                (mFragments[position] as PlaceholderFragment).sessoes = grid[position]
                (mFragments[position] as PlaceholderFragment).update()
            }
            super.notifyDataSetChanged()
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, position, `object`)
            if (mFragments.containsKey(position)) {
                mFragments.remove(position)
            }
        }

        override fun getItem(position: Int): Fragment {

            var item = PlaceholderFragment.newInstance(grid[position])
            mFragments.put(position, item as Fragment)
            return item
        }

        override fun getCount(): Int {
            if (sessoes == null) {
                return 0
            }
            return grid.size
        }
    }


    class PlaceholderFragment : Fragment() {

        var sessoes: ArrayList<SessaoPlenaria>? = null
        var rootView: View? = null

        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: RecyclerView.Adapter<*>

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)

            rootView = inflater.inflate(R.layout.fragment_sessao_plenaria, container, false)
            update_title()

            viewAdapter = SessaoPlenariaAdapter(sessoes!!)
            recyclerView = rootView!!.findViewById(R.id.view_lista_sessoes)
            recyclerView.adapter = viewAdapter
            return rootView
        }

        fun update() {
            (viewAdapter as SessaoPlenariaAdapter).updateData(sessoes)
            update_title()
        }

        private fun update_title() {
            val cal = Calendar.getInstance()
            cal.time = this.sessoes!![0].data_inicio

            var tituloPagina = ""
            var ac: SessaoPlenariaListActivity = activity as SessaoPlenariaListActivity
            when {
                ac.screen_list_sessao_plenaria == "10" ->
                    tituloPagina = getString(R.string.title_grid_mensal_sessao_plenaria,
                        cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).capitalize(),
                        cal.get(Calendar.YEAR))
                ac.screen_list_sessao_plenaria == "20" ->
                    tituloPagina = getString(R.string.title_grid_semenstral_sessao_plenaria,
                        if (cal.get(Calendar.MONTH) <= 6) 1 else 2,
                        cal.get(Calendar.YEAR))
                ac.screen_list_sessao_plenaria == "30" ->
                    tituloPagina = getString(R.string.title_grid_anual_sessao_plenaria,
                        cal.get(Calendar.YEAR))
            }
            rootView!!.fragment_title.text = tituloPagina
        }

        companion object {
            fun newInstance(sessoes: ArrayList<SessaoPlenaria>): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                fragment.sessoes = sessoes
                return fragment
            }
        }

        class SessaoPlenariaAdapter(private val sessoes: ArrayList<SessaoPlenaria>?):
                RecyclerView.Adapter<SessaoPlenariaAdapter.SessaoPlenariaHolder>() {

            inner class SessaoPlenariaHolder(val _view: View): RecyclerView.ViewHolder(_view) {
                var sessao: SessaoPlenaria? = null

                init {
                    _view.setOnClickListener {
                        val intent = Intent(it.context, SessaoPlenariaActivity::class.java)
                        intent.putExtra("uid", sessao!!.uid)
                        it.context.startActivity(intent)
                        Log.d("SAPL", String.format("Click: Clicou na sessão: %d - hora_inicio:%s",
                                sessao!!.uid,
                                sessao!!.hora_inicio
                                ))
                    }
                }
            }

            fun updateData(_sessoes: ArrayList<SessaoPlenaria>?) {
                sessoes!!.clear()
                sessoes.addAll(_sessoes!!)
                notifyDataSetChanged()
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessaoPlenariaHolder {
                val _view = LayoutInflater.from(parent.context
                ).inflate(R.layout.item_sessao_plenaria, parent, false)

                return SessaoPlenariaHolder(_view)
            }

            override fun onBindViewHolder(holder: SessaoPlenariaHolder, position: Int) {
                holder.sessao = sessoes!![position]

                val sessao = sessoes!![position]
                var titulos = titulo_sessao(holder._view.context, sessao)

                holder._view.session_title.text = titulos["session_title"]
                holder._view.session_subtitle.text = titulos["session_subtitle"]
                holder._view.session_date_extended.text = titulos["session_date_extended"]
            }

            override fun getItemCount(): Int {
                return sessoes!!.size
            }

        }

    }
    companion object {
        fun titulo_sessao(context: Context, sessaoPlenaria: SessaoPlenaria): HashMap<String, String> {
            val quinzenal:Boolean = SettingsActivity.getBooleanPreference(context, "divisao_quizenal_display")

            val cal = Calendar.getInstance()
            cal.time = sessaoPlenaria.data_inicio

            val titulos = HashMap<String, String>()

            if (!quinzenal) {
                titulos["session_title"] = context.getString(
                    R.string.sessoes_default_title_extended,
                    sessaoPlenaria.numero,
                    sessaoPlenaria.tipo,
                    sessaoPlenaria.sessao_legislativa,
                    sessaoPlenaria.legislatura)

                titulos["session_subtitle"] = context.getString(
                    R.string.sessoes_default_subtitle_extended,
                    sessaoPlenaria.sessao_legislativa,
                    sessaoPlenaria.legislatura)
            } else {
                val numeroQuizena: Int = if (cal.get(Calendar.DAY_OF_MONTH) < 16) 1 else 2


                titulos["session_title"] = context.getString(
                    R.string.sessoes_quinzenal_title_extended,
                    sessaoPlenaria.numero,
                    sessaoPlenaria.tipo,
                    numeroQuizena,
                    cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).capitalize(),
                    cal.get(Calendar.YEAR))
                titulos["session_subtitle"] = context.getString(
                    R.string.sessoes_quinzenal_subtitle_extended,
                    sessaoPlenaria.sessao_legislativa,
                    sessaoPlenaria.legislatura)
            }

            titulos["session_date_extended"] = context.getString(
                R.string.sessoes_date_extended,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).capitalize(),
                cal.get(Calendar.YEAR),
                sessaoPlenaria.hora_inicio)
            return titulos
        }
    }
}
