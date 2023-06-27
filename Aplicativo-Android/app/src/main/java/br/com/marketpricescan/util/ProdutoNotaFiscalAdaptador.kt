package br.com.marketpricescan.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Produto

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

        private fun definirAcoes() {

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
        }
    }
}
