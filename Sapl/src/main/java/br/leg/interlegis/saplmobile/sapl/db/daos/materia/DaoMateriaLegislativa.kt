package br.leg.interlegis.saplmobile.sapl.db.daos.materia

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa

@Dao
interface DaoMateriaLegislativa: DaoBase<MateriaLegislativa> {

    @get:Query("SELECT * FROM "+ MateriaLegislativa.TABLE_NAME+" order by data_apresentacao desc")
    override val all: LiveData<List<MateriaLegislativa>>

    @get:Query("SELECT * FROM "+ MateriaLegislativa.TABLE_NAME+" order by data_apresentacao desc")
    val all_direct: List<MateriaLegislativa>

    @Query("SELECT * FROM "+ MateriaLegislativa.TABLE_NAME+" WHERE uid IN (:materiaIds)")
    fun loadAllByIds(materiaIds: IntArray): List<MateriaLegislativa>

    @Query("SELECT * FROM "+ MateriaLegislativa.TABLE_NAME+" WHERE uid = :materiaId")
    fun getLDMateria(materiaId: Int): LiveData<MateriaLegislativa>

    @Query("SELECT * FROM "+ MateriaLegislativa.TABLE_NAME+" WHERE uid = :materiaId")
    fun getMateria(materiaId: Int): MateriaLegislativa

    @Query("SELECT 1 FROM "+ MateriaLegislativa.TABLE_NAME+" WHERE uid = :matId")
    fun exists(matId: Int): Boolean

}