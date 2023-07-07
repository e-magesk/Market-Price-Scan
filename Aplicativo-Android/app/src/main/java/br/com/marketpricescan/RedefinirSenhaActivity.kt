package br.com.marketpricescan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RedefinirSenhaActivity : AppCompatActivity() {

    private lateinit var btnRedefinirSenha : Button
    private lateinit var etEmailRedefinirSenha : EditText
    private lateinit var pbRedefinirSenha : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.redefinir_senha)

        IniciarComponentes()

        val intent = Intent(this, LoginActivity::class.java)

        btnRedefinirSenha.setOnClickListener {view ->
            var auth = FirebaseAuth.getInstance();
            var emailAddress = etEmailRedefinirSenha.text.toString();

            pbRedefinirSenha.visibility = View.VISIBLE
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email enviado com sucesso
                        println("Email de redefinição de senha enviado!")
                        var snackbar = Snackbar.make(view, "E-mail enviado com sucesso!", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(getColor(R.color.green))
                        snackbar.setTextColor(getColor(R.color.black))
                        pbRedefinirSenha.visibility = View.INVISIBLE
                        snackbar.show()
                        val coroutineScope = CoroutineScope(Dispatchers.Main)
                        coroutineScope.launch {
                            delay(1000)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                .addOnFailureListener {
                    // Email não enviado
                    println("Email de redefinição de senha não enviado!")
                    var snackbar = Snackbar.make(view, "E-mail não enviado! Tente Novamente.", Snackbar.LENGTH_LONG)
                    snackbar.setBackgroundTint(getColor(R.color.red))
                    snackbar.setTextColor(getColor(R.color.black))
                    pbRedefinirSenha.visibility = View.INVISIBLE
                    snackbar.show()
                }
        }
    }

    private fun IniciarComponentes(){
        btnRedefinirSenha = findViewById(R.id.btnRedefinirSenha)
        etEmailRedefinirSenha = findViewById(R.id.etEmailRedefinirSenha)
        pbRedefinirSenha = findViewById(R.id.pbRedefinirSenha)
    }
}