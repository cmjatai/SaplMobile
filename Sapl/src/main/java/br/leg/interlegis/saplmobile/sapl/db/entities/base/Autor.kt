package br.leg.interlegis.saplmobile.sapl.db.entities.base

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import br.leg.interlegis.saplmobile.sapl.db.Converters
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity(tableName = Autor.TABLE_NAME)
class Autor constructor(uid: Int,
                         nome: String,
                         fotografia: String,
                         file_date_updated: Date? = null): Serializable {


    @PrimaryKey
    @SerializedName("id")
    var uid: Int = uid
    var nome: String = nome
    var fotografia: String = fotografia
    var file_date_updated: Date? = file_date_updated

    companion object {

        @Ignore
        const val APP_LABEL: String = "base"
        @Ignore
        const val TABLE_NAME: String = "autor"

        fun parse(i: JsonObject): Autor = Autor(
            uid = i.get("id").asInt,
            nome = i.get("nome").asString,
            fotografia = i.get("fotografia").asString,
            file_date_updated = if (i.get("file_date_updated").isJsonNull) null else Converters.dtf.parse(i.get("file_date_updated").asString))

        fun parseList(jsonArray: JsonArray): Map<out Int, Autor> {
            val mapAutores:HashMap<Int, Autor> = HashMap()
            jsonArray.forEach { itAutor ->
                val i = itAutor as JsonObject
                val autor = Autor.parse(i)
                mapAutores[autor.uid] = autor
            }
            return mapAutores
        }
    }
}

