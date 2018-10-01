package br.leg.interlegis.saplmobile.sapl.json.interfaces

import retrofit2.Retrofit
import java.util.*

interface JsonApiInterface {

    fun sync(retrofit: Retrofit?, data: Pair<Date?, Date?>)
}