package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria.SessaoPlenariaJsonApi
import br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria.SessaoPlenariaResponse
import br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria.SessaoPlenariaRetrofitService
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.jetbrains.anko.toast

class JsonApi {
    var API_BASE_URL : String = ""
    var context: Context? = null
    var retrofit: Retrofit? = null

    constructor(context: Context) {
        this.context = context
        API_BASE_URL = SettingsActivity.getStringPreference(context, "domain_casa_legislativa")

        retrofit = Retrofit
                .Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

    }
    @Throws(Exception::class)
    fun get_last_global_refresh_time(): String {

        val trs = retrofit?.create(TimeRefreshRetrofitService::class.java)
        val call = trs?.get_last_global_refresh_time("json")
        val time: TimeRefreshResponse = call?.execute()!!.body()!!

        return time.last_global_refresh_time!!



        /*call?.enqueue(object: Callback<TimeRefreshResponse?> {
            override fun onResponse(call: Call<TimeRefreshResponse?>, response: Response<TimeRefreshResponse?>) {


                Log.v("JSON-API", response.toString())
            }

            override fun onFailure(call: Call<TimeRefreshResponse?>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })*/
    }


    fun sync() {
        val sessao_plenaria: SessaoPlenariaJsonApi = SessaoPlenariaJsonApi()
        sessao_plenaria.sync(retrofit)


    }
}