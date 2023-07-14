package br.com.marketpricescan.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.AtualizarListaDeCompraActivity
import br.com.marketpricescan.R
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Adaptador personalizado para exibição de listas de compra em um RecyclerView.
 *
 * @property context Contexto da aplicação.
 * @property listasDeCompra Lista de listas de compra a serem exibidas.
 */
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

    /**
     * ViewHolder para os itens da lista de compra.
     *
     * @param itemView A visualização de um item da lista de compra.
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeListaDeCompra: TextView = itemView.findViewById(R.id.tvNomeListaDeCompra)

        init {
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarLista(listasDeCompra[currentPosition], currentPosition)
                true
            }

            itemView.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                notifyItemRangeChanged(currentPosition, 1, "selected")
            }

        }

        /**
         * Liga os dados de uma lista de compra à visualização do item.
         *
         * @param lista Lista de compra a ser exibida.
         */
        fun bind(lista: ListaDeCompra) {
            tvNomeListaDeCompra.setText(lista.nome)
        }

        /**
         * Exibe um pop-up de confirmação para deletar uma lista de compra.
         *
         * @param lista Lista de compra a ser deletada.
         * @param position Posição da lista de compra na lista de exibição.
         */
        private fun PopUpConfirmacaoDeletarLista(lista : ListaDeCompra, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("A lista " + lista.nome + " será deletado de Mnhas Listas. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    DeletarListaDeCompra(lista, position)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }

        /**
         * Deleta uma lista de compra do banco de dados.
         *
         * @param lista Lista de compra a ser deletada.
         * @param position Posição da lista de compra na lista de exibição.
         */
        private fun DeletarListaDeCompra(lista: ListaDeCompra, position: Int) {
            FirebaseFirestore.getInstance().collection("lista_de_compra")
                .document(lista.id).delete()
                .addOnSuccessListener {
                    Log.d("Teste", "Sucesso ao deletar a lista de compra no banco de dados")
                    listasDeCompra.remove(lista)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener {
                    Log.d("Teste", "Falha ao deletar a lista de compra no banco de dados")
                }
        }
    }
}