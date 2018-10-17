package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria

@Dao
interface DaoSessaoPlenaria: DaoBase<SessaoPlenaria> {

    @get:Query("SELECT * FROM "+SessaoPlenaria.TABLE_NAME+" order by data_inicio desc, hora_inicio desc")
    val all: LiveData<List<SessaoPlenaria>>

    @Query("SELECT * FROM "+SessaoPlenaria.TABLE_NAME+" WHERE uid IN (:sessaoIds)")
    fun loadAllByIds(sessaoIds: IntArray, table_name:String): List<SessaoPlenaria>

    @Query("SELECT * FROM "+SessaoPlenaria.TABLE_NAME+" WHERE uid = :sessaoId")
    fun getLDSessao(sessaoId: Int): LiveData<SessaoPlenaria>

}