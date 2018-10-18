package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiSessaoPlenaria(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = "api/mobile/sessaoplenaria/"

    companion object {
        val chave = String.format("%s:%s", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)
    }


    override fun sync(kwargs:Map<String, Any>): Int {
        servico = retrofit.create(SaplRetrofitService::class.java)

        val listSessao = ArrayList<SessaoPlenaria>()

        var response: SaplApiRestResponse? = null
        while (response == null || response.pagination!!.next_page != null) {
            response = call(response, kwargs)

            for (item in response.results!!) {
                listSessao.add(SessaoPlenaria.parse(item))
            }
        }

        val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
        val apagar = dao.loadAllByIds(response.deleted!!)
        dao.insertAll(listSessao)
        dao.delete(apagar)

        return listSessao.size
    }
}