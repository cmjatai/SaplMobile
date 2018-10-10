package br.leg.interlegis.saplmobile.sapl.json.interfaces

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TimeRefreshRetrofitService {
    @GET("api/mobile/time_refresh")
    fun sync_time_refresh(@Query("format") format: String ) : Call<JsonObject>
}