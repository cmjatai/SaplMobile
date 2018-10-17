package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.entities.ChaveValor
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh

@Dao
interface DaoTimeRefresh {

    @get:Query("SELECT * FROM time_refresh")
    val all: List<TimeRefresh>

    @Query("SELECT * FROM time_refresh WHERE chave IN (:chave)")
    fun loadAllByIds(chave: List<String>): List<TimeRefresh>

    @Insert
    fun insertAll(providers: List<TimeRefresh>)

    @Insert
    fun insert(time: TimeRefresh)

    @Update
    fun update(time: TimeRefresh)

    @Query("SELECT * FROM time_refresh WHERE chave = :chave")
    fun loadValue(chave: String): TimeRefresh?

    @Query("SELECT * FROM time_refresh ORDER BY data desc limit 1")
    fun maxValue(): TimeRefresh?

    @Delete
    fun delete(time: TimeRefresh)

    @Delete
    fun delete(items: List<TimeRefresh>)

}