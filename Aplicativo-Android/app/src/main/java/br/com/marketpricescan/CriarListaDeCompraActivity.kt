package br.com.marketpricescan

import android.annotation.SuppressLint
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CriarListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var etTituloLista: TextView
    private lateinit var cvCompartilharComAmigos : CardView
    private lateinit var rvCompartilharComAmigos : RecyclerView
    private lateinit var adaptadorUsuariosCompartilhar : UsuarioCompartilharAdaptador
    private lateinit var adaptadorProdutos: ProdutoListaDeCompraAdaptador
    private var amigosCompartilhar = mutableListOf<Usuario>()
    var produtos = mutableListOf<Produto>()
    private lateinit var listaDeCompra: ListaDeCompra
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var usuario: Usuario
    private lateinit var documentoListaDeCompra: DocumentReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        val loadingCard = findViewById<CardView>(R.id.loadingPageListaDeCompra)
        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                usuario = intent.getParcelableExtra("usuario", Usuario::class.java)!!
            } else {
                usuario = intent.getParcelableExtra<Usuario>("usuario")!!
            }

            IniciarComponentes()

            DefinirAdaptadorProdutos()

            DefinirAdaptadorUsuariosCompartilhar()

            VerificarSituacaoLista()

            DefinirAcoes()

            delay(2000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
        etTituloLista = findViewById(R.id.etTituloLista)

        rvCompartilharComAmigos = findViewById(R.id.rvCompartilharComAmigos)
        cvCompartilharComAmigos = findViewById(R.id.cvCompartilharComAmigos)

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

    private fun DefinirAcoes(){

        etTituloLista.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etTituloLista.compoundDrawables[2] // Ícone no lado direito (índice 2)
                if (event.rawX >= (etTituloLista.right - drawableRight.bounds.width())) {
                    if(cvCompartilharComAmigos.visibility === View.VISIBLE){
                        cvCompartilharComAmigos.visibility = View.GONE
                    }
                    else{
                        cvCompartilharComAmigos.visibility = View.VISIBLE
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        btnAdicionarItem.setOnClickListener() { view ->
            produtos.add(Produto("" ))
            adaptadorProdutos.notifyDataSetChanged()
            rvListaDeCompra.smoothScrollToPosition(adaptadorProdutos.itemCount - 1)
            VerificarSituacaoLista()

        }

    }

    override fun onBackPressed() {
        if(cvCompartilharComAmigos.visibility === View.VISIBLE){
            cvCompartilharComAmigos.visibility = View.GONE
        }
        else {
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

    private fun DefinirAdaptadorProdutos(){
        adaptadorProdutos = ProdutoListaDeCompraAdaptador(this, produtos)
        adaptadorProdutos.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                VerificarSituacaoLista()
            }
        })

        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptadorProdutos
        rvListaDeCompra.isClickable = true
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
            }
        })

        rvCompartilharComAmigos.setHasFixedSize(true)
        rvCompartilharComAmigos.layoutManager = LinearLayoutManager(this)
        rvCompartilharComAmigos.adapter = adaptadorUsuariosCompartilhar
        rvCompartilharComAmigos.isClickable = true
    }


}