package br.com.marketpricescan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import br.com.marketpricescan.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var etNomeEditar: TextView
    private lateinit var etEmailEditar: TextView
    private lateinit var etSenhaEditar: TextView
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

            delay(2000)

            loadingCard.visibility = View.GONE // Ocultar o indicador de progresso
        }
    }

    private fun IniciarComponentes(){
        etNomeEditar = findViewById(R.id.etNomeEditar)
        etEmailEditar = findViewById(R.id.etEmailEditar)
        etSenhaEditar = findViewById(R.id.etSenhaEditar)
    }

    private fun InicializarUsuario(){
//        database.document(usuarioId).get().addOnSuccessListener {
//            usuario = Usuario(it.getString("nome")!!)
//            etNomeEditar.setText(usuario.nome)
//        }
//        etEmailEditar.setText(FirebaseAuth.getInstance().currentUser?.email)
    }
}