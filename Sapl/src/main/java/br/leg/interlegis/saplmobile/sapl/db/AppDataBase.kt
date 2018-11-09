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
import br.leg.interlegis.saplmobile.sapl.db.daos.materia.*
import br.leg.interlegis.saplmobile.sapl.db.daos.norma.DaoLegislacaoCitada
import br.leg.interlegis.saplmobile.sapl.db.daos.norma.DaoNormaJuridica
import br.leg.interlegis.saplmobile.sapl.db.daos.od.DaoExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.daos.od.DaoOrdemDia
import br.leg.interlegis.saplmobile.sapl.db.daos.od.DaoRegistroVotacao
import br.leg.interlegis.saplmobile.sapl.db.entities.*
import br.leg.interlegis.saplmobile.sapl.db.entities.base.Autor
import br.leg.interlegis.saplmobile.sapl.db.entities.materia.*
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.LegislacaoCitada
import br.leg.interlegis.saplmobile.sapl.db.entities.norma.NormaJuridica
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.ExpedienteMateria
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.OrdemDia
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.RegistroVotacao
import br.leg.interlegis.saplmobile.sapl.db.entities.sessao.SessaoPlenaria

@Database(entities = [
    (TimeRefresh::class),
    (SessaoPlenaria::class),
    (ChaveValor::class),
    (Autor::class),
    (MateriaLegislativa::class),
    (Anexada::class),
    (Autoria::class),
    (DocumentoAcessorio::class),
    (Tramitacao::class),
    (NormaJuridica::class),
    (LegislacaoCitada::class),
    (ExpedienteMateria::class),
    (OrdemDia::class),
    (RegistroVotacao::class)
], version = 67, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun DaoAnexada(): DaoAnexada
    abstract fun DaoAutor(): DaoAutor
    abstract fun DaoAutoria(): DaoAutoria
    abstract fun DaoChaveValor(): DaoChaveValor
    abstract fun DaoSessaoPlenaria(): DaoSessaoPlenaria
    abstract fun DaoTimeRefresh(): DaoTimeRefresh
    abstract fun DaoMateriaLegislativa(): DaoMateriaLegislativa
    abstract fun DaoDocumentoAcessorio(): DaoDocumentoAcessorio
    abstract fun DaoTramitacao(): DaoTramitacao
    abstract fun DaoNormaJuridica(): DaoNormaJuridica
    abstract fun DaoLegislacaoCitada(): DaoLegislacaoCitada
    abstract fun DaoExpedienteMateria(): DaoExpedienteMateria
    abstract fun DaoOrdemDia(): DaoOrdemDia
    abstract fun DaoRegistroVotacao(): DaoRegistroVotacao

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