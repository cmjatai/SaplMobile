package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.TimeRefreshRetrofitService
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import br.leg.interlegis.saplmobile.sapl.support.Log
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Collectors.toCollection



class JsonApi {

    val key_sessaoplenaria = "sessao:sessaoplenaria"
    val key_ordemdia = "sessao:ordemdia"

    val modules = hashMapOf<String, JsonApiInterface>(
            key_sessaoplenaria to JsonApiSessaoPlenaria())

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

    fun sync_time_refresh(): List<Pair<String, Pair<Date?, Date?>>> {

        val dao = AppDataBase.getInstance(this.context!!).DaoTimeRefresh()

        val trs = retrofit?.create(TimeRefreshRetrofitService::class.java)
        val call = trs?.sync_time_refresh("json")
        val timeJson: JsonObject = call?.execute()!!.body()!!

        val syncResult : ArrayList<Pair<String, Pair<Date?, Date?>>> = ArrayList()


        for (item in timeJson.entrySet()) {
            val data_item = Converters.dtf.parse(item.value.asString)
            val time = dao.loadValue(item.key)
            if (time == null) {
                syncResult.add(Pair(item.key, Pair(data_item, null)))
                val tr = TimeRefresh(item.key, data_item)
                dao.insert(tr)
            }
            else {
                if (data_item > time.data) {
                    syncResult.add(Pair(time.chave, Pair(time.data, null)))
                    time.data = data_item
                    dao.update(time)
                }
            }
        }
        return syncResult

        /*call?.enqueue(object: Callback<TimeRefreshResponse?> {
            override fun onResponse(call: Call<TimeRefreshResponse?>, response: Response<TimeRefreshResponse?>) {
                Log.v("JSON-API", response.toString())
            }
            override fun onFailure(call: Call<TimeRefreshResponse?>, t: Throwable) {
                //To change body of created functions use File | Settings | File Templates.
            }
        })*/
    }

    fun sync(sync_modules: List<Pair<String, Pair<Date?, Date?>>>) {
        for (module in sync_modules) {
            val api_module= modules.get(module.first)
            api_module?.sync(retrofit, module.second)
        }
    }
}