package br.leg.interlegis.saplmobile.sapl.db.daos.od

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.RegistroVotacao

@Dao
interface DaoRegistroVotacao: DaoBase<RegistroVotacao> {

    @get:Query("SELECT * FROM "+ RegistroVotacao.TABLE_NAME+" order by materia asc")
    val all: LiveData<List<RegistroVotacao>>

    @get:Query("SELECT * FROM "+ RegistroVotacao.TABLE_NAME+" order by materia asc")
    val all_direct: List<RegistroVotacao>

    @Query("SELECT * FROM "+ RegistroVotacao.TABLE_NAME+" WHERE uid IN (:rvIds)")
    fun loadAllByIds(rvIds: IntArray): List<RegistroVotacao>

    @Query("SELECT * FROM "+ RegistroVotacao.TABLE_NAME+" WHERE uid = :rvId")
    fun getLDRegistroVotacao(rvId: Int): LiveData<RegistroVotacao>

}