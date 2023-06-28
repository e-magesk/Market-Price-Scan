package br.com.marketpricescan.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.text.toUpperCase
import com.google.firebase.firestore.DocumentReference

class Usuario() :Parcelable {

    public var id : String = ""
    public var nome : String = ""
    public var listasDeCompra : MutableList<ListaDeCompra> = mutableListOf()
    public var amigos : MutableList<Usuario> = mutableListOf()

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        nome = parcel.readString()!!
    }

    constructor(usuario: Usuario) : this(){
        this.id = usuario.id
        this.nome = usuario.nome.uppercase()
    }

    constructor(nome: String) : this(){
        this.nome = nome.uppercase()
    }

    constructor(nome: String, id : String) : this(){
        this.id = id
        this.nome = nome.uppercase()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nome)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario {
            return Usuario(parcel)
        }

        override fun newArray(size: Int): Array<Usuario?> {
            return arrayOfNulls(size)
        }
    }
}