package br.com.marketpricescan

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ListaDeCompraAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
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
import kotlin.math.floor

/**
 * Classe responsável por controlar a tela inicial da aplicação.
 */
class HomeActivity : AppCompatActivity() {

    lateinit var cvMinhasListas: CardView
    lateinit var cvMinhasListasBackground: CardView
    lateinit var cvCriarNovaLista: CardView
    lateinit var cvConfiguracoes : CardView
    lateinit var cvConfiguracoesBackground : CardView
    lateinit var rvMinhasListas : RecyclerView
    lateinit var tvEditarConta : TextView
    lateinit var tvListaDeAmigos : TextView
    lateinit var tvSairDaConta : TextView
    private lateinit var adaptador: ListaDeCompraAdaptador
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private var documentoUsuario: DocumentReference = database.collection("usuario").document(usuarioId!!)

    private lateinit var usuario: Usuario
    private var flagExibindoMinhasListas : Boolean = false

    lateinit var cvCriarListaQRCode: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val loadingCard = findViewById<CardView>(R.id.loadingPageHome)

        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            IniciarComponentes()

            InicializarUsuario()

            delay(3000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    /**
     * Função responsável por inicializar os componentes da tela.
     */
    private fun IniciarComponentes() {
        cvConfiguracoes = findViewById(R.id.cvConfiguracoes)
        cvCriarNovaLista = findViewById(R.id.cvCriarNovaLista)
        cvMinhasListas = findViewById(R.id.cvMinhasListas)
        cvMinhasListasBackground = findViewById(R.id.cvMinhasListasBackground)
        cvConfiguracoesBackground = findViewById(R.id.cvConfiguracoesBackground)
        rvMinhasListas = findViewById(R.id.rvMinhasListas)
        cvCriarListaQRCode = findViewById(R.id.cvCriarListaQRCode)
        tvEditarConta = findViewById(R.id.tvEditarConta)
        tvListaDeAmigos = findViewById(R.id.tvListaDeAmigos)
        tvSairDaConta = findViewById(R.id.tvSairDaConta)

        cvCriarNovaLista.isClickable = true
        cvMinhasListas.isClickable = true
        cvConfiguracoes.isClickable = true
    }

    /**
     * Função responsável por inicializar o usuário.
     */
    private fun InicializarUsuario() {

        lateinit var listas : ArrayList<DocumentReference>
        lateinit var amigos : ArrayList<DocumentReference>

        documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Criar objeto Usuario com base nos dados do documento
                usuario = Usuario(documentSnapshot.getString("nome")!!, usuarioId)

                listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>
                amigos = documentSnapshot.get("amigos") as ArrayList<DocumentReference>

                var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
                tvNomeUsuario.text = "Welcome, ${this.usuario.nome}!"

                // Chama funções para atualizar as listas de compras e amigos tempo real
                AtualizarListasEmTempoReal()
                AtualizarAmigosEmTempoReal()
                // Chama função para definir ações da interface
                DefinirAcoes()
            }
        }
    }
    /**
     * Atualiza as listas de compra do usuário em tempo real, sempre que houver alguma alteração no banco de dados.
     */
    private fun AtualizarListasEmTempoReal() {
        // Adiciona um listener para capturar mudanças nos dados do documento
        documentoUsuario.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let{
                // Obtém uma referência atualizada do documento do usuário
                documentoUsuario = database.collection("usuario").document(usuarioId)
                documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Obtém as referências das listas de compra do usuario
                        val listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>

                        // Chama função para inicializar as listas de compra do usuário
                        InicializarListasDeCompraUsuario(listas)
                    }
                }
            }
        }
    }

    /**
     * Atualiza as lista de amigos do usuário em tempo real, sempre que houver alguma alteração no banco de dados.
     */
    private fun AtualizarAmigosEmTempoReal() {
        // Adiciona um listener para capturar mudanças nos dados do documento
        documentoUsuario.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let{
                // Obtém uma referência atualizada do documento do usuário
                documentoUsuario = database.collection("usuario").document(usuarioId)
                documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Obtém as referências dos amigos do usuario
                        val amigos = documentSnapshot.get("amigos") as ArrayList<DocumentReference>
                        // Chama função para inicializar os amigos
                        InicializarAmigos(amigos)
                    }
                }
            }
        }
    }

    /**
     * Inicializa as listas de compra do usuário com base nas referências fornecidas.
     *
     * @param listas As referências das listas de compra.
     */
    private fun InicializarListasDeCompraUsuario(listas : ArrayList<DocumentReference>) {

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            runBlocking {
                usuario.listasDeCompra.clear()
                val tasks = mutableListOf<Deferred<Unit>>()
                for (lista in listas) {
                    val task = async {
                        // Obtém o documento da lista de compra
                        val documentSnapshot =
                            lista.get().await() // Await espera a conclusão da chamada assíncrona
                        if (documentSnapshot.exists()) {
                            // Extrai os dados do documento
                            val nome = documentSnapshot.getString("nome")!!
                            val id = documentSnapshot.id
                            val listaDeCompra = ListaDeCompra(nome, id)
                            // Adiciona a lista de compra à lista do usuário
                            usuario.listasDeCompra.add(listaDeCompra)
                        }
                    }
                    tasks.add(task)
                }
                // Aguarda a conclusão de todas as tarefas assíncronas
                tasks.awaitAll()
            }

            PrepararCardViewMinhasListas()
        }
    }

    /**
     * Inicializa os amigos do usuário com base nas referências fornecidas.
     *
     * @param amigos As referências dos amigos.
     */
    private fun InicializarAmigos(amigos : ArrayList<DocumentReference>){
        usuario.amigos.clear()
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val tasks = mutableListOf<Deferred<Unit>>()
            for (amigo in amigos) {
                val task = async {
                    // Obtém o documento do amigo
                    val documentSnapshot =
                        amigo.get()
                            .await() // Await espera a conclusão da chamada assíncrona
                    if (documentSnapshot.exists()) {
                        // Extrai os dados do documento
                        val nome = documentSnapshot.getString("nome")!!
                        val id = documentSnapshot.id
                        // Adiciona o amigo à lista de amigos do usuário
                        usuario.amigos.add(Usuario(nome, id))
                    }
                }
                tasks.add(task)
            }
            // Aguarda a conclusão de todas as tarefas assíncronas
            tasks.awaitAll()
        }
    }

    /**
     * Função responsável por definir as ações dos elementos da interface.
     */
    private fun DefinirAcoes() {
        // Define a ação quando o usuário tocar no campo de Configurações
        cvConfiguracoes.setOnClickListener() { view ->
            // Altera visibilidade da lista de opçoes de configuração
            if(cvConfiguracoesBackground.visibility === View.GONE){
                cvConfiguracoesBackground.visibility = View.VISIBLE
            }
            else{
                cvConfiguracoesBackground.visibility = View.GONE
            }
        }

        // Define a ação quando o usuário tocar no campo de Editar conta
        tvEditarConta.setOnClickListener() { view ->
            var intent = Intent(this, EditarUsuarioActivity::class.java)
            startActivity(intent)
            cvConfiguracoesBackground.visibility = View.GONE
        }

        // Define a ação quando o usuário tocar no campo de Lista de Amigos
        tvListaDeAmigos.setOnClickListener() { view ->
            val bundle = Bundle()
            bundle.putParcelable("usuario", usuario)
            var intent = Intent(this, GerenciarAmigosActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
            cvConfiguracoesBackground.visibility = View.GONE
        }

        // Define a ação quando o usuário tocar no campo de Sair da Conta
        tvSairDaConta.setOnClickListener() { view ->
            // Faz logout do usuário atual
            FirebaseAuth.getInstance().signOut()

            // Cria uma intent para abrir a atividade de login
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Define a ação quando o usuário tocar no campo de Criar Nova Lista
        cvCriarNovaLista.setOnClickListener() { view ->
            val bundle = Bundle()
            bundle.putParcelable("usuario", usuario)
            var intent = Intent(this, CriarListaDeCompraActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        // Define a ação quando o usuário tocar no campo de Minhas Listas
        cvMinhasListas.setOnClickListener { view ->
            // Define a exibição da lista de listas de compra
            if(flagExibindoMinhasListas) {
                var layout = cvMinhasListasBackground.layoutParams
                val density = resources.displayMetrics.density
                layout.height = (floor(60 * density)).toInt()
                cvMinhasListasBackground.layoutParams = layout
                flagExibindoMinhasListas = false
            }
            else{
                var layout = cvMinhasListasBackground.layoutParams
                layout.height = RecyclerView.LayoutParams.WRAP_CONTENT
                cvMinhasListasBackground.layoutParams = layout
                adaptador.notifyDataSetChanged()
                flagExibindoMinhasListas = true
            }
        }

        // Define a ação quando o usuário tocar no campo de Criar Lista QR Code
        cvCriarListaQRCode.setOnClickListener() { view ->
            // Caso permissão da câmera não esteja habilitada, faz a requisição.
            if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
            } else { // Permissão concedida
                // Verifica se há listas para serem deletadas
                runBlocking {
                    VerificarDelecaoDeListas()
                }

                // Cria uma intent para abrir a atividade de QR Code
                val intent = Intent(this, QRCodeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    /**
     * Função que é chamada quando o resultado de uma solicitação de permissão é retornado.
     * Função utilizada na requisição de uso da câmera.
     *
     * @param requestCode O código de solicitação fornecido ao solicitar permissão.
     * @param permissions As permissões solicitadas.
     * @param grantResults Os resultados das solicitações de permissão.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PERMISSION_GRANTED){
            // Se a permissão foi concedida, cria uma intent para abrir a atividade de QR Code
            val intent = Intent(this, QRCodeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Prepara o CardView para exibir as listas de compras do usuário.
     * Configura o adaptador, o layout manager e o clique nos itens da RecyclerView.
     * Verifica a remoção e alteração de itens para executar ações específicas.
     */
    private fun PrepararCardViewMinhasListas(){
        adaptador = ListaDeCompraAdaptador(this, usuario.listasDeCompra)
        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            /**
             * Chamado quando os itens da lista são removidos.
             *
             * @param positionStart A posição inicial dos itens removidos.
             * @param itemCount O número de itens removidos.
             */
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.d("Teste", "Entrei para deletar")
                // Atualiza a altura do layout do CardView com base no número de listas de compras
                var layout = cvMinhasListasBackground.layoutParams
                val density = resources.displayMetrics.density
                layout.height = (floor(60 * density) * (usuario.listasDeCompra.size + 1)).toInt()
                cvMinhasListasBackground.layoutParams = layout

                // Verifica se há listas para serem deletadas
                VerificarDelecaoDeListas()
            }

            /**
             * Chamado quando os itens da lista são alterados.
             *
             * @param positionStart A posição inicial dos itens alterados.
             * @param itemCount O número de itens alterados.
             * @param payload Dados adicionais sobre a alteração.
             */
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                // Verifica se uma lista de compras foi selecionada
                if(payload.toString() == "selected"){
                    val bundle = Bundle()
                    bundle.putParcelable("listaDeCompra", usuario.listasDeCompra[positionStart])
                    bundle.putParcelable("usuario", usuario)
                    val intent = Intent(this@HomeActivity, AtualizarListaDeCompraActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                    finish()
                }
            }
        })

        // Configura a RecyclerView
        rvMinhasListas.setHasFixedSize(true)
        rvMinhasListas.layoutManager = LinearLayoutManager(this)
        rvMinhasListas.adapter = adaptador
        rvMinhasListas.isClickable = true
    }

    /**
    * Verifica se há listas de compras para deleção e realiza as ações necessárias.
    */
    private fun VerificarDelecaoDeListas(){
        // Cria uma lista de referências dos documentos das listas de compras do usuário.
        var referenciasListas = mutableListOf<DocumentReference>()
        for(lista in usuario.listasDeCompra){
            Log.d("Teste", "Lista de compra conferida: ${lista.nome}")
            referenciasListas.add(database.collection("lista_de_compra").document(lista.id))
        }

        // Cria um mapa de atualizações contendo a lista de referências
        val updates = hashMapOf<String, Any>(
            "listasDeCompra" to referenciasListas,
        )

        //   Atualiza o documento do usuário no Firestore substituindo a lista de referências.
        documentoUsuario.update(updates).addOnSuccessListener {
            Log.d("Teste", "Substituição de lista feita com sucesso")
        }.addOnFailureListener {
            Log.d("Teste", "Substituição de lista falhou")
        }
    }
}