package br.leg.interlegis.saplmobile.sapl.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup

import br.leg.interlegis.saplmobile.sapl.R
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApi
import br.leg.interlegis.saplmobile.sapl.json.JsonApiSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.support.Log
import br.leg.interlegis.saplmobile.sapl.views.SessaoPlenariaViewModel
import kotlinx.android.synthetic.main.activity_sessao_plenaria.*
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.onPageChangeListener
import java.util.*


class SessaoPlenariaActivity : SaplBaseActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessao_plenaria)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        container.setPageTransformer(true, ZoomOutPageTransformer())
        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixel: Int) {}
            override fun onPageSelected(p0: Int) {
                var item = this@SessaoPlenariaActivity
                var sections = item.mSectionsPagerAdapter

                /*Log.d("SAPL 2", "p0.....: " + p0.toString())
                Log.d("SAPL 2", "sessoes: " + sections!!.sessoes!!.size.toString())
                Log.d("SAPL 2", "retroagir: " + JsonApi.retroagir)*/

                if (sections!!.sessoes!!.size - p0 == 3) {
                    doAsync {
                        val data_fim = sections!!.sessoes!![sections!!.sessoes!!.size-1].data_inicio

                        val c:Calendar = Calendar.getInstance()
                        c.time = data_fim
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
    class DepthPageTransformer : ViewPager.PageTransformer {

        private val MIN_SCALE = 0.75f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        // Use the default slide transition when moving to the left page
                        alpha = 1f
                        translationX = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // (0,1]
                        // Fade the page out.
                        alpha = 1 - position

                        // Counteract the default slide transition
                        translationX = pageWidth * -position

                        // Scale the page down (between MIN_SCALE and 1)
                        val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position)))
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
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
        var sessoes: List<SessaoPlenaria>? = null
        val mFragments: WeakHashMap<Int, Fragment> = WeakHashMap()

        override fun notifyDataSetChanged() {
            for (position in mFragments.keys) {
                (mFragments[position] as PlaceholderFragment).sessaoPlenaria = sessoes!![position]
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
            var item = PlaceholderFragment.newInstance(sessoes!!.get(position))
            mFragments.put(position, item as Fragment)
            return item
        }

        override fun getCount(): Int {
            if (sessoes == null) {
                return 0
            }
            return sessoes!!.size
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        var sessaoPlenaria: SessaoPlenaria? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_sessao_plenaria, container, false)
            //rootView.section_label.text = sessaoPlenaria!!.legislatura + " " + sessaoPlenaria!!.data_inicio + " " + sessaoPlenaria!!.hora_inicio
            update(rootView)
            return rootView
        }

        fun update(rootView: View? = null) {
            var _view = if (view == null) rootView else view
            _view!!.section_label.text = sessaoPlenaria!!.uid.toString() + " "+ sessaoPlenaria!!.legislatura + " " + sessaoPlenaria!!.data_inicio + " " + sessaoPlenaria!!.hora_inicio
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
            fun newInstance(sessao: SessaoPlenaria): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                fragment.sessaoPlenaria = sessao
                return fragment
            }
        }
    }
}
