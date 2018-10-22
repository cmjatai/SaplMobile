package br.leg.interlegis.saplmobile.sapl.json.base

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiAutorParlamentar(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/parlamentar/", Autor.APP_LABEL, Autor.TABLE_NAME)

    companion object {
        val chave = "parlamentares:parlamentar"
    }


    override fun sync(kwargs:Map<String, Any>): Int {
        val result = super.getList(kwargs)

        val listAutor = ArrayList<Autor>()

        (result["list"] as JsonArray).forEach {
            listAutor.add(Autor.importJsonObject(it.asJsonObject))
        }

        val dao = AppDataBase.getInstance(context).DaoAutor()
        val apagar = dao.loadAllByIds(result["deleted"] as IntArray)
        dao.insertAll(listAutor)
        dao.delete(apagar)

        doAsync {
            listAutor.forEach {
                if (it.fotografia.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context, servico, it.fotografia, it.file_date_updated)
            }
        }

        return listAutor.size
    }
}