package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class AccountType(val displayName: String) {
    CASH("Dinheiro"),
    BANK("Conta Bancária"),
    WALLET("Carteira"),
    SAVINGS("Poupança"),
    DIGITAL("Conta Digital"),
    CREDIT_CARD("Cartão de Crédito")
}

enum class RecurrenceFrequency(val displayName: String) {
    MONTHLY("Mensal"),
    WEEKLY("Semanal"),
    BIWEEKLY("Quinzenal"),
    ANNUAL("Anual")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER"
    val amount: Long, // Value in centavos (cents)
    val categoryId: Long,
    val accountId: Long,
    val targetAccountId: Long? = null,
    val description: String,
    val date: Long, // Timestamp millis
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val notes: String? = null,
    val groupId: String? = null,
    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null,
    val isPaid: Boolean = true
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // AccountType enum name
    val initialBalance: Long = 0L, // In centavos
    val currentBalance: Long = 0L, // In centavos
    val isActive: Boolean = true,
    val colorHex: String = "#2E7D5C",
    val creditLimit: Long = 0L, // In centavos (for CREDIT_CARD)
    val closingDay: Int = 1, // Closing/best purchase day (1..31)
    val dueDay: Int = 10 // Invoice due day (1..31)
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "INCOME", "EXPENSE"
    val icon: String = "category",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "INCOME", "EXPENSE"
    val amount: Long, // In centavos
    val categoryId: Long,
    val accountId: Long,
    val frequency: String, // "MONTHLY", "WEEKLY", "BIWEEKLY", "ANNUAL"
    val dueDay: Int = 1,
    val nextDate: Long, // Timestamp millis of next due date
    val endDate: Long? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long, // In centavos
    val currentAmount: Long = 0L, // In centavos
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val amount: Long, // In centavos
    val period: String = "MONTHLY"
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
