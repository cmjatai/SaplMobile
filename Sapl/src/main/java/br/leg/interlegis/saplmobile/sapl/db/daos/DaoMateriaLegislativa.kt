package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.entities.MateriaLegislativa

@Dao
interface DaoMateriaLegislativa: DaoBase<MateriaLegislativa> {

    @get:Query("SELECT * FROM "+MateriaLegislativa.TABLE_NAME+" order by data_apresentacao desc")
    val all: LiveData<List<MateriaLegislativa>>

    @Query("SELECT * FROM "+MateriaLegislativa.TABLE_NAME+" WHERE uid IN (:materiaIds)")
    fun loadAllByIds(materiaIds: IntArray): List<MateriaLegislativa>

    @Query("SELECT * FROM "+MateriaLegislativa.TABLE_NAME+" WHERE uid = :materiaId")
    fun getLDAutor(materiaId: Int): LiveData<MateriaLegislativa>
}