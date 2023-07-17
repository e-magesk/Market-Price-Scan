package br.com.marketpricescan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import br.com.marketpricescan.model.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Classe responsável pela atividade de edição de usuário.
 */
class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var etNomeEditar: EditText
    private lateinit var etEmailEditar: EditText
    private lateinit var etSenhaEditar: EditText
    private lateinit var tvIdUsuario: TextView
    private lateinit var iconCopyId: ImageView
    private lateinit var pbEditarUsuario: ProgressBar
    private lateinit var btnEditarUsuario: Button
    private lateinit var usuario: Usuario
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_usuario)

        val loadingCard = findViewById<CardView>(R.id.loadingPageEditarUsuario)

        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            IniciarComponentes()

            InicializarUsuario()

            DefinirAcoes()

            delay(2000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }
    }

    /**
     * Função responsável por iniciar os componentes da interface.
     */
    private fun IniciarComponentes(){
        etNomeEditar = findViewById(R.id.etNomeEditar)
        etEmailEditar = findViewById(R.id.etEmailEditar)
        etSenhaEditar = findViewById(R.id.etSenhaEditar)
        tvIdUsuario = findViewById(R.id.tvIdUsuario)
        iconCopyId = findViewById(R.id.iconCopyId)
        btnEditarUsuario = findViewById(R.id.btnEditarUsuario)
        pbEditarUsuario = findViewById(R.id.pbEditarUsuario)
    }

    /**
     * Função responsável por inicializar o usuário.
     * Busca as informações do usuário no banco de dados e preenche os campos de nome, email e ID.
     */
    private fun InicializarUsuario(){
        database.collection("usuario").document(usuarioId).get().addOnSuccessListener {document ->
            usuario = Usuario(document.getString("nome")!!)
            etNomeEditar.setText(usuario.nome)
        }
        etEmailEditar.setText(FirebaseAuth.getInstance().currentUser?.email)
        etSenhaEditar.setText("********")
        tvIdUsuario.setText("ID: " + usuarioId)
    }

    /**
     * Função responsável por definir as ações dos botões.
     */
    private fun DefinirAcoes(){

        val intent = Intent(this, HomeActivity::class.java)

        // Ação do botão de editar usuário
        btnEditarUsuario.setOnClickListener {view ->
            //Ao clicar no botão, verifica se os campos de email e senha estão preenchidos.
            if(etEmailEditar.text.toString().isEmpty()){
                etEmailEditar.error = "Campo obrigatório"
                etEmailEditar.requestFocus()
            }else if(etSenhaEditar.text.toString().isEmpty()){
                etSenhaEditar.error = "Campo obrigatório"
                etSenhaEditar.requestFocus()
            }else{ // Caso os campos estejam preenchidos
                pbEditarUsuario.visibility = View.VISIBLE
                // Atualiza o email do usuário
                FirebaseAuth.getInstance().currentUser?.updateEmail(etEmailEditar.text.toString())?.addOnSuccessListener {
                    // Atualiza a senha do usuário
                    FirebaseAuth.getInstance().currentUser?.updatePassword(etSenhaEditar.text.toString())?.addOnSuccessListener {
                        // Exibe uma snackbar com a mensagem de sucesso
                        var snackbar = Snackbar.make(view, "Os dados foram alterados com sucesso!", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(getColor(R.color.green))
                        snackbar.setTextColor(getColor(R.color.black))
                        pbEditarUsuario.visibility = View.INVISIBLE
                        snackbar.show()

                        // Delay para carregamento
                        val coroutineScope = CoroutineScope(Dispatchers.Main)
                        coroutineScope.launch {
                            delay(1000)
                            startActivity(intent)
                            finish()
                        }
                    }?.addOnFailureListener {
                        // Exibe uma snackbar com a mensagem de erro
                        pbEditarUsuario.visibility = View.INVISIBLE
                        var snackbar = Snackbar.make(view, "Erro ao alterar informações! Tente novamente.", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(getColor(R.color.red))
                        snackbar.setTextColor(getColor(R.color.black))
                        snackbar.show()
                    }
                }
            }
        }

        // Ação do ícone de copiar o ID do usuário
        iconCopyId.setOnClickListener { view ->
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textToCopy = usuarioId.trim()
            if (textToCopy.isNotEmpty()) {
                val clipData = ClipData.newPlainText("ID", textToCopy)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(this, "ID do usuário copiado para a área de transferência", Toast.LENGTH_SHORT).show()
            }
        }
    }
}