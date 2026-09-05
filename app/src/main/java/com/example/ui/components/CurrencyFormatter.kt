package com.example.ui.components

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object CurrencyFormatter {

    private val ptBrLocale = Locale("pt", "BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(ptBrLocale)

    // Max allowed value in centavos: R$ 999.999.999,99 (nearly 1 billion BRL)
    const val MAX_CENTAVOS: Long = 999_999_999_99L

    fun formatCentavos(centavos: Long, hidePrivacy: Boolean = false): String {
        if (hidePrivacy) return "R$ ••••••"
        val clamped = centavos.coerceIn(-MAX_CENTAVOS, MAX_CENTAVOS)
        val valueInReais = clamped / 100.0
        return currencyFormat.format(valueInReais)
    }

    fun formatPlainNumber(centavos: Long): String {
        val clamped = centavos.coerceIn(-MAX_CENTAVOS, MAX_CENTAVOS)
        val valueInReais = clamped / 100.0
        return String.format(ptBrLocale, "%.2f", valueInReais)
    }

    /**
     * Safely parses monetary text to centavos, preventing numerical overflow and enforcing domain limits.
     */
    fun parseToCentavos(text: String): Long {
        val clean = text.replace("[^0-9]".toRegex(), "").trimStart('0')
        if (clean.isBlank()) return 0L
        // Restrict length to prevent Long overflow (11 digits is R$ 999.999.999,99)
        val truncated = if (clean.length > 11) clean.take(11) else clean
        val parsed = truncated.toLongOrNull() ?: 0L
        return parsed.coerceIn(0L, MAX_CENTAVOS)
    }

    /**
     * Validates and bounds a day-of-month (1..31) against the real maximum days of the specified month and year.
     * monthIndex is 0-indexed (0 = January, 11 = December).
     */
    fun validateDayOfMonth(day: Int, monthIndex: Int, year: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return day.coerceIn(1, maxDay)
    }
}
