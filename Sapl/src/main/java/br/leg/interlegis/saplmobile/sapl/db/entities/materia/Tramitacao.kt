package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = Tramitacao.TABLE_NAME,
        foreignKeys = arrayOf(
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia"),
                        onDelete = ForeignKey.CASCADE
                )
        ),
        indices = arrayOf(
                Index(value= arrayOf("materia"))
                )
)
class Tramitacao constructor(uid: Int,
                             data_tramitacao: Date,
                             materia: Int,
                             texto: String,
                             unidade_tramitacao_local: String,
                             unidade_tramitacao_destino: String,
                             status: String): Serializable, SaplEntity {

    @PrimaryKey
    var uid: Int = uid
    var materia: Int = materia
    var data_tramitacao: Date = data_tramitacao
    var texto: String = texto
    var unidade_tramitacao_local: String = unidade_tramitacao_local
    var unidade_tramitacao_destino: String = unidade_tramitacao_destino
    var status: String = status

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "materia"
        @Ignore
        const val TABLE_NAME: String = "tramitacao"


        override fun importJsonObject(it: JsonObject): Tramitacao = Tramitacao(
                uid = it.get("id").asInt,
                materia = it.get("materia").asInt,
                data_tramitacao = Converters.df.parse(it.get("data_tramitacao").asString),
                texto = it.get("texto").asString,
                unidade_tramitacao_local = it.get("unidade_tramitacao_local").asString,
                unidade_tramitacao_destino = it.get("unidade_tramitacao_destino").asString,
                status = it.get("status").asString
        )


    }
}

