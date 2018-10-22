package br.leg.interlegis.saplmobile.sapl.json.materia

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.Anexada
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.json.JsonApiBaseAbstract
import br.leg.interlegis.saplmobile.sapl.support.Utils
import com.google.gson.JsonObject
import retrofit2.Retrofit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.jetbrains.anko.doAsync


class JsonApiMateriaLegislativa(context:Context, retrofit: Retrofit): JsonApiBaseAbstract(context, retrofit) {

    override val url = String.format("api/mobile/%s/%s/", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)


    companion object {
        val chave = String.format("%s:%s", MateriaLegislativa.APP_LABEL, MateriaLegislativa.TABLE_NAME)
    }

    override fun sync(kwargs:Map<String, Any>): Int {
        val result = super.get(kwargs)

        val mapMaterias:HashMap<Int, MateriaLegislativa> = HashMap()

        val mapAutores:HashMap<Int, Autor> = HashMap()

        val mapAnexada:HashMap<Int, Anexada> = HashMap()

        (result["list"] as ArrayList<JsonObject>).forEach {
            // Importar matéria
            val mat = MateriaLegislativa.importJsonObject(it)
            mapMaterias[mat.uid] = mat

            // Importar Autores -
            // TODO: criar entity Autoria e associar autores às materias.

            mapAutores.putAll(Autor.importJsonArray(it.get("autores").asJsonArray) as HashMap<Int, Autor>)

            // Importar referencias entre matérias sendo "val mat" a principal
            mapAnexada.putAll(Anexada.importAnexadasJsonArray(it.get("anexadas").asJsonArray) as HashMap<Int, Anexada>)

            // Cataloga as matérias anexadas para já adicionar caso ainda não tenha cido inserida no DB
            it.get("anexadas").asJsonArray.forEach { itAnexadaElement ->
                var itMatAnexada = itAnexadaElement as JsonObject

                val matAnexada = MateriaLegislativa.importJsonObject(
                        itMatAnexada.get("materia_anexada").asJsonObject)
                mapMaterias[matAnexada.uid] = matAnexada

                // das matérias anexadas, mapear os autores
                // TODO: associar autores às suas matérias Anexadas
                val autoresMatAnexada:HashMap<Int, Autor> = HashMap()
                autoresMatAnexada.putAll(Autor.importJsonArray(itMatAnexada.get("materia_anexada").asJsonObject.get("autores").asJsonArray) as HashMap<Int, Autor>)

                mapAutores.putAll(autoresMatAnexada)
            }

            // Importar referencias entre matérias sendo "val mat" a secundaria
            // TODO: buscar meio de isolar o processamento de uma matéria... seja ela de qual das tres fontes for
            // listagem principal, anexadas, anexo_de
            mapAnexada.putAll(Anexada.importPrincipaisDeJsonArray(it.get("anexo_de").asJsonArray) as HashMap<Int, Anexada>)

            // Cataloga as matérias anexadoras para já adicionar caso ainda não tenha cido inserida no DB
            it.get("anexo_de").asJsonArray.forEach { itPrincipalElement ->
                var itMatPrincipal = itPrincipalElement as JsonObject

                val matPrincipal = MateriaLegislativa.importJsonObject(
                        itMatPrincipal.get("materia_principal").asJsonObject)
                mapMaterias[matPrincipal.uid] = matPrincipal

                // das matérias anexadas, mapear os autores
                mapAutores.putAll(Autor.importJsonArray(itMatPrincipal.get("materia_principal").asJsonObject.get("autores").asJsonArray) as HashMap<Int, Autor>)
            }

        }

        val db = AppDataBase.getInstance(context)

        val daoMateria = db.DaoMateriaLegislativa()
        val daoAnexada = db.DaoAnexada()
        val daoAutor = db.DaoAutor()

        val apagar = daoMateria.loadAllByIds(result["deleted"] as IntArray)

        daoMateria.insertAll(ArrayList<MateriaLegislativa>(mapMaterias.values))
        daoAnexada.insertAll(ArrayList<Anexada>(mapAnexada.values))
        daoAutor.insertAll(ArrayList<Autor>(mapAutores.values))

        daoMateria.delete(apagar)

        doAsync {
            mapAutores.forEach {
                if (it.value.fotografia.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context, servico, it.value.fotografia, it.value.file_date_updated)
            }

            mapMaterias.forEach {
                if (it.value.texto_original.isNotEmpty())
                    Utils.DownloadAndWriteFiles.run(context, servico, it.value.texto_original, it.value.file_date_updated)
            }
        }


        return mapMaterias.size
    }

    fun deleteFiles( apagar: ArrayList<MateriaLegislativa>) {

    }


}