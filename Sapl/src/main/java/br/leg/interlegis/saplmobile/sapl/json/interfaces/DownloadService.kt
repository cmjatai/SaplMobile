package br.leg.interlegis.saplmobile.sapl.json.interfaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url
import okhttp3.ResponseBody



interface DownloadService {

    @GET
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>
}