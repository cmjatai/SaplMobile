package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.TimeRefreshRetrofitService
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import br.leg.interlegis.saplmobile.sapl.support.Log
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap


class JsonApi {
    //JsonApiMateriaLegislativa.chave to JsonApiMateriaLegislativa()
    val modules = hashMapOf<String, JsonApiInterface>(
            JsonApiSessaoPlenaria.chave to JsonApiSessaoPlenaria())

    var API_BASE_URL : String = ""
    var context: Context? = null
    var retrofit: Retrofit? = null

    var maximoGlobal: TimeRefresh? = null
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

        Log.d("SAPL", "========================")
        val dao = AppDataBase.getInstance(this.context!!).DaoTimeRefresh()
        maximoGlobal = dao.maxValue()

        if (maximoGlobal != null)
            Log.d("SAPL",String.format("max key: %s - %s", maximoGlobal?.chave, maximoGlobal?.data))

        val trs = retrofit?.create(TimeRefreshRetrofitService::class.java)
        val call = trs?.sync_time_refresh(
                format = "json",
                date = if (maximoGlobal == null) null else Converters.dtf.format(maximoGlobal!!.data))

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
                //map.put("data_inicio", c.time)
                //map.put("data_fim", Any())
                map.put("tipo_update", "get_initial")

                syncResult.add(Pair(item.key, map))
                val tr = TimeRefresh(item.key, ultimaAtualizacao)
                dao.insert(tr)

            }
            else {
                if (ultimaAtualizacao > time.data) {
                    Log.d("SAPL", item.key)
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

    fun get_sessao_sessao_plenaria(dataInicio:Date? = null, dataFim: Date? = null, tipoUpdate:String = "get"): Int? {
        val kwargs = HashMap<String, Any>()
        kwargs["tipo_update"] = tipoUpdate
        if (dataInicio != null)
            kwargs["data_inicio"] = dataInicio

        if (dataFim != null)
            kwargs["data_fim"] = dataFim

        val apiModule= modules[JsonApiSessaoPlenaria.chave]
        return apiModule?.sync(context!!, retrofit, kwargs)
    }

    fun sync(sync_modules:  ArrayList<Pair<String, HashMap<String, Any>>> ) {
        for (module in sync_modules) {
            val apiModule= modules[module.first]
            apiModule?.sync(context!!, retrofit, module.second)
        }

    }
}