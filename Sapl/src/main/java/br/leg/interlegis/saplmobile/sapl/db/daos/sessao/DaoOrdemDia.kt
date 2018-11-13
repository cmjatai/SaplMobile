package br.leg.interlegis.saplmobile.sapl.db.daos.od

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.OrdemDia

@Dao
interface DaoOrdemDia: DaoBase<OrdemDia> {

    @get:Query("SELECT * FROM "+ OrdemDia.TABLE_NAME+" order by sessao_plenaria desc, numero_ordem asc")
    override val all: LiveData<List<OrdemDia>>

    @get:Query("SELECT * FROM "+ OrdemDia.TABLE_NAME+" order by sessao_plenaria desc, numero_ordem asc")
    val all_direct: List<OrdemDia>

    @Query("SELECT * FROM "+ OrdemDia.TABLE_NAME+" where sessao_plenaria = :sessaoId order by sessao_plenaria desc, numero_ordem asc")
    fun all_by_sessao(sessaoId: Int): LiveData<List<OrdemDia>>

    @Query("SELECT * FROM "+ OrdemDia.TABLE_NAME+" WHERE uid IN (:odIds)")
    fun loadAllByIds(odIds: IntArray): List<OrdemDia>

    @Query("SELECT * FROM "+ OrdemDia.TABLE_NAME+" WHERE uid = :odId")
    fun getLDOrdemDia(odId: Int): LiveData<OrdemDia>

    @Query("SELECT 1 FROM "+ OrdemDia.TABLE_NAME+" WHERE uid = :odId")
    fun exists(odId: Int): Boolean

}