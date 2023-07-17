package br.com.marketpricescan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

/**
 * Activity responsável pela leitura de QR Code.
 */
class QRCodeActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qrcode)

        // Inicializa o CodeScanner e associa-o à CodeScannerView
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        // Parâmetros do CodeScanner (valores padrão)
        codeScanner.camera = CodeScanner.CAMERA_BACK // Define a câmera a ser utilizada
        codeScanner.formats = CodeScanner.ALL_FORMATS // Define os formatos de código de barras aceitos
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // Define o modo de foco automático (SAFE ou CONTINUOUS)
        codeScanner.scanMode = ScanMode.SINGLE // Define o modo de leitura do código
        codeScanner.isAutoFocusEnabled = true // Define se o foco automático está habilitado ou não
        codeScanner.isFlashEnabled = false  // Define se o flash está habilitado ou não

        // Callbacks do CodeScanner
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                // Exibe uma mensagem com o resultado da leitura
                Toast.makeText(this, "QR Code lido com sucesso", Toast.LENGTH_SHORT).show()

                // Cria uma Intent para a NotaFiscalActivity e passa a URL do QR Code lido como parâmetro
                val intent = Intent(this, NotaFiscalActivity::class.java)
                intent.putExtra(
                    "URL",
                    it.text
                )
                startActivity(intent)
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                // Exibe uma mensagem de erro em caso de falha na inicialização da câmera
                Toast.makeText(this, "Erro na inicialização da câmera: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        // Inicia a pré-visualização do CodeScanner ao clicar na CodeScannerView
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    /**
     * Sobrescreve o método onResume() para iniciar a pré-visualização do CodeScanner.
     */
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    /**
     * Sobrescreve o método onPause() para liberar os recursos do CodeScanner.
     */
    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    /**
     * Sobrescreve o método onBackPressed() para retornar para a HomeActivity ao pressionar o botão de voltar.
     */
    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}