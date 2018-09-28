package br.leg.interlegis.saplmobile.sapl.json.interfaces

import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface SessaoPlenariaRetrofitService {

    @GET("api/sessao-plenaria/")
    fun list(
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("data") data: String ) : Call<SaplApiRestResponse>
}