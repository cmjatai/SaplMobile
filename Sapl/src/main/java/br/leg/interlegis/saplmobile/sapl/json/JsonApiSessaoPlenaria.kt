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

class JsonApiSessaoPlenaria: JsonApiInterface {

    override fun sync(context: Context, retrofit: Retrofit?, data: Pair<Date?, Date?>) {

        val servico = retrofit?.create(SessaoPlenariaRetrofitService::class.java)
        var response: SaplApiRestResponse? = null

        val listSessao = ArrayList<SessaoPlenaria>()


        while (response == null || response?.pagination!!.next_page != null) {
            val dmin = if (data.first != null) Converters.dtf.format(data.first) else null
            val dmax = if (data.second != null) Converters.dtf.format(data.second) else null

            val call = servico?.list(
                    format = "json",
                    page = if (response == null) 1 else response?.pagination!!.next_page!!,
                    tipo_update = "1",
                        // Tipo 1 = filtro com base nas datas de alteração
                        // Tipo 2 = filtro com base nas datas da sessão plenária
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
        if (listSessao.isNotEmpty()) {
            val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
            dao.insertAll(listSessao)
        }
    }
}