package br.com.marketpricescan

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

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

//        Log.d("Debug","Quero cadastrar")
        btnCadastrar.setOnClickListener{view ->
            val nome = etNomeCadastro.text.toString()
            val email = etEmailCadastro.text.toString()
            val senha = etSenhaCadastro.text.toString()

            if(nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                var snackbar = Snackbar.make(view, "Preencha todos os campos", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(getColor(R.color.red))
                snackbar.setTextColor(getColor(R.color.black))
                snackbar.show()

            } else {
                CadastrarUsuario(view)
            }
        }
    }

    private fun CadastrarUsuario(view : View) {
        val email = etEmailCadastro.text.toString()
        val senha = etSenhaCadastro.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
            if(task.isSuccessful) {
                var snackbar = Snackbar.make(view, "Usuário cadastrado com sucesso", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(getColor(R.color.green))
                snackbar.setTextColor(getColor(R.color.black))
                snackbar.show()
            }
            else{
                var erro : String = ""
                try{
                    throw task.exception!!
                } catch (e : FirebaseAuthWeakPasswordException) {
                    erro = "A senha deve conter no mínimo 6 caracteres"
                } catch (e : FirebaseAuthUserCollisionException) {
                    erro = "E-mail já cadastrado"
                } catch (e : FirebaseAuthInvalidCredentialsException) {
                    erro = "E-mail inválido"
                } catch (e : Exception) {
                    erro = "Erro ao cadastrar usuário"
                }

                var snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(getColor(R.color.red))
                snackbar.setTextColor(getColor(R.color.black))
                snackbar.show()

            }
        }
    }

}