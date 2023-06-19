package br.com.marketpricescan.model

class Produto(
    var nome : String,
    var preco : Double) {

    var codigoLocal : Long
    var codigoBarras : Long

    init{
        this.nome = nome
        this.preco = preco
        this.codigoLocal = 0
        this.codigoBarras = 0
    }
}