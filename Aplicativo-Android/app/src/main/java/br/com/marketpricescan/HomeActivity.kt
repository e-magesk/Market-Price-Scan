package br.com.marketpricescan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.floor

class HomeActivity : AppCompatActivity() {

    lateinit var cvMinhasListas: CardView
    lateinit var cvMinhasListasBackground: CardView
    lateinit var cvCriarNovaLista: CardView
    lateinit var minhasListas: ArrayList<ListaDeCompra>
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

        DefinirAcoes()

        InicializarUsuario()
    }

    private fun IniciarComponentes() {
        cvCriarNovaLista = findViewById(R.id.cvCriarNovaLista)
        cvMinhasListas = findViewById(R.id.cvMinhasListas)
        cvMinhasListasBackground = findViewById(R.id.cvMinhasListasBackground)
    }

    private fun InicializarUsuario() {

        documentoUsuario = database.collection("usuario").document(usuarioId!!)
        documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                Log.d("Teste", "Usuario: ${documentSnapshot}")
                this.usuario = Usuario(documentSnapshot.toObject(Usuario::class.java)!!)
            }

            var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
            tvNomeUsuario.text = "Welcome, ${this.usuario.nome}!"

            InicializarListasDeCompraUsuario()
        }
    }

    private fun InicializarListasDeCompraUsuario() {
        Log.d("Teste", "Cheguei nas listas")
//        documentoListaDeCompra = database.collection("lista_de_compra").document()
//        Log.d("Teste", "Documento: ${documentoListaDeCompra}")
//        documentoListaDeCompra.get().addOnSuccessListener { documentSnapshot ->
//            if(documentSnapshot.exists()){
//                Log.d("Teste", "Lista de Compra: ${documentSnapshot.toObject(ListaDeCompra::class.java)}")
//                this.usuario.listas.add(ListaDeCompra(documentSnapshot.toObject(ListaDeCompra::class.java)!!))
//            }
//        }

        database.collection("lista_de_compra")
            .get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    this.usuario.listas.add(ListaDeCompra(document.toObject(ListaDeCompra::class.java)!!))
                }
            }
    }

    private fun DefinirAcoes() {
        cvCriarNovaLista.setOnClickListener() { view ->
            var intent = Intent(this, ListaDeCompraActivity::class.java)
            startActivity(intent)
        }

        cvMinhasListas.setOnClickListener { view ->
            var layout = cvMinhasListasBackground.layoutParams
            val density = resources.displayMetrics.density
            var heightCv = cvMinhasListasBackground.height

            if (heightCv > floor(60 * density)) {
                layout.height = (floor(60 * density)).toInt()
            } else {
                layout.height = (heightCv + floor(60 * density)).toInt()
            }
            cvMinhasListasBackground.layoutParams = layout
        }
    }
}