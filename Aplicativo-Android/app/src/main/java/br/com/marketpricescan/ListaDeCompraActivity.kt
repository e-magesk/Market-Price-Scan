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
import br.com.marketpricescan.util.ItemListaAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var adaptador: ItemListaAdaptador
    var itens = mutableListOf<String>(
        "Produto 1", "Produto 2", "Produto 3", "Produto 4", "Produto 5",
        "Produto 6", "Produto 7", "Produto 8", "Produto 9", "Produto 10", "Produto 11"
    )
    private lateinit var listaDeCompra: ListaDeCompra
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var documentoUsuario: DocumentReference
    private lateinit var documentoListaDeCompra: DocumentReference


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        IniciarComponentes()
        adaptador = ItemListaAdaptador(this, itens)
        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true

        CriarNovaListaDeCompra()

        VerificarSituacaoLista()

        btnAdicionarItem.setOnClickListener() { view ->
            itens.add("")
            adaptador.notifyDataSetChanged()
            rvListaDeCompra.smoothScrollToPosition(adaptador.itemCount - 1)
            VerificarSituacaoLista()

        }

    }

    private fun IniciarComponentes() {
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
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
        documentoUsuario.update("listas", FieldValue.arrayUnion(documentoListaDeCompra))
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao vincular a lista ao usuário")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao vincular a lista ao usuário")
            }

    }
}