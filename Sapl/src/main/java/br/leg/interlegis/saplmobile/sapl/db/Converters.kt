package br.leg.interlegis.saplmobile.sapl.db

import android.arch.persistence.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return (if (date == null) null else date!!.getTime())!!.toLong()
    }
}