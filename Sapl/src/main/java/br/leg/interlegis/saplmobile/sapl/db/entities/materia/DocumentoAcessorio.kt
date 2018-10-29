package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = DocumentoAcessorio.TABLE_NAME,
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
class DocumentoAcessorio constructor(uid: Int,
                                     materia: Int,
                                     tipo: String,
                                     data: Date,
                                     nome: String,
                                     arquivo: String,
                                     autor: String,
                                     ementa: String,
                                     indexacao: String): Serializable, SaplEntity {


    @PrimaryKey
    var uid: Int = uid
    var materia: Int = materia
    var tipo: String = tipo
    var data: Date = data
    var nome: String = nome
    var arquivo: String = arquivo
    var autor: String = autor
    var ementa: String = ementa
    var indexacao: String  = indexacao

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "materia"
        @Ignore
        const val TABLE_NAME: String = "documentoacessorio"


        override fun importJsonObject(it: JsonObject): DocumentoAcessorio = DocumentoAcessorio(
                uid = it.get("id").asInt,
                materia = it.get("materia").asInt,
                tipo = it.get("data").asString,
                data = Converters.df.parse(it.get("data").asString),
                nome = it.get("data").asString,
                arquivo = it.get("arquivo").asString,
                autor = it.get("autor").asString,
                ementa = it.get("ementa").asString,
                indexacao = it.get("indexacao").asString
        )

        fun importDeJsonArray(jsonArray: JsonArray): Map<Int, SaplEntity> {
            val mapItens:HashMap<Int, SaplEntity> = HashMap()
            jsonArray.forEach {
                val i = it as JsonObject
                mapItens[i.get("id").asInt] =  DocumentoAcessorio(
                        uid = i.get("id").asInt,
                        materia = i.get("materia").asInt,
                        tipo = i.get("data").asString,
                        data = Converters.df.parse(i.get("data").asString),
                        nome = i.get("data").asString,
                        arquivo = i.get("arquivo").asString,
                        autor = i.get("autor").asString,
                        ementa = i.get("ementa").asString,
                        indexacao = i.get("indexacao").asString)

            }
            return mapItens
        }

    }
}

