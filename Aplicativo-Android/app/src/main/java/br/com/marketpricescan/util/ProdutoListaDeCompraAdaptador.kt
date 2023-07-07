package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class ProdutoListaDeCompraAdaptador(private val context : Context, private val produtos: MutableList<Produto>) : RecyclerView.Adapter<ProdutoListaDeCompraAdaptador.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.produto_lista_de_compra_adaptador, parent, false)
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
        private val actvProdutoListaDeCompra: AutoCompleteTextView =
            itemView.findViewById(R.id.etProdutoListaDeCompra)
        private val etPrecoProdutoListaDeCompra: EditText =
            itemView.findViewById(R.id.etPrecoProdutoListaDeCompra)
        private val ivCircleCheck: ImageView = itemView.findViewById(R.id.circleCheck)
        private var checkOrUncheck: Int = 0

        init {
            DefinirAcoes()
        }

        fun bind(produto: Produto) {
            actvProdutoListaDeCompra.setText(produto.nome)
            actvProdutoListaDeCompra.requestFocus()
            if (produto.isChecked) {
                ivCircleCheck.setImageResource(R.drawable.check_circle)
                checkOrUncheck = 1
            } else {
                ivCircleCheck.setImageResource(R.drawable.unchecked_circle)
                checkOrUncheck = 0
            }
            etPrecoProdutoListaDeCompra.setText(produto.preco.toString())
        }

        private fun PopUpConfirmacaoDeletarItem(produto: Produto, position: Int) {
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("O item " + produto.nome + " será deletado da lista. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    if(produto.id.isEmpty()){
                        produtos.remove(produto)
                        notifyItemRemoved(position)
                    }
                    else{
                        DeletarProdutoBancoDeDados(produto)
                    }
                    notifyItemRemoved(position)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }

        private fun DeletarProdutoBancoDeDados(produto: Produto) {
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

        private fun DefinirAcoes() {

            actvProdutoListaDeCompra.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Não é necessário implementar
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val position = bindingAdapterPosition
                    produtos[position].nome = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {
                    val texto = s.toString()
                    BuscarProdutosPorNome(texto)
                }
            })

            actvProdutoListaDeCompra.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                true
            }

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

            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                // Implemente o clique longo do item aqui
                true
            }

            etPrecoProdutoListaDeCompra.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Não é necessário implementar
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val position = bindingAdapterPosition
                    Log.d("Teste", "onTextChanged string: " + s.toString())
                    if (s.toString().isEmpty()) {
                        etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.red))
                        return
                    }
                    try{
                        val preco = DecimalFormat().parse(s.toString())
                        Log.d("Teste", "onTextChanged: " + preco)
                        if(preco != null){
                            etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.black))
                            produtos[position].preco = preco.toDouble()
                        }
                        else{
                            etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.red))
                            return
                        }
                    }
                    catch (e: Exception){
                        Log.d("Teste", "onTextChanged: " + e.message)
                        etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.red))
                        return
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Não é necessário implementar
                }
            })
        }

        private fun BuscarProdutosPorNome(texto : String){
            val produtos = FirebaseFirestore.getInstance().collection("produto_nota_fiscal")
            produtos.whereGreaterThanOrEqualTo("nome", texto.uppercase())
                .orderBy("nome")
                .limit(10)
                .get()
                .addOnSuccessListener { documents ->
                    val sugestoes = mutableListOf<Produto>()
                    for (document in documents) {
                        val nome = document.getString("nome")!!
                        val id = document.id
                        val produto = Produto(nome, id)
                        sugestoes.add(produto)
                    }
                    val adapter = ProdutoNomeArrayAdaptador(itemView.context, sugestoes)
                    actvProdutoListaDeCompra.setAdapter(adapter)
                }
                .addOnFailureListener { exception ->
                    actvProdutoListaDeCompra.setError("Nenhum usuário encontrado")
                }
        }
    }
}
