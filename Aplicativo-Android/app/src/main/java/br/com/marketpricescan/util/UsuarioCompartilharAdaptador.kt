package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Usuario

/**
 * Adaptador para exibir uma lista de usuários para compartilhamento em um RecyclerView.
 *
 * @property context O contexto da aplicação.
 * @property usuarios A lista de usuários a ser exibida.
 */
class UsuarioCompartilharAdaptador(private val context : Context, private val usuarios: MutableList<Usuario>) : RecyclerView.Adapter<UsuarioCompartilharAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.usuario_compartilhar_adaptador, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario)
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }

    /**
     * Classe interna que representa cada item de visualização individual na lista.
     *
     * @param itemView A exibição do item.
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeUsuarioCompartilhar: TextView = itemView.findViewById(R.id.tvNomeUsuarioCompartilhar)
        private val iconCheckAmigo : ImageView = itemView.findViewById(R.id.iconCheckAmigo)
        private var checkOrUncheck: Int = 0

        init {
            itemView.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                CheckOrUncheckFriend(usuarios[currentPosition], currentPosition)
            }

            iconCheckAmigo.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                CheckOrUncheckFriend(usuarios[currentPosition], currentPosition)
            }

        }

        fun bind(usuario: Usuario) {
            tvNomeUsuarioCompartilhar.setText(usuario.nome)
        }

        /**
         * Marca ou desmarca um amigo com base no estado atual.
         *
         * @param usuario O usuário a ser verificado ou desmarcado.
         * @param position A posição do usuário na lista.
         */
        private fun CheckOrUncheckFriend(usuario: Usuario, position : Int){
            if (checkOrUncheck == 1) {
                iconCheckAmigo.setImageResource(R.drawable.unchecked_circle)
                checkOrUncheck = 0
                notifyItemChanged(position, "unchecked")
            } else {
                checkOrUncheck = 1
                iconCheckAmigo.setImageResource(R.drawable.check_circle)
                notifyItemChanged(position, "checked")
            }
        }
    }
}