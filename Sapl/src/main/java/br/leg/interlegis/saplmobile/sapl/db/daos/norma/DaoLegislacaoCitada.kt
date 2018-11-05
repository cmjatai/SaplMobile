package br.leg.interlegis.saplmobile.sapl.db.daos.norma

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.LegislacaoCitada
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.NormaJuridica

@Dao
interface DaoLegislacaoCitada: DaoBase<LegislacaoCitada> {

    @get:Query("SELECT * FROM "+ LegislacaoCitada.TABLE_NAME+" order by norma asc")
    val all: LiveData<List<LegislacaoCitada>>

    @get:Query("SELECT * FROM "+ LegislacaoCitada.TABLE_NAME+" order by norma asc")
    val all_direct: List<LegislacaoCitada>

    @Query("SELECT * FROM "+ LegislacaoCitada.TABLE_NAME+" WHERE uid IN (:lcIds)")
    fun loadAllByIds(lcIds: IntArray): List<LegislacaoCitada>

    @Query("SELECT * FROM "+ LegislacaoCitada.TABLE_NAME+" WHERE uid = :lcId")
    fun getLDlc(lcId: Int): LiveData<LegislacaoCitada>

}