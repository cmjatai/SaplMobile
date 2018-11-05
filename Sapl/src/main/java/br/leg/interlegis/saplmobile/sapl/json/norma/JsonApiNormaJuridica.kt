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

        val mapNj = NormaJuridica.importJsonArray(
                list as JsonArray) as Map<Int, NormaJuridica>

        val daoNj = AppDataBase.getInstance(context).DaoNormaJuridica()
        val apagar = daoNj.loadAllByIds(deleted as IntArray)

        daoNj.delete(apagar)
        daoNj.insertAll(ArrayList<NormaJuridica>(mapNj.values))

        return mapNj.size
    }
}