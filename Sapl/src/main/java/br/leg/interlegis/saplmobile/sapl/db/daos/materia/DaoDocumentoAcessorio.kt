package br.leg.interlegis.saplmobile.sapl.db.daos.materia

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.DocumentoAcessorio
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa

@Dao
interface DaoDocumentoAcessorio: DaoBase<DocumentoAcessorio> {

    @get:Query("SELECT * FROM "+ DocumentoAcessorio.TABLE_NAME+" order by data asc")
    override val all: LiveData<List<DocumentoAcessorio>>

    @get:Query("SELECT * FROM "+ DocumentoAcessorio.TABLE_NAME+" order by data asc")
    val all_direct: List<DocumentoAcessorio>

    @Query("SELECT * FROM "+ DocumentoAcessorio.TABLE_NAME+" WHERE uid IN (:docIds)")
    fun loadAllByIds(docIds: IntArray): List<DocumentoAcessorio>

    @Query("SELECT * FROM "+ DocumentoAcessorio.TABLE_NAME+" WHERE uid = :docId")
    fun getLDDoc(docId: Int): LiveData<DocumentoAcessorio>

}