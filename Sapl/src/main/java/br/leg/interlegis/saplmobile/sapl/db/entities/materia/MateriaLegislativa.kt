package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
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
        )
)
class Anexada constructor(uid: Int,
                          materia_principal: Int,
                          materia_anexada: Int,
                          data_anexacao: Date,
                          data_desanexacao: Date? = null
                                     ): Serializable, SaplEntity {


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


        override fun importJsonObject(it: JsonObject): MateriaLegislativa = MateriaLegislativa(
            uid = it.get("id").asInt,
            tipo = it.get("tipo").asString,
            tipo_sigla = it.get("tipo_sigla").asString,
            numero = it.get("numero").asInt,
            ano = it.get("ano").asInt,
            numero_protocolo = if (it.get("numero_protocolo").isJsonNull) 0 else it.get("numero_protocolo").asInt,
            data_apresentacao = Converters.df.parse(it.get("data_apresentacao").asString),
            ementa = it.get("ementa").asString,
            texto_original = it.get("texto_original").asString,
            file_date_updated = if (it.get("file_date_updated").isJsonNull) null else Converters.dtf.parse(it.get("file_date_updated").asString)
        )
    }
}

