package br.leg.interlegis.saplmobile.sapl.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import br.leg.interlegis.saplmobile.sapl.R
import br.leg.interlegis.saplmobile.sapl.SaplBaseActivity
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.views.SessaoPlenariaViewModel
import kotlinx.android.synthetic.main.activity_sessao_plenaria.*
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.view.*
import java.util.*
import android.R.attr.keySet
import kotlinx.android.synthetic.main.fragment_sessao_plenaria.*


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

        var sessaoModel = ViewModelProviders.of(this).get(SessaoPlenariaViewModel::class.java)
        sessaoModel.sessoes?.observe(this,
                Observer<List<SessaoPlenaria>> {sessoes ->
                    if (sessoes != null) {
                        mSectionsPagerAdapter?.sessoes = sessoes
                        mSectionsPagerAdapter?.notifyDataSetChanged()
                    }
                })
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
            _view!!.section_label.text = sessaoPlenaria!!.legislatura + " " + sessaoPlenaria!!.data_inicio + " " + sessaoPlenaria!!.hora_inicio
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
