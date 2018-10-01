package br.leg.interlegis.saplmobile.sapl.json.interfaces

import android.content.Context
import retrofit2.Retrofit
import java.util.*

interface JsonApiInterface {

    fun sync(context: Context, retrofit: Retrofit?, data: Pair<Date?, Date?>)
}