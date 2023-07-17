package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Produto

/**
 * Adaptador para exibição de produtos em uma lista de nota fiscal.
 *
 * @param context O contexto da aplicação.
 * @param produtos A lista de produtos a ser exibida.
 */
class ProdutoNotaFiscalAdaptador(private val context : Context, private val produtos: MutableList<Produto>) : RecyclerView.Adapter<ProdutoNotaFiscalAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.produto_nota_fiscal_adaptador, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val produto = produtos[position]
        holder.bind(produto)
    }

    override fun getItemCount(): Int {
        return produtos.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProdutoNotaFiscal: TextView =
            itemView.findViewById(R.id.tvProdutoNotaFiscal)
        private val tvPrecoProdutoNotaFiscal: TextView =
            itemView.findViewById(R.id.tvPrecoProdutoNotaFiscal)
        private val ivCircleCheck: ImageView = itemView.findViewById(R.id.circleCheck)
        private var checkOrUncheck: Int = 0

        init {

            definirAcoes()

        }

        /**
         * Atualiza a visualização do ItemViewHolder com base nos dados do produto fornecido.
         *
         * @param produto O produto a ser exibido.
         */
        fun bind(produto: Produto) {
            tvProdutoNotaFiscal.text = produto.nome
            tvProdutoNotaFiscal.requestFocus()
            checkOrUncheck = if (produto.isChecked) {
                ivCircleCheck.setImageResource(R.drawable.check_circle)
                1
            } else {
                ivCircleCheck.setImageResource(R.drawable.unchecked_circle)
                0
            }
            tvPrecoProdutoNotaFiscal.text = produto.preco.toString()
        }

        /**
         * Define as ações dos elementos do item da lista.
         */
        private fun definirAcoes() {
            // Define a ação de clique para o "ivCircleCheck".
            // Quando ocorre o clique, alterna entre o ícone de check e uncheck e atualiza o estado do produto na lista.
            ivCircleCheck.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (checkOrUncheck == 1) {
                    ivCircleCheck.setImageResource(R.drawable.unchecked_circle)
                    checkOrUncheck = 0
                    produtos[currentPosition].isChecked = false
                } else {
                    checkOrUncheck = 1
                    ivCircleCheck.setImageResource(R.drawable.check_circle)
                    produtos[currentPosition].isChecked = true
                }
            }

            // Define a ação de clique longo para o itemView do produto.
            // Quando ocorre o clique longo, exibe um pop-up de confirmação para deletar o item da lista.
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                // Implemente o clique longo do item aqui
                true
            }

            // Define a ação de clique longo para o textView do prouto.
            // Quando ocorre o clique longo, exibe um pop-up de confirmação para deletar o item da lista.
            tvProdutoNotaFiscal.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                true
            }
        }

        /**
         * Exibe um popup de confirmação para deletar o item da lista.
         *
         * @param produto O produto a ser deletado.
         * @param position A posição do item na lista.
         */
        private fun PopUpConfirmacaoDeletarItem(produto: Produto, position: Int) {
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("O item " + produto.nome + " será deletado da lista. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    // Remove o produto da lista de produtos
                    produtos.remove(produto)
                    notifyItemRemoved(position)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }

    }
}
