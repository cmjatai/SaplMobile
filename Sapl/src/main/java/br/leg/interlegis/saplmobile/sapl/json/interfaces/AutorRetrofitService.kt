package br.leg.interlegis.saplmobile.sapl.json.interfaces

import br.leg.interlegis.saplmobile.sapl.json.SaplApiRestResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import okhttp3.ResponseBody



interface AutorRetrofitService {

    @GET("api/mobile/autor/")
    fun list(
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("tipo_update") tipo_update: String,
            @Query("data_min") data_min: String?,
            @Query("data_max") data_max: String?) : Call<SaplApiRestResponse>

    @GET
    fun downloadFotografia(@Url fileUrl: String): Call<ResponseBody>

}