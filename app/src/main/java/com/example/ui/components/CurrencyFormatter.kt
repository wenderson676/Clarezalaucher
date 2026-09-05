package com.example.ui.components

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val ptBrLocale = Locale("pt", "BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(ptBrLocale)

    fun formatCentavos(centavos: Long, hidePrivacy: Boolean = false): String {
        if (hidePrivacy) return "R$ ••••••"
        val valueInReais = centavos / 100.0
        return currencyFormat.format(valueInReais)
    }

    fun formatPlainNumber(centavos: Long): String {
        val valueInReais = centavos / 100.0
        return String.format(ptBrLocale, "%.2f", valueInReais)
    }

    fun parseToCentavos(text: String): Long {
        val clean = text.replace("[^0-9]".toRegex(), "")
        return clean.toLongOrNull() ?: 0L
    }
}
