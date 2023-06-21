package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.ListaDeCompra

class ListaDeCompraAdaptador(private val context : Context, private val listasDeCompra: MutableList<ListaDeCompra>) : RecyclerView.Adapter<ListaDeCompraAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.lista_adaptador, parent, false)
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
        private val tvNomeListaDeCompra: TextView = itemView.findViewById(R.id.tvNomeListaDeCompra)

        init {
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarLista(listasDeCompra[currentPosition], currentPosition)
                true
            }

        }

        fun bind(lista: ListaDeCompra) {
            tvNomeListaDeCompra.setText("Nome da Lista")
        }

        private fun PopUpConfirmacaoDeletarLista(lista : ListaDeCompra, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("A lista " + lista.nome + " será deletado de Mnhas Listas. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    listasDeCompra.removeAt(position)
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