package br.com.marketpricescan.model

class Produto() {

    var id : String = ""
    var nome : String = ""
    var preco : Double = 0.0
    var codigoLocal : Long = 0
    var codigoBarras : Long = 0

    constructor(produto: Produto) : this(){
        this.nome = produto.nome
        this.preco = produto.preco
        this.codigoLocal = produto.codigoLocal
        this.codigoBarras = produto.codigoBarras
        this.id = produto.id
    }

    constructor(nome: String) : this(){
        this.nome = nome
        this.preco = 0.0
        this.codigoLocal = 0
        this.codigoBarras = 0
    }

    constructor(nome: String, preco: Double) : this(){
        this.nome = nome
        this.preco = preco
        this.codigoLocal = 0
        this.codigoBarras = 0
    }

}