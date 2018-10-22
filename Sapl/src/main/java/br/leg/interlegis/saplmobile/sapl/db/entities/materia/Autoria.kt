package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = Autoria.TABLE_NAME,
        foreignKeys = arrayOf(
                ForeignKey(entity = Autor::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("autor"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = MateriaLegislativa::class,
                        parentColumns = arrayOf("uid"),
                        childColumns = arrayOf("materia"),
                        onDelete = ForeignKey.CASCADE
                )
        ),
        indices = arrayOf(
                Index(value= arrayOf("autor")),
                Index(value= arrayOf("materia")),
                Index(value= arrayOf("autor", "materia"), unique = true))
)
class Autoria constructor(uid: Int,
                          autor: Int,
                          materia: Int,
                          primeiro_autor: Boolean): Serializable, SaplEntity {

    @PrimaryKey
    var uid: Int = uid
    var autor: Int = autor
    var materia: Int = materia
    var primeiro_autor: Boolean = primeiro_autor

    companion object: SaplEntityCompanion()  {

        @Ignore
        const val APP_LABEL: String = "materia"
        @Ignore
        const val TABLE_NAME: String = "autoria"

        override fun importJsonObject(it: JsonObject): Autoria = Autoria(
                uid = it.get("id").asInt,
                autor = if (it.get("autor").isJsonObject) it.get("autor").asJsonObject.get("id").asInt else it.get("autor").asInt ,
                materia = if (it.get("materia").isJsonObject) it.get("materia").asJsonObject.get("id").asInt else it.get("materia").asInt,
                primeiro_autor = it.get("primeiro_autor").asBoolean
        )
    }
}

