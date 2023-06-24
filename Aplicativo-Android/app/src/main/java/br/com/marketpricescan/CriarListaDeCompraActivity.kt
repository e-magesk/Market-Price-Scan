package br.com.marketpricescan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.util.ProdutoListaDeCompraAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.floor

class CriarListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var etTituloLista: TextView
    private lateinit var adaptador: ProdutoListaDeCompraAdaptador
    var produtos = mutableListOf<Produto>()
    private lateinit var listaDeCompra: ListaDeCompra
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var documentoListaDeCompra: DocumentReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        val loadingCard = findViewById<CardView>(R.id.loadingPageListaDeCompra)
        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            IniciarComponentes()

            DefinirAdaptador()

            VerificarSituacaoLista()

            btnAdicionarItem.setOnClickListener() { view ->
                produtos.add(Produto("" ))
                adaptador.notifyDataSetChanged()
                rvListaDeCompra.smoothScrollToPosition(adaptador.itemCount - 1)
                VerificarSituacaoLista()

            }

            delay(2000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
        etTituloLista = findViewById(R.id.etTituloLista)

        listaDeCompra = ListaDeCompra("")
    }

    private fun VerificarSituacaoLista() {
        if (produtos.size == 0) {
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    private fun VincularListaAoUsuario() {

        documentoUsuario = database.collection("usuario")
            .document(usuarioId)

        documentoUsuario.update("listasDeCompra", FieldValue.arrayUnion(documentoListaDeCompra))
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao vincular a lista ao usuário")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao vincular a lista ao usuário")
            }

    }

    override fun onBackPressed() {
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("Salvar Lista?")
            .setMessage("Deseja salvar a lista " + etTituloLista.text + "?")
            .setPositiveButton("OK") { dialog, which ->
                runBlocking {
                    CriarProdutos()
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

    private fun CriarProdutos(){

        if(produtos.size == 0){
            runBlocking {
                SalvarListaDeCompra(mutableListOf<DocumentReference>())
            }
            return
        }

        var referenciasProdutos = mutableListOf<DocumentReference>()
        var flagFinal = 0
        for(produto in produtos){
            var documentoProduto = FirebaseFirestore.getInstance().collection("produto")
                .document()
            produto.id = documentoProduto.id
            referenciasProdutos.add(documentoProduto)
            documentoProduto.set(produto)
                .addOnSuccessListener {
                    flagFinal++
                    if (flagFinal == produtos.size){
                        runBlocking {
                            SalvarListaDeCompra(referenciasProdutos)
                        }
                    }
                    Log.d("Teste", "Sucesso ao salvar o produto no banco de dados")
                }
                .addOnFailureListener {
                    Log.d("Teste", "Erro ao salvar o produto no banco de dados")
                }
        }
    }

    private fun SalvarListaDeCompra(referenciaProdutos: MutableList<DocumentReference>){

        documentoListaDeCompra = database.collection("lista_de_compra")
            .document()

        val updates = hashMapOf<String, Any>(
            "produtos" to referenciaProdutos,
            "nome" to etTituloLista.text.toString(),
            "id" to documentoListaDeCompra.id
        )

        documentoListaDeCompra.set(updates)
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao criar a lista no banco de dados")
                runBlocking {
                    VincularListaAoUsuario()
                }
            }
            .addOnFailureListener { e ->
                Log.d("Teste", "Erro ao criar a lista no banco de dados")
            }
    }

    private fun DefinirAdaptador(){
        adaptador = ProdutoListaDeCompraAdaptador(this, produtos)
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