package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria

@Dao
interface DaoSessaoPlenaria {

    @get:Query("SELECT * FROM sessao_plenaria order by data_inicio desc, hora_inicio desc")
    val all: LiveData<List<SessaoPlenaria>>

    @get:Query("SELECT * FROM sessao_plenaria order by data_inicio desc, hora_inicio desc")
    val all_test: List<SessaoPlenaria>

    @Query("SELECT * FROM sessao_plenaria WHERE uid IN (:sessaoIds)")
    fun loadAllByIds(sessaoIds: IntArray): List<SessaoPlenaria>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(sessoes: List<SessaoPlenaria>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sessao: SessaoPlenaria)

    @Update
    fun update(sessao: SessaoPlenaria)

    @Delete
    fun delete(sessoes: List<SessaoPlenaria>)

}