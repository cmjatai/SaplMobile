package br.leg.interlegis.saplmobile.sapl.json.interfaces

import android.content.Context
import com.google.gson.JsonObject
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface JsonApiInterface{
    fun getObject(uid: Int): JsonObject
    fun getList(kwargs:Map<String, Any>): HashMap<String, Any>
    fun sync(kwargs:Map<String, Any>): Int
    fun syncList(list:Any?, deleted: IntArray? = null): Int
}