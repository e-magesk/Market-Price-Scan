package br.com.marketpricescan

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.Usuario
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GerenciarAmigosActivity : AppCompatActivity() {

    private lateinit var cbId : CheckBox
    private lateinit var cbNome : CheckBox
    private lateinit var actvBuscarAmigos : AutoCompleteTextView
    private lateinit var tvListaDeAmigosVazia : TextView
    private lateinit var rvListaDeAmigos : RecyclerView
    private lateinit var usuario: Usuario
    private lateinit var documentoUsuario: DocumentReference
    private val database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gerenciar_amigos)

        Log.d("TESTE", "onCreate: Cheguei aqui pelo menos")

        val loadingCard = findViewById<CardView>(R.id.loadingPageAdicionarAmigos)

        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                usuario = intent.getParcelableExtra("usuario", Usuario::class.java)!!
            } else {
                usuario = intent.getParcelableExtra<Usuario>("usuario")!!
            }

            Log.d("TESTE", "onCoroutine: Receive the user " + usuario.nome + " with id " + usuario.id)

            IniciarComponentes()

            BuscarAmigosUsuario()

            DefinirAcoes()

            delay(2000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    private fun IniciarComponentes(){
        cbId = findViewById(R.id.cbId)
        cbNome = findViewById(R.id.cbNome)
        actvBuscarAmigos = findViewById(R.id.actvBuscarAmigos)
        tvListaDeAmigosVazia = findViewById(R.id.tvListaDeAmigosVazia)
        rvListaDeAmigos = findViewById(R.id.rvListaDeAmigos)
    }

    private fun BuscarAmigosUsuario() {
        lateinit var amigos: ArrayList<DocumentReference>

        documentoUsuario = database.collection("usuario").document(usuario.id)
        documentoUsuario.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                amigos = documentSnapshot.get("amigos") as ArrayList<DocumentReference>

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

                    VerificarSituacaoListaDeAmigos()
                }
            }
        }
    }

    private fun DefinirAcoes(){

        cbId.setOnCheckedChangeListener { _, _ ->
            if(cbId.isChecked && cbNome.isChecked){
                cbNome.isChecked = false
            }
        }

        cbNome.setOnCheckedChangeListener { _, _ ->
            if(cbNome.isChecked && cbId.isChecked){
                cbId.isChecked = false
            }
        }

    }

    private fun VerificarSituacaoListaDeAmigos(){
        if(usuario.amigos.size === 0){
            tvListaDeAmigosVazia.visibility = View.VISIBLE
            rvListaDeAmigos.visibility = View.GONE
        }
        else{
            tvListaDeAmigosVazia.visibility = View.GONE
            rvListaDeAmigos.visibility = View.VISIBLE
        }
    }
}