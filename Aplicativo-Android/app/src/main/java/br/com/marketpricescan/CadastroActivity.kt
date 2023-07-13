package br.com.marketpricescan

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import br.com.marketpricescan.model.Usuario
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity responsável pelo cadastro de um novo usuário.
 */
class CadastroActivity : ComponentActivity() {

    lateinit var btnCadastrar : Button
    lateinit var etNomeCadastro : EditText
    lateinit var etEmailCadastro : EditText
    lateinit var etSenhaCadastro : EditText
    lateinit var pbCadastro : ProgressBar
    lateinit var usuarioId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro)

        IniciarComponentes()

        // Configura o clique do botão cadastrar
        btnCadastrar.setOnClickListener{view ->
            val nome = etNomeCadastro.text.toString()
            val email = etEmailCadastro.text.toString()
            val senha = etSenhaCadastro.text.toString()

            // Verifica se todos os campos foram preenchidos
            if(nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                // Exibe uma snackbar com a mensagem de preenchimento obrigatório
                val snackbar = Snackbar.make(view, "Preencha todos os campos", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(getColor(R.color.red))
                snackbar.setTextColor(getColor(R.color.black))
                snackbar.show()

            } else {
                CadastrarUsuario(view)
            }
        }
    }

    /**
     * Inicializa os componentes da tela de cadastro de usuário.
     * Associa as variáveis locais aos elementos de layout correspondentes através de seus IDs.
     */
    private fun IniciarComponentes() {
        btnCadastrar = findViewById(R.id.btnCadastrar)
        etNomeCadastro = findViewById(R.id.etNomeCadastro)
        etEmailCadastro = findViewById(R.id.etEmailCadastro)
        etSenhaCadastro = findViewById(R.id.etSenhaCadastro)
        pbCadastro = findViewById(R.id.pbCadastro)
    }

    /**
     * Faz o cadastro do usuário no Firebase Authentication.
     *
     * @param view A view atual.
     */
    private fun CadastrarUsuario(view : View) {
        val email = etEmailCadastro.text.toString()
        val senha = etSenhaCadastro.text.toString()

        pbCadastro.visibility = View.VISIBLE

        // Cria um novo usuário no Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
            if(task.isSuccessful) {
                // Chama função para salvar os dados do usuário no Firebase Firestore
                SalvarDadosUsuario(view)

                // Exibe uma snackbar com a mensagem de sucesso
                val snackbar = Snackbar.make(view, "Usuário cadastrado com sucesso", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(getColor(R.color.green))
                snackbar.setTextColor(getColor(R.color.black))
                snackbar.show()
                pbCadastro.visibility = View.INVISIBLE
            }
            else{
                // Verifica o tipo de exceção lançada e define a mensagem de erro correspondente
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

                pbCadastro.visibility = View.INVISIBLE
                // Exibe uma snackbar com a mensagem de erro
                val snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(getColor(R.color.red))
                snackbar.setTextColor(getColor(R.color.black))
                snackbar.show()

            }
        }
    }

    /**
    * Salva os dados do usuário no Firebase Firestore.
    *
    * @param view A view atual.
    */
    private fun SalvarDadosUsuario(view : View) {
         val nome : String = etNomeCadastro.text.toString()

        // Obtém o ID do usuário atualmente logado
        usuarioId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val database = FirebaseFirestore.getInstance()

        // Cria um objeto Usuario com os dados do usuário
        val usuario = Usuario(nome, usuarioId)

        // Salva os dados do usuário no Firestore
        database.collection("usuario")
            .document(usuarioId)
            .set(usuario)
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao salvar os dados do usuário")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao salvar os dados do usuário")
            }
    }

}