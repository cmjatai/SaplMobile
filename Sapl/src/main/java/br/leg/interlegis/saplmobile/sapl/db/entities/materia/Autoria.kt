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

@Entity(tableName = Anexada.TABLE_NAME,
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
                Index(value= arrayOf("autor", "materia"), unique = true))
)
class Autoria constructor(uid: Int,
                          autor: Int,
                          materia: Int): Serializable, SaplEntity {

    @PrimaryKey
    var uid: Int = uid
    var autor: Int = autor
    var materia: Int = materia

    companion object: SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "materia"
        @Ignore
        const val TABLE_NAME: String = "autoria"


        override fun importJsonObject(it: JsonObject): Autoria = Autoria(
                uid = it.get("id").asInt,
                autor = it.get("autor").asInt,
                materia = it.get("materia").asInt
        )

    }
}

