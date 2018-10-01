package br.leg.interlegis.saplmobile.sapl.db

import android.arch.persistence.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*



class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time!!.toLong()
    }

    companion object {
        val dtf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val df: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    }
}