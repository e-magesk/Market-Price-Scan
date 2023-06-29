package br.com.marketpricescan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var etNomeEditar: EditText
    private lateinit var etEmailEditar: EditText
    private lateinit var etSenhaEditar: EditText
    private lateinit var etIdUsuario: EditText
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

    private fun IniciarComponentes(){
        etNomeEditar = findViewById(R.id.etNomeEditar)
        etEmailEditar = findViewById(R.id.etEmailEditar)
        etSenhaEditar = findViewById(R.id.etSenhaEditar)
        etIdUsuario = findViewById(R.id.etIdUsuario)
        btnEditarUsuario = findViewById(R.id.btnEditarUsuario)
        pbEditarUsuario = findViewById(R.id.pbEditarUsuario)
    }

    private fun InicializarUsuario(){
        database.collection("usuario").document(usuarioId).get().addOnSuccessListener {document ->
            usuario = Usuario(document.getString("nome")!!)
            etNomeEditar.setText(usuario.nome)
        }
        etEmailEditar.setText(FirebaseAuth.getInstance().currentUser?.email)
        etSenhaEditar.setText("********")
    }

    private fun DefinirAcoes(){

        val intent = Intent(this, HomeActivity::class.java)

        btnEditarUsuario.setOnClickListener {view ->
            if(etEmailEditar.text.toString().isEmpty()){
                etEmailEditar.error = "Campo obrigatório"
                etEmailEditar.requestFocus()
            }else if(etSenhaEditar.text.toString().isEmpty()){
                etSenhaEditar.error = "Campo obrigatório"
                etSenhaEditar.requestFocus()
            }else{
                pbEditarUsuario.visibility = View.VISIBLE
                FirebaseAuth.getInstance().currentUser?.updateEmail(etEmailEditar.text.toString())?.addOnSuccessListener {
                    FirebaseAuth.getInstance().currentUser?.updatePassword(etSenhaEditar.text.toString())?.addOnSuccessListener {
                        var snackbar = Snackbar.make(view, "Os dados foram alterados com sucesso!", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(getColor(R.color.green))
                        snackbar.setTextColor(getColor(R.color.black))
                        pbEditarUsuario.visibility = View.INVISIBLE
                        snackbar.show()
                        val coroutineScope = CoroutineScope(Dispatchers.Main)
                        coroutineScope.launch {
                            delay(1000)
                            startActivity(intent)
                            finish()
                        }
                    }?.addOnFailureListener {
                        pbEditarUsuario.visibility = View.INVISIBLE
                        var snackbar = Snackbar.make(view, "Erro ao alterar informações! Tente novamente.", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(getColor(R.color.red))
                        snackbar.setTextColor(getColor(R.color.black))
                        snackbar.show()
                    }
                }
            }
        }
        etIdUsuario.isEnabled = true
        etIdUsuario.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = etIdUsuario.compoundDrawables[2] // Ícone no lado direito (índice 2)
                if (event.rawX >= (etIdUsuario.right - drawableRight.bounds.width())) {
                    Log.d("Teste", "Clicou no ícone de copiar")
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}