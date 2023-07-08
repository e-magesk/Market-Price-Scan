package br.com.marketpricescan.model

class ProdutoNotaFiscal() {

    var id : String = ""
    var nome : String = ""

    constructor(produto: ProdutoNotaFiscal) : this(){
        this.id = produto.id
        this.nome = produto.nome.uppercase()
    }

    constructor(id : String, nome : String) : this(){
        this.id = id
        this.nome = nome.uppercase()
    }

}