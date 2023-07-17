package br.com.marketpricescan.model

/**
 * Classe que representa um supermercado.
 *
 * @property nome O nome do supermercado.
 * @property id O ID do supermercado.
 * @property endereco O endereço do supermercado.
 * @property cnpj O CNPJ do supermercado.
 */
class Supermercado() {

    var nome : String = ""
    var id : String = ""
    var endereco : String = ""
    var cnpj : String = ""

    /**
     * Construtor que cria uma cópia de um supermercado existente.
     *
     * @param supermercado O supermercado a ser copiado.
     */
    constructor(supermercado: Supermercado) : this(){
        this.nome = supermercado.nome.uppercase()
        this.id = supermercado.id
        this.endereco = supermercado.endereco
        this.cnpj = supermercado.cnpj
    }

    /**
     * Construtor que cria um novo supermercado com o ID, nome, endereço e CNPJ especificados.
     *
     * @param id O ID do supermercado.
     * @param nome O nome do supermercado.
     * @param endereco O endereço do supermercado.
     * @param cnpj O CNPJ do supermercado.
     */
    constructor(id : String, nome : String, endereco : String, cnpj : String) : this(){
        this.nome = nome.uppercase()
        this.id = id
        this.endereco = endereco
        this.cnpj = cnpj
    }
}