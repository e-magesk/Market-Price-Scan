package br.com.marketpricescan.model

class ProdutoNotaFiscal() {

    var id : String = ""
    var nome : String = ""
    var codigoBarras : Long = 0

    constructor(produto: ProdutoNotaFiscal) : this(){
        this.nome = produto.nome.uppercase()
        this.codigoBarras = produto.codigoBarras
        this.id = produto.id
    }

    constructor(id : String, nome : String, codigoBarras : Long) : this(){
        this.id = id
        this.nome = nome.uppercase()
        this.codigoBarras = codigoBarras
    }

}