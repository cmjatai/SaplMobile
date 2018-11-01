package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh
import br.leg.interlegis.saplmobile.sapl.json.base.JsonApiAutorParlamentar
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.TimeRefreshRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiAnexada
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiAutoria
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiDocumentoAcessorio
import br.leg.interlegis.saplmobile.sapl.json.materia.JsonApiMateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.sessao.JsonApiSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import br.leg.interlegis.saplmobile.sapl.support.Log
import com.google.gson.JsonObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap


class JsonApi(_context: Context) {

    val context: Context = _context
    val retrofit: Retrofit = Retrofit
            .Builder()
            .baseUrl(SettingsActivity.getStringPreference(context, "domain_casa_legislativa"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val modules = arrayListOf(
            JsonApiMateriaLegislativa.chave to arrayListOf(JsonApiMateriaLegislativa(context, retrofit) as JsonApiInterface to arrayOf<String>()),
            JsonApiSessaoPlenaria.chave to arrayListOf(JsonApiSessaoPlenaria(context, retrofit) as JsonApiInterface to arrayOf<String>()),
            JsonApiAutorParlamentar.chave to arrayListOf(JsonApiAutorParlamentar(context, retrofit) as JsonApiInterface to arrayOf<String>()),
            JsonApiAutoria.chave to arrayListOf(JsonApiAutoria(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiAnexada.chave to arrayListOf(JsonApiAnexada(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiDocumentoAcessorio.chave to arrayListOf(JsonApiDocumentoAcessorio(context, retrofit) as JsonApiInterface to arrayOf("sync"))
            )

    var maximoGlobal: TimeRefresh? = null

    companion object {
        var retroagir = -60

    }

    fun sync_time_refresh(): ArrayList<Pair<String, HashMap<String, Any>>> {

        Log.d("SAPL", "========================")
        val dao = AppDataBase.getInstance(context).DaoTimeRefresh()
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

    }

    fun get_sessao_sessao_plenaria(dataInicio:Date? = null, dataFim: Date? = null, tipoUpdate:String = "getList"){
        val kwargs = HashMap<String, Any>()
        kwargs["tipo_update"] = tipoUpdate
        if (dataInicio != null)
            kwargs["data_inicio"] = dataInicio

        if (dataFim != null)
            kwargs["data_fim"] = dataFim

        modules.forEach {
            if (it.first == JsonApiSessaoPlenaria.chave)
                it.second.forEach {itApi ->
                    itApi.first.sync(kwargs)
                }
        }

    }

    fun sync(sync_modules:  ArrayList<Pair<String, HashMap<String, Any>>> ) {
        for (module in sync_modules) {

            modules.forEach {
                if (it.first.equals(module.first)) {
                    it.second.forEach{itApi ->

                        if (itApi.second.isEmpty() || module.second["tipo_update"] in itApi.second) {
                            itApi.first.sync(module.second)
                            Log.d("SAPL", String.format("Sincronizando: %s", module.first))

                        }
                    }
                }
            }
        }
    }
}