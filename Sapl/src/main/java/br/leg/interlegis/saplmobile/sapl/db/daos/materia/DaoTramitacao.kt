package br.leg.interlegis.saplmobile.sapl.db.daos.materia

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Tramitacao

@Dao
interface DaoTramitacao: DaoBase<Tramitacao> {

    @get:Query("SELECT * FROM "+ Tramitacao.TABLE_NAME+" order by data_tramitacao asc")
    val all: LiveData<List<Tramitacao>>

    @get:Query("SELECT * FROM "+ Tramitacao.TABLE_NAME+" order by data_tramitacao asc")
    val all_direct: List<Tramitacao>

    @Query("SELECT * FROM "+ Tramitacao.TABLE_NAME+" WHERE uid IN (:tramitacaoIds)")
    fun loadAllByIds(tramitacaoIds: IntArray): List<Tramitacao>

    @Query("SELECT * FROM "+ Tramitacao.TABLE_NAME+" WHERE uid = :tramitacaoId")
    fun getLDTramitacao(tramitacaoId: Int): LiveData<Tramitacao>

}