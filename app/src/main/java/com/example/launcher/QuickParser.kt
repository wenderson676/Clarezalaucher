package com.example.launcher

import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import java.util.regex.Pattern

data class ParsedTransaction(
    val type: TransactionType,
    val amountCentavos: Long,
    val description: String,
    val suggestedCategory: CategoryEntity? = null,
    val isAmbiguous: Boolean = false
)

object QuickParser {

    private val numberRegex = Pattern.compile("(?:r\\$\\s*)?(\\d+(?:[.,]\\d{1,2})?)", Pattern.CASE_INSENSITIVE)

    private val removeWords = setOf(
        "r$", "recebi", "ganhei", "gastei", "comprei", "paguei", "transferi",
        "de", "no", "na", "em", "para", "pra", "pro", "com", "+", "-"
    )

    fun parse(
        input: String,
        categories: List<CategoryEntity> = emptyList()
    ): ParsedTransaction? {
        val raw = input.trim()
        if (raw.isEmpty()) return null

        val lower = raw.lowercase()

        // 1. Detect Type
        val incomeKeywords = listOf("recebi", "ganhei", "salario", "salário", "renda", "pix recebido", "entrada", "receita", "+")
        val transferKeywords = listOf("transferi", "transferência", "transferencia", "mandei para poupança", "guardei")
        
        val type = when {
            transferKeywords.any { lower.contains(it) } -> TransactionType.TRANSFER
            incomeKeywords.any { lower.contains(it) } -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // 2. Extract numeric amount
        val matcher = numberRegex.matcher(raw)
        if (!matcher.find()) return null

        val numberStr = matcher.group(1)?.replace(",", ".") ?: return null
        val amountDouble = numberStr.toDoubleOrNull() ?: return null
        val amountCentavos = (amountDouble * 100).toLong()
        if (amountCentavos <= 0) return null

        // 3. Extract description by removing the number and noise words
        val rawWithoutNumber = (raw.substring(0, matcher.start()) + " " + raw.substring(matcher.end())).trim()

        val words = rawWithoutNumber.split(Regex("\\s+"))
        val filteredWords = words.filter { word ->
            val clean = word.lowercase().trim('.', ',', ':', ';', '!', '?', '(', ')', '"', '\'')
            clean.isNotEmpty() && clean !in removeWords && word !in removeWords
        }

        var desc = filteredWords.joinToString(" ").trim()
        if (desc.isEmpty()) {
            desc = if (type == TransactionType.INCOME) "Receita rápida" else "Despesa rápida"
        } else {
            // Capitalize first letter
            desc = desc.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        // 4. Auto-suggest category
        val suggestedCat = if (categories.isNotEmpty()) {
            findMatchingCategory(desc, type, categories)
        } else null

        return ParsedTransaction(
            type = type,
            amountCentavos = amountCentavos,
            description = desc,
            suggestedCategory = suggestedCat,
            isAmbiguous = false
        )
    }

    private fun findMatchingCategory(
        description: String,
        type: TransactionType,
        categories: List<CategoryEntity>
    ): CategoryEntity? {
        val lower = description.lowercase()
        val typeStr = if (type == TransactionType.INCOME) "INCOME" else "EXPENSE"
        val filtered = categories.filter { it.type == typeStr }

        val keywordMap = mapOf(
            "Alimentação" to listOf("mercado", "supermercado", "padaria", "lanche", "restaurante", "almoço", "almoco", "jantar", "ifood", "comida", "feira", "açougue", "acougue", "café", "cafe", "pão", "pao", "pizza", "burger"),
            "Transporte" to listOf("uber", "99", "taxi", "gasolina", "combustível", "combustivel", "ônibus", "onibus", "metrô", "metro", "posto", "estacionamento", "pedágio", "pedagio"),
            "Moradia" to listOf("aluguel", "condomínio", "condominio", "iptu", "casa"),
            "Contas" to listOf("luz", "energia", "água", "agua", "internet", "wifi", "net", "claro", "vivo", "tim", "telefone", "celular", "netflix", "spotify"),
            "Saúde" to listOf("farmácia", "farmacia", "drogaria", "remédio", "remedio", "médico", "medico", "dentista", "hospital", "exame"),
            "Educação" to listOf("curso", "faculdade", "escola", "livro", "mensalidade"),
            "Lazer" to listOf("cinema", "show", "praia", "bar", "cerveja", "festa", "jogos", "steam"),
            "Compras" to listOf("roupa", "calçado", "tenis", "tênis", "shopping", "amazon", "shopee", "mercado livre"),
            "Dívidas" to listOf("empréstimo", "emprestimo", "juros", "parcela", "fatura", "cartão", "cartao"),
            "Salário" to listOf("salário", "salario", "holerite", "13", "adiantamento"),
            "Trabalho" to listOf("diária", "diaria", "freela", "freelance", "bico", "serviço", "servico"),
            "Renda Extra" to listOf("venda", "reembolso", "rendimento", "dividendo", "lucro", "cashback")
        )

        for ((catName, keywords) in keywordMap) {
            if (keywords.any { lower.contains(it) }) {
                val cat = filtered.find { it.name.equals(catName, ignoreCase = true) }
                if (cat != null) return cat
            }
        }

        return filtered.firstOrNull()
    }
}
