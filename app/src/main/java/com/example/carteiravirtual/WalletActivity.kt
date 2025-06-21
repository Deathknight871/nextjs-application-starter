package com.example.carteiravirtual

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class WalletActivity : AppCompatActivity() {
    private lateinit var walletManager: WalletManager
    private var currentWallet: UserWallet? = null
    private lateinit var txtUserName: MaterialTextView
    private lateinit var txtBalance: MaterialTextView
    private lateinit var cardWallet: MaterialCardView
    private lateinit var btnStartGame: MaterialButton
    private lateinit var btnRefresh: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        // Recupera o dispositivo USB da intent
        val usbDevice = intent.getParcelableExtra<UsbDevice>("device")
        if (usbDevice == null) {
            Toast.makeText(this, "Erro: Dispositivo USB não encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inicializa o gerenciador da carteira
        walletManager = WalletManager(this, usbDevice)

        // Inicializa as views
        setupViews()
        
        // Carrega os dados da carteira
        loadWalletData()

        // Configura os listeners dos botões
        setupListeners(usbDevice)
    }

    private fun setupViews() {
        txtUserName = findViewById(R.id.txtUserName)
        txtBalance = findViewById(R.id.txtBalance)
        cardWallet = findViewById(R.id.cardWallet)
        btnStartGame = findViewById(R.id.btnStartGame)
        btnRefresh = findViewById(R.id.btnRefresh)
    }

    private fun loadWalletData() {
        currentWallet = walletManager.lerCarteira()
        currentWallet?.let { wallet ->
            txtUserName.text = wallet.userName
            txtBalance.text = String.format("Saldo: R$ %.2f", wallet.balance)
        } ?: run {
            Toast.makeText(this, "Erro ao carregar dados da carteira", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners(usbDevice: UsbDevice) {
        btnStartGame.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("device", usbDevice)
                putExtra("currentBalance", currentWallet?.balance ?: 0.0)
            }
            startActivity(intent)
        }

        btnRefresh.setOnClickListener {
            loadWalletData()
            Toast.makeText(this, "Dados atualizados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza os dados quando voltar para a tela
        loadWalletData()
    }
}
