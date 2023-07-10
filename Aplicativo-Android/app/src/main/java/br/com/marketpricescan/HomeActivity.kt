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

    private fun InicializarUsuario() {

        lateinit var listas : ArrayList<DocumentReference>
        lateinit var amigos : ArrayList<DocumentReference>

        // documentoUsuario = database.collection("usuario").document(usuarioId!!)
        documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                usuario = Usuario(documentSnapshot.getString("nome")!!, usuarioId)
                // listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>
               // usuario = Usuario(documentSnapshot.getString("nome")!!, usuarioId!!)
                listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>
                amigos = documentSnapshot.get("amigos") as ArrayList<DocumentReference>

                var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
                tvNomeUsuario.text = "Welcome, ${this.usuario.nome}!"

                // listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>
                AtualizarListasEmTempoReal()
                // InicializarListasDeCompraUsuario(listas) // transformar em função de corrotina
                AtualizarAmigosEmTempoReal()
                DefinirAcoes()
            }
        }
    }
    private fun AtualizarListasEmTempoReal() {
        documentoUsuario.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let{
                documentoUsuario = database.collection("usuario").document(usuarioId)
                documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        Log.d("Atualizacao", "------------------ Detectou")
                        val listas = documentSnapshot.get("listasDeCompra") as ArrayList<DocumentReference>
                        InicializarListasDeCompraUsuario(listas) // transformar em função de corrotina
                    }
                }
            }
        }
    }

    private fun AtualizarAmigosEmTempoReal() {
        documentoUsuario.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let{
                documentoUsuario = database.collection("usuario").document(usuarioId)
                documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        Log.d("Atualizacao", "------------------ Detectou")
                        val amigos = documentSnapshot.get("amigos") as ArrayList<DocumentReference>
                        InicializarAmigos(amigos) // transformar em função de corrotina
                    }
                }
            }
        }
    }
    private fun InicializarListasDeCompraUsuario(listas : ArrayList<DocumentReference>) {

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            runBlocking {
                usuario.listasDeCompra.clear()
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
            }

            PrepararCardViewMinhasListas()
        }
    }

    private fun InicializarAmigos(amigos : ArrayList<DocumentReference>){
        usuario.amigos.clear()
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val tasks = mutableListOf<Deferred<Unit>>()
            for (amigo in amigos) {
                val task = async {
                    val documentSnapshot =
                        amigo.get()
                            .await() // Await espera a conclusão da chamada assíncrona
                    if (documentSnapshot.exists()) {
                        val nome = documentSnapshot.getString("nome")!!
                        val id = documentSnapshot.id
                        usuario.amigos.add(Usuario(nome, id))
                    }
                }
                tasks.add(task)
            }
            // Aguarde a conclusão de todas as tarefas assíncronas
            tasks.awaitAll()
        }
        Log.d("Teste", "Cheguei até aqui")
    }

    private fun DefinirAcoes() {

        // CONFIGURAÇÕES

        cvConfiguracoes.setOnClickListener() { view ->
            if(cvConfiguracoesBackground.visibility === View.GONE){
                cvConfiguracoesBackground.visibility = View.VISIBLE
            }
            else{
                cvConfiguracoesBackground.visibility = View.GONE
            }
        }

        tvEditarConta.setOnClickListener() { view ->
            var intent = Intent(this, EditarUsuarioActivity::class.java)
            startActivity(intent)
            cvConfiguracoesBackground.visibility = View.GONE
        }

        tvListaDeAmigos.setOnClickListener() { view ->
            val bundle = Bundle()
            bundle.putParcelable("usuario", usuario)
            var intent = Intent(this, GerenciarAmigosActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
            cvConfiguracoesBackground.visibility = View.GONE
        }

        tvSairDaConta.setOnClickListener() { view ->
            FirebaseAuth.getInstance().signOut()
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        cvCriarNovaLista.setOnClickListener() { view ->
            val bundle = Bundle()
            bundle.putParcelable("usuario", usuario)
            var intent = Intent(this, CriarListaDeCompraActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
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

        cvCriarListaQRCode.setOnClickListener() { view ->
            if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
            } else {
                runBlocking {
                    VerificarDelecaoDeListas()
                }

                val intent = Intent(this, QRCodeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PERMISSION_GRANTED){
            val intent = Intent(this, QRCodeActivity::class.java)
            startActivity(intent)
            finish()
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

                VerificarDelecaoDeListas()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)

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

        rvMinhasListas.setHasFixedSize(true)
        rvMinhasListas.layoutManager = LinearLayoutManager(this)
        rvMinhasListas.adapter = adaptador
        rvMinhasListas.isClickable = true
    }

    private fun VerificarDelecaoDeListas(){
        var referenciasListas = mutableListOf<DocumentReference>()
        for(lista in usuario.listasDeCompra){
            Log.d("Teste", "Lista de compra conferida: ${lista.nome}")
            referenciasListas.add(database.collection("lista_de_compra").document(lista.id))
        }

        val updates = hashMapOf<String, Any>(
            "listasDeCompra" to referenciasListas,
        )

        documentoUsuario.update(updates).addOnSuccessListener {
            Log.d("Teste", "Substituição de lista feita com sucesso")
        }.addOnFailureListener {
            Log.d("Teste", "Substituição de lista falhou")
        }
    }
}