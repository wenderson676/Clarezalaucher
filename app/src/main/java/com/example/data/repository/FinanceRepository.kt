package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.dao.AccountDao
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.GoalDao
import com.example.data.dao.RecurringDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.TransactionDao
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SettingEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class AccountWithBalance(
    val account: AccountEntity,
    val calculatedBalance: Long // In centavos
)

data class UpcomingBill(
    val id: Long,
    val name: String,
    val amount: Long,
    val dueDate: Long,
    val categoryName: String,
    val daysUntilDue: Int,
    val isIncome: Boolean = false,
    val isPaidThisMonth: Boolean = false,
    val recurringId: Long? = id,
    val isCreditCardInvoice: Boolean = false,
    val cardAccountId: Long? = null
)

data class CreditCardCycleInfo(
    val accountId: Long,
    val cardName: String,
    val creditLimit: Long,
    val currentInvoiceAmount: Long,
    val availableLimit: Long,
    val closingDay: Int,
    val dueDay: Int,
    val nextDueDate: Long,
    val isClosed: Boolean
)

data class SpendingLimitCalculation(
    val currentBalance: Long,
    val upcomingBillsMonth: Long,
    val upcomingIncomesMonth: Long = 0L,
    val reservedForGoals: Long,
    val netAvailable: Long,
    val daysRemainingInMonth: Int,
    val dailyLimit: Long,
    val todaySpent: Long = 0L,
    val todayRemainingLimit: Long = 0L,
    val formulaExplanation: String
)

data class ForecastPoint(
    val timestamp: Long,
    val dayLabel: String,
    val projectedBalance: Long,
    val isNegative: Boolean,
    val realisticBalance: Long = projectedBalance
)

data class ForecastResult(
    val currentBalance: Long,
    val projected7Days: Long,
    val projected30Days: Long,
    val projected90Days: Long,
    val points: List<ForecastPoint>,
    val riskWarningDate: String? = null,
    val dailyAverageExpense: Long = 0L,
    val realisticProjected30Days: Long = 0L,
    val realisticProjected90Days: Long = 0L
)

