package br.leg.interlegis.saplmobile.sapl.db.daos.materia

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria

@Dao
interface DaoAutoria: DaoBase<Autoria> {

    @get:Query("SELECT * FROM "+ Autoria.TABLE_NAME+" order by autor.nome asc")
    val all: LiveData<List<Autoria>>

    @get:Query("SELECT * FROM "+ Autoria.TABLE_NAME+" order by autor.nome asc")
    val all_direct: List<Autoria>

    @Query("SELECT * FROM "+ Autoria.TABLE_NAME+" WHERE uid IN (:autoriaIds)")
    fun loadAllByIds(autoriaIds: IntArray): List<Autoria>

    @Query("SELECT * FROM "+ Autoria.TABLE_NAME+" WHERE uid = :autoriaId")
    fun getLDMateria(autoriaId: Int): LiveData<Autoria>

}