package br.com.marketpricescan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import br.com.marketpricescan.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    lateinit var btnCriarListaDeCompra : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        IniciarComponentes()

        btnCriarListaDeCompra.setOnClickListener(){view ->
            var intent = Intent(this, ListaDeCompraActivity::class.java)
            startActivity(intent)
        }

        var usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("usuario").document(usuarioId!!)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    var usuario = snapshot.toObject(Usuario::class.java)
                    var tvNomeUsuario = findViewById<TextView>(R.id.tvWelcomeHome)
                    tvNomeUsuario.text = "Welcome, ${usuario?.nome}!"
                }
            }
    }

    private fun IniciarComponentes(){
        btnCriarListaDeCompra = findViewById(R.id.btnCriarListaDeCompra)
    }
}