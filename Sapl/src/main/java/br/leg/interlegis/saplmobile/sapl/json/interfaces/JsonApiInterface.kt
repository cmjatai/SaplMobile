package br.leg.interlegis.saplmobile.sapl.json.interfaces

import android.content.Context
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.HashMap

interface JsonApiInterface {
    fun sync(kwargs:Map<String, Any>): Int
}