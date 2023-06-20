package br.com.marketpricescan.model

class Usuario() {

    public var nome : String = ""
    public var listas : MutableList<ListaDeCompra> = mutableListOf()

    constructor(usuario: Usuario) : this(){
        this.nome = usuario.nome
    }

    constructor(nome: String) : this(){
        this.nome = nome
    }

    fun adicionarLista(lista : ListaDeCompra){
        listas.add(lista)
    }

    fun adicionarListas(listas : MutableList<ListaDeCompra>){
        this.listas.addAll(listas)
    }

    fun getLista(index : Int) : ListaDeCompra{
        return listas[index]
    }

    fun removeLista(index : Int){
        listas.removeAt(index)
    }

    fun removeLista(lista : ListaDeCompra){
        listas.remove(lista)
    }

    fun getQuantidadeDeListas() : Int{
        return listas.size
    }
}