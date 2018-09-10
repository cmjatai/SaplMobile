package br.leg.interlegis.saplmobile.sapl.json

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TimeRefreshRetrofitService {
    @GET("api/time_refresh")
    fun get_last_global_refresh_time( @Query("format") format: String ) : Call<TimeRefreshResponse>
}