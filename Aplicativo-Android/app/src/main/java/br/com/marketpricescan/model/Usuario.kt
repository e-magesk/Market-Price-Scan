package br.com.marketpricescan.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.text.toUpperCase
import com.google.firebase.firestore.DocumentReference


/**
 * Classe que representa um usuário.
 *
 * @property id O ID do usuário.
 * @property nome O nome do usuário.
 * @property listasDeCompra As listas de compras do usuário.
 * @property amigos Os amigos do usuário.
 */
class Usuario() :Parcelable {

    public var id : String = ""
    public var nome : String = ""
    public var listasDeCompra : MutableList<ListaDeCompra> = mutableListOf()
    public var amigos : MutableList<Usuario> = mutableListOf()

    /**
     * Construtor que recebe um objeto Parcel para desserializar o usuário.
     *
     * @param parcel O objeto Parcel contendo os dados do usuário.
     */
    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        nome = parcel.readString()!!
        parcel.readTypedList(amigos, Usuario.CREATOR)
    }

    /**
     * Construtor que cria uma cópia de um usuário existente.
     *
     * @param usuario O usuário a ser copiado.
     */
    constructor(usuario: Usuario) : this(){
        this.id = usuario.id
        this.nome = usuario.nome.uppercase()
    }

    /**
     * Construtor que cria um novo usuário com o nome especificado.
     *
     * @param nome O nome do usuário.
     */
    constructor(nome: String) : this(){
        this.nome = nome.uppercase()
    }

    /**
     * Construtor que cria um novo usuário com o nome e o ID especificados.
     *
     * @param nome O nome do usuário.
     * @param id O ID do usuário.
     */
    constructor(nome: String, id : String) : this(){
        this.id = id
        this.nome = nome.uppercase()
    }

    /**
     * Retorna uma representação em string do usuário.
     *
     * @return A representação em string do usuário.
     */
    override fun toString(): String {
        return nome
    }

    /**
     * Escreve os dados do usuário no objeto Parcel fornecido.
     *
     * @param parcel O objeto Parcel no qual os dados serão escritos.
     * @param flags Flags adicionais para controlar o comportamento da escrita.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
        parcel.writeTypedList(amigos)
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
     * Cria uma nova instância do usuário a partir de um objeto Parcel.
     *
     * @param parcel O objeto Parcel contendo os dados do usuário.
     * @return A nova instância do usuário.
     */
    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario {
            return Usuario(parcel)
        }

        /**
         * Cria um novo array do usuário.
         *
         * @param size O tamanho do array.
         * @return O novo array do usuário.
         */
        override fun newArray(size: Int): Array<Usuario?> {
            return arrayOfNulls(size)
        }
    }
}