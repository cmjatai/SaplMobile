package br.leg.interlegis.saplmobile.sapl.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoChaveValor
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoSessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.daos.DaoTimeRefresh
import br.leg.interlegis.saplmobile.sapl.db.entities.ChaveValor
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria
import br.leg.interlegis.saplmobile.sapl.db.entities.TimeRefresh

@Database(entities = [(TimeRefresh::class),
                        (SessaoPlenaria::class),
                        (ChaveValor::class)], version = 40, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun DaoSessaoPlenaria(): DaoSessaoPlenaria
    abstract fun DaoChaveValor(): DaoChaveValor
    abstract fun DaoTimeRefresh(): DaoTimeRefresh

    companion object {
        private var sInstance: AppDataBase? = null

        @Synchronized
        fun getInstance(context: Context): AppDataBase {
            if (sInstance == null) {
                sInstance = Room
                        .databaseBuilder(
                                context.applicationContext,
                                AppDataBase::class.java, "SaplMobile.db")
                        .fallbackToDestructiveMigration()
                        .build()

            }
            return sInstance!!
        }




    }
}