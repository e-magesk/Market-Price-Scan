package br.com.marketpricescan

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.marketpricescan.model.ListaDeCompra

class AtualizarListaDeCompraActivity : AppCompatActivity() {

    private lateinit var listaDeCompra : ListaDeCompra

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listaDeCompra = intent.getParcelableExtra("listaDeCompra", ListaDeCompra::class.java)!!
        } else {
            listaDeCompra = intent.getParcelableExtra<ListaDeCompra>("listaDeCompra")!!
        }

        Log.d("Teste", "listaDeCompra: " + listaDeCompra.nome)

    }

}