package com.naulify.agent.util

import android.content.Context
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.NumberFormat
import java.util.Locale

object Utils {
    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "KE")).format(amount)
    }

    fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun generateQRCode(content: String, width: Int, height: Int): BitMatrix {
        return MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height
        )
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^[0-9]{10}$"))
    }

    fun validateVehicleRegistration(registration: String): Boolean {
        return registration.matches(Regex("^[A-Z]{3}\\s?[0-9]{3}[A-Z]$"))
    }

    fun validateMpesaShortCode(shortCode: String): Boolean {
        return shortCode.matches(Regex("^[0-9]{5,6}$"))
    }
}
