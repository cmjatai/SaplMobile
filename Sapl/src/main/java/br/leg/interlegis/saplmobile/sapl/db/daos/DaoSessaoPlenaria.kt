package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria

@Dao
interface DaoSessaoPlenaria {

    @get:Query("SELECT * FROM sessao_plenaria")
    val all: List<SessaoPlenaria>

    @Query("SELECT * FROM sessao_plenaria WHERE uid IN (:sessaoIds)")
    fun loadAllByIds(sessaoIds: IntArray): List<SessaoPlenaria>

    @Insert
    fun insertAll(providers: List<SessaoPlenaria>)

    @Delete
    fun delete(provider: SessaoPlenaria)

}