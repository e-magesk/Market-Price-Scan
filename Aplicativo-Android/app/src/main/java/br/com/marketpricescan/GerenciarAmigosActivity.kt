package br.com.marketpricescan

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.UsuarioAdaptador
import br.com.marketpricescan.util.UsuarioIdArrayAdaptador
import br.com.marketpricescan.util.UsuarioNomeArrayAdaptador
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

/**
 * Classe responsável por gerenciar a lista de amigos de um usuário.
 * Permite adicionar amigos, buscar amigos por ID ou nome e exibir a lista de amigos atual.
 */
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

        val loadingCard = findViewById<CardView>(R.id.loadingPageAdicionarAmigos)

        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            // Obtém o usuario enviado pela activity HomeActivity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                usuario = intent.getParcelableExtra("usuario", Usuario::class.java)!!
            } else {
                usuario = intent.getParcelableExtra<Usuario>("usuario")!!
            }
            IniciarComponentes()

            PrepararExibicaoListaDeAmigos()

            DefinirAcoes()

            delay(500)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }

    }

    /**
     * Inicializa os componentes da atividade.
     * Obtém o documento do Firestore referente ao usuário.
     */
    private fun IniciarComponentes(){
        cbId = findViewById(R.id.cbId)
        cbNome = findViewById(R.id.cbNome)
        actvBuscarAmigos = findViewById(R.id.actvBuscarAmigos)
        tvListaDeAmigosVazia = findViewById(R.id.tvListaDeAmigosVazia)
        rvListaDeAmigos = findViewById(R.id.rvListaDeAmigos)

        documentoUsuario = database.collection("usuario").document(usuario.id)
    }

    /**
     * Define as ações dos elementos interativos da activity.
     */
    private fun DefinirAcoes(){

        cbId.setOnCheckedChangeListener { _, _ ->
            // Define ações nos casos de seleção das opções busca por ID e nome.
            if(cbId.isChecked && cbNome.isChecked){
                cbNome.isChecked = false
            }
            else if(!cbId.isChecked && !cbNome.isChecked){
                cbId.isChecked = true
            }
            actvBuscarAmigos.hint = if(cbId.isChecked) "Buscar usuario por ID" else "Buscar usuario por nome"
        }

        cbNome.setOnCheckedChangeListener { _, _ ->
            // Define ações nos casos de seleção das opções busca por ID e nome.
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

            // Método invocado depois que o texto foi alterado
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                if(cbId.isChecked)
                    // Chama a função para buscar usuários por ID com base no texto digitado
                    BuscarUsuariosPorId(texto)
                else if(cbNome.isChecked)
                    // Chama a função para buscar usuários por nome com base no texto digitado
                    BuscarUsuariosPorNome(texto)
            }
        })

        actvBuscarAmigos.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val objetoCompleto = parent.getItemAtPosition(position) as Usuario
                // Chama a função para exibir um pop-up de confirmação para adicionar o amigo
                PopUpConfirmacaoAdicionarAmigo(objetoCompleto)
            }

    }

    /**
     * Busca usuários por nome no Firestore com base no texto fornecido.
     *
     * @param texto O texto para buscar usuários por nome.
     */
    private fun BuscarUsuariosPorNome(texto : String){
        // Realiza uma consulta no Firestore para buscar usuários cujo nome seja maior ou igual ao texto fornecido (ignorando maiúsculas e minúsculas)
        usuarios.whereGreaterThanOrEqualTo("nome", texto.uppercase())
            .orderBy("nome")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val sugestoes = mutableListOf<Usuario>()
                for (document in documents) {
                    //Adiciona os usuarios sugeridos na lista de sugestões
                    val nome = document.getString("nome")!!
                    val id = document.id
                    val usuario = Usuario(nome, id)
                    sugestoes.add(usuario)
                }
                // Cria um adaptador personalizado para exibir as sugestões de usuários no AutoCompleteTextView
                val adapter = UsuarioNomeArrayAdaptador(this, sugestoes)
                actvBuscarAmigos.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                // Define uma mensagem de erro no AutoCompleteTextView caso a busca não tenha sucesso
                actvBuscarAmigos.setError("Nenhum usuário encontrado")
            }
    }

    /**
     * Busca usuários por ID no Firestore com base no texto fornecido.
     *
     * @param texto O texto para buscar usuários por ID.
     */
    private fun BuscarUsuariosPorId(texto : String){
        // Realiza uma consulta no Firestore para buscar usuários cujo ID seja maior ou igual ao texto fornecido
        usuarios.whereGreaterThanOrEqualTo("id", texto)
            .orderBy("id")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val sugestoes = mutableListOf<Usuario>()
                for (document in documents) {
                    //Adiciona os usuarios sugeridos na lista de sugestões
                    val nome = document.getString("nome")!!
                    val id = document.id
                    val usuario = Usuario(nome, id)
                    sugestoes.add(usuario)
                    Log.d("Teste", "Sugestoes")
                    for(usuario in sugestoes){
                        Log.d("Teste", usuario.nome)
                    }
                }
                // Cria um adaptador personalizado para exibir as sugestões de usuários no AutoCompleteTextView
                val adapter = UsuarioIdArrayAdaptador(this, sugestoes)
                actvBuscarAmigos.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                // Define uma mensagem de erro no AutoCompleteTextView caso a busca não tenha sucesso
                actvBuscarAmigos.setError("Nenhum usuário encontrado")
            }
    }

    /**
     * Verifica a situação da lista de amigos e atualiza a visibilidade dos elementos correspondentes.
     */
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

    /**
     * Exibe um pop-up de confirmação para adicionar um amigo à lista de amigos do usuário.
     * Chama função de adicionar amigo em caso de resposta positiva.
     *
     * @param amigo O objeto `Usuario` representando o amigo a ser adicionado.
     */
    private fun PopUpConfirmacaoAdicionarAmigo(amigo : Usuario){
        // Cria um AlertDialog com título e mensagem de confirmação
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Adicionar Amigo")
            .setMessage("Deseja adicionar " + amigo.nome + " a sua lista de amigos?")
            .setPositiveButton("Adicionar") { dialog, which ->
                // Chama a função AdicionarAmigo para adicionar o amigo
                AdicionarAmigo(amigo)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            .create()
        // Exibe confirmação
        alertDialog.show()
    }

    /**
     * Adiciona um amigo à lista de amigos do usuário.
     *
     * @param amigo O objeto `Usuario` representando o amigo a ser adicionado.
     */
    private fun AdicionarAmigo(amigo : Usuario){
        val amigoRef = database.collection("usuario").document(amigo.id)

        // Atualiza o campo "amigos" do documento do usuário para adicionar a referência do amigo
        documentoUsuario.update("amigos", FieldValue.arrayUnion(amigoRef))
            .addOnSuccessListener {
                usuario.amigos.add(amigo)
                // Verifica a situação da lista de amigos e atualiza a visibilidade dos elementos correspondentes
                VerificarSituacaoListaDeAmigos()
                Toast.makeText(this, "Amigo adicionado com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao adicionar amigo", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Prepara a exibição da lista de amigos do usuário.
     * Configura o adaptador e o gerenciador de layout do RecyclerView, adiciona um observador ao adaptador para
     * detectar a remoção de itens da lista, e atualiza a visibilidade dos elementos correspondentes.
     */
    private fun PrepararExibicaoListaDeAmigos(){
        adaptador = UsuarioAdaptador(this, usuario.amigos)
        // Adiciona um observador ao adaptador para detectar a remoção de itens da lista
        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
//                Log.d("Teste", "Entrei para deletar: " + usuario.amigos[positionStart].nome + " " + usuario.amigos[positionStart].id)

                // Remove a referência do amigo do campo "amigos" do documento do usuário
                var amigoRef : DocumentReference = database.collection("usuario").document(usuario.amigos[positionStart].id)
//                Log.d("Teste", "Entrei para deletar: " + amigoRef)
                documentoUsuario.update("amigos", FieldValue.arrayRemove(amigoRef))
                    .addOnSuccessListener {
                        Log.d("Teste", "Deletado com sucesso")
                        // Chama função para atualizar a exibição na lista de amigos
                        VerificarSituacaoListaDeAmigos()
                        adaptador.notifyDataSetChanged()
                    }
            }
        })

        // Configura o RecyclerView
        rvListaDeAmigos.setHasFixedSize(true)
        rvListaDeAmigos.layoutManager = LinearLayoutManager(this)
        rvListaDeAmigos.adapter = adaptador
        rvListaDeAmigos.isClickable = true

        // Chama função para atualizar a exibição na lista de amigos
        VerificarSituacaoListaDeAmigos()
    }
}