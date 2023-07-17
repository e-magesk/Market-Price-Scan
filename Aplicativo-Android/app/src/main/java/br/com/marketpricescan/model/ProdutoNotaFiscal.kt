package br.com.marketpricescan.model

/**
 * Classe que representa um produto em uma nota fiscal.
 *
 * @property id O ID do produto na nota fiscal.
 * @property nome O nome do produto na nota fiscal.
 */
class ProdutoNotaFiscal() {

    var id : String = ""
    var nome : String = ""

    /**
     * Construtor que cria uma cópia de um produto da nota fiscal existente.
     *
     * @param produto O produto da nota fiscal a ser copiado.
     */
    constructor(produto: ProdutoNotaFiscal) : this(){
        this.id = produto.id
        this.nome = produto.nome.uppercase()
    }

    /**
     * Construtor que cria um novo produto da nota fiscal com o ID e o nome especificados.
     *
     * @param id O ID do produto na nota fiscal.
     * @param nome O nome do produto na nota fiscal.
     */
    constructor(id : String, nome : String) : this(){
        this.id = id
        this.nome = nome.uppercase()
    }

}