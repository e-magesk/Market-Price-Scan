package br.com.marketpricescan

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.marketpricescan.model.ListaDeCompra
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.ProdutoNotaFiscal
import br.com.marketpricescan.model.Supermercado
import br.com.marketpricescan.util.ProdutoNotaFiscalAdaptador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

class NotaFiscalActivity : AppCompatActivity() {

    //data class Produto(var nome: String="", val preco: Double=0.0)
    //data class ScrapingResult(val produtos: MutableList<Produto> = mutableListOf(), var count:Int = 0)

    lateinit var btnAdicionarItem: Button
    lateinit var listaDeCompra : ListaDeCompra

    lateinit var rvListaDeCompra: RecyclerView
    lateinit var tvListaVazia: TextView
    private lateinit var etTituloLista: TextView


    var produtos = mutableListOf<Produto>()
    private lateinit var adaptador: ProdutoNotaFiscalAdaptador
    lateinit var supermercado : Supermercado

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var documentoListaDeCompra: DocumentReference
    private lateinit var documentoUsuario: DocumentReference
    private val usuarioId: String = FirebaseAuth.getInstance().currentUser!!.uid
    lateinit var documentoSupermercado : DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_de_compra)

        // Loading
        val loadingCard = findViewById<CardView>(R.id.loadingPageListaDeCompra)
        loadingCard.visibility = View.VISIBLE // Exibir o indicador de progresso

        // Retira botão
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        btnAdicionarItem.visibility = View.GONE

        // Recebe url
        val url = intent.getStringExtra("URL").toString()
        // val url = "http://app.sefaz.es.gov.br/ConsultaNFCe/qrcode.aspx?p=32230627473669002877650200000120971230620974|2|1|1|22460C9FAB2F7D74FD15FF2334A99FEB32A09ED7"

        // Codifica URL
        val encodedUrl = URLEncoder.encode(url, "UTF-8")

            // Desfaz a codificação para barras, dois pontos, ponto de interrogação e igual
        var decodedUrl = encodedUrl
            .replace("%2F", "/")
            .replace("%3A", ":")
            .replace("%3F", "?")
            .replace("%3D", "=")

        CoroutineScope(Dispatchers.IO).launch{
            getSupermercadoFromUrl(decodedUrl)
            produtos = getProdutosFromUrl(decodedUrl)

            withContext(Dispatchers.Main) {
                for (produto in produtos){
                    println(produto.nome)
                    println(produto.preco)
                }

                // IniciarComponentes()
                rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
                tvListaVazia = findViewById(R.id.tvListaVazia)
                etTituloLista = findViewById(R.id.etTituloLista)

                DefinirAdaptador()
                VerificarSituacaoLista()

                loadingCard.visibility = View.INVISIBLE
            }
        }
    }


    private fun getProdutosFromUrl(url: String): MutableList<Produto>{
        val produtos = mutableListOf<Produto>()
        Log.d("Teste", "Depois documento " + url)
        try{
            val document = Jsoup.connect(url).get()
        }
        catch (e: Exception){
            Log.d("Teste", "Erro " + e.message)
        }
        val document = Jsoup.connect(url).get()
        val tabela = document.select("table#tabResult")

        val tableRows = tabela.select("table tr")
        for (row in tableRows) {
            val rowData = row.select("td")

            var nome: String = ""
            var preco: Double = 0.0
            var cod: Long = 0

            for (data in rowData) {
                nome = rowData.select("span.txtTit").text()
                preco = rowData.select("span.valor").text().replace(',', '.').toDouble()

                val textoCod = rowData.select("span.Rcod")
                cod = Regex("\\d+").find(textoCod.text())?.value?.toLong() ?: 0L
            }

            produtos.add(Produto(nome, preco, cod.toString()))
        }

        return produtos
    }

    private fun getSupermercadoFromUrl(url : String){
        val document = Jsoup.connect(url).get()
        val div = document.select("div#conteudo")
        val nome = div.select("#u20").text()
        val textos = div.select(".text")
        var cnpj : String = ""
        if(textos[0].text().startsWith("CNPJ: ")){
            cnpj = textos[0].text().substring(6)
        }
        else if(textos[1].text().startsWith("CNPJ:")){
            cnpj = textos[1].text().substring(5)
        }
        val id = cnpj.replace(".", "").replace("/", "").replace("-", "")
        val endereco = textos[1].text()
        supermercado = Supermercado(id, nome, endereco, cnpj)

        AdicionarSupermercadoAoBanco(supermercado)
    }

    private fun AdicionarSupermercadoAoBanco(supermercado : Supermercado){
        try{
            Log.d("Teste", "Antes documento " + supermercado.id)
            documentoSupermercado = database.collection("supermercado").document(supermercado.id)
            supermercado.id = documentoSupermercado.id
            documentoSupermercado.set(supermercado)
                .addOnSuccessListener {
                    Log.d("Teste", "Sucesso ao salvar o supermercado no banco de dados")
                }
                .addOnFailureListener {
                    Log.d("Teste", "Erro ao salvar o supermercado no banco de dados")
                }
        }
        catch (e : Exception){
            Log.d("Teste", "Erro " + e.message)
        }
    }

    private fun DefinirAdaptador(){
        adaptador = ProdutoNotaFiscalAdaptador(this, produtos)
//        adaptador.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
//                super.onItemRangeRemoved(positionStart, itemCount)
//                VerificarSituacaoLista()
//            }
//        })

        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true
    }

    private fun VerificarSituacaoLista() {
        if (produtos.size == 0) {
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE

            tvListaVazia.text = "Lista vazia - Não foram encontrados produtos no QR Code lido"
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    override fun onBackPressed() {
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("Salvar Lista?")
            .setMessage("Deseja salvar a lista " + etTituloLista.text + "?")
            .setPositiveButton("OK") { dialog, which ->
                runBlocking {
                    CriarProdutos()
                }
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
            .create()
        alertDialog.show()
    }

    private fun CriarProdutos(){
        if(produtos.size == 0){
            runBlocking {
                SalvarListaDeCompra(mutableListOf<DocumentReference>())
            }
            return
        }

        val referenciasProdutos = mutableListOf<DocumentReference>()
        var flagFinal = 0
        for(produto in produtos) {
            Log.d("Teste", "Entrei pra salvar" + produto.codigoLocal)
            val query = database.collection("produto")
                .whereEqualTo("supermercadoId", supermercado.id)
                .whereEqualTo("codigoLocal", produto.codigoLocal)

            // ATUALIZANDO PRODUTOS
            query.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("Teste", "QuerySnapshot " + querySnapshot.size())

                    if (querySnapshot.size() !== 0) {
                        Log.d("Teste", "Documento encontrado")
                        val docRef = querySnapshot.documents[0].reference
                        produto.id = docRef.id
                        produto.supermercadoId = supermercado.id
                        referenciasProdutos.add(docRef)
                        docRef.set(produto)
                            .addOnSuccessListener {
                                flagFinal++
                                if (flagFinal == produtos.size) {
                                    runBlocking {
                                        SalvarListaDeCompra(referenciasProdutos)
                                    }
                                }
                                Log.d("Teste","Sucesso ao atualizar o produto no banco de dados"
                                )
                            }
                            .addOnFailureListener {
                                Log.d("Teste", "Erro ao atualizar o produto no banco de dados")
                            }
                    }
                    else{
                        Log.d("Teste", "Documento não encontrado")
                        val documentoProdutoOriginal = database.collection("produto_nota_fiscal").document(produto.codigoLocal.toString())
                        var produtoOriginal = ProdutoNotaFiscal(produto.codigoLocal.toString(), produto.nome)
                        documentoProdutoOriginal.set(produtoOriginal)
                            .addOnSuccessListener {
                                Log.d("Teste", "Sucesso ao salvar o produto original no banco de dados")
                                val documentoProduto = FirebaseFirestore.getInstance().collection("produto")
                                    .document()
                                produto.id = documentoProduto.id
                                produto.supermercadoId = supermercado.id
                                referenciasProdutos.add(documentoProduto)
                                documentoProduto.set(produto)
                                    .addOnSuccessListener {
                                        flagFinal++
                                        if (flagFinal == produtos.size){
                                            runBlocking {
                                                SalvarListaDeCompra(referenciasProdutos)
                                            }
                                        }
                                        Log.d("Teste", "Sucesso ao salvar o produto no banco de dados")
                                    }
                                    .addOnFailureListener {
                                        Log.d("Teste", "Erro ao salvar o produto no banco de dados")
                                    }

                            }
                            .addOnFailureListener {
                                Log.d("Teste", "Erro ao salvar o produto original no banco de dados")
                            }
                    }

                }
                .addOnFailureListener { exception ->
                    Log.d("Teste", "Erro ao buscar o produto no banco de dados " + exception.message)
                }
        }
    }

    private fun SalvarListaDeCompra(referenciaProdutos: MutableList<DocumentReference>){

        documentoListaDeCompra = database.collection("lista_de_compra")
            .document()

        val updates = hashMapOf<String, Any>(
            "produtos" to referenciaProdutos,
            "nome" to etTituloLista.text.toString(),
            "id" to documentoListaDeCompra.id
        )

        documentoListaDeCompra.set(updates)
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao criar a lista no banco de dados")
                runBlocking {
                    VincularListaAoUsuario()
                }
            }
            .addOnFailureListener { e ->
                Log.d("Teste", "Erro ao criar a lista no banco de dados")
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





}
