package br.leg.interlegis.saplmobile.sapl.json.base

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.services.SaplService
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiAutorParlamentar(context:Context, retrofit: Retrofit?): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/parlamentar/", Autor.APP_LABEL, Autor.TABLE_NAME)

    companion object {
        val chave = "parlamentares:parlamentar"
    }

    override fun syncList(list: Any?, deleted: IntArray?): Int {

        val dao = AppDataBase.getInstance(context).DaoAutor()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = dao.loadAllByIds(deleted as IntArray)
            SaplService.ManagerDownloadFiles.deleteFile(context, apagar, arrayListOf("fotografia"))
            dao.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val listAutor = ArrayList<Autor>()

        (list).forEach {
            listAutor.add(Autor.importJsonObject(it.asJsonObject))
        }

        dao.insertAll(listAutor)

        listAutor.forEach {
            if (it.fotografia.isNotEmpty())
                SaplService.downloadFileLazy(it.fotografia, it.file_date_updated)
        }

        return listAutor.size
    }
}