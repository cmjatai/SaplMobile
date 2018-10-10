package br.leg.interlegis.saplmobile.sapl.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.NonNull
import java.util.*

@Entity(tableName = "sessao_plenaria")
class SessaoPlenaria constructor(uid: Int,
                                 legislatura: String,
                                 sessao_legislativa: String,
                                 tipo: String,
                                 hora_inicio: String,
                                 hora_fim: String,
                                 numero: Int,
                                 data_inicio: Date? = null,
                                 data_fim: Date? = null) {


    @PrimaryKey
    var uid: Int = uid
    var legislatura: String = legislatura
    var sessao_legislativa: String = sessao_legislativa
    var tipo: String? = tipo
    var data_inicio: Date? = data_inicio
    var data_fim: Date? = data_fim
    var hora_inicio = hora_inicio
    var hora_fim = hora_fim
    var numero = numero
}

