package br.leg.interlegis.saplmobile.sapl.db.entities.base

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = Autor.TABLE_NAME)
class Autor constructor(uid: Int,
                         nome: String,
                         fotografia: String,
                         file_date_updated: Date? = null): Serializable, SaplEntity {



    @PrimaryKey
    var uid: Int = uid
    var nome: String = nome
    var fotografia: String = fotografia
    var file_date_updated: Date? = file_date_updated

    companion object:SaplEntityCompanion() {
        @Ignore
        const val APP_LABEL: String = "base"
        @Ignore
        const val TABLE_NAME: String = "autor"

        override fun importJsonObject(i: JsonObject): Autor = Autor(
            uid = i.get("id").asInt,
            nome = i.get("nome").asString,
            fotografia = i.get("fotografia").asString,
            file_date_updated = if (i.get("file_date_updated").isJsonNull) null else Converters.dtf.parse(i.get("file_date_updated").asString))

        fun importJsonArrayFromAutoria(jsonArray: JsonArray): Map<Int, SaplEntity>  {
            val mapItens:HashMap<Int, SaplEntity> = HashMap()
            jsonArray.forEach {
                val i = it as JsonObject
                mapItens[it.get("id").asInt] = this.importJsonObject(i.get("autor").asJsonObject)
            }
            return mapItens
        }
    }
}

