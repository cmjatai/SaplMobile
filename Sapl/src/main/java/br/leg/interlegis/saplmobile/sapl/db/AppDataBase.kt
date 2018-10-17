package br.leg.interlegis.saplmobile.sapl.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.daos.base.DaoAutor
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoChaveValor
import br.leg.interlegis.saplmobile.sapl.db.daos.sessao.DaoSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoTimeRefresh
import br.leg.interlegis.saplmobile.sapl.db.entities.*
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.MateriaLegislativa
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria

@Database(entities = [
    (TimeRefresh::class),
    (SessaoPlenaria::class),
    (ChaveValor::class),
    (Autor::class),
    (MateriaLegislativa::class)
], version = 45, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun DaoAutor(): DaoAutor
    abstract fun DaoChaveValor(): DaoChaveValor
    abstract fun DaoSessaoPlenaria(): DaoSessaoPlenaria
    abstract fun DaoTimeRefresh(): DaoTimeRefresh

    companion object {
        private var sInstance: AppDataBase? = null

        @Synchronized
        fun getInstance(context: Context): AppDataBase {
            if (sInstance == null) {
                sInstance = Room
                        .databaseBuilder(
                                context.applicationContext,
                                AppDataBase::class.java, "SaplMobile1.db")
                        .fallbackToDestructiveMigration()
                        .build()

            }
            return sInstance!!
        }




    }
}