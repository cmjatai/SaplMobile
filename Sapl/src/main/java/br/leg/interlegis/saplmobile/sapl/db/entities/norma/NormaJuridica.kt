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

@Entity(tableName = NormaJuridica.TABLE_NAME,
        indices = arrayOf(
            Index(value= arrayOf("data"))
        )
)
class NormaJuridica constructor(uid: Int,
                                numero: String,
                                ano: Int,
                                data: Date,
                                data_publicacao: Date,
                                ementa: String,
                                tipo: String): Serializable, SaplEntity {

    @PrimaryKey
    var uid = uid
    var numero = numero
    var ano = ano
    var data = data
    var data_publicacao = data_publicacao
    var ementa = ementa
    var tipo = tipo

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "norma"
        @Ignore
        const val TABLE_NAME: String = "normajuridica"


        override fun importJsonObject(it: JsonObject): NormaJuridica = NormaJuridica(
                uid = it.get("id").asInt,
                numero = it.get("numero").asString,
                ano = it.get("ano").asInt,
                data = Converters.df.parse(it.get("data").asString),
                data_publicacao = Converters.df.parse(it.get("data_publicacao").asString),
                ementa = it.get("ementa").asString,
                tipo = it.get("tipo").asString
        )
    }
}

