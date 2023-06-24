package br.com.marketpricescan

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.util.ProdutoListaDeCompraAdaptador
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AtualizarListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var etTituloLista: TextView

    private lateinit var listaDeCompra: ListaDeCompra
    private lateinit var adaptador: ProdutoListaDeCompraAdaptador
    private lateinit var documentoListaDeCompra: DocumentReference

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        val loadingCard = findViewById<CardView>(R.id.loadingPageListaDeCompra)
        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listaDeCompra = intent.getParcelableExtra("listaDeCompra", ListaDeCompra::class.java)!!
            } else {
                listaDeCompra = intent.getParcelableExtra<ListaDeCompra>("listaDeCompra")!!
            }

            IniciarComponentes()

            DefinirAdaptador()

            ObterProdutos()

            VerificarSituacaoLista()

            DefinirAcoes()

            delay(2000)

            loadingCard.visibility = View.INVISIBLE
        }
    }

    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
        etTituloLista = findViewById(R.id.etTituloLista)

        documentoListaDeCompra = FirebaseFirestore.getInstance().collection("lista_de_compra")
            .document(listaDeCompra.id)

        etTituloLista.text = listaDeCompra.nome
    }

    private fun ObterProdutos(){

        documentoListaDeCompra.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var referenciaProdutos = document.get("produtos") as ArrayList<DocumentReference>
                    for (produto in referenciaProdutos) {
                            produto.get().addOnSuccessListener {it ->
                                listaDeCompra.produtos.add(Produto(it.toObject(Produto::class.java)!!))
                                adaptador.notifyDataSetChanged()
                                VerificarSituacaoLista()
                            }

                    }
                }
            }
    }

    private fun DefinirAcoes(){
        btnAdicionarItem.setOnClickListener() { view ->
            var documentoProduto = FirebaseFirestore.getInstance().collection("produto")
                .document()
            var produto = Produto()
            produto.id = documentoProduto.id
            listaDeCompra.adicionarProduto(produto)
            adaptador.notifyDataSetChanged()
            if(adaptador.itemCount > 0) {
                rvListaDeCompra.smoothScrollToPosition(adaptador.itemCount - 1)
            }
            VerificarSituacaoLista()
        }
    }

    private fun VerificarSituacaoLista() {
        if (listaDeCompra.produtos.size == 0) {
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    override fun onBackPressed() {
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("Salvar Lista?")
            .setMessage("Deseja salvar a lista " + etTituloLista.text + "?")
            .setPositiveButton("OK") { dialog, which ->
                runBlocking {
                    AtualizarProdutos()
                }
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
            .create()
        alertDialog.show()
    }

    private fun AtualizarProdutos(){

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val tasks = mutableListOf<Deferred<Unit>>()
            val referenciasProdutos = mutableListOf<DocumentReference>()
            for (produto in listaDeCompra.produtos) {
                val task : Deferred<Unit> = async {
                    val documentoProduto =
                        FirebaseFirestore.getInstance().collection("produto").document(produto.id)
                    referenciasProdutos.add(documentoProduto)
                    documentoProduto.set(produto).await()
                    // Você pode adicionar o código para tratamento de sucesso/erro aqui se necessário
                }
                tasks.add(task)
            }
            tasks.awaitAll()

            AtualizarListaDeCompra(referenciasProdutos)
        }

//        var referenciasProdutos = mutableListOf<DocumentReference>()
//        for(produto in listaDeCompra.produtos){
//            var documentoProduto = FirebaseFirestore.getInstance().collection("produto")
//                .document(produto.id)
//            referenciasProdutos.add(documentoProduto)
//            documentoProduto.set(produto)
//                .addOnSuccessListener {
//                    Log.d("Teste", "Sucesso ao salvar o produto no banco de dados")
//                }
//                .addOnFailureListener {
//                    Log.d("Teste", "Erro ao salvar o produto no banco de dados")
//                }
//        }
//        AtualizarListaDeCompra(referenciasProdutos)
    }

    private fun AtualizarListaDeCompra(referenciasProdutos : MutableList<DocumentReference>) {

        val updates = hashMapOf<String, Any>(
            "id" to listaDeCompra.id,
            "produtos" to referenciasProdutos,
            "nome" to etTituloLista.text.toString(),
        )

        runBlocking {
            documentoListaDeCompra.update(updates)
                .addOnSuccessListener {
                    Log.d("Teste", "Sucesso ao atualizar a lista no banco de dados")
                }
                .addOnFailureListener { e ->
                    Log.d("Teste", "Erro ao atualizar a lista no banco de dados")
                }
        }
    }

    private fun DefinirAdaptador(){
        adaptador = ProdutoListaDeCompraAdaptador(this, listaDeCompra.produtos)
        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                VerificarSituacaoLista()
            }
        })

        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true
    }
}