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
import androidx.constraintlayout.widget.ConstraintLayout
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

/**
 * Classe responsável por exibir e gerenciar a tela de atualização da lista de compra.
 * A lista de compra é exibida em uma RecyclerView e pode ser compartilhada com amigos.
 * Os produtos da lista são obtidos a partir do Firestore e podem ser atualizados e salvos no banco de dados.
 */
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
    private lateinit var rootLayout : ConstraintLayout
    private lateinit var tvCompartilhar : TextView
    private lateinit var tvCompararPrecos : TextView
    private lateinit var cvOpcoes : CardView

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

    /**
     * Inicializa os componentes da tela de atualização da lista de compra.
     * Associa as variáveis locais aos elementos de layout correspondentes através de seus IDs.
     * Configura a referência do documento da lista de compra no Firestore.
     * Define o texto do título da lista com base no nome da lista de compra.
     */
    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
        etTituloLista = findViewById(R.id.etTituloLista)
        btnCompartilhar = findViewById(R.id.btnCompartilhar)
        rvCompartilharComAmigos = findViewById(R.id.rvCompartilharComAmigos)
        cvCompartilharComAmigos = findViewById(R.id.cvCompartilharComAmigos)
        rootLayout = findViewById(R.id.rootLayoutListaDeCompra)
        tvCompartilhar = findViewById(R.id.tvCompartilhar)
        tvCompararPrecos = findViewById(R.id.tvCompararPrecos)
        cvOpcoes = findViewById(R.id.cvOpcoes)

        documentoListaDeCompra = FirebaseFirestore.getInstance().collection("lista_de_compra")
                .document(listaDeCompra.id)

        etTituloLista.text = listaDeCompra.nome
    }

    /**
     * Obtém os produtos da lista de compra a partir do documento no Firestore.
     */
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

    /**
     * Define o adaptador  de usuários com os quais é possível compartilhar a lista.
     * O adaptador exibe uma lista de usuários com base nos amigos do usuário atual.
     * Os amigos selecionados são armazenados na lista amigosCompartilhar.
     */
    private fun DefinirAdaptadorUsuariosCompartilhar(){
        // Cria um novo adaptador de usuários com os quais é possível compartilhar a lista.
        adaptadorUsuariosCompartilhar = UsuarioCompartilharAdaptador(this, usuario.amigos)

        // Registra um observador para o adaptador de dados do RecyclerView
        adaptadorUsuariosCompartilhar.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                // Verifica se o payload indica que o usuário foi selecionado
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

        // Configura o RecyclerView
        rvCompartilharComAmigos.setHasFixedSize(true)
        rvCompartilharComAmigos.layoutManager = LinearLayoutManager(this)
        rvCompartilharComAmigos.adapter = adaptadorUsuariosCompartilhar
        rvCompartilharComAmigos.isClickable = true
    }

    /**
     * Define as ações dos componentes interativos na tela de atualização da lista de compra.
     * Configura os listeners e manipuladores de eventos para os elementos da tela, como botões e textviews.
     * As ações incluem exibir/ocultar seções, adicionar produtos à lista, compartilhar a lista com amigos e navegar para a tela de comparação de preços.
     */
    private fun DefinirAcoes(){
        // Configuração do listener para o toque no título da lista
        etTituloLista.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Configuração para o toque em opções ao lado do título da lista
                val drawableRight = etTituloLista.compoundDrawables[2] // Ícone no lado direito (índice 2)
                if (event.rawX >= (etTituloLista.right - drawableRight.bounds.width())) {
                    if(cvOpcoes.visibility === View.GONE){
                        cvOpcoes.visibility = View.VISIBLE
                    }
                    else{
                        cvOpcoes.visibility = View.GONE
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Configuração do listener para o clique no botão "Adicionar Item"
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

        // Configuração do listener para o clique no botão "Compartilhar"
        tvCompartilhar.setOnClickListener() { view ->
            cvCompartilharComAmigos.visibility = View.VISIBLE
            cvOpcoes.visibility = View.GONE
        }

        // Configuração do listener para o clique no botão "Comparar Preços"
        tvCompararPrecos.setOnClickListener() { view ->
            val bundle = Bundle()
            bundle.putParcelable("listaDeCompra", listaDeCompra)
            val intent = Intent(this@AtualizarListaDeCompraActivity, CompararPrecosActivity::class.java)
            Log.d("Teste", "Atualizar Lista de compra: " + listaDeCompra.produtos.size)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        // Configuração do listener para o clique no layout raiz para ocultar opções
        rootLayout.setOnClickListener { view ->
            if(cvOpcoes.visibility === View.VISIBLE){
                cvOpcoes.visibility = View.GONE
            }
        }

        // Configuração do listener para o clique no botão "Compartilhar" para ocultar a seção de compartilhamento com amigos
        btnCompartilhar.setOnClickListener() { view ->
            cvCompartilharComAmigos.visibility = View.GONE
        }
    }

    /**
     * Verifica a situação da lista de compra e atualiza a visibilidade dos elementos relacionados na tela.
     * Se a lista de compra estiver vazia, exibe uma mensagem indicando que a lista está vazia e oculta a RecyclerView.
     * Caso contrário, oculta a mensagem de lista vazia e exibe a RecyclerView com os itens da lista de compra.
     */
    private fun VerificarSituacaoLista() {
        if (listaDeCompra.produtos.size == 0) {
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    /**
     * Função responsável por compartilhar uma lista de compra com os amigos.
     */
    private fun CompartilharLista(){

        runBlocking {
            for(amigo in amigosCompartilhar){
                // Obtém o documento do usuário amigo
                var documentoUsuario = database.collection("usuario")
                        .document(amigo.id)

                // Adiciona a lista de compra ao array "listasDeCompra" do usuário amigo
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

    /**
     * Sobrescreve o método onBackPressed() para tratar o evento do botão "Voltar".
     */
    override fun onBackPressed() {
        // Oculta a visibilidade dos CardView opções e compartilhar com amigos
        if(cvOpcoes.visibility === View.VISIBLE){
            cvOpcoes.visibility = View.GONE
        }
        else if(cvCompartilharComAmigos.visibility === View.VISIBLE){
            cvCompartilharComAmigos.visibility = View.GONE
            amigosCompartilhar.clear()
        }
        else {
            // Exibe um diálogo de confirmação para salvar a lista
            var alertDialog = AlertDialog.Builder(this)
                    .setTitle("Salvar Lista?")
                    .setMessage("Deseja salvar a lista " + etTituloLista.text + "?")
                    .setPositiveButton("OK") { dialog, which ->
                        // Em caso afirmativo, chama função para atualizar os produtos no Firestore
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

    /**
     * Função responsável por atualizar os produtos no Firestore.
     */
    private fun AtualizarProdutos(){

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val tasks = mutableListOf<Deferred<Unit>>()
            val referenciasProdutos = mutableListOf<DocumentReference>()
            for (produto in listaDeCompra.produtos) {
                Log.d("Teste", "Produto: " + produto.codigoLocal)
                // Cria uma tarefa assíncrona para atualizar o produto no Firestore
                val task : Deferred<Unit> = async {
                    val documentoProduto =
                            FirebaseFirestore.getInstance().collection("produto").document(produto.id)
                    referenciasProdutos.add(documentoProduto)
                    documentoProduto.set(produto).await()
                }
                tasks.add(task)
            }
            // Aguarda o término de todas as tarefas assíncronas
            tasks.awaitAll()
            // Chama a função para atualizar a lista de compra
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

    /**
     * Função responsável por atualizar a lista de compra no Firestore.
     *
     * @param referenciasProdutos Lista de referências dos produtos atualizados.
     */
    private fun AtualizarListaDeCompra(referenciasProdutos : MutableList<DocumentReference>) {
        // Cria um mapa com os campos a serem atualizados na lista de compra
        val updates = hashMapOf<String, Any>(
                "id" to listaDeCompra.id,
                "produtos" to referenciasProdutos,
                "nome" to etTituloLista.text.toString(),
        )

        runBlocking {
            // Executa a atualização da lista de compra no Firestore
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

    /**
     * Função responsável por definir o adaptador e configurar o RecyclerView da lista de compra.
     */
    private fun DefinirAdaptador(){
        // Cria o adaptador com a lista de produtos da lista de compra
        adaptador = ProdutoListaDeCompraAdaptador(this, listaDeCompra.produtos)
        // Registra um observador para verificar a situação da lista de compra quando itens são removidos
        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                VerificarSituacaoLista()
            }
        })
        // Configura o RecyclerView
        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true
    }
}