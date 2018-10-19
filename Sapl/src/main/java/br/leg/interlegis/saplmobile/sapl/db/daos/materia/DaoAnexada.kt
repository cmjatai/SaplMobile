package br.leg.interlegis.saplmobile.sapl.db.daos.materia

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa

@Dao
interface DaoAnexada: DaoBase<Anexada> {

    @get:Query("SELECT * FROM "+ Anexada.TABLE_NAME+" order by data_anexacao asc")
    val all: LiveData<List<MateriaLegislativa>>

    @get:Query("SELECT * FROM "+ Anexada.TABLE_NAME+" order by data_anexacao asc")
    val all_direct: List<MateriaLegislativa>

    @Query("SELECT * FROM "+ Anexada.TABLE_NAME+" WHERE uid IN (:anexadaIds)")
    fun loadAllByIds(anexadaIds: IntArray): List<Anexada>

    @Query("SELECT * FROM "+ Anexada.TABLE_NAME+" WHERE uid = :anexadaId")
    fun getLDMateria(anexadaId: Int): LiveData<Anexada>

}