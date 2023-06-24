package br.com.marketpricescan.model

class Produto() {

    var id : String = ""
    var nome : String = ""
    var preco : Double = 0.0
    var isChecked : Boolean = false
    var codigoLocal : Long = 0
    var codigoBarras : Long = 0

    constructor(produto: Produto) : this(){
        this.nome = produto.nome
        this.preco = produto.preco
        this.isChecked = produto.isChecked
        this.codigoLocal = produto.codigoLocal
        this.codigoBarras = produto.codigoBarras
        this.id = produto.id
    }

    constructor(nome: String) : this(){
        this.nome = nome
        this.id = ""
        this.preco = 0.0
        this.isChecked = false
        this.codigoLocal = 0
        this.codigoBarras = 0
    }

    constructor(nome: String, preco: Double) : this(){
        this.nome = nome
        this.id = ""
        this.preco = preco
        this.isChecked = false
        this.codigoLocal = 0
        this.codigoBarras = 0
    }

    constructor(nome: String, id: String) : this(){
        this.nome = nome
        this.id = id
        this.preco = 0.0
        this.isChecked = false
        this.codigoLocal = 0
        this.codigoBarras = 0
    }

}