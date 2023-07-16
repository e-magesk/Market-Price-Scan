package br.com.marketpricescan.model

class Supermercado() {

    var nome : String = ""
    var id : String = ""
    var endereco : String = ""
    var cnpj : String = ""

    constructor(supermercado: Supermercado) : this(){
        this.nome = supermercado.nome.uppercase()
        this.id = supermercado.id
        this.endereco = supermercado.endereco
        this.cnpj = supermercado.cnpj
    }

    constructor(id : String, nome : String, endereco : String, cnpj : String) : this(){
        this.nome = nome.uppercase()
        this.id = id
        this.endereco = endereco
        this.cnpj = cnpj
    }
}