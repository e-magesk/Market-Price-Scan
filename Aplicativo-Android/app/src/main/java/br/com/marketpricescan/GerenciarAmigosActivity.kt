package br.com.marketpricescan

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ListaDeCompraAdaptador
import br.com.marketpricescan.util.UsuarioAdaptador
import br.com.marketpricescan.util.UsuarioArrayAdaptador
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
import kotlinx.coroutines.tasks.await
import kotlin.math.floor

class GerenciarAmigosActivity : AppCompatActivity() {

    private lateinit var cbId : CheckBox
    private lateinit var cbNome : CheckBox
    private lateinit var actvBuscarAmigos : AutoCompleteTextView
    private lateinit var tvListaDeAmigosVazia : TextView
    private lateinit var rvListaDeAmigos : RecyclerView
    private lateinit var usuario: Usuario
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var adaptador: UsuarioAdaptador
    private val database = FirebaseFirestore.getInstance()
    private val usuarios = FirebaseFirestore.getInstance().collection("usuario")

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

                    PrepararExibicaoListaDeAmigos()
                }
            }
        }
    }

    private fun DefinirAcoes(){

        cbId.setOnCheckedChangeListener { _, _ ->
            if(cbId.isChecked && cbNome.isChecked){
                cbNome.isChecked = false
            }
            else if(!cbId.isChecked && !cbNome.isChecked){
                cbId.isChecked = true
            }
            actvBuscarAmigos.hint = if(cbId.isChecked) "Buscar usuario por ID" else "Buscar usuario por nome"
        }

        cbNome.setOnCheckedChangeListener { _, _ ->
            if(cbNome.isChecked && cbId.isChecked){
                cbId.isChecked = false
            }
            else if(!cbId.isChecked && !cbNome.isChecked){
                cbId.isChecked = true
            }
            actvBuscarAmigos.hint = if(cbId.isChecked) "Buscar usuario por ID" else "Buscar usuario por nome"
        }

        actvBuscarAmigos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                // Aqui você pode chamar a função para buscar sugestões no Firebase Firestore com base no texto digitado
                if(cbId.isChecked)
                    BuscarUsuariosPorId(texto)
                else if(cbNome.isChecked)
                    BuscarUsuariosPorNome(texto)
            }
        })

        actvBuscarAmigos.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val objetoCompleto = parent.getItemAtPosition(position) as Usuario
                PopUpConfirmacaoAdicionarAmigo(objetoCompleto)
            }

    }

    private fun BuscarUsuariosPorNome(texto : String){
        usuarios.whereGreaterThanOrEqualTo("nome", texto.uppercase())
            .orderBy("nome")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val sugestoes = mutableListOf<Usuario>()
                for (document in documents) {
                    val nome = document.getString("nome")!!
                    val id = document.id
                    val usuario = Usuario(nome, id)
                    sugestoes.add(usuario)
                }
                val adapter = UsuarioArrayAdaptador(this, sugestoes)
                actvBuscarAmigos.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                actvBuscarAmigos.setError("Nenhum usuário encontrado")
            }
    }

    private fun BuscarUsuariosPorId(texto : String){
        usuarios.whereGreaterThanOrEqualTo("id", texto)
            .orderBy("id")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val sugestoes = mutableListOf<Usuario>()
                for (document in documents) {
                    val nome = document.getString("nome")!!
                    val id = document.id
                    val usuario = Usuario(nome, id)
                    sugestoes.add(usuario)
                }
                val adapter = UsuarioArrayAdaptador(this, sugestoes)
                actvBuscarAmigos.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                actvBuscarAmigos.setError("Nenhum usuário encontrado")
            }
    }

    private fun VerificarSituacaoListaDeAmigos(){
        adaptador.notifyDataSetChanged()
        if(usuario.amigos.size === 0){
            tvListaDeAmigosVazia.visibility = View.VISIBLE
            rvListaDeAmigos.visibility = View.GONE
        }
        else{
            tvListaDeAmigosVazia.visibility = View.GONE
            rvListaDeAmigos.visibility = View.VISIBLE
        }
    }

    private fun PopUpConfirmacaoAdicionarAmigo(amigo : Usuario){
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Adicionar Amigo")
            .setMessage("Deseja adicionar " + amigo.nome + " a sua lista de amigos?")
            .setPositiveButton("Adicionar") { dialog, which ->
                AdicionarAmigo(amigo)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

    private fun AdicionarAmigo(amigo : Usuario){
        val amigoRef = database.collection("usuario").document(amigo.id)

        documentoUsuario.update("amigos", FieldValue.arrayUnion(amigoRef))
            .addOnSuccessListener {
                usuario.amigos.add(amigo)
                VerificarSituacaoListaDeAmigos()
                Toast.makeText(this, "Amigo adicionado com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao adicionar amigo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun PrepararExibicaoListaDeAmigos(){
        adaptador = UsuarioAdaptador(this, usuario.amigos)
        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.d("Teste", "Entrei para deletar")
                val amigoRef = database.collection("usuario").document(usuario.amigos[positionStart].id)
                documentoUsuario.update("amigos", FieldValue.arrayRemove(amigoRef))
                    .addOnSuccessListener {
                        Log.d("Teste", "Deletado com sucesso")
                        VerificarSituacaoListaDeAmigos()
                    }
            }
        })
        rvListaDeAmigos.setHasFixedSize(true)
        rvListaDeAmigos.layoutManager = LinearLayoutManager(this)
        rvListaDeAmigos.adapter = adaptador
        rvListaDeAmigos.isClickable = true

        VerificarSituacaoListaDeAmigos()
    }
}