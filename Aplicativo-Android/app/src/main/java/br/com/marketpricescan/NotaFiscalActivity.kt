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

/**
 * Classe responsável por exibir a lista de compra com base em uma nota fiscal.
 */
class NotaFiscalActivity : AppCompatActivity() {
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
                // Inicia componentes
                rvListaDeCompra = findViewById(R.id.rvListaDeCompra)
                tvListaVazia = findViewById(R.id.tvListaVazia)
                etTituloLista = findViewById(R.id.etTituloLista)

                DefinirAdaptador()
                VerificarSituacaoLista()

                loadingCard.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * Função responsável por obter os produtos da nota fiscal a partir de uma URL.
     *
     * @param url URL da nota fiscal.
     * @return Lista de produtos da nota fiscal.
     */
    private fun getProdutosFromUrl(url: String): MutableList<Produto>{
        val produtos = mutableListOf<Produto>()

        // Tenta conectar à URL e obter o documento HTML
        try{
            val document = Jsoup.connect(url).get()
        }
        catch (e: Exception){
            Log.d("Teste", "Erro " + e.message)
        }

        // Obtém o documento HTML da URL
        val document = Jsoup.connect(url).get()
        val tabela = document.select("table#tabResult")

        val tableRows = tabela.select("table tr")
        for (row in tableRows) {
            val rowData = row.select("td")

            var nome: String = ""
            var preco: Double = 0.0
            var cod: Long = 0

            for (data in rowData) {
                // Obtém nome preço e codigo do produto a partir do elemento HTML
                nome = rowData.select("span.txtTit").text()
                preco = rowData.select("span.valor").text().replace(',', '.').toDouble()

                val textoCod = rowData.select("span.Rcod")
                cod = Regex("\\d+").find(textoCod.text())?.value?.toLong() ?: 0L
            }

            // Cria um objeto Produto com os dados coletados e adiciona à lista de produtos
            produtos.add(Produto(nome, preco, cod.toString()))
        }

        return produtos
    }

    /**
     * Obtém informações de um supermercado a partir de uma URL.
     *
     * @param url A URL do supermercado.
     */
    private fun getSupermercadoFromUrl(url : String){
        // Obtém o documento HTML da URL
        val document = Jsoup.connect(url).get()

        val div = document.select("div#conteudo")
        val nome = div.select("#u20").text()
        val textos = div.select(".text")

        // Obtem valor do CNPJ a paretir do texto
        var cnpj : String = ""
        if(textos[0].text().startsWith("CNPJ: ")){
            cnpj = textos[0].text().substring(6)
        }
        else if(textos[1].text().startsWith("CNPJ:")){
            cnpj = textos[1].text().substring(5)
        }

        // Remove pontos, barras e traços do CNPJ para obter o ID
        val id = cnpj.replace(".", "").replace("/", "").replace("-", "")

        // Obtém o endereço do supermercado a partir do segundo elemento de texto
        val endereco = textos[1].text()

        // Cria um objeto Supermercado com as informações coletadas
        supermercado = Supermercado(id, nome, endereco, cnpj)

        // Chama função para dicionar o supermercado ao banco de dados
        AdicionarSupermercadoAoBanco(supermercado)
    }

