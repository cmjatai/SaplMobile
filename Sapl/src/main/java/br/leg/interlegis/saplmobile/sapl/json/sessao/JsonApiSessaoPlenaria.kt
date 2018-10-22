package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiSessaoPlenaria(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {
    override val url = String.format("api/mobile/%s/%s/", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)

    companion object {

        val chave = String.format("%s:%s", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)
    }


    override fun sync(kwargs:Map<String, Any>): Int {
        val result = super.get(kwargs)

        val listaSessao = ArrayList<SessaoPlenaria>()

        (result["list"] as JsonArray).forEach {
            listaSessao.add(SessaoPlenaria.importJsonObject(it.asJsonObject))
        }

        val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
        val apagar = dao.loadAllByIds(result["deleted"] as IntArray)
        dao.insertAll(listaSessao)
        dao.delete(apagar)

        return listaSessao.size
    }
}