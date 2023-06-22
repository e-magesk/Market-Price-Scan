package br.com.marketpricescan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.util.ProdutoListaDeCompraAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking

class ListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var etTituloLista: TextView
    private lateinit var adaptador: ProdutoListaDeCompraAdaptador
    var itens = mutableListOf<Produto>()
    private lateinit var listaDeCompra: ListaDeCompra
    private var produtos: ArrayList<Produto> = ArrayList()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var documentoListaDeCompra: DocumentReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        IniciarComponentes()
        adaptador = ProdutoListaDeCompraAdaptador(this, itens)
        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true

        CriarNovaListaDeCompra()

        VerificarSituacaoLista()

        btnAdicionarItem.setOnClickListener() { view ->
            itens.add(Produto("" ))
            adaptador.notifyDataSetChanged()
            rvListaDeCompra.smoothScrollToPosition(adaptador.itemCount - 1)
            VerificarSituacaoLista()

        }
    }

    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
        etTituloLista = findViewById(R.id.etTituloLista)
    }

    private fun VerificarSituacaoLista() {
        if (itens.size == 0) {
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    private fun CriarNovaListaDeCompra() {
        listaDeCompra = ListaDeCompra("")
        documentoListaDeCompra = database.collection("lista_de_compra")
            .document()
        documentoListaDeCompra.set(listaDeCompra)
            .addOnSuccessListener {
                VincularListaAoUsuario()
                Log.d("Teste", "Sucesso ao salvar a lista no banco de dados")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao salvar a lista no banco de dados")
            }
    }

    private fun VincularListaAoUsuario() {

        documentoUsuario = database.collection("usuario")
            .document(usuarioId)
        documentoUsuario.update("listasDeCompra", FieldValue.arrayUnion(documentoListaDeCompra))
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao vincular a lista ao usuário")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao vincular a lista ao usuário")
            }

    }

    override fun onBackPressed() {
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("Salvar Lista?")
            .setMessage("Deseja salvar a lista " + etTituloLista.text + "?")
            .setPositiveButton("OK") { dialog, which ->
                runBlocking {
                    SalvarListaDeCompra()
                }
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
                finish()
            }
            .create()
        alertDialog.show()
    }

    private fun SalvarListaDeCompra(){
        val updates = hashMapOf<String, Any>(
            "produtos" to produtos,
            "nome" to etTituloLista.text.toString(),
        )

        documentoListaDeCompra.update(updates)
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao atualizar a lista no banco de dados")
            }
            .addOnFailureListener { e ->
                Log.d("Teste", "Erro ao atualizar a lista no banco de dados")
            }
    }


}