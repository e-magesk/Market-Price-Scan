package br.com.marketpricescan.util

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.R
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.ProdutoNotaFiscal
import br.com.marketpricescan.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

/**
 * Adaptador personalizado para exibição de produtos em uma lista de compra no RecyclerView.
 *
 * @property context Contexto da aplicação.
 * @property produtos Lista de produtos a serem exibidos.
 */
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

    /**
     * ViewHolder para os itens de produto da lista de compra.
     *
     * @param itemView A visualização de um item de produto.
     */
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

        /**
         * Liga os dados de um produto à visualização do item.
         *
         * @param produto Produto a ser exibido.
         */
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
            etPrecoProdutoListaDeCompra.setText(String.format("%.2f", produto.preco))
        }

        /**
         * Exibe um diálogo de confirmação para deletar um item da lista de compra.
         *
         * @param produto Produto a ser deletado.
         * @param position Posição do produto na lista.
         */
        private fun PopUpConfirmacaoDeletarItem(produto: Produto, position: Int) {
            val alertDialog = AlertDialog.Builder(itemView.context)
                .setTitle("Deletar Item")
                .setMessage("O item " + produto.nome + " será deletado da lista. Deseja continuar?")
                .setPositiveButton("OK") { dialog, which ->
                    if(produto.id.isEmpty()){
                        // Se o produto não tem ID, remove-o diretamente da lista
                        produtos.remove(produto)
                        notifyItemRemoved(position)
                    }
                    else{
                        // Caso contrário, deleta o produto do banco de dados
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

        /**
         * Deleta um produto do banco de dados.
         *
         * @param produto Produto a ser deletado.
         */
        private fun DeletarProdutoBancoDeDados(produto: Produto) {
            // Remove o produto da lista local
            produtos.remove(produto)
            // Deleta o documento do produto no banco de dados
            FirebaseFirestore.getInstance().collection("produto")
                .document(produto.id).delete()
                .addOnSuccessListener {
                    Log.d("Teste", "Sucesso ao deletar o produto no banco de dados")
                }
                .addOnFailureListener {
                    Log.d("Teste", "Falha ao deletar o produto no banco de dados")
                }
        }

        /**
         * Define as ações de interação para os elementos da lista de produtos.
         */
        private fun DefinirAcoes() {
            // Configura um TextWatcher para o campo de texto "actvProdutoListaDeCompra"
            actvProdutoListaDeCompra.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Não é necessário implementar
                }


                 // Chamado quando o texto é alterado no campo de texto.
                 // Atualiza o nome do produto na lista local com base no texto digitado
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val position = bindingAdapterPosition
                    produtos[position].nome = s.toString()
                }

                // Chamado após o texto ser alterado no campo de texto.
                // Realiza a busca de produtos com base no texto digitado.
                override fun afterTextChanged(s: Editable?) {
                    val texto = s.toString()
                    BuscarProdutosPorNome(texto, bindingAdapterPosition)
                }
            })

            // Configura um clique longo para o campo de texto "actvProdutoListaDeCompra"
            // Quando ocorre o clique longo, exibe um pop-up de confirmação para deletar o item da lista.
            actvProdutoListaDeCompra.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                true
            }

            // Define a ação de clique para o "ivCircleCheck".
            // Quando ocorre o clique, alterna entre o ícone de check e uncheck e atualiza o estado do produto na lista.
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

            // Define a ação de clique longo para o itemView.
            // Quando ocorre o clique longo, exibe um pop-up de confirmação para deletar o item da lista.
            itemView.setOnLongClickListener {
                val currentPosition = bindingAdapterPosition
                PopUpConfirmacaoDeletarItem(produtos[currentPosition], currentPosition)
                // Implemente o clique longo do item aqui
                true
            }

            // Monitora as alterações no texto do preço do produto
            etPrecoProdutoListaDeCompra.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Não é necessário implementar
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val position = bindingAdapterPosition
                    Log.d("Teste", "onTextChanged string: " + s.toString())

                    // MUda a cor para vermelho se o campo de texto estiver vazio
                    if (s.toString().isEmpty()) {
                        etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.red))
                        return
                    }
                    try{
                        // Converte o valor do campo de texto em um número decimal
                        val preco = DecimalFormat().parse(s.toString())
                        Log.d("Teste", "onTextChanged: " + preco)
                        if(preco != null){
                            // O valor é válido, atualiza a cor do texto para preto
                            etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.black))
                            produtos[position].preco = preco.toDouble()
                        }
                        else{
                            // O valor é inválido, mantém a cor do texto como vermelho
                            etPrecoProdutoListaDeCompra.setTextColor(ContextCompat.getColor(context, R.color.red))
                            return
                        }
                    }
                    catch (e: Exception){
                        // Ocorreu uma exceção ao converter o valor, mantém a cor do texto como vermelho
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

        /**
         * Realiza a busca de produtos por nome a partir de um texto fornecido.
         *
         * @param texto O texto a ser utilizado na busca.
         * @param posicao A posição do item na lista de produtos.
         */
        private fun BuscarProdutosPorNome(texto : String, posicao: Int){
            // Realiza a consulta no banco de dados
            val produtos = FirebaseFirestore.getInstance().collection("produto_nota_fiscal")
            produtos.whereGreaterThanOrEqualTo("nome", texto.uppercase())
                .orderBy("nome")
                .limit(10)
                .get()
                .addOnSuccessListener { documents ->
                    val sugestoes = mutableListOf<ProdutoNotaFiscal>()

                    // Itera sobre os documentos retornados
                    for (document in documents) {
                        val nome = document.getString("nome")!!
                        val id = document.id
                        // Cria um objeto ProdutoNotaFiscal com os dados obtidos
                        val produto = ProdutoNotaFiscal(id, nome)
                        // Adiciona o produto à lista de sugestões
                        sugestoes.add(produto)
                    }
                    // Define um adaptador como o adaptador do AutoCompleteTextView
                    val adapter = ProdutoNomeArrayAdaptador(itemView.context, sugestoes)
                    actvProdutoListaDeCompra.setAdapter(adapter)

                    // Configura o evento de clique em um item do AutoCompleteTextView
                    actvProdutoListaDeCompra.setOnItemClickListener { parent, view, position, id ->
                        val selectedItem = parent.getItemAtPosition(position) as ProdutoNotaFiscal
                        // Chama o método MudarItem para atualizar o item na posição especificada
                        MudarItem(posicao, selectedItem)
                    }
                }
                .addOnFailureListener { exception ->
                    actvProdutoListaDeCompra.setError("Nenhum produto encontrado")
                }
        }

        /**
         * Atualiza o item na posição especificada com os dados do produto fornecido.
         *
         * @param position A posição do item na lista de produtos.
         * @param produto O produto contendo os novos dados.
         */
        private fun MudarItem(position: Int, produto: ProdutoNotaFiscal){
            Log.d("Teste", "MudarItem: " + produto.nome + " " + produto.id)
            // Atualiza o nome e o código local do produto na posição especificada
            produtos[position].nome = produto.nome
            produtos[position].codigoLocal = produto.id

            Log.d("Teste", "MudarItem: " + produtos[position].nome + " " + produtos[position].codigoLocal)

            // Notifica a mudança no item da posição especificada para atualizar a exibição na lista
            notifyItemChanged(position)
        }
    }
}
