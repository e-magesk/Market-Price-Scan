package br.com.marketpricescan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.marketpricescan.model.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * Classe responsável pela tela de login do aplicativo.
 * Permite que o usuário faça login utilizando seu email e senha.
 */
class LoginActivity : AppCompatActivity() {

    lateinit var tvCadastrar : TextView
    lateinit var tvRedefinirSenha : TextView
    lateinit var btnLogin : Button
    lateinit var etEmailUsuario : EditText
    lateinit var etSenhaUsuario : EditText
    lateinit var pbLogin : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        IniciarComponentes()

        // Indo da página de login para a página de cadastro
        tvCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // Indo da página de login para a página home (se o login for bem sucedido)
        btnLogin.setOnClickListener { view ->
            AutenticarUsuario(view)
        }

        tvRedefinirSenha.setOnClickListener {
            val intent = Intent(this, RedefinirSenhaActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Inicializa os componentes da tela.
     */
    private fun IniciarComponentes() {
        etEmailUsuario = findViewById(R.id.etEmailUsuario)
        etSenhaUsuario = findViewById(R.id.etSenhaUsuario)
        tvCadastrar = findViewById(R.id.tvCadastrar)
        btnLogin = findViewById(R.id.btnLogin)
        pbLogin = findViewById(R.id.pbLogin)
        tvRedefinirSenha = findViewById(R.id.tvRedefinirSenha)
    }

    /**
     * Autentica o usuário utilizando o email e senha fornecidos.
     * Exibe uma Snackbar com mensagem de erro se o email ou senha estiverem vazios.
     * Inicia a atividade HomeActivity se o login for bem sucedido.
     *
     * @param view A referência para a view que chamou a função.
     */
    private fun AutenticarUsuario(view : View) {

        var email = etEmailUsuario.text.toString()
        var senha = etSenhaUsuario.text.toString()

        if(email.isEmpty() || senha.isEmpty()) {
            // Exibe uma Snackbar com mensagem de erro se o email ou senha estiverem vazios
            var snackbar = Snackbar.make(view, "Preencha todos os campos", Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(getColor(R.color.white))
            snackbar.setTextColor(getColor(R.color.black))
            snackbar.show()
        }
        else{
            pbLogin.visibility = View.VISIBLE
            // Realiza a autenticação pelo Firebase.
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    // Inicia HomeActivity se o login for bem sucedido
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    // Exibe uma Snackbar de erro se o login falhar
                    pbLogin.visibility = View.INVISIBLE
                    var snackbar = Snackbar.make(view, "Erro ao autenticar usuário", Snackbar.LENGTH_LONG)
                    snackbar.setBackgroundTint(getColor(R.color.red))
                    snackbar.setTextColor(getColor(R.color.black))
                    snackbar.show()
                }
            }
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

}