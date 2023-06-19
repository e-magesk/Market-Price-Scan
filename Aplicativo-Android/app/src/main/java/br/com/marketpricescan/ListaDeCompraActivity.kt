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
import com.google.firebase.firestore.FirebaseFirestore

class ListaDeCompraActivity : AppCompatActivity() {

    private lateinit var rvListaDeCompra: RecyclerView
    private lateinit var btnAdicionarItem: Button
    private lateinit var tvListaVazia: TextView
    private lateinit var adaptador: ItemListaAdaptador
    var itens = mutableListOf<String>(
        "Produto 1", "Produto 2", "Produto 3", "Produto 4", "Produto 5",
        "Produto 6", "Produto 7", "Produto 8", "Produto 9", "Produto 10", "Produto 11"
    )
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

        VerificarSituacaoLista()

        btnAdicionarItem.setOnClickListener(){view ->
            itens.add("")
            adaptador.notifyDataSetChanged()
            rvListaDeCompra.smoothScrollToPosition(adaptador.itemCount - 1)
            VerificarSituacaoLista()

        }

        val firestore = FirebaseFirestore.getInstance()
        val usuariosRef = firestore.collection("usuario")

        val listaDeProdutos = ListaDeCompra("Nome da Lista")
        listaDeProdutos.adicionarProduto(Produto("Produto teste", 1.0))

        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val listasDeProdutosRef = usuariosRef.document(usuarioId).collection("lista_de_compra")
        val novaListaRef = listasDeProdutosRef.document()

        novaListaRef.set(listaDeProdutos)
    }

    private fun IniciarComponentes(){
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
        tvListaVazia = findViewById(R.id.tvListaVazia)
    }

    private fun VerificarSituacaoLista(){
        if(itens.size == 0){
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE
        }
        else{
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    private fun PopUpConfirmacaoDeletarItem(item : String, position : Int){
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Deletar Item")
            .setMessage("O item " + item + " será deletado da lista. Deseja continuar?")
            .setPositiveButton("OK") { dialog, which ->
                itens.removeAt(position)
                adaptador.notifyDataSetChanged()
                VerificarSituacaoLista()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

}