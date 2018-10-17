package br.leg.interlegis.saplmobile.sapl.db.entities.materia

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = MateriaLegislativa.TABLE_NAME)
class MateriaLegislativa constructor(uid: Int,
                                     tipo: String,
                                     tipo_sigla: String,
                                     numero: Int,
                                     ano: Int,
                                     numero_protocolo: String,
                                     data_apresentacao: Date,
                                     ementa: String,
                                     texto_original: String,
                                     file_date_updated: Date?
                                     ) {


    @PrimaryKey
    var uid: Int = uid
    var tipo: String = tipo
    var tipo_sigla: String = tipo_sigla
    var numero: Int = numero
    var ano: Int = ano
    var numero_protocolo: String = numero_protocolo
    var data_apresentacao: Date = data_apresentacao
    var ementa: String = ementa
    var texto_original: String = texto_original
    var file_date_updated: Date? = file_date_updated

    companion object {
        @Ignore
        const val TABLE_NAME: String = "materialegislativa"
    }
}