    /**
     * Adiciona um objeto Supermercado ao banco de dados.
     *
     * @param supermercado O objeto Supermercado a ser adicionado ao banco de dados.
     */
    private fun AdicionarSupermercadoAoBanco(supermercado : Supermercado){
        try{
            Log.d("Teste", "Antes documento " + supermercado.id)

            // Obtém uma referência ao documento do supermercado no banco de dados
            documentoSupermercado = database.collection("supermercado").document(supermercado.id)
            supermercado.id = documentoSupermercado.id

            // Salva os dados do supermercado no documento
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

    /**
     * Define o adaptador para a lista de produtos.
     */
    private fun DefinirAdaptador(){
        // Cria o adaptador de ProdutoNotaFiscalAdaptador, passando a referência para a atividade (this) e a lista de produtos
        adaptador = ProdutoNotaFiscalAdaptador(this, produtos)

        // Define configurações para RecyclerView
        rvListaDeCompra.setHasFixedSize(true)
        rvListaDeCompra.layoutManager = LinearLayoutManager(this)
        rvListaDeCompra.adapter = adaptador
        rvListaDeCompra.isClickable = true
    }

    /**
     * Verifica a situação da lista de produtos e atualiza a visibilidade dos elementos na tela.
     */
    private fun VerificarSituacaoLista() {
        if (produtos.size == 0) {
            // Se a lista de produtos estiver vazia, exibe a mensagem de lista vazia e oculta a RecyclerView
            tvListaVazia.visibility = TextView.VISIBLE
            rvListaDeCompra.visibility = ListView.INVISIBLE

            tvListaVazia.text = "Lista vazia - Não foram encontrados produtos no QR Code lido"
        } else {
            tvListaVazia.visibility = TextView.INVISIBLE
            rvListaDeCompra.visibility = ListView.VISIBLE
        }
    }

    /**
     * Sobrescreve o método onBackPressed() para exibir um diálogo de confirmação ao pressionar o botão de voltar.
     */
    override fun onBackPressed() {
        // Cria um AlertDialog com título, mensagem e botões de ação para confirmar salvamento da lista
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("Salvar Lista?")
            .setMessage("Deseja salvar a lista " + etTituloLista.text + "?")
            .setPositiveButton("OK") { dialog, which ->
                // Ao pressionar o botão "OK", executa função para criar os produtos
                runBlocking {
                    CriarProdutos()
                }
                // Inicia HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                // Inicia HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
            .create()
        alertDialog.show()
    }

    /**
     * Cria os produtos no banco de dados.
     */
    private fun CriarProdutos(){
        // Se a lista de produtos estiver vazia, salva a lista de compra vazia
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

            // Cria uma query para buscar o produto no banco de dados
            val query = database.collection("produto")
                .whereEqualTo("supermercadoId", supermercado.id)
                .whereEqualTo("codigoLocal", produto.codigoLocal)

            // Atualiza produtos
            query.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("Teste", "QuerySnapshot " + querySnapshot.size())

                    if (querySnapshot.size() !== 0) { // Se o produto já existe no banco de dados, atualiza seus dados
                        Log.d("Teste", "Documento encontrado")

                        // Obtém a referência do documento do produto
                        val docRef = querySnapshot.documents[0].reference

                        // Define id do produto e id do supermercado do produto
                        produto.id = docRef.id
                        produto.supermercadoId = supermercado.id

                        // Adiciona a referência do documento do produto na lista de referências
                        referenciasProdutos.add(docRef)

                        // Atualiza os dados do produto no documento
                        docRef.set(produto)
                            .addOnSuccessListener {
                                // Incrementa a contagem de produtos inseridos
                                flagFinal++
                                // Se todos produtos foram inseridos, chama função para salvar lista de compras
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
                        // Se o produto não existe no banco de dados, cria um novo documento para ele
                        Log.d("Teste", "Documento não encontrado")

                        // Cria um novo documento para o produto na coleção "produto_nota_fiscal"
                        val documentoProdutoOriginal = database.collection("produto_nota_fiscal").document(produto.codigoLocal.toString())

                        // Cria um objeto ProdutoNotaFiscal com os dados do produto original
                        var produtoOriginal = ProdutoNotaFiscal(produto.codigoLocal.toString(), produto.nome)

                        // Salva o produto original no banco de dados
                        documentoProdutoOriginal.set(produtoOriginal)
                            .addOnSuccessListener {
                                Log.d("Teste", "Sucesso ao salvar o produto original no banco de dados")

                                // Cria um novo documento para o produto na coleção "produto"
                                val documentoProduto = FirebaseFirestore.getInstance().collection("produto")
                                    .document()
                                // Define o ID e o supermercadoId do produto
                                produto.id = documentoProduto.id
                                produto.supermercadoId = supermercado.id

                                // Adiciona a referência do documento do produto na lista de referências
                                referenciasProdutos.add(documentoProduto)

                                // Salva o produto no banco de dados
                                documentoProduto.set(produto)
                                    .addOnSuccessListener {
                                        // Incrementa a contagem de produtos inseridos
                                        flagFinal++
                                        if (flagFinal == produtos.size){
                                            // Se todos produtos foram inseridos, chama função para salvar lista de compras
                                            runBlocking {
                                                SalvarListaDeCompra(referenciasProdutos)
                                            }
                                        }
                                        Log.d("Teste", "Sucesso ao salvar o produto no banco de dados")
                                    }
                                    .addOnFailureListener {
                                        // Em caso de erro ao salvar o produto
                                        Log.d("Teste", "Erro ao salvar o produto no banco de dados")
                                    }

                            }
                            .addOnFailureListener {
                                // Em caso de erro ao salvar o produto original
                                Log.d("Teste", "Erro ao salvar o produto original no banco de dados")
                            }
                    }

                }
                .addOnFailureListener { exception ->
                    Log.d("Teste", "Erro ao buscar o produto no banco de dados " + exception.message)
                }
        }
    }

    /**
     * Salva a lista de compra no banco de dados.
     *
     * @param referenciaProdutos A lista de referências dos produtos da lista de compra.
     */
    private fun SalvarListaDeCompra(referenciaProdutos: MutableList<DocumentReference>){
        // Cria um novo documento para a lista de compra na coleção "lista_de_compra"
        documentoListaDeCompra = database.collection("lista_de_compra")
            .document()

        // Cria um mapa de atualizações com os dados da lista de compra
        val updates = hashMapOf<String, Any>(
            "produtos" to referenciaProdutos,
            "nome" to etTituloLista.text.toString(),
            "id" to documentoListaDeCompra.id
        )

        // Salva os dados da lista de compra no documento
        documentoListaDeCompra.set(updates)
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao criar a lista no banco de dados")

                // Executa a função para vincular a lista ao usuário
                runBlocking {
                    VincularListaAoUsuario()
                }
            }
            .addOnFailureListener { e ->
                Log.d("Teste", "Erro ao criar a lista no banco de dados")
            }
    }

    /**
     * Vincula a lista de compra ao usuário.
     */
    private fun VincularListaAoUsuario() {
        // Obtém a referência do documento do usuário na coleção "usuario"
        documentoUsuario = database.collection("usuario")
            .document(usuarioId)

        // Atualiza o campo "listasDeCompra" do documento do usuário adicionando a referência da lista de compra
        documentoUsuario.update("listasDeCompra", FieldValue.arrayUnion(documentoListaDeCompra))
            .addOnSuccessListener {
                Log.d("Teste", "Sucesso ao vincular a lista ao usuário")
            }
            .addOnFailureListener {
                Log.d("Teste", "Erro ao vincular a lista ao usuário")
            }

    }





}
