package br.leg.interlegis.saplmobile.sapl.db.entities.sessao

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = ExpedienteMateria.TABLE_NAME,
        foreignKeys = arrayOf(
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = SessaoPlenaria::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("sessao_plenaria"),
                        onDelete = ForeignKey.CASCADE
                )
        ),
        indices = arrayOf(
            Index(value= arrayOf("materia")),
            Index(value= arrayOf("sessao_plenaria")),
            Index(value= arrayOf("materia", "sessao_plenaria")),
            Index(value= arrayOf("sessao_plenaria", "numero_ordem"))
        )
)
class ExpedienteMateria constructor(uid: Int,
                           materia: Int,
                           observacao: String,
                           numero_ordem: Int,
                           resultado: String,
                           tipo_votacao: Int,
                           sessao_plenaria: Int): Serializable, SaplEntity {


    @PrimaryKey
    var uid: Int = uid
    var materia: Int = materia
    var observacao = observacao
    var numero_ordem = numero_ordem
    var resultado = resultado
    var tipo_votacao = tipo_votacao
    var sessao_plenaria = sessao_plenaria

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "sessao"
        @Ignore
        const val TABLE_NAME: String = "expedientemateria"

        override fun importJsonObject(it: JsonObject): ExpedienteMateria = ExpedienteMateria(
                uid = it.get("id").asInt,
                materia = it.get("materia").asInt,
                observacao =  it.get("observacao").asString,
                numero_ordem = it.get("numero_ordem").asInt,
                resultado =  it.get("resultado").asString,
                tipo_votacao = it.get("tipo_votacao").asInt,
                sessao_plenaria = it.get("sessao_plenaria").asInt
        )
    }
}

