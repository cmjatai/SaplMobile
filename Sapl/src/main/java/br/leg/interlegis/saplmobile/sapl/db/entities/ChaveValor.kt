package br.leg.interlegis.saplmobile.sapl.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "chave_valor")
class ChaveValor constructor(chave: String, valor: String) {

    @PrimaryKey
    @ColumnInfo(name = "chave")
    var chave: String = chave

    @ColumnInfo(name = "valor")
    var valor: String = valor

}