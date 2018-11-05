package br.leg.interlegis.saplmobile.sapl.db.entities.norma

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = LegislacaoCitada.TABLE_NAME,
        foreignKeys = arrayOf(
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = NormaJuridica::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("norma"),
                        onDelete = ForeignKey.CASCADE
                )
        ),
        indices = arrayOf(
                Index(value= arrayOf("materia")),
                Index(value= arrayOf("norma")),
                Index(value= arrayOf("materia", "norma"))
                )
)
class LegislacaoCitada constructor(uid: Int,
                                   materia: Int,
                                   norma: Int): Serializable, SaplEntity {


    @PrimaryKey
    var uid: Int = uid
    var materia: Int = materia
    var norma: Int = norma

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "norma"
        @Ignore
        const val TABLE_NAME: String = "legislacaocitada"

        override fun importJsonObject(it: JsonObject): LegislacaoCitada = LegislacaoCitada(
                uid = it.get("id").asInt,
                materia = it.get("materia").asInt,
                norma = it.get("norma").asInt
        )
    }
}

