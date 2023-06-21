package br.com.marketpricescan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ItemListaAdaptador
import br.com.marketpricescan.util.ListaAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor

class HomeActivity : AppCompatActivity() {

    lateinit var cvMinhasListas: CardView
    lateinit var cvMinhasListasBackground: CardView
    lateinit var cvCriarNovaLista: CardView
    lateinit var rvMinhasListas : RecyclerView
    private lateinit var adaptador: ListaAdaptador
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var documentoListaDeCompra: DocumentReference
    private lateinit var usuario: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        IniciarComponentes()

        cvCriarNovaLista.isClickable = true
        cvMinhasListas.isClickable = true

        InicializarUsuario()

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
                listas = documentSnapshot.get("listas") as ArrayList<DocumentReference>

                var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
                tvNomeUsuario.text = "Welcome, ${this.usuario.nome}!"

                InicializarListasDeCompraUsuario(listas)

            }
        }
    }

    private fun InicializarListasDeCompraUsuario(listas : ArrayList<DocumentReference>) {
        for(lista in listas){
            lista.get().addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot.exists()){
                    this.usuario.adicionarLista(ListaDeCompra(documentSnapshot.toObject(ListaDeCompra::class.java)!!))
                    DefinirAcoes()
                }
            }
        }
        PrepararCardViewMinhasListas()
    }

    private fun DefinirAcoes() {
        cvCriarNovaLista.setOnClickListener() { view ->
            var intent = Intent(this, ListaDeCompraActivity::class.java)
            startActivity(intent)
        }

        Log.d("Teste", usuario.listas.toString())
        Log.d("Teste", usuario.listas.size.toString())

        cvMinhasListas.setOnClickListener { view ->
            var layout = cvMinhasListasBackground.layoutParams
            val density = resources.displayMetrics.density
            var heightCv = cvMinhasListasBackground.height
            if (heightCv > floor(60 * density)) {
                layout.height = (floor(60 * density * usuario.listas.size)).toInt()
            } else {
                layout.height = (heightCv + floor(60 * density)).toInt()
            }
            cvMinhasListasBackground.layoutParams = layout
        }
    }

    private fun PrepararCardViewMinhasListas(){
        adaptador = ListaAdaptador(this, usuario.listas)
        rvMinhasListas.setHasFixedSize(true)
        rvMinhasListas.layoutManager = LinearLayoutManager(this)
        rvMinhasListas.adapter = adaptador
        rvMinhasListas.isClickable = true
    }
}