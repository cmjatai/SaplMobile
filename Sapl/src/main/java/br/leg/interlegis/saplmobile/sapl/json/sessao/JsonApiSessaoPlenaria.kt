package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import com.google.gson.JsonArray
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiSessaoPlenaria(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {


    override val url = String.format("api/mobile/%s/%s/", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)

    companion object {

        val chave = String.format("%s:%s", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)
    }


    override fun syncList(list: Any?, deleted: IntArray?): Int {
        val listaSessao = ArrayList<SessaoPlenaria>()

        (list as JsonArray).forEach {
            listaSessao.add(SessaoPlenaria.importJsonObject(it.asJsonObject))
        }

        val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()
        val apagar = dao.loadAllByIds(deleted as IntArray)
        dao.insertAll(listaSessao)
        dao.delete(apagar)

        return listaSessao.size
    }
}