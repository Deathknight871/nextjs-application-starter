package com.example.carteiravirtual

import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

class GameActivity : AppCompatActivity() {
    private lateinit var walletManager: WalletManager
    private lateinit var txtGamePot: MaterialTextView
    private lateinit var txtCurrentBalance: MaterialTextView
    private lateinit var btnCollectToken: MaterialButton
    private lateinit var btnFinishGame: MaterialButton

    private var gamePot: Double = 0.0
    private var currentBalance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Recupera o dispositivo USB e saldo atual
        val usbDevice = intent.getParcelableExtra<UsbDevice>("device")
        currentBalance = intent.getDoubleExtra("currentBalance", 0.0)

        if (usbDevice == null) {
            Toast.makeText(this, "Erro: Dispositivo USB não encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inicializa o gerenciador da carteira
        walletManager = WalletManager(this, usbDevice)

        // Inicializa as views
        setupViews()
        
        // Atualiza a interface
        updateUI()

        // Configura os listeners dos botões
        setupListeners()
    }

    private fun setupViews() {
        txtGamePot = findViewById(R.id.txtGamePot)
        txtCurrentBalance = findViewById(R.id.txtCurrentBalance)
        btnCollectToken = findViewById(R.id.btnCollectToken)
        btnFinishGame = findViewById(R.id.btnFinishGame)
    }

    private fun updateUI() {
        txtGamePot.text = String.format("Pote do Jogo: R$ %.2f", gamePot)
        txtCurrentBalance.text = String.format("Saldo Atual: R$ %.2f", currentBalance)
        
        // Desabilita o botão de coletar ficha se não houver saldo suficiente
        btnCollectToken.isEnabled = currentBalance >= 1.0
    }

    private fun setupListeners() {
        btnCollectToken.setOnClickListener {
            collectToken()
        }

        btnFinishGame.setOnClickListener {
            showFinishGameDialog()
        }
    }

    private fun collectToken() {
        if (currentBalance >= 1.0) {
            currentBalance -= 1.0
            gamePot += 1.0
            
            // Atualiza o saldo na carteira
            walletManager.atualizarSaldo(currentBalance) { success ->
                if (!success) {
                    Toast.makeText(this, "Erro ao atualizar saldo", Toast.LENGTH_SHORT).show()
                }
            }
            
            updateUI()
        } else {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFinishGameDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Finalizar Jogo")
            .setMessage("Deseja finalizar o jogo e transferir R$ $gamePot para o vencedor?")
            .setPositiveButton("Sim") { _, _ ->
                finishGame()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun finishGame() {
        // Aqui você implementaria a lógica para selecionar o vencedor
        // Por enquanto, vamos apenas devolver o valor para a carteira atual
        val novoSaldo = currentBalance + gamePot
        
        walletManager.atualizarSaldo(novoSaldo) { success ->
            if (success) {
                Toast.makeText(this, "Jogo finalizado! Valor transferido com sucesso", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao transferir valor para o vencedor", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        if (gamePot > 0) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Atenção")
                .setMessage("Existe um valor no pote do jogo. Deseja realmente sair?")
                .setPositiveButton("Sim") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Não", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}
