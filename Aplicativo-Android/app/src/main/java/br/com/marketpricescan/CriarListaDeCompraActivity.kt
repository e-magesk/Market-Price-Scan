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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Activity responsável por criar uma nova lista de compra.
 */
class CriarListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var etTituloLista: TextView
    private lateinit var btnCompartilhar: Button
    private lateinit var cvCompartilharComAmigos : CardView
    private lateinit var rvCompartilharComAmigos : RecyclerView
    private lateinit var adaptadorUsuariosCompartilhar : UsuarioCompartilharAdaptador
    private lateinit var adaptadorProdutos: ProdutoListaDeCompraAdaptador
    private lateinit var listaDeCompra: ListaDeCompra
    private var produtos = mutableListOf<Produto>()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private var amigosCompartilhar = mutableListOf<Usuario>()
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var usuario: Usuario
    private lateinit var documentoListaDeCompra: DocumentReference
    private lateinit var loadingCard : CardView
    private lateinit var rootLayout : ConstraintLayout
    private lateinit var tvCompartilhar : TextView
    private lateinit var tvCompararPrecos : TextView
    private lateinit var cvOpcoes : CardView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        loadingCard = findViewById<CardView>(R.id.loadingPageListaDeCompra)
        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            // Obtém o objeto "usuario" enviado pela activity HomeActivity
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

            delay(500)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    /**
     * Inicializa os componentes da tela de cadastro de usuário.
     * Associa as variáveis locais aos elementos de layout correspondentes através de seus IDs.
     */
    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
        etTituloLista = findViewById(R.id.etTituloLista)
        btnCompartilhar = findViewById(R.id.btnCompartilhar)
        rootLayout = findViewById(R.id.rootLayoutListaDeCompra)
        cvOpcoes = findViewById(R.id.cvOpcoes)
        rvCompartilharComAmigos = findViewById(R.id.rvCompartilharComAmigos)
        cvCompartilharComAmigos = findViewById(R.id.cvCompartilharComAmigos)
        tvCompararPrecos = findViewById(R.id.tvCompararPrecos)
        tvCompartilhar = findViewById(R.id.tvCompartilhar)
        listaDeCompra = ListaDeCompra("")
    }

    /**
     * Função responsável por exibir a lista de compras de acordo com sua situação (vazia ou não).
     */
    private fun VerificarSituacaoLista() {
        if (produtos.size == 0) {
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    /**
     * Função responsável por vincular a lista de compras ao usuário.
     */
    private fun VincularListaAoUsuario() {
        // Obtém o documento do usuário a partir do ID
        documentoUsuario = database.collection("usuario")
            .document(usuarioId)

        // Atualiza o campo "listasDeCompra" do documento do usuário, adicionando o documento da lista de compras
        documentoUsuario.update("listasDeCompra", FieldValue.arrayUnion(documentoListaDeCompra))
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao vincular a lista ao usuário")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao vincular a lista ao usuário")
            }

    }

    /**
     * Função responsável por definir as ações dos componentes da tela.
     */
    private fun DefinirAcoes(){
        // Define a ação quando o usuário tocar no campo de título da lista
        etTituloLista.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etTituloLista.compoundDrawables[2] // Ícone no lado direito (índice 2)
                // Configuração de visibilidade para o toque em opções ao lado do título da lista
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

        // Define a ação quando o usuário tocar em qualquer área da tela
        rootLayout.setOnClickListener { view ->
            // Esconde as opções caso estejam visíveis
            if(cvOpcoes.visibility === View.VISIBLE){
                cvOpcoes.visibility = View.GONE
            }
        }

        // Define a ação quando o usuário clicar no botão "Comparar Preços"
        tvCompararPrecos.setOnClickListener() { view ->
            val intent = Intent(this, CompararPrecosActivity::class.java)
            intent.putExtra("listaDeCompra", listaDeCompra)
            startActivity(intent)
        }

        // Define a ação quando o usuário clicar no botão "Compartilhar"
        tvCompartilhar.setOnClickListener() { view ->
            // Esconde as opções e exibe o componente para compartilhar com amigos
            cvOpcoes.visibility = View.GONE
            cvCompartilharComAmigos.visibility = View.VISIBLE
        }

        // Define a ação quando o usuário clicar no botão "Adicionar Item"
        btnAdicionarItem.setOnClickListener() { view ->
            produtos.add(Produto("" ))
            adaptadorProdutos.notifyDataSetChanged()
            rvListaDeCompra.smoothScrollToPosition(adaptadorProdutos.itemCount - 1)
            VerificarSituacaoLista()

        }

        // Configuração do listener para o clique no botão "Compartilhar" para ocultar a seção de compartilhamento com amigos
        btnCompartilhar.setOnClickListener() { view ->
            cvCompartilharComAmigos.visibility = View.GONE
        }

    }

    /**
     * Sobrescreve o método onBackPressed() para tratar o evento de pressionar o botão "Voltar".
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
                    // Em caso afirmativo, chama função para criar os produtos no Firestore
                    runBlocking {
                        CriarProdutos()
                    }

                    // Delay para carregamento
                    loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso
                    val coroutineScope = CoroutineScope(Dispatchers.Main)
                    coroutineScope.launch {
                        delay(1000)
                        loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
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
     * Função responsável por criar e salvar os produtos no banco de dados.
     */
    private fun CriarProdutos(){

        if(produtos.size == 0){
            runBlocking {
                // Salva a lista de compras vazia no banco de dados
                SalvarListaDeCompra(mutableListOf<DocumentReference>())
            }
            return
        }

        var referenciasProdutos = mutableListOf<DocumentReference>()
        var flagFinal = 0
        for(produto in produtos){
            // Cria um novo documento para cada produto na lista de produtos
            var documentoProduto = FirebaseFirestore.getInstance().collection("produto")
                .document()
            produto.id = documentoProduto.id
            referenciasProdutos.add(documentoProduto)
            documentoProduto.set(produto)
                .addOnSuccessListener {
                    flagFinal++
                    // Verifica se todos os produtos foram salvos
                    if (flagFinal == produtos.size){
                        // Salva a lista de compras no banco de dados
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

    /**
     * Função responsável por salvar a lista de compra no banco de dados.
     * @param referenciaProdutos Lista de referências aos documentos dos produtos.
     */
    private fun SalvarListaDeCompra(referenciaProdutos: MutableList<DocumentReference>){
        // Cria um novo documento para a lista de compra
        documentoListaDeCompra = database.collection("lista_de_compra")
            .document()

        // Cria um mapa com os dados a serem salvos
        val updates = hashMapOf<String, Any>(
            "produtos" to referenciaProdutos,
            "nome" to etTituloLista.text.toString(),
            "id" to documentoListaDeCompra.id
        )

        // Salva os dados da lista de compra no documento
        documentoListaDeCompra.set(updates)
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao criar a lista no banco de dados")
                // Vincula lista de compras com o usuário e compartilha com amigos.
                runBlocking {
                    VincularListaAoUsuario()
                    CompartilharLista()
                }
            }
            .addOnFailureListener { e ->
                Log.d("Teste", "Erro ao criar a lista no banco de dados")
            }
    }

    /**
     * Função responsável por compartilhar a lista de compra com os amigos selecionados.
     */
    private fun CompartilharLista(){

        for(amigo in amigosCompartilhar){
            documentoUsuario = database.collection("usuario")
                .document(amigo.id)

            // Adiciona a lista de compra ao array de listas de compra do amigo
            documentoUsuario.update("listasDeCompra", FieldValue.arrayUnion(documentoListaDeCompra))
                .addOnSuccessListener {
                    Log.d("Teste", "Sucesso ao compartilhar a lista de compra")
                }
                .addOnFailureListener {
                    Log.d("Teste", "Erro ao compartilhar a lista de compra")
                }
        }

    }

    /**
     * Função responsável por definir o adaptador para a lista de produtos exibida no RecyclerView.
     */
    private fun DefinirAdaptadorProdutos(){
        adaptadorProdutos = ProdutoListaDeCompraAdaptador(this, produtos)
        // Registra um observer para monitorar remoções de itens do adaptador
        adaptadorProdutos.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                // Verifica a situação da lista após a remoção de itens
                VerificarSituacaoLista()
            }
        })

        // Configura o RecyclerView
        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptadorProdutos
        rvListaDeCompra.isClickable = true
    }

    /**
     * Função responsável por definir o adaptador para a lista de usuários disponíveis para compartilhar.
     */
    private fun DefinirAdaptadorUsuariosCompartilhar(){

        adaptadorUsuariosCompartilhar = UsuarioCompartilharAdaptador(this, usuario.amigos)

        // Registra um observer para monitorar alterações nos itens do adaptador
        adaptadorUsuariosCompartilhar.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                // Verifica o payload para determinar se o usuário foi selecionado ou desmarcado.
                // Insere ou remove o amigo da lista de amigos de acordo com o payload.
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


}