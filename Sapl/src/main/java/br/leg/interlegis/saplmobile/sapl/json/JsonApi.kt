package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.TimeRefreshRetrofitService
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap


class JsonApi {

    val key_sessaoplenaria = "sessao:sessaoplenaria"
    val key_ordemdia = "sessao:ordemdia"

    val modules = hashMapOf<String, JsonApiInterface>(
            key_sessaoplenaria to JsonApiSessaoPlenaria())

    var API_BASE_URL : String = ""
    var context: Context? = null
    var retrofit: Retrofit? = null
    companion object {
        var retroagir = -60

    }

    constructor(context: Context) {
        this.context = context
        API_BASE_URL = SettingsActivity.getStringPreference(context, "domain_casa_legislativa")

        retrofit = Retrofit
                .Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

    }

    fun sync_time_refresh(): ArrayList<Pair<String, HashMap<String, Any>>> {

        val dao = AppDataBase.getInstance(this.context!!).DaoTimeRefresh()

        val trs = retrofit?.create(TimeRefreshRetrofitService::class.java)
        val call = trs?.sync_time_refresh("json")
        val timeJson: JsonObject = call?.execute()!!.body()!!

        val syncResult : ArrayList<Pair<String, HashMap<String, Any>>> = ArrayList()
        val c:Calendar = Calendar.getInstance()

        for (item in timeJson.entrySet()) {
            var ultimaAtualizacao = Converters.dtf.parse(item.value.asString)

            val time = dao.loadValue(item.key)
            if (time == null) {
                c.time = ultimaAtualizacao
                c.add(Calendar.DAY_OF_MONTH, retroagir )

                var map = HashMap<String, Any>()
                map.put("data_inicio", c.time)
                map.put("data_fim", Any())
                map.put("tipo_update", "get")

                syncResult.add(Pair(item.key, map))
                val tr = TimeRefresh(item.key, ultimaAtualizacao)
                dao.insert(tr)

            }
            else {
                if (ultimaAtualizacao > time.data) {
                    c.time = time.data

                    var map = HashMap<String, Any>()
                    map.put("data_inicio", time.data as Any)
                    map.put("data_fim", Any())
                    map.put("tipo_update", "sync")

                    syncResult.add(Pair(time.chave, map))

                    time.data = ultimaAtualizacao
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

    fun get_sessao_sessao_plenaria(data_inicio:Date, data_fim: Date) {
        val api_module= modules.get(this.key_sessaoplenaria)
        val kwargs = HashMap<String, Any>()
        kwargs.put("data_inicio", data_inicio)
        kwargs.put("data_fim", data_fim)
        kwargs.put("tipo_update", "get")
        api_module?.sync(context!!, retrofit, kwargs)

    }

    fun sync(sync_modules:  ArrayList<Pair<String, HashMap<String, Any>>> ) {
        for (module in sync_modules) {
            val api_module= modules.get(module.first)
            api_module?.sync(context!!, retrofit, module.second)
        }
    }
}