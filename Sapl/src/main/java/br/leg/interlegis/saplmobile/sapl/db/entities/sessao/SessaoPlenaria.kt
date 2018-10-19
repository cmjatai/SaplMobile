package br.leg.interlegis.saplmobile.sapl.db.entities.sessao

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.NonNull
import br.leg.interlegis.saplmobile.sapl.db.Converters
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntityCompanion
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

@Entity(tableName = SessaoPlenaria.TABLE_NAME)
class SessaoPlenaria constructor(uid: Int,
                                 legislatura: Int,
                                 sessao_legislativa: Int,
                                 tipo: String,
                                 hora_inicio: String,
                                 hora_fim: String,
                                 numero: Int,
                                 data_inicio: Date? = null,
                                 data_fim: Date? = null): Serializable, SaplEntity{


    @PrimaryKey
    var uid: Int = uid
    var legislatura: Int = legislatura
    var sessao_legislativa: Int = sessao_legislativa
    var tipo: String? = tipo
    var data_inicio: Date? = data_inicio
    var data_fim: Date? = data_fim
    var hora_inicio = hora_inicio
    var hora_fim = hora_fim
    var numero = numero

    companion object:SaplEntityCompanion() {

        @Ignore
        const val APP_LABEL: String = "sessao"
        @Ignore
        const val TABLE_NAME: String = "sessaoplenaria"

        override fun importJsonObject(item: JsonObject): SessaoPlenaria = SessaoPlenaria(
            uid = item.get("id").asInt,
            legislatura = item.get("legislatura").asInt,
            sessao_legislativa = item.get("sessao_legislativa").asInt,
            tipo = item.get("tipo").asString,
            hora_inicio = item.get("hora_inicio").asString,
            hora_fim = item.get("hora_fim").asString,
            numero = item.get("numero").asInt,
            data_inicio = if (item.get("data_inicio").isJsonNull) null else Converters.df.parse(item.get("data_inicio").asString),
            data_fim = if (item.get("data_fim").isJsonNull) Converters.df.parse(item.get("data_inicio").asString) else Converters.df.parse(item.get("data_fim").asString)
        )
    }
}

