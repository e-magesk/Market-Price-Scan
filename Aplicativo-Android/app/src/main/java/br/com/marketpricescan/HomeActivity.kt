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

    lateinit var cvMinhasListas : CardView
    lateinit var cvMinhasListasBackground : CardView
    lateinit var cvCriarNovaLista : CardView
    lateinit var minhasListas : ArrayList<ListaDeCompra>
    private val database : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId : String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario : DocumentReference
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

    private fun IniciarComponentes(){
        cvCriarNovaLista = findViewById(R.id.cvCriarNovaLista)
        cvMinhasListas = findViewById(R.id.cvMinhasListas)
        cvMinhasListasBackground = findViewById(R.id.cvMinhasListasBackground)
    }

    private fun InicializarUsuario(){

        documentoUsuario = database.collection("usuario").document(usuarioId!!)
        documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
            Log.d("Teste", "Entrei")
//            if(documentSnapshot.exists()){
//                usuario.nome = documentSnapshot.get("nome") as String
//            }
        }
//
//        var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
//        tvNomeUsuario.text = "Welcome, ${usuario?.nome}!"

//        var listas = documentoUsuario.collection("lista_de_compra").get()
//                .addOnSuccessListener { querySnapshot ->
//                    // O sucesso da consulta
//                    for (document in querySnapshot.documents) {
//                        // Processar os documentos da coleção interna
//                        Log.d("Teste", document.data?.values.toString())
//                        val data = document.data
//                        // ...
//                    }
//                }

    }

    private fun DefinirAcoes(){
        cvCriarNovaLista.setOnClickListener(){view ->
            var intent = Intent(this, ListaDeCompraActivity::class.java)
            startActivity(intent)
        }

        cvMinhasListas.setOnClickListener { view ->
            var layout = cvMinhasListasBackground.layoutParams
            val density = resources.displayMetrics.density
            var heightCv = cvMinhasListasBackground.height

            if(heightCv > floor(60 * density)){
                layout.height = (floor(60 * density)).toInt()
            }
            else{
                layout.height = (heightCv + floor(60 * density)).toInt()
            }
            cvMinhasListasBackground.layoutParams = layout
        }
    }
}