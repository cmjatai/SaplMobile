package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh
import br.leg.interlegis.saplmobile.sapl.json.base.JsonApiAutorParlamentar
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.TimeRefreshRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.materia.*
import br.leg.interlegis.saplmobile.sapl.json.norma.JsonApiLegislacaoCitada
import br.leg.interlegis.saplmobile.sapl.json.norma.JsonApiNormaJuridica
import br.leg.interlegis.saplmobile.sapl.json.sessao.JsonApiExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.json.sessao.JsonApiOrdemDia
import br.leg.interlegis.saplmobile.sapl.json.sessao.JsonApiRegistroVotacao
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

    // Nao mudar arrayList para outro formato como hashset ou hashmap... a sequencia de sync dos modulos faz parte da lógica
    val modules = arrayListOf(
            JsonApiSessaoPlenaria.chave to arrayListOf(JsonApiSessaoPlenaria(context, retrofit) as JsonApiInterface to arrayOf<String>()),
            JsonApiAutorParlamentar.chave to arrayListOf(JsonApiAutorParlamentar(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiMateriaLegislativa.chave to arrayListOf(JsonApiMateriaLegislativa(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiAutoria.chave to arrayListOf(JsonApiAutoria(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiAnexada.chave to arrayListOf(JsonApiAnexada(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiDocumentoAcessorio.chave to arrayListOf(JsonApiDocumentoAcessorio(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiTramitacao.chave to arrayListOf(JsonApiTramitacao(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiNormaJuridica.chave to arrayListOf(JsonApiNormaJuridica(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiLegislacaoCitada.chave to arrayListOf(JsonApiLegislacaoCitada(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiExpedienteMateria.chave to arrayListOf(JsonApiExpedienteMateria(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiOrdemDia.chave to arrayListOf(JsonApiOrdemDia(context, retrofit) as JsonApiInterface to arrayOf("sync")),
            JsonApiRegistroVotacao.chave to arrayListOf(JsonApiRegistroVotacao(context, retrofit) as JsonApiInterface to arrayOf("sync"))
            )

    fun get_sessao_sessao_plenaria(dataInicio:Date? = null, dataFim: Date? = null, tipoUpdate:String = "get"){
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