package br.leg.interlegis.saplmobile.sapl.db.daos.norma

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.NormaJuridica

@Dao
interface DaoNormaJuridica: DaoBase<NormaJuridica> {

    @get:Query("SELECT * FROM "+ NormaJuridica.TABLE_NAME+" order by data asc")
    val all: LiveData<List<NormaJuridica>>

    @get:Query("SELECT * FROM "+ NormaJuridica.TABLE_NAME+" order by data asc")
    val all_direct: List<NormaJuridica>

    @Query("SELECT * FROM "+ NormaJuridica.TABLE_NAME+" WHERE uid IN (:njIds)")
    fun loadAllByIds(njIds: IntArray): List<NormaJuridica>

    @Query("SELECT * FROM "+ NormaJuridica.TABLE_NAME+" WHERE uid = :njId")
    fun getLDNormaJuridica(njId: Int): LiveData<NormaJuridica>

}