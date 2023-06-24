package br.com.marketpricescan

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ListaDeCompraAdaptador
import com.google.firebase.auth.FirebaseAuth
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

class HomeActivity : AppCompatActivity() {

    lateinit var cvMinhasListas: CardView
    lateinit var cvMinhasListasBackground: CardView
    lateinit var cvCriarNovaLista: CardView
    lateinit var rvMinhasListas : RecyclerView
    private lateinit var adaptador: ListaDeCompraAdaptador
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var usuario: Usuario
    private var flagExibindoMinhasListas : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val loadingCard = findViewById<CardView>(R.id.loadingPageHome)

        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            IniciarComponentes()

            cvCriarNovaLista.isClickable = true
            cvMinhasListas.isClickable = true

            InicializarUsuario()

            delay(2000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    private fun IniciarComponentes() {
        cvCriarNovaLista = findViewById(R.id.cvCriarNovaLista)
        cvMinhasListas = findViewById(R.id.cvMinhasListas)
        cvMinhasListasBackground = findViewById(R.id.cvMinhasListasBackground)
        rvMinhasListas = findViewById(R.id.rvMinhasListas)
    }

    private fun InicializarUsuario() {

        lateinit var listas : ArrayList<DocumentReference>

        documentoUsuario = database.collection("usuario").document(usuarioId!!)
        documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                usuario = Usuario(documentSnapshot.getString("nome")!!)
                listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>

                var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
                tvNomeUsuario.text = "Welcome, ${this.usuario.nome}!"

                InicializarListasDeCompraUsuario(listas) // transformar em função de corrotina

                DefinirAcoes()
            }
        }
    }

    private fun InicializarListasDeCompraUsuario(listas : ArrayList<DocumentReference>) {

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val tasks = mutableListOf<Deferred<Unit>>()
            for (lista in listas) {
                val task = async {
                    val documentSnapshot =
                        lista.get().await() // Await espera a conclusão da chamada assíncrona
                    if (documentSnapshot.exists()) {
                        val nome = documentSnapshot.getString("nome")!!
                        val id = documentSnapshot.id
                        val listaDeCompra = ListaDeCompra(nome, id)
                        usuario.listasDeCompra.add(listaDeCompra)
                    }
                }
                tasks.add(task)
            }
            // Aguarde a conclusão de todas as tarefas assíncronas
            tasks.awaitAll()

            PrepararCardViewMinhasListas()
        }
    }

    private fun DefinirAcoes() {
        cvCriarNovaLista.setOnClickListener() { view ->
            runBlocking {
                VerificarDelecaoDeListas()
            }
            var intent = Intent(this, CriarListaDeCompraActivity::class.java)
            startActivity(intent)
            finish()
        }

        cvMinhasListas.setOnClickListener { view ->
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
    }

    private fun PrepararCardViewMinhasListas(){
        adaptador = ListaDeCompraAdaptador(this, usuario.listasDeCompra)
        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.d("Teste", "Entrei para deletar")
                var layout = cvMinhasListasBackground.layoutParams
                val density = resources.displayMetrics.density
                layout.height = (floor(60 * density) * (usuario.listasDeCompra.size + 1)).toInt()
                cvMinhasListasBackground.layoutParams = layout
            }
        })
        rvMinhasListas.setHasFixedSize(true)
        rvMinhasListas.layoutManager = LinearLayoutManager(this)
        rvMinhasListas.adapter = adaptador
        rvMinhasListas.isClickable = true
    }

    override fun onBackPressed() {
        VerificarDelecaoDeListas()
        finish()
    }

    private fun VerificarDelecaoDeListas(){
        var referenciasListas = mutableListOf<DocumentReference>()
        for(lista in usuario.listasDeCompra){
            referenciasListas.add(database.collection("lista_de_compra").document(lista.id))
        }

        val updates = hashMapOf<String, Any>(
            "listasDeCompra" to referenciasListas,
        )

        runBlocking {
            documentoUsuario.update(updates).addOnSuccessListener {
                Log.d("Teste", "Substituição de lista feita com sucesso")
            }.addOnFailureListener {
                Log.d("Teste", "Substituição de lista falhou")
            }
        }
    }
}