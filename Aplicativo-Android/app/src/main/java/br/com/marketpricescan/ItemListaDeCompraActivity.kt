package br.com.marketpricescan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ItemListaDeCompraActivity : AppCompatActivity() {

    lateinit var iconCircleCheck : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_lista_de_compra)

        IniciarComponentes()
        Log.d("Teste", "Entrou aqui")
        iconCircleCheck.setOnClickListener(){view ->
            if(iconCircleCheck.drawable.constantState === getDrawable(R.drawable.check_circle)?.constantState){
                iconCircleCheck.setImageResource(R.drawable.unchecked_circle)
            }
            else{
                iconCircleCheck.setImageResource(R.drawable.check_circle)
            }
        }
    }

    fun IniciarComponentes(){
        iconCircleCheck = findViewById(R.id.circleCheck)
    }
}