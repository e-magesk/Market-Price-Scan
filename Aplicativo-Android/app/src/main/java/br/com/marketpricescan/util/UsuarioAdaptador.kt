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
import br.com.marketpricescan.model.Usuario

/**
 * Adaptador para exibição de usuários em uma lista.
 *
 * @param context O contexto da aplicação.
 * @param usuarios A lista de usuários a ser exibida.
 */
class UsuarioAdaptador(private val context : Context, private val usuarios: MutableList<Usuario>) : RecyclerView.Adapter<UsuarioAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.usuario_adaptador, parent, false)
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
     * ViewHolder para exibição de cada item da lista de usuários.
     *
     * @param itemView A visualização do item.
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeUsuario: TextView = itemView.findViewById(R.id.tvNomeUsuario)
        private val iconDeletaAmigo : ImageView = itemView.findViewById(R.id.iconDeletaAmigo)

        init {
            // Configura o clique longo no item para exibir o pop-up de confirmação de remoção do amigo
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoRemoverAmigo(usuarios[currentPosition], currentPosition)
                true
            }
            // Configura o clique no ícone de deletar amigo para exibir o pop-up de confirmação de remoção do amigo
            iconDeletaAmigo.setOnClickListener{
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoRemoverAmigo(usuarios[currentPosition], currentPosition)
            }

        }

        fun bind(usuario: Usuario) {
            tvNomeUsuario.setText(usuario.nome)
        }

        /**
         * Exibe o pop-up de confirmação para remover o amigo.
         *
         * @param usuario O usuário a ser removido como amigo.
         * @param position A posição do item na lista de usuários.
         */
        private fun PopUpConfirmacaoRemoverAmigo(usuario: Usuario, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Remover Amigo")
                .setMessage(usuario.nome + " será deletado da sua lista de amigos. Tem certeza que deseja fazer isso?")
                .setPositiveButton("Remover amigo") { dialog, which ->
                    // Remove o usuário da lista de amigos
                    usuarios.remove(usuario)
                    // Notifica o RecyclerView sobre a remoção do item na posição especificada
                    notifyItemRemoved(position)
                    // Notifica o RecyclerView que houve alteração nos dados
                    notifyDataSetChanged()
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