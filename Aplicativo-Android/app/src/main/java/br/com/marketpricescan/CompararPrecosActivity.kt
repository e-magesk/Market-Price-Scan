package br.com.marketpricescan

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.Supermercado
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ProdutoListaDeCompraAdaptador
import br.com.marketpricescan.util.SupermercadoAdaptador
import com.google.firebase.firestore.FirebaseFirestore

class CompararPrecosActivity : AppCompatActivity() {

    lateinit var rvSupermercados : RecyclerView
    lateinit var adaptadorSupermercados : SupermercadoAdaptador
    lateinit var supermercados : MutableList<Supermercado>
    lateinit var listaDeCompraComparacao : ListaDeCompra

    var database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comparar_preco)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listaDeCompraComparacao = intent.getParcelableExtra("listaDeCompra", ListaDeCompra::class.java)!!
        } else {
            listaDeCompraComparacao = intent.getParcelableExtra<ListaDeCompra>("listaDeCompra")!!
        }

        Log.d("Teste", "Lista chegou no comparador" + "${listaDeCompraComparacao.produtos.size}")

        InicializarComponentes()

        BuscarSupermercados()
    }

    private fun InicializarComponentes(){
        rvSupermercados = findViewById(R.id.rvSupermercados)
    }

    private fun DefinirAdaptador(){
        adaptadorSupermercados = SupermercadoAdaptador(this, supermercados, listaDeCompraComparacao)
        rvSupermercados.setHasFixedSize(true)
        rvSupermercados.layoutManager = LinearLayoutManager(this)
        rvSupermercados.adapter = adaptadorSupermercados
    }

    private fun BuscarSupermercados(){
        supermercados = mutableListOf()
        database.collection("supermercado")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    var supermercado = Supermercado(document.toObject(Supermercado::class.java)!!)
                    supermercados.add(supermercado)
                }
                DefinirAdaptador()
            }
            .addOnFailureListener { exception ->
                println("Erro ao buscar supermercados: $exception")
            }
    }
}