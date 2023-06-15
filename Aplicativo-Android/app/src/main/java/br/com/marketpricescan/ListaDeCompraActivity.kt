package br.com.marketpricescan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import br.com.marketpricescan.util.ItemListaAdaptador

class ListaDeCompraActivity : AppCompatActivity() {

    lateinit var listView : ListView
    lateinit var iconCircleCheck : ImageView
    var itens : Array<String> = arrayOf("Produto 1", "Produto 2", "Produto 3", "Produto 4", "Produto 5", "Produto 6", "Produto 7", "Produto 8", "Produto 9", "Produto 10", "Produto 11")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        listView = findViewById(R.id.listaDeCompra)
        listView.adapter = ItemListaAdaptador(applicationContext, itens)
        listView.isClickable = false
//        listView.setOnItemClickListener { parent, view, position, id ->
//            Log.d("Teste", "Clicou no item ${itens[position]}")
//        }

    }

}