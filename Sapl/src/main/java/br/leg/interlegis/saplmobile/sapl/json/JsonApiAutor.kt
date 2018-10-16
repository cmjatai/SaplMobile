package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.json.interfaces.AutorRetrofitService
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.support.Log
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class JsonApiAutor: JsonApiInterface {
    companion object {

        val chave = "base:autor"
    }
    override fun sync(context: Context, retrofit: Retrofit?, kwargs:Map<String, Any>): Int {

        val servico = retrofit?.create(AutorRetrofitService::class.java)
        var response: SaplApiRestResponse? = null

        val listAutor = ArrayList<Autor>()


        while (response == null || response.pagination!!.next_page != null) {
            var dmin = if (kwargs["data_inicio"] is Date) Converters.dtf.format(kwargs["data_inicio"] as Date) else null
            var dmax = if (kwargs["data_fim"] is Date) Converters.dtf.format(kwargs["data_fim"] as Date) else null

            var tipo_update = "sync"
            if (kwargs.get("tipo_update") is String) {
                tipo_update = kwargs.get("tipo_update").toString()
            }

            val call = servico?.list(
                    format = "json",
                    page = if (response == null) 1 else response.pagination!!.next_page!!,
                    tipo_update = tipo_update,
                        // Tipo sync = filtro com base nas datas de alteração
                        // Tipo get = filtro com base nas datas da sessão plenária
                        // Tipo last_items = uma pagina só com os ultimos dados da listagem
                        // Tipo first_items = uma página só com os primeiros dados da listagem
                        // Tipo get_initial = uma página com os últimos dados do servidor
                    data_min = dmin,
                    data_max = dmax
            )

            response = call?.execute()!!.body()!!

            for (item in response.results!!) {
                val autor = Autor(
                    uid = item.get("id").asInt,
                    nome = item.get("nome").asString,
                    fotografia = item.get("fotografia").asString,
                    file_date_updated = if (item.get("file_date_updated").isJsonNull) null else Converters.df.parse(item.get("file_date_updated").asString)
                )
                listAutor.add(autor)
                Log.d("SAPL", autor.nome)
            }
        }

        val dao = AppDataBase.getInstance(context).DaoAutor()
        val apagar = dao.loadAllByIds(response.deleted!!)
        dao.insertAll(listAutor)
        dao.delete(apagar)

        return listAutor.size
    }
}