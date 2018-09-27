package br.leg.interlegis.saplmobile.sapl.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import java.util.*

@Entity(tableName = "sessao_plenaria")
class SessaoPlenaria constructor(uid: Int, data_inicio: Date) {

    @PrimaryKey
    var uid: Int = uid
    var data_inicio: Date? = null
}

