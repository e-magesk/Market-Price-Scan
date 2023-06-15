package br.com.marketpricescan.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import br.com.marketpricescan.R

class ItemListaAdaptador(context: Context, itens : Array<String>) : BaseAdapter(){

    var itens : Array<String> = itens
    var layoutInflater : LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return itens.size
    }

    override fun getItem(position: Int): Any {
        return itens[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = layoutInflater.inflate(R.layout.item_lista_de_compra, parent, false)
        }
        var tvProdutoListaDeCompra = itemView!!.findViewById<TextView>(R.id.tvProdutoListaDeCompra)
        var ivCircleCheck = itemView!!.findViewById<ImageView>(R.id.circleCheck)
        var checkOrUncheck : Int = 0

        tvProdutoListaDeCompra.text = itens[position]
        ivCircleCheck.setOnClickListener(){view ->

            if(checkOrUncheck === 1){
                ivCircleCheck.setImageResource(R.drawable.unchecked_circle)
                checkOrUncheck = 0
            }
            else{
                checkOrUncheck = 1
                ivCircleCheck.setImageResource(R.drawable.check_circle)
            }
        }

        return itemView
    }
}
