package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.ProdutoNotaFiscal
import br.com.marketpricescan.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

/**
 * Adaptador personalizado para exibição de produtos em um RecyclerView.
 *
 * @property context Contexto da aplicação.
 * @property produtos Lista de produtos a serem exibidos.
 */
class ProdutoAdaptador(private val context : Context, private val produtos: MutableList<Produto>) : RecyclerView.Adapter<ProdutoAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.produto_lista_de_compra_adaptador, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val produto = produtos[position]
        holder.bind(produto)
    }

    override fun getItemCount(): Int {
        return produtos.size
    }

    /**
     * ViewHolder para os itens de produto.
     *
     * @param itemView A visualização de um item de produto.
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val actvProdutoListaDeCompra: AutoCompleteTextView = itemView.findViewById(R.id.etProdutoListaDeCompra)
        private val etPrecoProdutoListaDeCompra: EditText = itemView.findViewById(R.id.etPrecoProdutoListaDeCompra)
        private val ivCircleCheck: ImageView = itemView.findViewById(R.id.circleCheck)

        /**
         * Liga os dados de um produto à visualização do item.
         *
         * @param produto Produto a ser exibido.
         */
        fun bind(produto: Produto) {
            actvProdutoListaDeCompra.setText(produto.nome)
            actvProdutoListaDeCompra.isClickable = false
            etPrecoProdutoListaDeCompra.setText(produto.preco.toString())
            ivCircleCheck.visibility = View.GONE
        }

    }
}
