package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Autoria
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.DocumentoAcessorio
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.services.SaplService
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiDocumentoAcessorio(context:Context, retrofit: Retrofit?): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", DocumentoAcessorio.APP_LABEL, DocumentoAcessorio.TABLE_NAME)

    companion object {
        val chave = String.format("%s:%s", DocumentoAcessorio.APP_LABEL, DocumentoAcessorio.TABLE_NAME)
    }


    override fun syncList(list:Any?, deleted: IntArray?): Int {

        val daoDoc = AppDataBase.getInstance(context).DaoDocumentoAcessorio()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoDoc.loadAllByIds(deleted)
            SaplService.ManagerDownloadFiles.deleteFile(context, apagar, arrayListOf("arquivo"))
            daoDoc.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val listaMaterias = JsonArray()

        val mapDocumentoAcessorio = DocumentoAcessorio.importJsonArray(list) as Map<Int, DocumentoAcessorio>

        try {
            daoDoc.insertAll(ArrayList<DocumentoAcessorio>(mapDocumentoAcessorio.values))
        }
        catch (e: SQLiteConstraintException) {

            val jsonApiMateriaLegislativa = JsonApiMateriaLegislativa(context, retrofit)
            mapDocumentoAcessorio.values.forEach {

                try {
                    daoDoc.insert(it)
                }
                catch (e: SQLiteConstraintException) {
                    listaMaterias.add(jsonApiMateriaLegislativa.getObject(it.materia))
                }
            }
            jsonApiMateriaLegislativa.syncList(listaMaterias)
        }

        mapDocumentoAcessorio.forEach {
            if (it.value.arquivo.isNotEmpty())
                SaplService.downloadFileLazy(it.value.arquivo, it.value.file_date_updated)
        }

        return mapDocumentoAcessorio.size
    }
}