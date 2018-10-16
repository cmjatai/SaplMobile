package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.entities.Autor

@Dao
interface DaoAutor {

    @get:Query("SELECT * FROM autor order by nome")
    val all: LiveData<List<Autor>>


    @Query("SELECT * FROM autor WHERE uid IN (:autorIds)")
    fun loadAllByIds(autorIds: IntArray): List<Autor>

    @Query("SELECT * FROM autor WHERE uid = :autorId")
    fun getLDAutor(autorId: Int): LiveData<Autor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(autores: List<Autor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(autor: Autor)

    @Update
    fun update(autor: Autor)

    @Delete
    fun delete(autores: List<Autor>)

}