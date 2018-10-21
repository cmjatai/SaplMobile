package br.leg.interlegis.saplmobile.sapl.db.entities

import com.google.gson.JsonArray
import com.google.gson.JsonObject

abstract class SaplEntityCompanion: SaplEntityInterface {


    override fun importJsonObject(it: JsonObject): SaplEntity{
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importJsonArray(jsonArray: JsonArray): Map<Int, SaplEntity> {
        val mapItens:HashMap<Int, SaplEntity> = HashMap()
        jsonArray.forEach {
            val i = it as JsonObject
            mapItens[it.get("id").asInt] = this.importJsonObject(i)
        }
        return mapItens
    }

}