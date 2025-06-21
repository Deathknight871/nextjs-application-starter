package com.example.carteiravirtual

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.*
import android.os.Build
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class USBHelper(private val context: Context) {
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    
    companion object {
        private const val ACTION_USB_PERMISSION = "com.example.carteiravirtual.USB_PERMISSION"
    }

    fun getConnectedUSBDevice(): UsbDevice? {
        return usbManager.deviceList.values.firstOrNull { device ->
            // Verifica se é um dispositivo de armazenamento em massa
            device.interfaceCount > 0 && device.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE
        }
    }

    fun requestPermission(device: UsbDevice, callback: (Boolean) -> Unit) {
        if (usbManager.hasPermission(device)) {
            callback(true)
            return
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            flags
        )

        context.registerReceiver(
            { context, intent ->
                if (intent.action == ACTION_USB_PERMISSION) {
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    callback(granted)
                    context.unregisterReceiver(this)
                }
            },
            android.content.IntentFilter(ACTION_USB_PERMISSION)
        )

        usbManager.requestPermission(device, permissionIntent)
    }

    fun openDevice(device: UsbDevice): UsbDeviceConnection? {
        return usbManager.openDevice(device)
    }

    // Funções para manipulação de arquivos no pendrive
    fun writeFile(device: UsbDevice, path: String, content: ByteArray): Boolean {
        try {
            val connection = openDevice(device) ?: return false
            val interface0 = device.getInterface(0)
            connection.claimInterface(interface0, true)

            // Aqui implementaríamos a lógica específica do protocolo MTP/PTP
            // para escrita no dispositivo USB. Este é um exemplo simplificado.
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun readFile(device: UsbDevice, path: String): ByteArray? {
        try {
            val connection = openDevice(device) ?: return null
            val interface0 = device.getInterface(0)
            connection.claimInterface(interface0, true)

            // Aqui implementaríamos a lógica específica do protocolo MTP/PTP
            // para leitura do dispositivo USB. Este é um exemplo simplificado.
            
            return ByteArray(0)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun createDirectory(device: UsbDevice, path: String): Boolean {
        try {
            val connection = openDevice(device) ?: return false
            val interface0 = device.getInterface(0)
            connection.claimInterface(interface0, true)

            // Aqui implementaríamos a lógica específica do protocolo MTP/PTP
            // para criar diretório no dispositivo USB. Este é um exemplo simplificado.
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun fileExists(device: UsbDevice, path: String): Boolean {
        try {
            val connection = openDevice(device) ?: return false
            val interface0 = device.getInterface(0)
            connection.claimInterface(interface0, true)

            // Aqui implementaríamos a lógica específica do protocolo MTP/PTP
            // para verificar existência de arquivo no dispositivo USB. 
            // Este é um exemplo simplificado.
            
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
