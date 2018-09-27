package br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria

import br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria.SessaoPlenariaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SessaoPlenariaRetrofitService {

    @GET("api/sessao-plenaria/")
    fun list(
            @Query("format") format: String,
            @Query("page") page: Long ) : Call<SessaoPlenariaResponse>
}