package br.com.marketpricescan.model

class Usuario(var nome : String = "") {

    var listas : MutableList<ListaDeCompra> = mutableListOf()
    init{
        this.nome = nome
    }
}