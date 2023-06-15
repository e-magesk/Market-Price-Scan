package br.com.marketpricescan

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import br.com.marketpricescan.util.ItemListaAdaptador

class ListaDeCompraActivity : AppCompatActivity() {

    private lateinit var lvListaDeCompra : ListView
    private lateinit var btnAdicionarItem : Button
    private lateinit var adaptador: ItemListaAdaptador
    var itens = mutableListOf<String>("Produto 1", "Produto 2", "Produto 3", "Produto 4", "Produto 5", "Produto 6", "Produto 7", "Produto 8", "Produto 9", "Produto 10", "Produto 11")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        IniciarComponentes()
        adaptador = ItemListaAdaptador(applicationContext, itens)
        lvListaDeCompra.adapter = adaptador
        lvListaDeCompra.isClickable = false

        btnAdicionarItem.setOnClickListener(){view ->
            itens.add("")
            adaptador.itens = itens
            adaptador.notifyDataSetChanged()
            lvListaDeCompra.smoothScrollToPosition(adaptador.count - 1)

        }

        lvListaDeCompra.setOnItemLongClickListener { parent, view, position, id ->
            itens.removeAt(position)
            adaptador.itens = itens
            adaptador.notifyDataSetChanged()
            true
        }
    }

    private fun IniciarComponentes(){
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        lvListaDeCompra = findViewById(R.id.lvListaDeCompra)
    }

}