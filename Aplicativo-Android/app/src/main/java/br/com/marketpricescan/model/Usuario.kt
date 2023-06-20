package br.com.marketpricescan.model

class Usuario() {

    public var nome : String = ""
    public var listas : MutableList<ListaDeCompra> = mutableListOf()

    constructor(usuario: Usuario) : this(){
        this.nome = usuario.nome
        this.listas = usuario.listas
    }

    constructor(nome: String) : this(){
        this.nome = nome
    }
}