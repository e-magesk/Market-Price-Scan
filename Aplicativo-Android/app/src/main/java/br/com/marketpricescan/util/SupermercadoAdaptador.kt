package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.Supermercado
import br.com.marketpricescan.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class SupermercadoAdaptador(private val context : Context, private val supermercados: MutableList<Supermercado>, private val listaDeCompraReferencia : ListaDeCompra) : RecyclerView.Adapter<SupermercadoAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.d("Teste", "Chegou aqui no on create view holder")
        val itemView = LayoutInflater.from(context).inflate(R.layout.supermercado_adaptador, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val supermercado = supermercados[position]
        holder.bind(supermercado)
    }

    override fun getItemCount(): Int {
        return supermercados.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeSupermercado: TextView = itemView.findViewById(R.id.tvNomeSupermercado)
        private val rvProdutosSupermercado : RecyclerView = itemView.findViewById(R.id.rvProdutosSupermercado)
        lateinit var produtos : MutableList<Produto>

        init{
            BuscarProdutos()
        }

        fun bind(supermercado : Supermercado) {
            tvNomeSupermercado.setText(supermercado.nome)
        }

        private fun DefinirAdaptador(){
            var adaptadorProdutosSupermercado = ProdutoAdaptador(context, produtos)
            rvProdutosSupermercado.setHasFixedSize(true)
            rvProdutosSupermercado.layoutManager = LinearLayoutManager(context)
            rvProdutosSupermercado.adapter = adaptadorProdutosSupermercado
            rvProdutosSupermercado.isClickable = true
        }

        private fun BuscarProdutos(){
            produtos = mutableListOf()
            var database = FirebaseFirestore.getInstance()
            for(produto in listaDeCompraReferencia.produtos){
                if(!produto.codigoLocal.equals("")){
                    val query = database.collection("produto")
                        .whereEqualTo("supermercadoId", supermercados[bindingAdapterPosition].id)
                        .whereEqualTo("codigoLocal", produto.codigoLocal)

                    // ATUALIZANDO PRODUTOS
                    query.get()
                        .addOnSuccessListener { querySnapshot ->
                            Log.d("Teste", "QuerySnapshot " + querySnapshot.size())
                            if(querySnapshot.size() > 0){
                                var produto = Produto(querySnapshot.documents[0].toObject(Produto::class.java)!!)
                                produtos.add(produto)
                                notifyDataSetChanged()
                            }
                        }
                }
            }
            DefinirAdaptador()
        }
    }
}