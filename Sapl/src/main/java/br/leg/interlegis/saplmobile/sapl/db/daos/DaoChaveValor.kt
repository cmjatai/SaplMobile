package br.leg.interlegis.saplmobile.sapl.db.daos

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.AppDataBase
import br.leg.interlegis.saplmobile.sapl.db.entities.ChaveValor

@Dao
interface DaoChaveValor {

    @get:Query("SELECT * FROM chave_valor")
    val all: List<ChaveValor>

    @Query("SELECT * FROM chave_valor WHERE chave IN (:chaves)")
    fun loadAllByChaves(chaves: CharArray): List<ChaveValor>

    @Query("SELECT * FROM chave_valor WHERE chave = :chave")
    fun loadValue(chave: String): ChaveValor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cvs: List<ChaveValor>)

    @Delete
    fun delete(cv: ChaveValor)

    companion object {

        fun get_or_create(context: Context, chave: String, valor: String = ""): ChaveValor {
            var cv = AppDataBase.getInstance(context).DaoChaveValor().loadValue(chave)
            if (cv == null) {
                cv = ChaveValor(chave, valor)
                val list = ArrayList<ChaveValor>()
                list.add(cv)
                AppDataBase.getInstance(context).DaoChaveValor().insertAll(list)
                return cv
            }
            return cv
        }
    }

}