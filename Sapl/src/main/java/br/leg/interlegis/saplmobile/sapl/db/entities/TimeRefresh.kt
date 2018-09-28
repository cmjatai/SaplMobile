package br.leg.interlegis.saplmobile.sapl.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import java.util.*

@Entity(tableName = "time_refresh")
class TimeRefresh constructor(chave: String, data: Date) {

    @PrimaryKey
    var chave: String = chave

    @TypeConverters
    var data: Date? = data
}

