package br.leg.interlegis.saplmobile.sapl.db.entities

import android.arch.persistence.room.ForeignKey
import com.google.gson.JsonArray
import com.google.gson.JsonObject

abstract class SaplEntityCompanion: SaplEntityInterface {


    override fun importJsonObject(it: JsonObject): SaplEntity{
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importJsonArray(jsonArray: JsonArray, foreignKey: String): Map<Int, SaplEntity> {
        val mapItens:HashMap<Int, SaplEntity> = HashMap()
        jsonArray.forEach {
            val i = it as JsonObject
            mapItens[it.get("id").asInt] = this.importJsonObject(if (foreignKey.isEmpty()) i else i.get(foreignKey).asJsonObject)
        }
        return mapItens
    }

}