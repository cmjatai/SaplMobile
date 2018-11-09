package br.leg.interlegis.saplmobile.sapl.json.norma

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.DocumentoAcessorio
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.NormaJuridica
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonArray
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class JsonApiNormaJuridica(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", NormaJuridica.APP_LABEL, NormaJuridica.TABLE_NAME)

    companion object {
        val chave = String.format("%s:%s", NormaJuridica.APP_LABEL, NormaJuridica.TABLE_NAME)
    }


    override fun syncList(list:Any?, deleted: IntArray?): Int {

        val daoNj = AppDataBase.getInstance(context).DaoNormaJuridica()

        if (deleted != null && deleted.isNotEmpty()) {
            val apagar = daoNj.loadAllByIds(deleted)
            daoNj.delete(apagar)
        }

        if ((list as JsonArray).size() == 0)
            return 0

        val mapNj = NormaJuridica.importJsonArray(list) as Map<Int, NormaJuridica>
        daoNj.insertAll(ArrayList<NormaJuridica>(mapNj.values))
        return mapNj.size
    }
}