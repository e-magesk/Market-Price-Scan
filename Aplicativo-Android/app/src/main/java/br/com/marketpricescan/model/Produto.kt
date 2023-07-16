package br.com.marketpricescan.model

import android.os.Parcel
import android.os.Parcelable

class Produto() : Parcelable {

    var id : String = ""
    var nome : String = ""
    var preco : Double = 0.0
    var isChecked : Boolean = false
    var codigoLocal : String = ""
    var codigoBarras : String = ""
    var supermercadoId : String = ""

    constructor(produto: Produto) : this(){
        this.nome = produto.nome.uppercase()
        this.preco = produto.preco
        this.isChecked = produto.isChecked
        this.codigoLocal = produto.codigoLocal
        this.codigoBarras = produto.codigoBarras
        this.id = produto.id
        this.supermercadoId = produto.supermercadoId
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        nome = parcel.readString()!!
        preco = parcel.readDouble()
        isChecked = parcel.readByte() != 0.toByte()
        codigoLocal = parcel.readString()!!
        codigoBarras = parcel.readString()!!
        supermercadoId = parcel.readString()!!
    }

    constructor(nome: String) : this(){
        this.nome = nome.uppercase()
        this.id = ""
        this.preco = 0.0
        this.isChecked = false
        this.codigoLocal = ""
        this.codigoBarras = ""
    }

    constructor(nome: String, preco: Double) : this(){
        this.nome = nome.uppercase()
        this.id = ""
        this.preco = preco
        this.isChecked = false
        this.codigoLocal = ""
        this.codigoBarras = ""
    }

    constructor(nome: String, id: String) : this(){
        this.nome = nome.uppercase()
        this.id = id
        this.preco = 0.0
        this.isChecked = false
        this.codigoLocal = ""
        this.codigoBarras = ""
    }

    constructor(nome: String, preco: Double, codigoLocal: String) : this(){
        this.nome = nome
        this.id = ""
        this.preco = preco
        this.isChecked = false
        this.codigoLocal = codigoLocal
        this.codigoBarras = ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
        parcel.writeDouble(preco)
        parcel.writeByte(if (isChecked) 1 else 0)
        parcel.writeString(codigoLocal)
        parcel.writeString(codigoBarras)
        parcel.writeString(supermercadoId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Produto> {
        override fun createFromParcel(parcel: Parcel): Produto {
            return Produto(parcel)
        }

        override fun newArray(size: Int): Array<Produto?> {
            return arrayOfNulls(size)
        }
    }

}