package br.com.marketpricescan.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import br.com.marketpricescan.model.Produto
import br.com.marketpricescan.model.Usuario

class ProdutoNomeArrayAdaptador(context: Context, listaProdutos: List<Produto>) :
    ArrayAdapter<Produto>(context, android.R.layout.simple_dropdown_item_1line, listaProdutos) {
    private var sugestoes: List<Produto> = listaProdutos
    private var sugestoesFiltradas: List<Produto> = listaProdutos

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val objetoCompleto = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = objetoCompleto?.nome

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as Produto)?.nome ?: ""
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.toLowerCase()
                val resultados = FilterResults()

                if (filtro.isNullOrEmpty()) {
                    resultados.values = sugestoes
                    resultados.count = sugestoes.size
                } else {
                    val sugestoesFiltradasTemp = mutableListOf<Produto>()
                    for (produto in sugestoes) {
                        if (produto.nome.toLowerCase().startsWith(filtro)) {
                            sugestoesFiltradasTemp.add(produto)
                        }
                    }
                    resultados.values = sugestoesFiltradasTemp
                    resultados.count = sugestoesFiltradasTemp.size
                }

                return resultados
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                sugestoesFiltradas = results?.values as? List<Produto> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    override fun getCount(): Int {
        return sugestoesFiltradas.size
    }

    override fun getItem(position: Int): Produto? {
        return sugestoesFiltradas.getOrNull(position)
    }
}