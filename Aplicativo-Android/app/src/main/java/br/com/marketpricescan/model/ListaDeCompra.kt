package br.com.marketpricescan.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference

/**
 * Classe que representa uma lista de compras.
 *
 * @property id O ID da lista de compras.
 * @property produtos Os produtos contidos na lista.
 * @property nome O nome da lista de compras.
 */
class ListaDeCompra() : Parcelable{

    var id : String = ""
    var produtos : MutableList<Produto> = mutableListOf()
    var nome : String = ""

    /**
     * Construtor primário que recebe um objeto Parcel para desserializar a lista de compras.
     *
     * @param parcel O objeto Parcel contendo os dados da lista de compras.
     */
    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        nome = parcel.readString()!!
        parcel.readTypedList(produtos, Produto.CREATOR)
    }

    /**
     * Construtor secundário que cria uma cópia de uma lista de compras existente.
     *
     * @param lista A lista de compras a ser copiada.
     */
    constructor(lista : ListaDeCompra) : this(){
        id = lista.id
        nome = lista.nome.uppercase()
    }

    /**
     * Construtor secundário que cria uma nova lista de compras com o nome especificado.
     *
     * @param nome O nome da lista de compras.
     */
    constructor(nome : String) : this(){
        this.nome = nome.uppercase()
    }

    /**
     * Construtor secundário que cria uma nova lista de compras com o nome e o ID especificados.
     *
     * @param nome O nome da lista de compras.
     * @param id O ID da lista de compras.
     */
    constructor(nome : String, id : String) : this(){
        this.id = id
        this.nome = nome.uppercase()
    }

    /**
     * Adiciona um produto à lista de compras.
     *
     * @param produto O produto a ser adicionado.
     */
    fun adicionarProduto(produto : Produto){
        produtos.add(produto)
    }

    /**
     * Adiciona uma lista de produtos à lista de compras.
     *
     * @param produtos A lista de produtos a ser adicionada.
     */
    fun adicionarProdutos(produtos : MutableList<Produto>){
        this.produtos.addAll(produtos)
    }

    /**
     * Obtém o produto na posição especificada da lista de compras.
     *
     * @param index A posição do produto na lista.
     * @return O produto na posição especificada.
     */
    fun getProduto(index : Int) : Produto{
        return produtos[index]
    }

    /**
     * Remove o produto na posição especificada da lista de compras.
     *
     * @param index A posição do produto a ser removido.
     */
    fun removeProduto(index : Int){
        produtos.removeAt(index)
    }

    /**
     * Escreve os dados da lista de compras no objeto Parcel fornecido.
     *
     * @param parcel O objeto Parcel no qual os dados serão escritos.
     * @param flags Flags adicionais para controlar o comportamento da escrita.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
        parcel.writeTypedList(produtos)
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
     * Cria uma nova instância da lista de compras a partir de um objeto Parcel.
     *
     * @param parcel O objeto Parcel contendo os dados da lista de compras.
     * @return A nova instância da lista de compras.
     */
    companion object CREATOR : Parcelable.Creator<ListaDeCompra> {
        override fun createFromParcel(parcel: Parcel): ListaDeCompra {
            return ListaDeCompra(parcel)
        }

        /**
         * Cria um novo array da lista de compras.
         *
         * @param size O tamanho do array.
         * @return O novo array da lista de compras.
         */
        override fun newArray(size: Int): Array<ListaDeCompra?> {
            return arrayOfNulls(size)
        }
    }
}