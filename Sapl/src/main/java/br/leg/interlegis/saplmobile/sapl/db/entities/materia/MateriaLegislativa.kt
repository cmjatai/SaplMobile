package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import br.leg.interlegis.saplmobile.sapl.db.Converters
import com.google.gson.JsonObject
import java.util.*

@Entity(tableName = MateriaLegislativa.TABLE_NAME)
class MateriaLegislativa constructor(uid: Int,
                                     tipo: String,
                                     tipo_sigla: String,
                                     numero: Int,
                                     ano: Int,
                                     numero_protocolo: Int,
                                     data_apresentacao: Date,
                                     ementa: String,
                                     texto_original: String,
                                     file_date_updated: Date? = null
                                     ) {


    @PrimaryKey
    var uid: Int = uid
    var tipo: String = tipo
    var tipo_sigla: String = tipo_sigla
    var numero: Int = numero
    var ano: Int = ano
    var numero_protocolo: Int = numero_protocolo
    var data_apresentacao: Date = data_apresentacao
    var ementa: String = ementa
    var texto_original: String = texto_original
    var file_date_updated: Date? = file_date_updated

    companion object {
        @Ignore
        const val APP_LABEL: String = "materia"
        @Ignore
        const val TABLE_NAME: String = "materialegislativa"

        fun parse(it: JsonObject): MateriaLegislativa = MateriaLegislativa(
                uid = it.get("id").asInt,
                tipo = it.get("tipo").asString,
                tipo_sigla = it.get("tipo_sigla").asString,
                numero = it.get("numero").asInt,
                ano = it.get("ano").asInt,
                numero_protocolo = it.get("numero_protocolo").asInt,
                data_apresentacao = Converters.df.parse(it.get("data_apresentacao").asString),
                ementa = it.get("ementa").asString,
                texto_original = it.get("texto_original").asString,
                file_date_updated = if (it.get("file_date_updated").isJsonNull) null else Converters.dtf.parse(it.get("file_date_updated").asString)
            )

    }
}

