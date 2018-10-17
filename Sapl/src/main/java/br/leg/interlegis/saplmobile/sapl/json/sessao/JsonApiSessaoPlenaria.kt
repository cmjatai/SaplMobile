package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SessaoPlenariaRetrofitService
import br.leg.interlegis.saplmobile.sapl.support.Log
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class JsonApiSessaoPlenaria: JsonApiInterface {
    companion object {

        val chave = "sessao:sessaoplenaria"
    }
    override fun sync(context: Context, retrofit: Retrofit?, kwargs:Map<String, Any>): Int {

        val servico = retrofit?.create(SessaoPlenariaRetrofitService::class.java)
        var response: SaplApiRestResponse? = null

        val listSessao = ArrayList<SessaoPlenaria>()


        while (response == null || response.pagination!!.next_page != null) {
            var dmin = if (kwargs["data_inicio"] is Date) Converters.dtf.format(kwargs["data_inicio"] as Date) else null
            var dmax = if (kwargs["data_fim"] is Date) Converters.dtf.format(kwargs["data_fim"] as Date) else null

            var tipo_update = "sync"
            if (kwargs.get("tipo_update") is String) {
                tipo_update = kwargs.get("tipo_update").toString()
            }

            val call = servico?.list(
                    format = "json",
                    page = if (response == null) 1 else response.pagination!!.next_page!!,
                    tipo_update = tipo_update,
                        // Tipo sync = filtro com base nas datas de alteração
                        // Tipo get = filtro com base nas datas da sessão plenária
                        // Tipo last_items = uma pagina só com os ultimos dados da listagem
                        // Tipo first_items = uma página só com os primeiros dados da listagem
                        // Tipo get_initial = uma página com os últimos dados do servidor
                    data_min = dmin,
                    data_max = dmax
            )

            response = call?.execute()!!.body()!!

            for (item in response.results!!) {
                val sessao = SessaoPlenaria(
                        uid = item.get("id").asInt,
                        legislatura = item.get("legislatura").asInt,
                        sessao_legislativa = item.get("sessao_legislativa").asInt,
                        tipo = item.get("tipo").asString,
                        hora_inicio = item.get("hora_inicio").asString,
                        hora_fim = item.get("hora_fim").asString,
                        numero = item.get("numero").asInt,
                        data_inicio = if (item.get("data_inicio").isJsonNull) null else Converters.df.parse(item.get("data_inicio").asString),
                        data_fim = if (item.get("data_fim").isJsonNull) Converters.df.parse(item.get("data_inicio").asString) else Converters.df.parse(item.get("data_fim").asString)
                )
                listSessao.add(sessao)
            }
        }

        val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
        val apagar = dao.loadAllByIds(response.deleted!!)
        dao.insertAll(listSessao)
        dao.delete(apagar)

        return listSessao.size
    }
}