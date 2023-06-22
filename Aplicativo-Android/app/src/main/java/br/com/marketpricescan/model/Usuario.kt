package br.com.marketpricescan.model

import com.google.firebase.firestore.DocumentReference

class Usuario() {

    public var id : String = ""
    public var nome : String = ""
    public var listasDeCompra : MutableList<ListaDeCompra> = mutableListOf()

    constructor(usuario: Usuario) : this(){
        this.nome = usuario.nome
        this.id = usuario.id
    }

    constructor(nome: String) : this(){
        this.nome = nome
    }

    constructor(nome: String, id : String) : this(){
        this.nome = nome
        this.id = id
    }
}