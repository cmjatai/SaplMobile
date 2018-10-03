package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SessaoPlenariaRetrofitService
import br.leg.interlegis.saplmobile.sapl.support.Log
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class JsonApiSessaoPlenaria: JsonApiInterface {

    override fun sync(context: Context, retrofit: Retrofit?, kwargs:Map<String, Any>) {

        val servico = retrofit?.create(SessaoPlenariaRetrofitService::class.java)
        var response: SaplApiRestResponse? = null

        val listSessao = ArrayList<SessaoPlenaria>()


        while (response == null || response?.pagination!!.next_page != null) {
            val dmin = if (kwargs["data_inicio"] is Date) Converters.dtf.format(kwargs["data_inicio"] as Date) else null
            val dmax = if (kwargs["data_fim"] is Date) Converters.dtf.format(kwargs["data_fim"] as Date) else null

            var tipo_update = "sync"
            if (kwargs["tipo_update"] is String) {
                tipo_update = kwargs["tipo_update"].toString()
            }

            val call = servico?.list(
                    format = "json",
                    page = if (response == null) 1 else response?.pagination!!.next_page!!,
                    tipo_update = tipo_update,
                        // Tipo sync = filtro com base nas datas de alteração
                        // Tipo get = filtro com base nas datas da sessão plenária
                    data_min = dmin,
                    data_max = dmax
            )
            response = call?.execute()!!.body()!!


            for (item in response?.results!!) {
                val sessao = SessaoPlenaria(
                        item.get("id").asInt,
                        item.get("sessao_legislativa").asString,
                        item.get("legislatura").asString,
                        item.get("tipo").asString,
                        Converters.df.parse(item.get("data_inicio").asString),
                        Converters.df.parse(item.get("data_fim").asString),
                        item.get("hora_inicio").asString,
                        item.get("hora_fim").asString,
                        item.get("numero").asInt)
                listSessao.add(sessao)
            }
        }
        val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
        val apagar = dao.loadAllByIds(response?.deleted!!)
        dao.insertAll(listSessao)
        dao.delete(apagar)
    }
}