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
import br.com.marketpricescan.util.ItemListaAdaptador

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
        Log.d("Teste", "Vou criar o adaptador")
        adaptador = ItemListaAdaptador(this, itens)
        Log.d("Teste", "Vou setar o adaptador " + adaptador.itemCount)
        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true

        VerificarSituacaoLista()

        btnAdicionarItem.setOnClickListener(){view ->
            Log.d("Teste", "Vou adicionar um item")
            itens.add("")
            adaptador.notifyDataSetChanged()
            rvListaDeCompra.smoothScrollToPosition(adaptador.itemCount - 1)
            VerificarSituacaoLista()

        }

//        adaptador.setOnItemLongClickListener { parent, view, position, id ->
//            PopUpConfirmacaoDeletarItem(itens[position], position)
//            true
//        }
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