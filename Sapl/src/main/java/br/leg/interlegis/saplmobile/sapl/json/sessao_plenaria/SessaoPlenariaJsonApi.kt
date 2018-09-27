package br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria

import br.leg.interlegis.saplmobile.sapl.support.Log
import retrofit2.Retrofit

class SessaoPlenariaJsonApi {

    fun sync(retrofit: Retrofit?) {

        val servico = retrofit?.create(SessaoPlenariaRetrofitService::class.java)
        val call = servico?.list("json", 1)
        val response: SessaoPlenariaResponse = call?.execute()!!.body()!!

        Log.d("SAPL", response.pagination!!.total_pages!!.toString())

    }
}