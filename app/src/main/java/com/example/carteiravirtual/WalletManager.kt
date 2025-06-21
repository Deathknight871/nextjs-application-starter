package com.example.carteiravirtual

import android.content.Context
import android.hardware.usb.UsbDevice
import com.google.gson.Gson
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log

data class UserWallet(
    val userName: String,
    var balance: Double,
    val createdAt: Long = System.currentTimeMillis()
)

class WalletManager(private val context: Context, private val usbDevice: UsbDevice) {
    private val usbHelper = USBHelper(context)
    private val gson = Gson()
    
    companion object {
        private const val WALLET_FOLDER = "VirtualWallet"
        private const val WALLET_FILE = "wallet.dat"
        private const val SALT = "CarteiraVirtualSalt" // Em produção, deve ser gerado aleatoriamente
        private const val SECRET_KEY = "ChaveSecretaApp123" // Em produção, deve ser armazenado com segurança
    }

    fun initWallet(callback: (Boolean) -> Unit) {
        try {
            if (!usbHelper.fileExists(usbDevice, "$WALLET_FOLDER/$WALLET_FILE")) {
                // Cria pasta se não existir
                if (!usbHelper.createDirectory(usbDevice, WALLET_FOLDER)) {
                    callback(false)
                    return
                }

                // Cria carteira com valores padrão
                val novaCarteira = UserWallet("Usuário", 0.0)
                salvarCarteira(novaCarteira) { success ->
                    callback(success)
                }
            } else {
                // Verifica se consegue ler a carteira existente
                val carteira = lerCarteira()
                callback(carteira != null)
            }
        } catch (e: Exception) {
            Log.e("WalletManager", "Erro ao inicializar carteira", e)
            callback(false)
        }
    }

    fun lerCarteira(): UserWallet? {
        try {
            val conteudoCriptografado = usbHelper.readFile(usbDevice, "$WALLET_FOLDER/$WALLET_FILE")
                ?: return null

            val conteudoDescriptografado = decriptografar(conteudoCriptografado)
            return gson.fromJson(conteudoDescriptografado, UserWallet::class.java)
        } catch (e: Exception) {
            Log.e("WalletManager", "Erro ao ler carteira", e)
            return null
        }
    }

    fun salvarCarteira(carteira: UserWallet, callback: (Boolean) -> Unit) {
        try {
            val conteudoJson = gson.toJson(carteira)
            val conteudoCriptografado = criptografar(conteudoJson)

            val sucesso = usbHelper.writeFile(usbDevice, "$WALLET_FOLDER/$WALLET_FILE", conteudoCriptografado)
            callback(sucesso)
        } catch (e: Exception) {
            Log.e("WalletManager", "Erro ao salvar carteira", e)
            callback(false)
        }
    }

    fun atualizarSaldo(novoSaldo: Double, callback: (Boolean) -> Unit) {
        val carteira = lerCarteira()
        if (carteira != null) {
            carteira.balance = novoSaldo
            salvarCarteira(carteira, callback)
        } else {
            callback(false)
        }
    }

    private fun criptografar(texto: String): ByteArray {
        try {
            val cipher = getCipher(Cipher.ENCRYPT_MODE)
            val textoCriptografado = cipher.doFinal(texto.toByteArray(Charsets.UTF_8))
            return textoCriptografado
        } catch (e: Exception) {
            throw RuntimeException("Erro ao criptografar", e)
        }
    }

    private fun decriptografar(dados: ByteArray): String {
        try {
            val cipher = getCipher(Cipher.DECRYPT_MODE)
            val textoDescriptografado = cipher.doFinal(dados)
            return String(textoDescriptografado, Charsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Erro ao descriptografar", e)
        }
    }

    private fun getCipher(mode: Int): Cipher {
        // Em produção, use um método mais seguro para derivação de chave
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT.toByteArray(), 65536, 256)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(16)) // Em produção, use IV aleatório
        
        cipher.init(mode, secretKey, iv)
        return cipher
    }
}
