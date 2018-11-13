package br.leg.interlegis.saplmobile.sapl.json.sessao

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.settings.SettingsActivity
import com.google.gson.JsonArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList

class JsonApiSessaoPlenaria(context:Context, retrofit: Retrofit?): JsonApiBaseAbstract(context, retrofit) {


    override val url = String.format("api/mobile/%s/%s/", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)

    companion object {

        val chave = String.format("%s:%s", SessaoPlenaria.APP_LABEL, SessaoPlenaria.TABLE_NAME)
    }


    override fun syncList(list: Any?, deleted: IntArray?): Int {

        val dao = AppDataBase.getInstance(context).DaoSessaoPlenaria()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = dao.loadAllByIds(deleted)
            dao.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val listaSessao = ArrayList<SessaoPlenaria>()

        list.forEach {
            listaSessao.add(SessaoPlenaria.importJsonObject(it.asJsonObject))
        }

        dao.insertAll(listaSessao)
        return listaSessao.size
    }

}