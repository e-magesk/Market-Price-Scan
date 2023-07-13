package br.com.marketpricescan

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.Supermercado
import br.com.marketpricescan.model.Usuario
import br.com.marketpricescan.util.ProdutoListaDeCompraAdaptador
import br.com.marketpricescan.util.SupermercadoAdaptador
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity responsável por comparar os preços dos produtos em diferentes supermercados.
 */
class CompararPrecosActivity : AppCompatActivity() {

    lateinit var rvSupermercados : RecyclerView
    lateinit var adaptadorSupermercados : SupermercadoAdaptador
    lateinit var supermercados : MutableList<Supermercado>
    lateinit var listaDeCompraComparacao : ListaDeCompra
    lateinit var cvLoadingPage : CardView

    var database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comparar_preco)

        cvLoadingPage = findViewById(R.id.loadingPageCompararPrecos)
        cvLoadingPage.visibility = View.VISIBLE

        // Obtém a lista de compra enviada pela activity AtualizarListaDeComprasActivity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listaDeCompraComparacao = intent.getParcelableExtra("listaDeCompra", ListaDeCompra::class.java)!!
        } else {
            listaDeCompraComparacao = intent.getParcelableExtra<ListaDeCompra>("listaDeCompra")!!
        }

        Log.d("Teste", "Lista chegou no comparador" + "${listaDeCompraComparacao.produtos.size}")

        InicializarComponentes()
        // Busca os supermercados no Firestore
        BuscarSupermercados()

        // Delay para carregamento
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            delay(3000)
            cvLoadingPage.visibility = View.GONE
        }
    }

    /**
     * Inicializa os componentes da tela.
     */
    private fun InicializarComponentes(){
        rvSupermercados = findViewById(R.id.rvSupermercados)
    }

    /**
     * Define o adaptador e configura o RecyclerView com os supermercados e a lista de compra.
     */
    private fun DefinirAdaptador(){
        adaptadorSupermercados = SupermercadoAdaptador(this, supermercados, listaDeCompraComparacao)
        rvSupermercados.setHasFixedSize(true)
        rvSupermercados.layoutManager = LinearLayoutManager(this)
        rvSupermercados.adapter = adaptadorSupermercados
    }

    /**
     * Busca os supermercados no Firestore e adiciona-os à lista de supermercados.
     */
    private fun BuscarSupermercados(){
        supermercados = mutableListOf()

        // Acessa a coleção "supermercado" no Firestore
        database.collection("supermercado")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Para cada documento retornado, cria um objeto Supermercado e adiciona à lista
                    val supermercado = Supermercado(document.toObject(Supermercado::class.java)!!)
                    supermercados.add(supermercado)
                }
                DefinirAdaptador()
            }
            .addOnFailureListener { exception ->
                println("Erro ao buscar supermercados: $exception")
            }
    }
}