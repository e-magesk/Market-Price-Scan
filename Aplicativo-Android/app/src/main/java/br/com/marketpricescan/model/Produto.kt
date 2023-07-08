package br.com.marketpricescan.model

class Produto() {

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

}