data class CategorySpend(
    val category: CategoryEntity,
    val totalSpent: Long,
    val budgetAmount: Long? = null,
    val percentage: Float = 0f
)

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val recurringDao: RecurringDao,
    private val goalDao: GoalDao,
    private val budgetDao: BudgetDao,
    private val settingsDao: SettingsDao,
    private val database: AppDatabase? = null
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val activeAccounts: Flow<List<AccountEntity>> = accountDao.getAllActiveAccounts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val activeRecurring: Flow<List<RecurringTransactionEntity>> = recurringDao.getActiveRecurring()
    val allRecurring: Flow<List<RecurringTransactionEntity>> = recurringDao.getAllRecurring()
    val allGoals: Flow<List<FinancialGoalEntity>> = goalDao.getAllGoals()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allSettings: Flow<List<SettingEntity>> = settingsDao.getAllSettings()

    // Deterministic Balance Calculations
    suspend fun getAccountBalance(account: AccountEntity, transactions: List<TransactionEntity>): Long {
        var balance = account.initialBalance
        for (tx in transactions) {
            if (!tx.isPaid) continue
            when (tx.type) {
                "INCOME" -> {
                    if (tx.accountId == account.id) {
                        balance += tx.amount
                    }
                }
                "EXPENSE" -> {
                    if (tx.accountId == account.id) {
                        balance -= tx.amount
                    }
                }
                "TRANSFER" -> {
                    if (tx.accountId == account.id) {
                        balance -= tx.amount
                    }
                    if (tx.targetAccountId == account.id) {
                        balance += tx.amount
                    }
                }
            }
        }
        return balance
    }

    suspend fun getTotalAvailableBalance(accounts: List<AccountEntity>, transactions: List<TransactionEntity>): Long {
        var total = 0L
        for (account in accounts.filter { it.isActive && it.type != "CREDIT_CARD" }) {
            total += getAccountBalance(account, transactions)
        }
        return total
    }

    suspend fun getTotalNetWorth(accounts: List<AccountEntity>, transactions: List<TransactionEntity>): Long {
        var total = 0L
        for (account in accounts.filter { it.isActive }) {
            total += getAccountBalance(account, transactions)
        }
        return total
    }

    // Upcoming Bills Calculation
    fun getUpcomingBills(
        recurring: List<RecurringTransactionEntity>,
        categories: List<CategoryEntity>,
        daysAhead: Int = 30,
        monthTransactions: List<TransactionEntity> = emptyList(),
        accounts: List<AccountEntity> = emptyList()
    ): List<UpcomingBill> = Companion.getUpcomingBills(recurring, categories, daysAhead, monthTransactions, accounts)

    fun getCreditCardCycleInfo(
        card: AccountEntity,
        transactions: List<TransactionEntity>
    ): CreditCardCycleInfo = Companion.getCreditCardCycleInfo(card, transactions)

    // Daily Spending Limit Calculator: "Quanto Posso Gastar?"
    fun calculateDailyLimit(
        availableBalance: Long,
        upcomingBills: List<UpcomingBill>,
        upcomingIncomes: List<UpcomingBill> = emptyList(),
        goals: List<FinancialGoalEntity>,
        todaySpent: Long = 0L
    ): SpendingLimitCalculation = Companion.calculateDailyLimit(availableBalance, upcomingBills, upcomingIncomes, goals, todaySpent)

    // Financial Projection / Forecast (7, 30, 90 days)
    fun calculateForecast(
        currentBalance: Long,
        recurring: List<RecurringTransactionEntity>,
        recentTransactions: List<TransactionEntity> = emptyList()
    ): ForecastResult = Companion.calculateForecast(currentBalance, recurring, recentTransactions)

    // Deterministic Category Auto-Classifier based on Brazilian Portuguese keywords
    fun suggestCategory(description: String, categories: List<CategoryEntity>): CategoryEntity? =
        Companion.suggestCategory(description, categories)

    companion object {
        // Upcoming Bills Calculation
        fun getUpcomingBills(
            recurring: List<RecurringTransactionEntity>,
            categories: List<CategoryEntity>,
            daysAhead: Int = 30,
            monthTransactions: List<TransactionEntity> = emptyList(),
            accounts: List<AccountEntity> = emptyList()
        ): List<UpcomingBill> {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val result = mutableListOf<UpcomingBill>()

            for (rec in recurring.filter { it.isActive }) {
                val dueDay = if (rec.dueDay in 1..31) rec.dueDay else 1
                val isIncome = rec.type == "INCOME"

                // Check if already paid or received this month
                val isPaidThisMonth = monthTransactions.any { tx ->
                    tx.isPaid && (
                        tx.recurrenceRule == "RECURRING_ID:${rec.id}" ||
                        (tx.accountId == rec.accountId && tx.categoryId == rec.categoryId && tx.type == rec.type && (
                            tx.description.equals(rec.name, ignoreCase = true) ||
                            tx.description.equals("Pagamento: ${rec.name}", ignoreCase = true) ||
                            tx.description.equals("Recebimento: ${rec.name}", ignoreCase = true)
                        ))
                    )
                }

                // Calculate next occurrence timestamp
                val billCal = Calendar.getInstance()
                if (dueDay < currentDayOfMonth) {
                    // Next month
                    billCal.add(Calendar.MONTH, 1)
                    val maxNextMonth = billCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    billCal.set(Calendar.DAY_OF_MONTH, dueDay.coerceAtMost(maxNextMonth))
                } else {
                    // Current month
                    billCal.set(Calendar.DAY_OF_MONTH, dueDay.coerceAtMost(maxDaysInMonth))
                }
                billCal.set(Calendar.HOUR_OF_DAY, 23)
                billCal.set(Calendar.MINUTE, 59)
                billCal.set(Calendar.SECOND, 0)
                billCal.set(Calendar.MILLISECOND, 0)

                val dueDate = billCal.timeInMillis
                val diffDays = ((dueDate - now) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

                if (diffDays <= daysAhead) {
                    val catName = categories.find { it.id == rec.categoryId }?.name ?: if (isIncome) "Renda" else "Contas"
                    result.add(
                        UpcomingBill(
                            id = rec.id,
                            name = rec.name,
                            amount = rec.amount,
                            dueDate = dueDate,
                            categoryName = catName,
                            daysUntilDue = diffDays,
                            isIncome = isIncome,
                            isPaidThisMonth = isPaidThisMonth,
                            recurringId = rec.id
                        )
                    )
                }
            }

            // Also include pending (unpaid) transactions strictly within upcoming horizon (0..daysAhead)
            val pendingTxs = monthTransactions.filter { !it.isPaid && it.type != "TRANSFER" }
            for (tx in pendingTxs) {
                val diffDays = ((tx.date - now) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays in 0..daysAhead) {
                    val isIncome = tx.type == "INCOME"
                    val catName = categories.find { it.id == tx.categoryId }?.name ?: if (isIncome) "Renda" else "Contas"
                    result.add(
                        UpcomingBill(
                            id = 10000000L + tx.id, // Offset to avoid duplicate IDs with recurring
                            name = tx.description.ifBlank { "Lançamento Pendente" },
                            amount = tx.amount,
                            dueDate = tx.date,
                            categoryName = catName,
                            daysUntilDue = diffDays,
                            isIncome = isIncome,
                            isPaidThisMonth = false,
                            recurringId = null
                        )
                    )
                }
            }

            // Include Credit Card Invoices for active credit card accounts
            for (card in accounts.filter { it.isActive && it.type == "CREDIT_CARD" }) {
                val cycleInfo = getCreditCardCycleInfo(card, monthTransactions)
                if (cycleInfo.currentInvoiceAmount > 0L) {
                    val diffDays = ((cycleInfo.nextDueDate - now) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    if (diffDays <= daysAhead) {
                        result.add(
                            UpcomingBill(
                                id = -card.id,
                                name = "Fatura ${card.name}",
                                amount = cycleInfo.currentInvoiceAmount,
                                dueDate = cycleInfo.nextDueDate,
                                categoryName = "Cartão de Crédito",
                                daysUntilDue = diffDays,
                                isIncome = false,
                                isPaidThisMonth = false,
                                recurringId = null,
                                isCreditCardInvoice = true,
                                cardAccountId = card.id
                            )
                        )
                    }
                }
            }

            return result.sortedBy { it.dueDate }
        }

        // Credit Card Billing Cycle Information
        fun getCreditCardCycleInfo(
            card: AccountEntity,
            transactions: List<TransactionEntity>
        ): CreditCardCycleInfo {
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            val maxDaysThisMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val closingDay = card.closingDay.coerceIn(1, 31).coerceAtMost(maxDaysThisMonth)
            val dueDay = card.dueDay.coerceIn(1, 31).coerceAtMost(maxDaysThisMonth)

            // Total debt on this card across all time
            var totalCardExpenses = 0L
            var totalCardPayments = 0L
            for (tx in transactions) {
                if (tx.accountId == card.id && tx.type == "EXPENSE") {
                    totalCardExpenses += tx.amount
                } else if ((tx.targetAccountId == card.id && tx.type == "TRANSFER") || (tx.accountId == card.id && tx.type == "INCOME")) {
                    totalCardPayments += tx.amount
                }
            }
            val totalDebt = (totalCardExpenses - totalCardPayments).coerceAtLeast(0L)
            val availableLimit = if (card.creditLimit > 0L) (card.creditLimit - totalDebt).coerceAtLeast(0L) else 0L

            // Calculate next invoice due date
            val nextDueCal = Calendar.getInstance()
            if (today > dueDay) {
                nextDueCal.add(Calendar.MONTH, 1)
                val maxNextMonth = nextDueCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextDueCal.set(Calendar.DAY_OF_MONTH, card.dueDay.coerceIn(1, 31).coerceAtMost(maxNextMonth))
            } else {
                nextDueCal.set(Calendar.DAY_OF_MONTH, dueDay)
            }
            nextDueCal.set(Calendar.HOUR_OF_DAY, 23)
            nextDueCal.set(Calendar.MINUTE, 59)
            nextDueCal.set(Calendar.SECOND, 0)
            nextDueCal.set(Calendar.MILLISECOND, 0)

            val isClosed = if (closingDay < dueDay) {
                today in (closingDay + 1)..dueDay
            } else {
                today > closingDay || today <= dueDay
            }

            return CreditCardCycleInfo(
                accountId = card.id,
                cardName = card.name,
                creditLimit = card.creditLimit,
                currentInvoiceAmount = totalDebt,
                availableLimit = availableLimit,
                closingDay = card.closingDay,
                dueDay = card.dueDay,
                nextDueDate = nextDueCal.timeInMillis,
                isClosed = isClosed
            )
        }

        // Daily Spending Limit Calculator: "Quanto Posso Gastar?"
        fun calculateDailyLimit(
            availableBalance: Long,
            upcomingBills: List<UpcomingBill>,
            upcomingIncomes: List<UpcomingBill> = emptyList(),
            goals: List<FinancialGoalEntity>,
            todaySpent: Long = 0L
        ): SpendingLimitCalculation {
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysRemaining = (maxDaysInMonth - currentDay + 1).coerceAtLeast(1)

            // Only count pending (not yet paid) obligations this month
            val billsMonthSum = upcomingBills
                .filter { !it.isIncome && !it.isPaidThisMonth && it.daysUntilDue <= daysRemaining }
                .sumOf { it.amount }

            // Scheduled incomes still to arrive this month
            val incomesMonthSum = upcomingIncomes
                .filter { it.isIncome && !it.isPaidThisMonth && it.daysUntilDue <= daysRemaining }
                .sumOf { it.amount }

            // Reserve for goals: calculate based on realistic remaining months until deadline
            val now = System.currentTimeMillis()
            val reservedForGoals = goals.sumOf { goal ->
                val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
                if (remaining == 0L) {
                    0L
                } else if (goal.deadline != null && goal.deadline > now) {
                    val diffDays = (goal.deadline - now) / (1000L * 60 * 60 * 24)
                    val monthsRemaining = ((diffDays / 30) + 1).toInt().coerceIn(1, 120)
                    remaining / monthsRemaining
                } else {
                    remaining / 12 // Fallback padrão: 1 ano para acumular
                }
            }

            val netAvailable = (availableBalance + incomesMonthSum - billsMonthSum - reservedForGoals).coerceAtLeast(0L)
            val dailyLimit = netAvailable / daysRemaining
            val todayRemainingLimit = (dailyLimit - todaySpent).coerceAtLeast(0L)

            val explanation = buildString {
                append("Saldo Disponível: R$ ${String.format(java.util.Locale.US, "%.2f", availableBalance / 100.0)}.")
                if (incomesMonthSum > 0) {
                    append(" (+) R$ ${String.format(java.util.Locale.US, "%.2f", incomesMonthSum / 100.0)} a receber este mês.")
                }
                if (billsMonthSum > 0) {
                    append(" (-) R$ ${String.format(java.util.Locale.US, "%.2f", billsMonthSum / 100.0)} em contas a pagar.")
                }
                if (reservedForGoals > 0) {
                    append(" (-) R$ ${String.format(java.util.Locale.US, "%.2f", reservedForGoals / 100.0)} em reservas de metas.")
                }
                append(" Saldo livre de R$ ${String.format(java.util.Locale.US, "%.2f", netAvailable / 100.0)} dividido por $daysRemaining dias restantes = Limite diário de R$ ${String.format(java.util.Locale.US, "%.2f", dailyLimit / 100.0)}.")
                if (todaySpent > 0) {
                    append(" Você já gastou R$ ${String.format(java.util.Locale.US, "%.2f", todaySpent / 100.0)} hoje, restando R$ ${String.format(java.util.Locale.US, "%.2f", todayRemainingLimit / 100.0)}.")
                }
            }

            return SpendingLimitCalculation(
                currentBalance = availableBalance,
                upcomingBillsMonth = billsMonthSum,
                upcomingIncomesMonth = incomesMonthSum,
                reservedForGoals = reservedForGoals,
                netAvailable = netAvailable,
                daysRemainingInMonth = daysRemaining,
                dailyLimit = dailyLimit,
                todaySpent = todaySpent,
                todayRemainingLimit = todayRemainingLimit,
                formulaExplanation = explanation
            )
        }

        // Financial Projection / Forecast (7, 30, 90 days)
        fun calculateForecast(
            currentBalance: Long,
            recurring: List<RecurringTransactionEntity>,
            recentTransactions: List<TransactionEntity> = emptyList()
        ): ForecastResult {
            val points = mutableListOf<ForecastPoint>()
            val calendar = Calendar.getInstance()
            var runningBalance = currentBalance
            var realisticRunningBalance = currentBalance
            var riskWarningDate: String? = null

            val activeExpenses = recurring.filter { it.isActive && it.type == "EXPENSE" }
            val activeIncomes = recurring.filter { it.isActive && it.type == "INCOME" }

            // Compute average daily variable spending from the last 30 days non-recurring expenses
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
            val recentVariableExpenses = recentTransactions.filter {
                it.type == "EXPENSE" && it.date >= thirtyDaysAgo && !it.isRecurring
            }
            val dailyAvgVariable = if (recentVariableExpenses.isNotEmpty()) {
                recentVariableExpenses.sumOf { it.amount } / 30
            } else {
                0L
            }

            var proj7 = currentBalance
            var proj30 = currentBalance
            var proj90 = currentBalance
            var realisticProj30 = currentBalance
            var realisticProj90 = currentBalance

            for (day in 1..90) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val timestamp = calendar.timeInMillis

                // Add scheduled incomes & expenses with short-month clamp (e.g. 31st in 30-day month triggers on 30th)
                val dayIncome = activeIncomes.filter { it.dueDay.coerceAtMost(maxDaysInMonth) == dayOfMonth }.sumOf { it.amount }
                val dayExpense = activeExpenses.filter { it.dueDay.coerceAtMost(maxDaysInMonth) == dayOfMonth }.sumOf { it.amount }

                runningBalance = runningBalance + dayIncome - dayExpense
                realisticRunningBalance = realisticRunningBalance + dayIncome - dayExpense - dailyAvgVariable

                if (day == 7) proj7 = runningBalance
                if (day == 30) {
                    proj30 = runningBalance
                    realisticProj30 = realisticRunningBalance
                }
                if (day == 90) {
                    proj90 = runningBalance
                    realisticProj90 = realisticRunningBalance
                }

                if (runningBalance < 0 && riskWarningDate == null) {
                    val dayStr = String.format(java.util.Locale.US, "%02d/%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1)
                    riskWarningDate = dayStr
                }

                if (day in listOf(1, 3, 7, 14, 21, 30, 45, 60, 75, 90)) {
                    val label = String.format(java.util.Locale.US, "%02d/%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1)
                    points.add(
                        ForecastPoint(
                            timestamp = timestamp,
                            dayLabel = label,
                            projectedBalance = runningBalance,
                            isNegative = runningBalance < 0,
                            realisticBalance = realisticRunningBalance
                        )
                    )
                }
            }

            return ForecastResult(
                currentBalance = currentBalance,
                projected7Days = proj7,
                projected30Days = proj30,
                projected90Days = proj90,
                points = points,
                riskWarningDate = riskWarningDate,
                dailyAverageExpense = dailyAvgVariable,
                realisticProjected30Days = realisticProj30,
                realisticProjected90Days = realisticProj90
            )
        }

        // Deterministic Category Auto-Classifier based on Brazilian Portuguese keywords
        fun suggestCategory(description: String, categories: List<CategoryEntity>): CategoryEntity? {
            val lower = description.lowercase().trim()
            if (lower.isEmpty()) return null

            val expenseKeywords = mapOf(
                "Alimentação" to listOf("mercado", "supermercado", "padaria", "lanche", "restaurante", "almoco", "almoço", "jantar", "ifood", "comida", "feira", "acougue", "açougue", "cafe", "café", "pao", "pão", "pizza", "burger", "hortifruti"),
                "Transporte" to listOf("uber", "99", "taxi", "gasolina", "combustivel", "combustível", "onibus", "ônibus", "metro", "metrô", "estacionamento", "pedagio", "pedágio", "ipva", "multa", "posto"),
                "Moradia" to listOf("aluguel", "condominio", "condomínio", "iptu", "reforma", "moveis", "móveis", "casa"),
                "Contas" to listOf("luz", "energia", "enel", "cpfl", "cemig", "light", "agua", "água", "sabesp", "copasa", "sanepar", "internet", "wifi", "net", "claro", "vivo", "tim", "telefone", "celular", "assinatura", "netflix", "spotify", "prime"),
                "Saúde" to listOf("farmacia", "farmácia", "drogaria", "remedio", "remédio", "consulta", "medico", "médico", "dentista", "hospital", "exame", "plano de saude", "terapia"),
                "Educação" to listOf("curso", "faculdade", "escola", "livro", "mensalidade", "apostila", "udemy"),
                "Lazer" to listOf("cinema", "show", "viagem", "hotel", "praia", "bar", "cerveja", "festa", "jogos", "steam", "playstation", "xbox"),
                "Compras" to listOf("roupa", "calcado", "calçado", "tenis", "tênis", "shopping", "eletronico", "eletrônico", "amazon", "mercadolivre", "shopee", "shein"),
                "Dívidas" to listOf("emprestimo", "empréstimo", "financiamento", "juros", "parcela", "divida", "dívida", "cartao", "fatura")
            )

            val incomeKeywords = mapOf(
                "Salário" to listOf("salario", "salário", "adiantamento", "holerite", "13", "decimo"),
                "Trabalho" to listOf("diaria", "diária", "freela", "freelance", "bico", "comissao", "comissão", "servico", "serviço"),
                "Renda Extra" to listOf("venda", "reembolso", "rendimento", "dividendo", "juros", "lucro", "cashback", "premio", "prêmio")
            )

            for ((catName, keywords) in expenseKeywords) {
                if (keywords.any { lower.contains(it) }) {
                    val found = categories.find { it.name.equals(catName, ignoreCase = true) && it.type == "EXPENSE" }
                    if (found != null) return found
                }
            }

            for ((catName, keywords) in incomeKeywords) {
                if (keywords.any { lower.contains(it) }) {
                    val found = categories.find { it.name.equals(catName, ignoreCase = true) && it.type == "INCOME" }
                    if (found != null) return found
                }
            }

            return null
        }
    }

    // CRUD Methods
    suspend fun insertTransaction(tx: TransactionEntity): Long {
        return if (database != null) {
            database.withTransaction {
                transactionDao.insertTransaction(tx)
            }
        } else {
            transactionDao.insertTransaction(tx)
        }
    }

    suspend fun insertTransfer(transfer: TransactionEntity): Long {
        require(transfer.type == "TRANSFER") { "Transação deve ser do tipo TRANSFER" }
        require(transfer.targetAccountId != null) { "Conta de destino é obrigatória para transferências" }
        return if (database != null) {
            database.withTransaction {
                transactionDao.insertTransaction(transfer)
            }
        } else {
            transactionDao.insertTransaction(transfer)
        }
    }

    suspend fun updateTransaction(tx: TransactionEntity) {
        if (database != null) {
            database.withTransaction {
                transactionDao.updateTransaction(tx)
            }
        } else {
            transactionDao.updateTransaction(tx)
        }
    }

    suspend fun deleteTransaction(tx: TransactionEntity) {
        if (database != null) {
            database.withTransaction {
                transactionDao.deleteTransaction(tx)
            }
        } else {
            transactionDao.deleteTransaction(tx)
        }
    }

    suspend fun deleteTransactionById(id: Long) {
        if (database != null) {
            database.withTransaction {
                transactionDao.deleteTransactionById(id)
            }
        } else {
            transactionDao.deleteTransactionById(id)
        }
    }

    suspend fun getTransactionsFromGroup(groupId: String, startInstallment: Int) = transactionDao.getTransactionsFromGroup(groupId, startInstallment)
    suspend fun getAllTransactionsFromGroup(groupId: String) = transactionDao.getAllTransactionsFromGroup(groupId)
    suspend fun getTransactionsByRecurrenceRule(rule: String) = transactionDao.getTransactionsByRecurrenceRule(rule)
    suspend fun getRecurringById(id: Long) = recurringDao.getRecurringById(id)


    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    suspend fun insertCategory(cat: CategoryEntity): Long = categoryDao.insertCategory(cat)
    suspend fun updateCategory(cat: CategoryEntity) = categoryDao.updateCategory(cat)
    suspend fun deleteCategory(cat: CategoryEntity) = categoryDao.deleteCategory(cat)

    suspend fun insertRecurring(rec: RecurringTransactionEntity): Long = recurringDao.insertRecurring(rec)
    suspend fun updateRecurring(rec: RecurringTransactionEntity) = recurringDao.updateRecurring(rec)
    suspend fun deleteRecurring(rec: RecurringTransactionEntity) = recurringDao.deleteRecurring(rec)

    suspend fun insertGoal(goal: FinancialGoalEntity): Long = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: FinancialGoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: FinancialGoalEntity) = goalDao.deleteGoal(goal)

    suspend fun insertBudget(budget: BudgetEntity): Long = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)

    suspend fun getSetting(key: String): String? = settingsDao.getSetting(key)
    suspend fun setSetting(key: String, value: String) = settingsDao.setSetting(SettingEntity(key, value))

    suspend fun clearAllData() {
        transactionDao.clearAll()
        accountDao.clearAll()
        recurringDao.clearAll()
        goalDao.clearAll()
        budgetDao.clearAll()
    }
}
