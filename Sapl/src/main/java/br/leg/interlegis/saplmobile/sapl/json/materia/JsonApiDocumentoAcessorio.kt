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
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiDocumentoAcessorio(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", DocumentoAcessorio.APP_LABEL, DocumentoAcessorio.TABLE_NAME)

    companion object {
        val chave = String.format("%s:%s", DocumentoAcessorio.APP_LABEL, DocumentoAcessorio.TABLE_NAME)
    }


    override fun syncList(list:Any?, deleted: IntArray?): Int {

        val listaMaterias = JsonArray()

        val mapDocumentoAcessorio = DocumentoAcessorio.importJsonArray(
                list as JsonArray) as Map<Int, DocumentoAcessorio>

        val daoDoc = AppDataBase.getInstance(context).DaoDocumentoAcessorio()
        val apagar = daoDoc.loadAllByIds(deleted as IntArray)

        daoDoc.delete(apagar)
        Utils.ManageFiles.deleteFile(context, apagar, arrayListOf("arquivo"))

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
                Utils.ManageFiles.download(context, servico, it.value.arquivo, it.value.file_date_updated)
        }

        return mapDocumentoAcessorio.size
    }
}