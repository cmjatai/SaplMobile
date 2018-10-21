package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = Anexada.TABLE_NAME,
        foreignKeys = arrayOf(
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia_principal"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia_anexada"),
                        onDelete = ForeignKey.CASCADE
                )
        ),
        indices = arrayOf(
                Index(value= arrayOf("materia_principal", "materia_anexada"), unique = true))
)
class Anexada constructor(uid: Int,
                          materia_principal: Int,
                          materia_anexada: Int,
                          data_anexacao: Date,
                          data_desanexacao: Date? = null): Serializable, SaplEntity {


    @PrimaryKey
    var uid: Int = uid
    var materia_principal: Int = materia_principal
    var materia_anexada: Int = materia_anexada
    var data_anexacao: Date = data_anexacao
    var data_desanexacao: Date? = data_desanexacao

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "materia"
        @Ignore
        const val TABLE_NAME: String = "anexada"


        override fun importJsonObject(it: JsonObject): Anexada = Anexada(
                uid = it.get("id").asInt,
                materia_principal = it.get("materia_principal").asInt,
                materia_anexada = it.get("materia_anexada").asInt,
                data_anexacao = Converters.df.parse(it.get("data_anexacao").asString),
                data_desanexacao = if (it.get("data_desanexacao").isJsonNull) null else Converters.dtf.parse(it.get("data_desanexacao").asString)
        )

        fun importAnexadasJsonArray(jsonArray: JsonArray): Map<Int, SaplEntity> {
            val mapItens:HashMap<Int, SaplEntity> = HashMap()
            jsonArray.forEach {
                val i = it as JsonObject
                mapItens[i.get("id").asInt] =  Anexada(
                        uid = i.get("id").asInt,
                        materia_principal = i.get("materia_principal").asInt,
                        materia_anexada = i.get("materia_anexada").asJsonObject.get("id").asInt,
                        data_anexacao = Converters.df.parse(it.get("data_anexacao").asString),
                        data_desanexacao = if (i.get("data_desanexacao").isJsonNull) null else Converters.dtf.parse(i.get("data_desanexacao").asString))

            }
            return mapItens
        }
        fun importPrincipaisDeJsonArray(jsonArray: JsonArray): Map<Int, SaplEntity> {
            val mapItens:HashMap<Int, SaplEntity> = HashMap()
            jsonArray.forEach {
                val i = it as JsonObject
                mapItens[i.get("id").asInt] =  Anexada(
                        uid = i.get("id").asInt,
                        materia_principal = i.get("materia_principal").asJsonObject.get("id").asInt,
                        materia_anexada = i.get("materia_anexada").asJsonObject.get("id").asInt,
                        data_anexacao = Converters.df.parse(it.get("data_anexacao").asString),
                        data_desanexacao = if (i.get("data_desanexacao").isJsonNull) null else Converters.dtf.parse(i.get("data_desanexacao").asString))

            }
            return mapItens
        }

    }
}

