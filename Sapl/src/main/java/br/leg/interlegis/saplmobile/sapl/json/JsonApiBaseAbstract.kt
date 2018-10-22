package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

abstract class JsonApiBaseAbstract(context:Context, retrofit: Retrofit): JsonApiInterface{

    abstract val url: String

    var context: Context = context
    var retrofit: Retrofit = retrofit
    var servico: SaplRetrofitService? = null

    fun call(old_response: SaplApiRestResponse?, kwargs:Map<String, Any>): SaplApiRestResponse {

        var dmin = if (kwargs["data_inicio"] is Date) Converters.dtf.format(kwargs["data_inicio"] as Date) else null
        var dmax = if (kwargs["data_fim"] is Date) Converters.dtf.format(kwargs["data_fim"] as Date) else null

        var tipo_update = "sync"
        if (kwargs.get("tipo_update") is String) {
            tipo_update = kwargs.get("tipo_update").toString()
        }

        val call = servico?.api(
            url = url,
            format = "json",
            page = if (old_response == null) 1 else old_response.pagination!!.next_page!!,
            tipo_update = tipo_update,
            // Tipo sync = filtro com base nas datas de alteração
            // Tipo get = filtro com base nas datas da sessão plenária
            // Tipo last_items = uma pagina só com os ultimos dados da listagem
            // Tipo first_items = uma página só com os primeiros dados da listagem
            // Tipo get_initial = uma página com os últimos dados do servidor
            data_min = dmin,
            data_max = dmax
        )

        return call?.execute()!!.body()!!
    }


    override fun get(kwargs:Map<String, Any>): HashMap<String, Any> {
        servico = retrofit.create(SaplRetrofitService::class.java)

        val result = HashMap<String, Any>()
        val list = JsonArray()

        var response: SaplApiRestResponse? = null

        while (response == null || response.pagination?.next_page != null) {

            response = call(response, kwargs)

            if (response.pagination?.page == 1) result["deleted"] = response.deleted as Any


            response.results?.forEach {
                list.add(it)
            }
        }
        result["list"] = list
        return result


    }

}