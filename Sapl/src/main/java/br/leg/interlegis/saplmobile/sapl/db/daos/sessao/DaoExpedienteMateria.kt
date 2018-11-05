package br.leg.interlegis.saplmobile.sapl.db.daos.od

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.ExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.OrdemDia

@Dao
interface DaoExpedienteMateria: DaoBase<ExpedienteMateria> {

    @get:Query("SELECT * FROM "+ ExpedienteMateria.TABLE_NAME+" order by sessao_plenaria desc, numero_ordem asc")
    val all: LiveData<List<ExpedienteMateria>>

    @get:Query("SELECT * FROM "+ ExpedienteMateria.TABLE_NAME+" order by sessao_plenaria desc, numero_ordem asc")
    val all_direct: List<ExpedienteMateria>

    @Query("SELECT * FROM "+ ExpedienteMateria.TABLE_NAME+" WHERE uid IN (:ExpMatIds)")
    fun loadAllByIds(ExpMatIds: IntArray): List<ExpedienteMateria>

    @Query("SELECT * FROM "+ ExpedienteMateria.TABLE_NAME+" WHERE uid = :ExpMatId")
    fun getLDOrdemDia(ExpMatId: Int): LiveData<ExpedienteMateria>

}