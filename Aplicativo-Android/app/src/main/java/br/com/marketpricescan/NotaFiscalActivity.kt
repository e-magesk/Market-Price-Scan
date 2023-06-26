package br.com.marketpricescan

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder


class NotaFiscalActivity : AppCompatActivity() {

    // Child class
    data class Produto(var nome: String="", val preco: Double=0.0)
    // Main class
    data class ScrapingResult(val produtos: MutableList<Produto> = mutableListOf(), var count:Int = 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nota_fiscal)

        val url = intent.getStringExtra("URL").toString()

        val encodedUrl = URLEncoder.encode(url, "UTF-8")

        // Desfaz a codificação para barras, dois pontos, ponto de interrogação e igual
        val decodedUrl = encodedUrl
            .replace("%2F", "/")
            .replace("%3A", ":")
            .replace("%3F", "?")
            .replace("%3D", "=")

        // url = "http://www.google.com"

        val tvNF = findViewById<TextView>(R.id.tvNF)

        CoroutineScope(Dispatchers.IO).launch{
            val text : String = getTextFromUrl(decodedUrl)
            withContext(Dispatchers.Main){
                tvNF.text = text
            }
        }
    }

    private fun getTextFromUrl(url: String): String{
        val document = Jsoup.connect(url).get()

        val span = document.select("span.txtTit")
        var ret = document.text()

        val body = document.body()
        //ret = body.text()

        ret = span.text()

        return ret
    }
}
