package com.example.carteiravirtual

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var usbHelper: USBHelper
    private var currentDevice: UsbDevice? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    device?.let { detectPendrive(it) }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Toast.makeText(context, "Pendrive desconectado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbHelper = USBHelper(this)

        // Registra o receptor para eventos USB
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        registerReceiver(usbReceiver, filter)

        // Verifica se já existe um pendrive conectado
        usbHelper.getConnectedUSBDevice()?.let { device ->
            detectPendrive(device)
        }
    }

    private fun detectPendrive(device: UsbDevice) {
        currentDevice = device
        try {
            usbHelper.requestPermission(device) { granted ->
                if (granted) {
                    inicializarCarteira(device)
                } else {
                    mostrarErro("Permissão negada para acessar o pendrive")
                }
            }
        } catch (e: Exception) {
            mostrarErro("Erro ao detectar pendrive: ${e.message}")
        }
    }

    private fun inicializarCarteira(device: UsbDevice) {
        val walletManager = WalletManager(this, device)
        walletManager.initWallet { success ->
            if (success) {
                // Abre a tela da carteira
                val intent = Intent(this, WalletActivity::class.java).apply {
                    putExtra("device", device)
                }
                startActivity(intent)
            } else {
                mostrarErro("Falha ao inicializar a carteira virtual")
            }
        }
    }

    private fun mostrarErro(mensagem: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Erro")
            .setMessage(mensagem)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }
}
