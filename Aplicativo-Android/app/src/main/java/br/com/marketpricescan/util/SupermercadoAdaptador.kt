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

/**
 * Adaptador para exibição de supermercados em uma lista.
 *
 * @param context O contexto da aplicação.
 * @param supermercados A lista de supermercados a ser exibida.
 * @param listaDeCompraReferencia A lista de compra de referência para buscar os produtos.
 */
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

        /**
         * Atualiza a visualização do ItemViewHolder com base nos dados do supermercado fornecido.
         *
         * @param supermercado O supermercado a ser exibido.
         * @param position A posição do item na lista de supermercados.
         */
        fun bind(supermercado : Supermercado, position : Int) {
            tvNomeSupermercado.setText(supermercado.nome)
            BuscarProdutos(position)
        }

        /**
         * Define o adaptador do RecyclerView e suas configurações.
         */
        private fun DefinirAdaptador(){
            adaptadorProdutosSupermercado = ProdutoNotaFiscalAdaptador(context, produtos)
            rvProdutosSupermercado.setHasFixedSize(true)
            rvProdutosSupermercado.layoutManager = LinearLayoutManager(context)
            rvProdutosSupermercado.adapter = adaptadorProdutosSupermercado
            rvProdutosSupermercado.isClickable = true
        }

        /**
         * Busca os produtos do supermercado na lista de compra de referência.
         *
         * @param posicao A posição do supermercado na lista de supermercados.
         */
        private fun BuscarProdutos(posicao : Int){
            produtos = mutableListOf()
            // Contador para controlar a quantidade de produtos avaliados
            var produtosAvaliados : Int = 0
            // Obtém uma instância do FirebaseFirestore
            var database = FirebaseFirestore.getInstance()
            try {
                // Percorre a lista de produtos da lista de compra de referência
                for (produto in listaDeCompraReferencia.produtos) {
                    Log.d(
                        "Teste",
                        "Codigo local " + produto.codigoLocal + " supermercado id " + supermercados[posicao].id
                    )

                    if (!produto.codigoLocal.equals("")) {
                        Log.d("Teste", "Chegou aqui na query " + posicao)

                        // Caso o codigo local do produto não seja vazio, cria uma query para buscar o produto no Firestore
                        val query = database.collection("produto")
                            .whereEqualTo("supermercadoId", supermercados[posicao].id)
                            .whereEqualTo("codigoLocal", produto.codigoLocal)

                        // Atualiza produtos
                        // Executa a query para obter o resultado
                        query.get()
                            .addOnSuccessListener { querySnapshot ->
                                Log.d("Teste", "QuerySnapshot " + querySnapshot.size())
                                if (querySnapshot.size() > 0) {
                                    var produto =
                                        Produto(querySnapshot.documents[0].toObject(Produto::class.java)!!)
                                    // Adiciona o produto na lista de produtos
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
                                // Verifica se todos os produtos foram avaliados e se não há produtos encontrados
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

        /**
         * Recalcula o preço total dos produtos e atualiza a visualização.
         */
        private fun RecalculaPrecoTotal(){
            var valorTotal = 0.0
            for(produto in produtos){
                valorTotal += produto.preco
            }
            tvPrecoTotal.setText("R$ " + String.format("%.2f", valorTotal))
        }
    }
}