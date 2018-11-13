package br.leg.interlegis.saplmobile.sapl.db.daos.materia

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria

@Dao
interface DaoAutoria: DaoBase<Autoria> {

    @get:Query("SELECT autoria.uid as uid, autor, materia, primeiro_autor FROM "+ Autoria.TABLE_NAME+" inner join autor on autoria.autor = autor.uid order by autor.nome asc")
    override val all: LiveData<List<Autoria>>

    @get:Query("SELECT autoria.uid as uid, autor, materia, primeiro_autor FROM "+ Autoria.TABLE_NAME+" inner join autor on autoria.autor = autor.uid order by autor.nome asc")
    val all_direct: List<Autoria>

    @Query("SELECT * FROM "+ Autoria.TABLE_NAME+" WHERE uid IN (:autoriaIds)")
    fun loadAllByIds(autoriaIds: IntArray): List<Autoria>

    @Query("SELECT * FROM "+ Autoria.TABLE_NAME+" WHERE uid = :autoriaId")
    fun getLDMateria(autoriaId: Int): LiveData<Autoria>

}