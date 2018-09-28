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
                    "json",
                    if (response == null) 1 else response?.pagination!!.next_page!!,
                    Converters.df.format(data)
            )
            response = call?.execute()!!.body()!!

            for (item in response?.results!!) {
                Log.d("SAPL", item.get("id").toString())
            }
        }
    }
}