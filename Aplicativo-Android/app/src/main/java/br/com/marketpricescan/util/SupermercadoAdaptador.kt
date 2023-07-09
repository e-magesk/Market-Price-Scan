package br.com.marketpricescan.util

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.Supermercado
import com.google.firebase.firestore.FirebaseFirestore

class SupermercadoAdaptador(private val context : Context, private val supermercados: MutableList<Supermercado>, private val listaDeCompraReferencia : ListaDeCompra) : RecyclerView.Adapter<SupermercadoAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.supermercado_adaptador, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val supermercado = supermercados[position]
        holder.bind(supermercado, position)
    }

    override fun getItemCount(): Int {
        return supermercados.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeSupermercado: TextView = itemView.findViewById(R.id.tvNomeSupermercado)
        private val rvProdutosSupermercado : RecyclerView = itemView.findViewById(R.id.rvProdutosSupermercado)
        private val tvPrecoTotal : TextView = itemView.findViewById(R.id.tvPrecoTotal)
        private val cvSupermercado : CardView = itemView.findViewById(R.id.cvSupermercado)
        lateinit var produtos : MutableList<Produto>
        lateinit var adaptadorProdutosSupermercado : ProdutoNotaFiscalAdaptador

        fun bind(supermercado : Supermercado, position : Int) {
            tvNomeSupermercado.setText(supermercado.nome)
            BuscarProdutos(position)
        }

        private fun DefinirAdaptador(){
            adaptadorProdutosSupermercado = ProdutoNotaFiscalAdaptador(context, produtos)
            rvProdutosSupermercado.setHasFixedSize(true)
            rvProdutosSupermercado.layoutManager = LinearLayoutManager(context)
            rvProdutosSupermercado.adapter = adaptadorProdutosSupermercado
            rvProdutosSupermercado.isClickable = true
        }

        private fun BuscarProdutos(posicao : Int){
            produtos = mutableListOf()
            var produtosAvaliados : Int = 0
            var database = FirebaseFirestore.getInstance()
            try {
                for (produto in listaDeCompraReferencia.produtos) {
                    Log.d(
                        "Teste",
                        "Codigo local " + produto.codigoLocal + " supermercado id " + produto.supermercadoId
                    )
                    if (!produto.codigoLocal.equals("")) {
                        Log.d("Teste", "Chegou aqui na query " + posicao)
                        val query = database.collection("produto")
                            .whereEqualTo("supermercadoId", supermercados[posicao].id)
                            .whereEqualTo("codigoLocal", produto.codigoLocal)
                        //ATUALIZANDO PRODUTOS
                        query.get()
                            .addOnSuccessListener { querySnapshot ->
                                Log.d("Teste", "QuerySnapshot " + querySnapshot.size())
                                if (querySnapshot.size() > 0) {
                                    var produto =
                                        Produto(querySnapshot.documents[0].toObject(Produto::class.java)!!)
                                    produtos.add(produto)
                                    RecalculaPrecoTotal()
                                    adaptadorProdutosSupermercado.notifyDataSetChanged()
                                }
                                produtosAvaliados = produtosAvaliados + 1
                                if (produtosAvaliados == listaDeCompraReferencia.produtos.size && produtos.size == 0) {
                                    cvSupermercado.visibility = View.GONE
                                }
                            }
                            .addOnFailureListener { exception ->
                                produtosAvaliados = produtosAvaliados + 1
                                if (produtosAvaliados == listaDeCompraReferencia.produtos.size && produtos.size == 0) {
                                    cvSupermercado.visibility = View.GONE
                                }
                                Log.d("Teste", "Erro ao buscar produtos " + exception.message)
                            }
                    }
                    else{
                        produtosAvaliados = produtosAvaliados + 1
                        if (produtosAvaliados == listaDeCompraReferencia.produtos.size && produtos.size == 0) {
                            cvSupermercado.visibility = View.GONE
                        }
                    }
                }
            }
            catch (e : Exception){
                Log.d("Teste", "Erro ao buscar produtos " + e.message)
            }
            DefinirAdaptador()
        }

        private fun RecalculaPrecoTotal(){
            var valorTotal = 0.0
            for(produto in produtos){
                valorTotal += produto.preco
            }
            tvPrecoTotal.setText("R$ " + valorTotal.toString())
        }
    }
}