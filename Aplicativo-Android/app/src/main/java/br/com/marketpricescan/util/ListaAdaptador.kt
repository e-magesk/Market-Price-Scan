package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.ListaDeCompra

class ListaAdaptador(private val context : Context, private val listasDeCompra: MutableList<ListaDeCompra>) : RecyclerView.Adapter<ListaAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_lista_de_compra, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val lista = listasDeCompra[position]
        holder.bind(lista)
    }

    override fun getItemCount(): Int {
        return listasDeCompra.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val etNomeListaDeCompra: EditText = itemView.findViewById(R.id.etNomeListaDeCompra)

        init {
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarLista(listasDeCompra[currentPosition], currentPosition)
                true
            }

        }

        fun bind(lista: ListaDeCompra) {
            etNomeListaDeCompra.setText(lista.nome)
        }

        private fun PopUpConfirmacaoDeletarLista(lista : ListaDeCompra, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("A lista " + lista.nome + " será deletado de Mnhas Listas. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    listasDeCompra.removeAt(position)
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