package com.example

import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.data.repository.UpcomingBill
import com.example.ui.components.CurrencyFormatter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FinanceDomainTest {

    private val testAccount1 = AccountEntity(
        id = 1L,
        name = "Conta Corrente",
        type = "BANK",
        initialBalance = 1000_00L, // R$ 1.000,00
        currentBalance = 1000_00L
    )

    private val testAccount2 = AccountEntity(
        id = 2L,
        name = "Poupança",
        type = "SAVINGS",
        initialBalance = 500_00L, // R$ 500,00
        currentBalance = 500_00L
    )

    private val categorySalary = CategoryEntity(id = 10L, name = "Salário", type = "INCOME")
    private val categoryBills = CategoryEntity(id = 20L, name = "Contas", type = "EXPENSE")

    @Test
    fun testAccountBalanceCalculation_IncomeAndExpense() = runBlocking {
        val transactions = listOf(
            TransactionEntity(
                id = 101L,
                type = "INCOME",
                amount = 250_00L,
                categoryId = categorySalary.id,
                accountId = testAccount1.id,
                description = "Freelance",
                date = System.currentTimeMillis(),
                isPaid = true
            ),
            TransactionEntity(
                id = 102L,
                type = "EXPENSE",
                amount = 100_00L,
                categoryId = categoryBills.id,
                accountId = testAccount1.id,
                description = "Mercado",
                date = System.currentTimeMillis(),
                isPaid = true
            ),
            // Unpaid transaction should NOT affect current balance
            TransactionEntity(
                id = 103L,
                type = "EXPENSE",
                amount = 500_00L,
                categoryId = categoryBills.id,
                accountId = testAccount1.id,
                description = "Conta de Luz Futura",
                date = System.currentTimeMillis() + 86400000L,
                isPaid = false
            )
        )

        // Initial 1000.00 + 250.00 (income) - 100.00 (expense) = 1150.00 (unpaid 500.00 ignored)
        val calculated = calculateBalanceHelper(testAccount1, transactions)
        assertEquals(1150_00L, calculated)
    }

    @Test
    fun testAccountBalanceCalculation_InternalTransfer() = runBlocking {
        val transferTx = TransactionEntity(
            id = 201L,
            type = "TRANSFER",
            amount = 300_00L,
            categoryId = 0L,
            accountId = testAccount1.id,
            targetAccountId = testAccount2.id,
            description = "Transferência para Poupança",
            date = System.currentTimeMillis(),
            isPaid = true
        )

        val txs = listOf(transferTx)

        val balanceAcc1 = calculateBalanceHelper(testAccount1, txs)
        val balanceAcc2 = calculateBalanceHelper(testAccount2, txs)

        // Account 1: 1000 - 300 = 700
        assertEquals(700_00L, balanceAcc1)
        // Account 2: 500 + 300 = 800
        assertEquals(800_00L, balanceAcc2)
    }

    @Test
    fun testUpcomingBills_OverdueNotIncludedInUpcomingHorizon() {
        val now = System.currentTimeMillis()
        val overdueDate = now - (45L * 24 * 60 * 60 * 1000) // 45 days ago
        val futureDate = now + (5L * 24 * 60 * 60 * 1000)   // 5 days ahead

        val transactions = listOf(
            TransactionEntity(
                id = 301L,
                type = "EXPENSE",
                amount = 80_00L,
                categoryId = categoryBills.id,
                accountId = testAccount1.id,
                description = "Conta Atrasada de 45 dias atrás",
                date = overdueDate,
                isPaid = false
            ),
            TransactionEntity(
                id = 302L,
                type = "EXPENSE",
                amount = 120_00L,
                categoryId = categoryBills.id,
                accountId = testAccount1.id,
                description = "Internet próx semana",
                date = futureDate,
                isPaid = false
            )
        )

        val upcoming = FinanceRepository.getUpcomingBills(
            recurring = emptyList(),
            categories = listOf(categoryBills),
            daysAhead = 30,
            monthTransactions = transactions,
            accounts = emptyList()
        )

        // Only the 5-day upcoming bill should be in upcoming (0..30 days), overdue 45-day should NOT contaminate it
        assertEquals(1, upcoming.size)
        assertEquals("Internet próx semana", upcoming[0].name)
        assertEquals(120_00L, upcoming[0].amount)
    }

    @Test
    fun testRecurrenceIdentification_DoesNotFalseMatchText() {
        val rec = RecurringTransactionEntity(
            id = 55L,
            name = "Luz",
            type = "EXPENSE",
            amount = 150_00L,
            categoryId = categoryBills.id,
            accountId = testAccount1.id,
            frequency = "MONTHLY",
            dueDay = 15,
            nextDate = System.currentTimeMillis()
        )

        // Transaction that happens to have "Luz" in the name on a different account or category
        val manualTx = TransactionEntity(
            id = 401L,
            type = "EXPENSE",
            amount = 25_00L,
            categoryId = categorySalary.id, // Different category
            accountId = 999L,              // Different account
            description = "Lâmpada de Luz para quarto",
            date = System.currentTimeMillis(),
            isPaid = true,
            recurrenceRule = null // Not registered as this recurring rule
        )

        val upcoming = FinanceRepository.getUpcomingBills(
            recurring = listOf(rec),
            categories = listOf(categoryBills),
            daysAhead = 30,
            monthTransactions = listOf(manualTx),
            accounts = emptyList()
        )

        val bill = upcoming.firstOrNull { it.recurringId == rec.id }
        // Should NOT consider the bill as paid because manualTx was just an unrelated transaction
        assertFalse(bill?.isPaidThisMonth ?: true)
    }

    @Test
    fun testCurrencyFormatter_DomainLimitsAndOverflowProtection() {
        // Zero
        assertEquals(0L, CurrencyFormatter.parseToCentavos("0"))
        assertEquals(0L, CurrencyFormatter.parseToCentavos(""))

        // Normal values
        assertEquals(150_50L, CurrencyFormatter.parseToCentavos("150,50"))
        assertEquals(1000_00L, CurrencyFormatter.parseToCentavos("1.000,00"))

        // Extremely huge string to test overflow prevention
        val hugeInput = "99999999999999999999999999"
        val parsed = CurrencyFormatter.parseToCentavos(hugeInput)
        assertTrue(parsed <= CurrencyFormatter.MAX_CENTAVOS)
        assertTrue(parsed > 0L)
    }

    @Test
    fun testDateValidation_ShortMonths() {
        // February in non-leap year (e.g. 2023) has 28 days max
        val dayFebNonLeap = CurrencyFormatter.validateDayOfMonth(day = 31, monthIndex = Calendar.FEBRUARY, year = 2023)
        assertEquals(28, dayFebNonLeap)

        // February in leap year (e.g. 2024) has 29 days max
        val dayFebLeap = CurrencyFormatter.validateDayOfMonth(day = 31, monthIndex = Calendar.FEBRUARY, year = 2024)
        assertEquals(29, dayFebLeap)

        // April has 30 days max
        val dayApril = CurrencyFormatter.validateDayOfMonth(day = 31, monthIndex = Calendar.APRIL, year = 2024)
        assertEquals(30, dayApril)

        // Valid day in January remains unchanged
        val dayJan = CurrencyFormatter.validateDayOfMonth(day = 15, monthIndex = Calendar.JANUARY, year = 2024)
        assertEquals(15, dayJan)
    }

    private fun calculateBalanceHelper(account: AccountEntity, transactions: List<TransactionEntity>): Long {
        var balance = account.initialBalance
        for (tx in transactions) {
            if (!tx.isPaid) continue
            when (tx.type) {
                "INCOME" -> {
                    if (tx.accountId == account.id) balance += tx.amount
                }
                "EXPENSE" -> {
                    if (tx.accountId == account.id) balance -= tx.amount
                }
                "TRANSFER" -> {
                    if (tx.accountId == account.id) balance -= tx.amount
                    if (tx.targetAccountId == account.id) balance += tx.amount
                }
            }
        }
        return balance
    }
}
