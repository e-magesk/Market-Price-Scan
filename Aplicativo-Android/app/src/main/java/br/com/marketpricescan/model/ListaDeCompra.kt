package br.com.marketpricescan.model

class ListaDeCompra() {

    private var produtos : MutableList<Produto> = mutableListOf()
    var name : String = ""

    constructor(lista : ListaDeCompra) : this(){
        produtos = lista.getProdutos()
        name = lista.name
    }

    constructor(name : String) : this(){
        this.name = name
    }

    fun adicionarProduto(produto : Produto){
        produtos.add(produto)
    }

    fun adicionarProdutos(produtos : MutableList<Produto>){
        this.produtos.addAll(produtos)
    }

    fun getProdutos() : MutableList<Produto>{
        return produtos
    }

    fun getProduto(index : Int) : Produto{
        return produtos[index]
    }

    fun removeProduto(index : Int){
        produtos.removeAt(index)
    }

    fun removeProduto(produto : Produto){
        produtos.remove(produto)
    }

    fun getQuantidadeDeProdutos() : Int{
        return produtos.size
    }

    fun getValorTotal() : Double{
        var valorTotal : Double = 0.0
        for(produto in produtos){
            valorTotal += produto.preco
        }
        return valorTotal
    }


}