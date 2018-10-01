package br.leg.interlegis.saplmobile.sapl.json

import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SessaoPlenariaRetrofitService
import br.leg.interlegis.saplmobile.sapl.support.Log
import retrofit2.Retrofit
import java.util.*

class JsonApiSessaoPlenaria: JsonApiInterface {

    override fun sync(retrofit: Retrofit?, data: Date) {

        val servico = retrofit?.create(SessaoPlenariaRetrofitService::class.java)
        var response: SaplApiRestResponse? = null

        while (response == null || response?.pagination!!.next_page != null) {
            val call = servico?.list(
                    format = "json",
                    page = if (response == null) 1 else response?.pagination!!.next_page!!,
                    tipo_update = "1",
                        // Tipo 1 = filtro com base nas datas de alteração
                        // Tipo 2 = filtro com base nas datas da sessão plenária
                    data_min = Converters.df.format(data),
                    data_max = null
            )
            response = call?.execute()!!.body()!!

            for (item in response?.results!!) {
                Log.d("SAPL", item.get("id").toString())
            }
        }
    }
}