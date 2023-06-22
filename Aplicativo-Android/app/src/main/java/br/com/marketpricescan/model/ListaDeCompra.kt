package br.com.marketpricescan.model

import com.google.firebase.firestore.DocumentReference

class ListaDeCompra() {

    var id : String = ""
    var produtos : MutableList<Produto> = mutableListOf()
    var nome : String = ""


    constructor(lista : ListaDeCompra) : this(){
        nome = lista.nome
        id = lista.id
    }

    constructor(nome : String) : this(){
        this.nome = nome
    }

    constructor(nome : String, id : String) : this(){
        this.nome = nome
        this.id = id
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
}