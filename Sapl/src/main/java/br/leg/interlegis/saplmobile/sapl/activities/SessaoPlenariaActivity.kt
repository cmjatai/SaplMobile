package br.leg.interlegis.saplmobile.sapl.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders

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
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.json.JsonApiSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.views.SessaoPlenariaViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_sessao_plenaria.*
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.*
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.view.*
import kotlinx.android.synthetic.main.item_sessao_plenaria.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.onPageChangeListener
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.ArrayList


class SessaoPlenariaActivity : SaplBaseActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessao_plenaria)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        container.setPageTransformer(true, ZoomOutPageTransformer())

        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixel: Int) {}
            override fun onPageSelected(p0: Int) {
                var item = this@SessaoPlenariaActivity
                var sections = item.mSectionsPagerAdapter

                if (sections!!.sessoes!!.size - p0 == 3) {
                    doAsync {
                        val data_fim = sections.sessoes!![sections.sessoes!!.size-1].data_inicio

                        val c:Calendar = Calendar.getInstance()
                        c.time = data_fim!!
                        c.add(Calendar.DAY_OF_MONTH, JsonApi.retroagir)

                        var json = JsonApi(this@SessaoPlenariaActivity)
                        json.get_sessao_sessao_plenaria(c.time, data_fim)
                    }
                }
            }
        })

        var sessaoModel = ViewModelProviders.of(
                this).get(SessaoPlenariaViewModel::class.java)

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
                    if (it[0].data_inicio!!.year == sessao.data_inicio!!.year &&
                            it[0].data_inicio!!.month == sessao.data_inicio!!.month) {
                        it.add(sessao)
                        flagInsert = true
                        return@grid
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

        class SessaoPlenariaAdapter(private val sessoes: ArrayList<SessaoPlenaria>?):
                RecyclerView.Adapter<SessaoPlenariaAdapter.SessaoPlenariaHolder>() {

            class SessaoPlenariaHolder(val _view: View): RecyclerView.ViewHolder(_view)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessaoPlenariaHolder {
                val _view = LayoutInflater.from(parent.context
                    ).inflate(R.layout.item_sessao_plenaria, parent, false)

                return SessaoPlenariaHolder(_view)
            }

            override fun onBindViewHolder(holder: SessaoPlenariaHolder, position: Int) {
                holder._view.session_title.text = sessoes!![position].uid.toString()
                holder._view.session_subtitle.text = sessoes[position].data_inicio.toString()
            }

            override fun getItemCount(): Int {
                return sessoes!!.size
            }

        }

        var sessoes: ArrayList<SessaoPlenaria>? = null

        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: RecyclerView.Adapter<*>
        private lateinit var viewManager: RecyclerView.LayoutManager

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)

            val rootView = inflater.inflate(R.layout.fragment_sessao_plenaria, container, false)
            update(rootView)

            val cal = Calendar.getInstance()
            cal.time = this.sessoes!![0].data_inicio
            rootView.fragment_title.text = String.format("%s de %d",
                    cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).capitalize(),
                    cal.get(Calendar.YEAR))

            return rootView
        }

        fun update(rootView: View? = null) {

            viewManager = GridLayoutManager(context, 4)
            viewAdapter = SessaoPlenariaAdapter(sessoes!!)

            recyclerView = rootView!!.findViewById(R.id.view_lista_sessoes)
            recyclerView.layoutManager = viewManager
            recyclerView.adapter = viewAdapter



        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sessoes: ArrayList<SessaoPlenaria>): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                fragment.sessoes = sessoes
                return fragment
            }
        }
    }
}
