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

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeUsuario: TextView = itemView.findViewById(R.id.tvNomeUsuario)
        private val iconDeletaAmigo : ImageView = itemView.findViewById(R.id.iconDeletaAmigo)

        init {
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoRemoverAmigo(usuarios[currentPosition], currentPosition)
                true
            }

            iconDeletaAmigo.setOnClickListener{
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoRemoverAmigo(usuarios[currentPosition], currentPosition)
            }

        }

        fun bind(usuario: Usuario) {
            tvNomeUsuario.setText(usuario.nome)
        }

        private fun PopUpConfirmacaoRemoverAmigo(usuario: Usuario, position : Int){
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Remover Amigo")
                .setMessage(usuario.nome + " será deletado da sua lista de amigos. Tem certeza que deseja fazer isso?")
                .setPositiveButton("Remover amigo") { dialog, which ->
                    usuarios.remove(usuario)
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