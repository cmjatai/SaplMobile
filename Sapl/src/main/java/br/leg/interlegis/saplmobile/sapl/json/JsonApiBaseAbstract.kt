package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

abstract class JsonApiBaseAbstract(context:Context, retrofit: Retrofit?): JsonApiInterface{

    abstract val url: String

    var context: Context = context
    var retrofit: Retrofit? = retrofit
    var servico: SaplRetrofitService? = null

    init {
        if (this.retrofit == null) {
            this.retrofit = Retrofit.Builder()
                    .baseUrl(SettingsActivity.getStringPreference(context, "domain_casa_legislativa"))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        servico = this.retrofit!!.create(SaplRetrofitService::class.java)
    }

    fun call(old_response: SaplApiRestResponse?, kwargs:Map<String, Any>): SaplApiRestResponse {

        var dmin = if (kwargs["data_inicio"] is Date) Converters.dtf.format(kwargs["data_inicio"] as Date) else null
        var dmax = if (kwargs["data_fim"] is Date) Converters.dtf.format(kwargs["data_fim"] as Date) else null


        var fk_name:String = ""
        var fk_pk:Int = 0

        if (kwargs.containsKey("fk_name")) {
            fk_name = kwargs["fk_name"] as String
            fk_pk = kwargs["fk_pk"] as Int
        }

        var tipo_update = "sync"
        if (kwargs.get("tipo_update") is String) {
            tipo_update = kwargs.get("tipo_update").toString()
        }

        val call = servico?.api(
                url = String.format("%s%s", url, if (kwargs.containsKey("uid")) kwargs["uid"] as String else ""),
                format = "json",
                page = if (old_response == null) 1 else old_response.pagination!!.next_page!!,
                tipo_update = tipo_update,
                // Tipo sync = filtra e se baseia nas alterações registradas após data_min
                // Tipo get = pega todos os dados e caso tenha filtros abaixo, filtra
                // Tipo last_items = uma pagina só com os ultimos dados da listagem
                // Tipo first_items = uma página só com os primeiros dados da listagem
                // Tipo get_initial = uma página com os últimos dados do servidor
                fk_name = fk_name,
                fk_pk = fk_pk,
                data_min = dmin,
                data_max = dmax
        )

        return call?.execute()!!.body()!!
    }

    fun callUid(uid: Int): JsonObject? {

        val call = servico?.uid(
                url = String.format("%s%s/", url, uid),
                format = "json")


        val response = call?.execute()!!.body()!!
        return response
    }

    override fun sync(kwargs:Map<String, Any>): Int {
        val result = getList(kwargs)
        return syncList(result["list"], result["deleted"] as IntArray)
    }


    override fun getObject(uid: Int): JsonObject {
        return callUid(uid) as JsonObject
    }

    override fun getList(kwargs:Map<String, Any>): HashMap<String, Any> {
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