package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.persistence.room.*

interface DaoBase<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: T)

    @Update
    fun update(item: T)

    @Delete
    fun delete(items: List<T>)
}