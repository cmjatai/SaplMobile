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

@Entity(tableName = RegistroVotacao.TABLE_NAME,
        foreignKeys = arrayOf(
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = OrdemDia::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("ordem"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = ExpedienteMateria::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("expediente"),
                        onDelete = ForeignKey.CASCADE
                )
        ),
        indices = arrayOf(
            Index(value= arrayOf("materia")),
            Index(value= arrayOf("ordem")),
            Index(value= arrayOf("expediente")),
            Index(value= arrayOf("materia", "ordem", "expediente"))
        )
)
class RegistroVotacao constructor(uid: Int,
                                  numero_votos_sim: Int,
                                  numero_votos_nao: Int,
                                  numero_abstencoes: Int,
                                  observacao: String,
                                  tipo_resultado_votacao: String,
                                  materia: Int,
                                  ordem: Int,
                                  expediente: Int): Serializable, SaplEntity {


    @PrimaryKey
    var uid: Int = uid
    var numero_votos_sim = numero_votos_sim
    var numero_votos_nao = numero_votos_nao
    var numero_abstencoes = numero_abstencoes
    var observacao = observacao
    var tipo_resultado_votacao = tipo_resultado_votacao
    var materia = materia
    var ordem = ordem
    var expediente = expediente

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "sessao"
        @Ignore
        const val TABLE_NAME: String = "registrovotacao"

        override fun importJsonObject(it: JsonObject): RegistroVotacao = RegistroVotacao(
            uid = it.get("id").asInt,
            numero_votos_sim = it.get("numero_votos_sim").asInt,
            numero_votos_nao = it.get("numero_votos_nao").asInt,
            numero_abstencoes = it.get("numero_abstencoes").asInt,
            observacao =  it.get("observacao").asString,
            tipo_resultado_votacao =  it.get("tipo_resultado_votacao").asString,
            materia = it.get("materia").asInt,
            ordem = it.get("ordem").asInt,
            expediente = it.get("expediente").asInt
        )
    }
}

