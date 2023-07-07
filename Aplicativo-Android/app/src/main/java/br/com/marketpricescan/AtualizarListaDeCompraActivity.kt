package br.com.marketpricescan

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
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
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ProdutoListaDeCompraAdaptador
import br.com.marketpricescan.util.UsuarioCompartilharAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
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
    private lateinit var btnCompartilhar: Button
    private lateinit var cvCompartilharComAmigos : CardView
    private lateinit var rvCompartilharComAmigos : RecyclerView
    private lateinit var adaptadorUsuariosCompartilhar : UsuarioCompartilharAdaptador
    private lateinit var listaDeCompra: ListaDeCompra
    private lateinit var adaptador: ProdutoListaDeCompraAdaptador
    private lateinit var documentoListaDeCompra: DocumentReference
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var amigosCompartilhar = mutableListOf<Usuario>()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var usuario: Usuario

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
                usuario = intent.getParcelableExtra("usuario", Usuario::class.java)!!
            } else {
                listaDeCompra = intent.getParcelableExtra<ListaDeCompra>("listaDeCompra")!!
                usuario = intent.getParcelableExtra<Usuario>("usuario")!!
            }

            Log.d("Teste", "Nome usuario: " + usuario.nome)

            IniciarComponentes()

            DefinirAdaptador()

            DefinirAdaptadorUsuariosCompartilhar()

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
        btnCompartilhar = findViewById(R.id.btnCompartilhar)
        rvCompartilharComAmigos = findViewById(R.id.rvCompartilharComAmigos)
        cvCompartilharComAmigos = findViewById(R.id.cvCompartilharComAmigos)

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

    private fun DefinirAdaptadorUsuariosCompartilhar(){

        adaptadorUsuariosCompartilhar = UsuarioCompartilharAdaptador(this, usuario.amigos)
        adaptadorUsuariosCompartilhar.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                if(payload.toString() === "checked"){
                    Log.d("Teste", "Usuário selecionado")
                    amigosCompartilhar.add(usuario.amigos[positionStart])
                }
                else if(payload.toString() === "unchecked"){
                    Log.d("Teste", "Usuário desmarcado")
                    amigosCompartilhar.remove(usuario.amigos[positionStart])
                }
                Log.d("Teste", "Amigos compartilhar: " + amigosCompartilhar.size)
            }
        })

        rvCompartilharComAmigos.setHasFixedSize(true)
        rvCompartilharComAmigos.layoutManager = LinearLayoutManager(this)
        rvCompartilharComAmigos.adapter = adaptadorUsuariosCompartilhar
        rvCompartilharComAmigos.isClickable = true
    }

    private fun DefinirAcoes(){

        etTituloLista.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etTituloLista.compoundDrawables[2] // Ícone no lado direito (índice 2)
                if (event.rawX >= (etTituloLista.right - drawableRight.bounds.width())) {
                    if(cvCompartilharComAmigos.visibility === View.GONE){
                        cvCompartilharComAmigos.visibility = View.VISIBLE
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

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

        btnCompartilhar.setOnClickListener() { view ->
            cvCompartilharComAmigos.visibility = View.GONE
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

    private fun CompartilharLista(){

        runBlocking {
            for(amigo in amigosCompartilhar){
                var documentoUsuario = database.collection("usuario")
                    .document(amigo.id)

                documentoUsuario.update("listasDeCompra", FieldValue.arrayUnion(documentoListaDeCompra))
                    .addOnSuccessListener {
                        Log.d("Teste", "Sucesso ao compartilhar a lista de compra")
                    }
                    .addOnFailureListener {
                        Log.d("Teste", "Erro ao compartilhar a lista de compra")
                    }
            }
        }

    }

    override fun onBackPressed() {

        if(cvCompartilharComAmigos.visibility === View.VISIBLE){
            cvCompartilharComAmigos.visibility = View.GONE
            amigosCompartilhar.clear()
        }
        else {
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
                    CompartilharLista()
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