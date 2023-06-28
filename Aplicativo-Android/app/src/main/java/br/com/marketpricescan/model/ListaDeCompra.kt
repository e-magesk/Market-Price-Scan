package br.com.marketpricescan.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference

class ListaDeCompra() : Parcelable{

    var id : String = ""
    var produtos : MutableList<Produto> = mutableListOf()
    var nome : String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        nome = parcel.readString()!!
    }


    constructor(lista : ListaDeCompra) : this(){
        id = lista.id
        nome = lista.nome.uppercase()
    }

    constructor(nome : String) : this(){
        this.nome = nome.uppercase()
    }

    constructor(nome : String, id : String) : this(){
        this.id = id
        this.nome = nome.uppercase()
    }
    fun adicionarProduto(produto : Produto){
        produtos.add(produto)
    }

    fun adicionarProdutos(produtos : MutableList<Produto>){
        this.produtos.addAll(produtos)
    }

    fun getProduto(index : Int) : Produto{
        return produtos[index]
    }

    fun removeProduto(index : Int){
        produtos.removeAt(index)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ListaDeCompra> {
        override fun createFromParcel(parcel: Parcel): ListaDeCompra {
            return ListaDeCompra(parcel)
        }

        override fun newArray(size: Int): Array<ListaDeCompra?> {
            return arrayOfNulls(size)
        }
    }
}