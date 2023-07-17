package br.com.marketpricescan.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import br.com.marketpricescan.model.Usuario

/**
 * Adaptador para exibir uma lista de usuários em um AutoCompleteTextView, filtrando-os por nome.
 *
 * @param context O contexto da aplicação.
 * @param listaUsuarios A lista de usuários completa.
 */
class UsuarioNomeArrayAdaptador(context: Context, listaUsuarios: List<Usuario>) :
    ArrayAdapter<Usuario>(context, android.R.layout.simple_dropdown_item_1line, listaUsuarios) {
    private var sugestoes: List<Usuario> = listaUsuarios
    private var sugestoesFiltradas: List<Usuario> = listaUsuarios

    /**
     * Obtém a exibição do item na posição especificada.
     *
     * @param position A posição do item na lista.
     * @param convertView A exibição do item reutilizada, se disponível.
     * @param parent O ViewGroup pai ao qual a exibição será adicionada.
     * @return A exibição do item na posição especificada.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val objetoCompleto = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = objetoCompleto?.nome

        return view
    }

    /**
     * Obtém um filtro que realiza a filtragem de sugestões com base em um texto de restrição.
     *
     * @return Um filtro para realizar a filtragem de sugestões.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            /**
             * Executa a filtragem das sugestões com base no texto de restrição fornecido.
             *
             * @param constraint O texto de restrição fornecido para a filtragem.
             * @return Os resultados da filtragem.
             */
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtro = constraint?.toString()?.toLowerCase()
                val resultados = FilterResults()

                // Se o filtro estiver vazio ou nulo, exibir todas as sugestões
                if (filtro.isNullOrEmpty()) {
                    resultados.values = sugestoes
                    resultados.count = sugestoes.size
                } else {
                    // Filtrar as sugestões com base no texto de restrição (nome)
                    val sugestoesFiltradasTemp = mutableListOf<Usuario>()
                    for (usuario in sugestoes) {
                        if (usuario.nome.toLowerCase().startsWith(filtro)) {
                            sugestoesFiltradasTemp.add(usuario)
                        }
                    }
                    resultados.values = sugestoesFiltradasTemp
                    resultados.count = sugestoesFiltradasTemp.size
                }

                return resultados
            }

            /**
             * Atualiza a lista de sugestões filtradas e notifica o adaptador para atualizar a exibição.
             *
             * @param constraint O texto de restrição fornecido para a filtragem.
             * @param results Os resultados da filtragem.
             */
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                sugestoesFiltradas = results?.values as? List<Usuario> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Obtém o número total de itens filtrados.
     *
     * @return O número total de itens filtrados.
     */
    override fun getCount(): Int {
        return sugestoesFiltradas.size
    }

    /**
     * Obtém o item de usuário na posição especificada.
     *
     * @param position A posição do item na lista filtrada.
     * @return O item de usuário na posição especificada, ou null se a posição estiver fora dos limites.
     */
    override fun getItem(position: Int): Usuario? {
        return sugestoesFiltradas.getOrNull(position)
    }
}