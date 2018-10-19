package br.leg.interlegis.saplmobile.sapl.db.entities

import com.google.gson.JsonArray
import com.google.gson.JsonObject

interface SaplEntityInterface {

    fun importJsonArray(jsonArray: JsonArray): Map<out Int, SaplEntity>
    fun importJsonObject(it: JsonObject): SaplEntity

}