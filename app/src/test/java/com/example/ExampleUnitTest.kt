package com.example

import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.data.repository.UpcomingBill
import com.example.ui.components.CurrencyFormatter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCurrencyFormatter() {
        assertEquals("R$\u00A0150,00", CurrencyFormatter.formatCentavos(15000L, hidePrivacy = false).replace(" ", "\u00A0"))
        assertEquals("R$ ••••••", CurrencyFormatter.formatCentavos(15000L, hidePrivacy = true))
        assertEquals(15000L, CurrencyFormatter.parseToCentavos("150,00"))
        assertEquals(15000L, CurrencyFormatter.parseToCentavos("150.00"))
    }

    @Test
    fun testDailyLimitCalculation() {
        val availableBalance = 300000L // R$ 3.000,00
        val upcomingBills = listOf(
            UpcomingBill(
                id = 1L,
                name = "Aluguel",
                amount = 100000L, // R$ 1.000,00
                dueDate = System.currentTimeMillis() + 86400000L * 5,
                categoryName = "Moradia",
                daysUntilDue = 5,
                isIncome = false,
                isPaidThisMonth = false
            )
        )
        val limitCalc = FinanceRepository.calculateDailyLimit(
            availableBalance = availableBalance,
            upcomingBills = upcomingBills,
            upcomingIncomes = emptyList(),
            goals = emptyList(),
            todaySpent = 0L
        )

        assertTrue(limitCalc.netAvailable > 0L)
        assertTrue(limitCalc.dailyLimit > 0L)
        assertTrue(limitCalc.daysRemainingInMonth in 1..31)
    }

    @Test
    fun testCategorySuggestion() {
        val categories = listOf(
            CategoryEntity(id = 1L, name = "Alimentação", type = "EXPENSE"),
            CategoryEntity(id = 2L, name = "Transporte", type = "EXPENSE"),
            CategoryEntity(id = 3L, name = "Saúde", type = "EXPENSE")
        )

        val foodCat = FinanceRepository.suggestCategory("Almoço no restaurante", categories)
        assertEquals("Alimentação", foodCat?.name)

        val uberCat = FinanceRepository.suggestCategory("Corrida Uber para o trabalho", categories)
        assertEquals("Transporte", uberCat?.name)

        val pharmaCat = FinanceRepository.suggestCategory("Farmácia remédios", categories)
        assertEquals("Saúde", pharmaCat?.name)
    }

    @Test
    fun testCreditCardCycleInfo() {
        val card = AccountEntity(
            id = 5L,
            name = "Nubank",
            type = "CREDIT_CARD",
            creditLimit = 500000L, // R$ 5.000,00
            closingDay = 5,
            dueDay = 12
        )
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                type = "EXPENSE",
                amount = 120000L, // R$ 1.200,00
                categoryId = 1L,
                accountId = 5L,
                description = "Compras diversas",
                date = System.currentTimeMillis()
            )
        )

        val cycleInfo = FinanceRepository.getCreditCardCycleInfo(card, transactions)
        assertEquals(120000L, cycleInfo.currentInvoiceAmount)
        assertEquals(380000L, cycleInfo.availableLimit)
        assertEquals(500000L, cycleInfo.creditLimit)
    }
}
