package br.com.marketpricescan.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Classe que representa um produto.
 *
 * @property id O ID do produto.
 * @property nome O nome do produto.
 * @property preco O preço do produto.
 * @property isChecked Indica se o produto está marcado como selecionado.
 * @property codigoLocal O código local do produto.
 * @property codigoBarras O código de barras do produto.
 * @property supermercadoId O ID do supermercado ao qual o produto está associado.
 */
class Produto() : Parcelable {

    var id : String = ""
    var nome : String = ""
    var preco : Double = 0.0
    var isChecked : Boolean = false
    var codigoLocal : String = ""
    var codigoBarras : String = ""
    var supermercadoId : String = ""

    /**
     * Construtor que cria uma cópia de um produto existente.
     *
     * @param produto O produto a ser copiado.
     */
    constructor(produto: Produto) : this(){
        this.nome = produto.nome.uppercase()
        this.preco = produto.preco
        this.isChecked = produto.isChecked
        this.codigoLocal = produto.codigoLocal
        this.codigoBarras = produto.codigoBarras
        this.id = produto.id
        this.supermercadoId = produto.supermercadoId
    }

    /**
     * Construtor que recebe um objeto Parcel para desserializar o produto.
     *
     * @param parcel O objeto Parcel contendo os dados do produto.
     */
    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        nome = parcel.readString()!!
        preco = parcel.readDouble()
        isChecked = parcel.readByte() != 0.toByte()
        codigoLocal = parcel.readString()!!
        codigoBarras = parcel.readString()!!
        supermercadoId = parcel.readString()!!
    }

    /**
     * Construtor que cria um novo produto com o nome especificado.
     *
     * @param nome O nome do produto.
     */
    constructor(nome: String) : this(){
        this.nome = nome.uppercase()
        this.id = ""
        this.preco = 0.0
        this.isChecked = false
        this.codigoLocal = ""
        this.codigoBarras = ""
    }

    /**
     * Construtor que cria um novo produto com o nome e o preço especificados.
     *
     * @param nome O nome do produto.
     * @param preco O preço do produto.
     */
    constructor(nome: String, preco: Double) : this(){
        this.nome = nome.uppercase()
        this.id = ""
        this.preco = preco
        this.isChecked = false
        this.codigoLocal = ""
        this.codigoBarras = ""
    }

    /**
     * Construtor que cria um novo produto com o nome e o ID especificados.
     *
     * @param nome O nome do produto.
     * @param id O ID do produto.
     */
    constructor(nome: String, id: String) : this(){
        this.nome = nome.uppercase()
        this.id = id
        this.preco = 0.0
        this.isChecked = false
        this.codigoLocal = ""
        this.codigoBarras = ""
    }

    /**
     * Construtor que cria um novo produto com o nome, o preço e o código local especificados.
     *
     * @param nome O nome do produto.
     * @param preco O preço do produto.
     * @param codigoLocal O código local do produto.
     */
    constructor(nome: String, preco: Double, codigoLocal: String) : this(){
        this.nome = nome
        this.id = ""
        this.preco = preco
        this.isChecked = false
        this.codigoLocal = codigoLocal
        this.codigoBarras = ""
    }

    /**
     * Escreve os dados do produto no objeto Parcel fornecido.
     *
     * @param parcel O objeto Parcel no qual os dados serão escritos.
     * @param flags Flags adicionais para controlar o comportamento da escrita.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
        parcel.writeDouble(preco)
        parcel.writeByte(if (isChecked) 1 else 0)
        parcel.writeString(codigoLocal)
        parcel.writeString(codigoBarras)
        parcel.writeString(supermercadoId)
    }

    /**
     * Obtém um inteiro representando o tipo de conteúdo do objeto Parcelable.
     *
     * @return Um inteiro representando o tipo de conteúdo.
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Cria uma nova instância do produto a partir de um objeto Parcel.
     *
     * @param parcel O objeto Parcel contendo os dados do produto.
     * @return A nova instância do produto.
     */
    companion object CREATOR : Parcelable.Creator<Produto> {
        override fun createFromParcel(parcel: Parcel): Produto {
            return Produto(parcel)
        }

        /**
         * Cria um novo array do produto.
         *
         * @param size O tamanho do array.
         * @return O novo array do produto.
         */
        override fun newArray(size: Int): Array<Produto?> {
            return arrayOfNulls(size)
        }
    }

}