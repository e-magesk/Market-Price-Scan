package br.com.marketpricescan

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import br.com.marketpricescan.databinding.ListaDeCompraBinding
import br.com.marketpricescan.util.ItemListaAdaptador

class ListaDeCompraActivity : AppCompatActivity() {

    lateinit var listView : ListView
    var itens : Array<String> = arrayOf("Produto 1", "Produto 2", "Produto 3", "Produto 4", "Produto 5", "Produto 6", "Produto 7", "Produto 8", "Produto 9", "Produto 10")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        listView = findViewById(R.id.listaDeCompra)
        listView.adapter = ItemListaAdaptador(applicationContext, itens)
        listView.isClickable = true
        Log.d("Teste", "Chegou aqui 1")
//        binding.listaDeCompra.isClickable = true
//        Log.d("Teste", "Chegou aqui 2")
//        binding.listaDeCompra.adapter = ItemListaAdaptador(this, itens)
//        Log.d("Teste", "Chegou aqui 3")
//        binding.listaDeCompra.setOnItemClickListener { parent, view, position, id ->
//            Log.d("Teste", "Clicou no item ${itens[0]}")
//        }

    }
}