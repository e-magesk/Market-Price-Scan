package br.com.marketpricescan

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.google.android.material.snackbar.Snackbar

class CadastroActivity : ComponentActivity() {

    lateinit var btnCadastrar : Button
    lateinit var etNomeCadastro : EditText
    lateinit var etEmailCadastro : EditText
    lateinit var etSenhaCadastro : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro)

        btnCadastrar = findViewById(R.id.btnCadastrar)
        etNomeCadastro = findViewById(R.id.etNomeCadastro)
        etEmailCadastro  = findViewById(R.id.etEmailCadastro)
        etSenhaCadastro = findViewById(R.id.etSenhaCadastro)

        Log.d("Debug","Quero cadastrar")
        btnCadastrar.setOnClickListener{view ->
            Log.d("Debug","Estou cadastrando")
            val nome = etNomeCadastro.text.toString()
            val email = etEmailCadastro.text.toString()
            val senha = etSenhaCadastro.text.toString()

            if(nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                if(view != null) {
                    Log.d("Debug", "Faltam parametros")
                    Snackbar.make(btnCadastrar, "Preencha todos os campos", Snackbar.LENGTH_LONG)
                        .show()
                }
            } else {
                Log.d("Debug", view.toString())
            }
        }
    }

}