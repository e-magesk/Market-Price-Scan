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

class ItemListaAdaptador(private val context : Context, private val itens: MutableList<String>) : RecyclerView.Adapter<ItemListaAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.d("Teste", "onCreateViewHolder: " + itens.size)
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_lista_de_compra, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.d("Teste", "onBindViewHolder: " + itens[position])
        val item = itens[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itens.size
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
                    itens[position] = s.toString()
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
                PopUpConfirmacaoDeletarItem(itens[currentPosition], currentPosition)
                // Implemente o clique longo do item aqui
                true
            }

        }

        fun bind(item: String) {
            Log.d("Tester", "bind: $item")
            etProdutoListaDeCompra.setText(item)
        }

        private fun PopUpConfirmacaoDeletarItem(item : String, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("O item " + item + " será deletado da lista. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    itens.removeAt(position)
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
