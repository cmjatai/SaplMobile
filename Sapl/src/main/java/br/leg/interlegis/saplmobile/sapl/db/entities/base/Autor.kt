package br.leg.interlegis.saplmobile.sapl.db.entities.base

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity(tableName = Autor.TABLE_NAME)
class Autor constructor(uid: Int,
                         nome: String,
                         fotografia: String,
                         file_date_updated: Date? = null): Serializable {


    @PrimaryKey
    @SerializedName("id")
    var uid: Int = uid
    var nome: String = nome
    var fotografia: String = fotografia
    var file_date_updated: Date? = file_date_updated

    companion object {
        @Ignore
        const val APP_LABEL: String = "base"
        @Ignore
        const val TABLE_NAME: String = "autor"
    }
}

