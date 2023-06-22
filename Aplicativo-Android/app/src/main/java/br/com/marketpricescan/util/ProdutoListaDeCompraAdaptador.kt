package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Produto
import com.google.firebase.firestore.FirebaseFirestore

class ProdutoListaDeCompraAdaptador(private val context : Context, private val produtos: MutableList<Produto>) : RecyclerView.Adapter<ProdutoListaDeCompraAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.produto_lista_de_compra_adaptador, parent, false)
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
        private val etProdutoListaDeCompra: EditText = itemView.findViewById(R.id.etProdutoListaDeCompra)
        private val ivCircleCheck: ImageView = itemView.findViewById(R.id.circleCheck)
        private var checkOrUncheck: Int = 0

        init {

            etProdutoListaDeCompra.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Não é necessário implementar
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val position = bindingAdapterPosition
                    produtos[position].nome = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {
                    // Não é necessário implementar
                }
            })

            ivCircleCheck.setOnClickListener {
                if (checkOrUncheck == 1) {
                    ivCircleCheck.setImageResource(R.drawable.unchecked_circle)
                    checkOrUncheck = 0
                } else {
                    checkOrUncheck = 1
                    ivCircleCheck.setImageResource(R.drawable.check_circle)
                }
            }

            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                // Implemente o clique longo do item aqui
                true
            }

        }

        fun bind(produto: Produto) {
            etProdutoListaDeCompra.setText(produto.nome)
        }

        private fun PopUpConfirmacaoDeletarItem(produto : Produto, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("O item " + produto.nome + " será deletado da lista. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    Log.d("Teste", "PopUpConfirmacaoDeletarItem: " + position)
                    DeletarProduto(produto)
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

    private fun DeletarProduto(produto : Produto){
        produtos.remove(produto)
        FirebaseFirestore.getInstance().collection("produto")
            .document(produto.id).delete()
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao deletar o produto no banco de dados")
            }
            .addOnFailureListener {
                Log.d("Teste", "Falha ao deletar o produto no banco de dados")
            }
    }
}
