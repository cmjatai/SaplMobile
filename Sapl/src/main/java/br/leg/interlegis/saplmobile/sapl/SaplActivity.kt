package br.leg.interlegis.saplmobile.sapl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import br.leg.interlegis.saplmobile.sapl.activities.SessaoPlenariaActivity
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_sapl.*
import kotlinx.android.synthetic.main.app_bar_sapl.*
import kotlinx.android.synthetic.main.content_sapl.*
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.ChaveValor
import br.leg.interlegis.saplmobile.sapl.services.SaplService
import br.leg.interlegis.saplmobile.sapl.support.Log
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast


class SaplActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sapl)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val intent = Intent(this, SaplService::class.java)
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        SaplApplication.activityResumed()

        if (SaplService.isRunning()) {
            val message = Message()
            message.arg1 = 1
            message.arg2 = 2
            SaplService.sendMessage(message)
        }
    }

    override fun onPause() {
        super.onPause()
        SaplApplication.activityPaused()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.sapl, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = SettingsActivity.newIntent(this)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_sessoes_plenarias -> {
                hello_word.text = SettingsActivity.getStringPreference(this, "domain_casa_legislativa")
                val intent = Intent(this, SessaoPlenariaActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_gallery -> {
            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
