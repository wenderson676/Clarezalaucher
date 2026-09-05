package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.data.repository.ForecastResult
import com.example.data.repository.SpendingLimitCalculation
import com.example.data.repository.UpcomingBill
import com.example.launcher.AppItem
import com.example.launcher.AppManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val isLoading: Boolean = true,
    val totalAvailableBalance: Long = 0L,
    val totalNetWorth: Long = 0L,
    val monthIncomeCentavos: Long = 0L,
    val monthExpenseCentavos: Long = 0L,
    val todaySpentCentavos: Long = 0L,
    val savingsRatePercentage: Float = 0f,
    val emergencyFundRecommended: Long = 0L,
    val upcomingBills: List<UpcomingBill> = emptyList(),
    val spendingLimit: SpendingLimitCalculation = SpendingLimitCalculation(0, 0, 0, 0, 0, 1, 0, 0, 0, ""),
    val riskWarningDate: String? = null,
    val isPrivacyEnabled: Boolean = false,
    val showBattery: Boolean = true,
    val isUltraSimpleMode: Boolean = false,
    val isFocusMode: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val installedApps: List<AppItem> = emptyList(),
    val favoritePackages: Set<String> = setOf(
        "com.google.android.dialer",
        "com.android.dialer",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.google.android.GoogleCamera",
        "com.android.camera2"
    ),
    val hiddenPackages: Set<String> = emptySet(),
    val appUsageCounts: Map<String, Int> = emptyMap(),
    val frequentlyUsedApps: List<AppItem> = emptyList(),
    val primaryAccountId: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val financeRepository = FinanceRepository(
        transactionDao = database.transactionDao(),
        accountDao = database.accountDao(),
        categoryDao = database.categoryDao(),
        recurringDao = database.recurringDao(),
        goalDao = database.goalDao(),
        budgetDao = database.budgetDao(),
        settingsDao = database.settingsDao()
    )

    val appManager = AppManager(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val allTransactions = financeRepository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allAccounts = financeRepository.allAccounts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allCategories = financeRepository.allCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allRecurring = financeRepository.allRecurring.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allGoals = financeRepository.allGoals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allBudgets = financeRepository.allBudgets.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        loadSettings()
        refreshApps()
        observeFinancials()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val privacy = financeRepository.getSetting("privacy_enabled")?.toBooleanStrictOrNull() ?: false
            val battery = financeRepository.getSetting("show_battery")?.toBooleanStrictOrNull() ?: true
            val ultraSimple = financeRepository.getSetting("ultra_simple")?.toBooleanStrictOrNull() ?: false
            val focus = financeRepository.getSetting("focus_mode")?.toBooleanStrictOrNull() ?: false
            val onboarding = financeRepository.getSetting("onboarding_done")?.toBooleanStrictOrNull() ?: false
            val themeStr = financeRepository.getSetting("theme_mode") ?: AppThemeMode.DARK.name
            val theme = try { AppThemeMode.valueOf(themeStr) } catch (e: Exception) { AppThemeMode.DARK }

            val favsStr = financeRepository.getSetting("favorite_packages") ?: ""
            val favs = if (favsStr.isNotEmpty()) favsStr.split(",").toSet() else _uiState.value.favoritePackages

            val hiddenStr = financeRepository.getSetting("hidden_packages") ?: ""
            val hidden = if (hiddenStr.isNotEmpty()) hiddenStr.split(",").toSet() else emptySet()

            val primaryAcc = financeRepository.getSetting("primary_account_id")?.toLongOrNull()

            val usageStr = financeRepository.getSetting("app_usage_counts") ?: ""
            val usageMap = mutableMapOf<String, Int>()
            if (usageStr.isNotEmpty()) {
                usageStr.split(";").forEach { pair ->
                    val parts = pair.split(":")
                    if (parts.size == 2) {
                        parts[1].toIntOrNull()?.let { count ->
                            usageMap[parts[0]] = count
                        }
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                isPrivacyEnabled = privacy,
                showBattery = battery,
                isUltraSimpleMode = ultraSimple,
                isFocusMode = focus,
                isOnboardingCompleted = onboarding,
                themeMode = theme,
                favoritePackages = favs,
                hiddenPackages = hidden,
                appUsageCounts = usageMap,
                primaryAccountId = primaryAcc,
                isLoading = false
            )
            refreshApps()
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            val apps = appManager.getInstalledApps(
                favoritePackages = _uiState.value.favoritePackages,
                hiddenPackages = _uiState.value.hiddenPackages
            )
            val usageMap = _uiState.value.appUsageCounts
            val frequent = if (usageMap.isNotEmpty()) {
                apps.filter { usageMap.containsKey(it.packageName) }
                    .sortedByDescending { usageMap[it.packageName] ?: 0 }
                    .take(8)
            } else {
                apps.filter { it.isFavorite }.take(6)
            }

            _uiState.value = _uiState.value.copy(
                installedApps = apps,
                frequentlyUsedApps = if (frequent.isNotEmpty()) frequent else apps.take(6)
            )
        }
    }

    fun recordAppLaunch(packageName: String) {
        val currentCounts = _uiState.value.appUsageCounts.toMutableMap()
        currentCounts[packageName] = (currentCounts[packageName] ?: 0) + 1
        _uiState.value = _uiState.value.copy(appUsageCounts = currentCounts)
        viewModelScope.launch(Dispatchers.IO) {
            val serialized = currentCounts.entries.joinToString(";") { "${it.key}:${it.value}" }
            financeRepository.setSetting("app_usage_counts", serialized)
            refreshApps()
        }
    }

    private fun observeFinancials() {
        viewModelScope.launch {
            combine(
                allTransactions,
                allAccounts,
                allCategories,
                allRecurring,
                allGoals
            ) { txs, accs, cats, recs, goals ->
                val available = financeRepository.getTotalAvailableBalance(accs, txs)
                val netWorth = financeRepository.getTotalNetWorth(accs, txs)

                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val startOfMonth = cal.timeInMillis
                val monthTxs = txs.filter { it.date >= startOfMonth }
                val monthIncome = monthTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val monthExpense = monthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                val todayCal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val startOfToday = todayCal.timeInMillis
                val todaySpent = txs.filter { it.date >= startOfToday && it.type == "EXPENSE" }.sumOf { it.amount }

                val allUpcoming = financeRepository.getUpcomingBills(recs, cats, 30, txs, accs)
                val upcomingBills = allUpcoming.filter { !it.isIncome }
                val upcomingIncomes = allUpcoming.filter { it.isIncome }

                val spending = financeRepository.calculateDailyLimit(
                    availableBalance = available,
                    upcomingBills = upcomingBills,
                    upcomingIncomes = upcomingIncomes,
                    goals = goals,
                    todaySpent = todaySpent
                )

                val forecast = financeRepository.calculateForecast(
                    currentBalance = available,
                    recurring = recs,
                    recentTransactions = txs
                )

                val savingsRate = if (monthIncome > 0) {
                    (((monthIncome - monthExpense).toFloat() / monthIncome.toFloat()) * 100f).coerceIn(-100f, 100f)
                } else 0f

                val emergencyFund = (monthExpense * 6).coerceAtLeast(3000_00L) // Min 3000 BRL or 6x monthly expenses

                _uiState.value = _uiState.value.copy(
                    totalAvailableBalance = available,
                    totalNetWorth = netWorth,
                    monthIncomeCentavos = monthIncome,
                    monthExpenseCentavos = monthExpense,
                    todaySpentCentavos = todaySpent,
                    savingsRatePercentage = savingsRate,
                    emergencyFundRecommended = emergencyFund,
                    upcomingBills = upcomingBills,
                    spendingLimit = spending,
                    riskWarningDate = forecast.riskWarningDate
                )
            }.collect {}
        }
    }

    // Toggle Actions
    fun togglePrivacy() {
        val next = !_uiState.value.isPrivacyEnabled
        _uiState.value = _uiState.value.copy(isPrivacyEnabled = next)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("privacy_enabled", next.toString())
        }
    }

    fun setShowBattery(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBattery = show)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("show_battery", show.toString())
        }
    }

    fun setUltraSimpleMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isUltraSimpleMode = enabled)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("ultra_simple", enabled.toString())
        }
    }

    fun setFocusMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isFocusMode = enabled)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("focus_mode", enabled.toString())
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("theme_mode", mode.name)
        }
    }

    fun setOnboardingCompleted() {
        _uiState.value = _uiState.value.copy(isOnboardingCompleted = true)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("onboarding_done", "true")
        }
    }

    fun setFavoritePackages(packages: Set<String>) {
        _uiState.value = _uiState.value.copy(favoritePackages = packages)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("favorite_packages", packages.joinToString(","))
            refreshApps()
        }
    }

    fun toggleFavorite(packageName: String) {
        val currentFavs = _uiState.value.favoritePackages.toMutableSet()
        if (currentFavs.contains(packageName)) {
            currentFavs.remove(packageName)
        } else {
            currentFavs.add(packageName)
        }
        _uiState.value = _uiState.value.copy(favoritePackages = currentFavs)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("favorite_packages", currentFavs.joinToString(","))
            refreshApps()
        }
    }

    fun toggleHideInFocus(packageName: String) {
        val currentHidden = _uiState.value.hiddenPackages.toMutableSet()
        if (currentHidden.contains(packageName)) {
            currentHidden.remove(packageName)
        } else {
            currentHidden.add(packageName)
        }
        _uiState.value = _uiState.value.copy(hiddenPackages = currentHidden)
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.setSetting("hidden_packages", currentHidden.joinToString(","))
            refreshApps()
        }
    }

    // Finance Operations
    fun addTransaction(
        type: TransactionType,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        targetAccountId: Long?,
        description: String,
        date: Long,
        repeatCount: Int = 1,
        repeatFrequency: String = "MONTHLY",
        isPaid: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val groupId = if (repeatCount > 1) java.util.UUID.randomUUID().toString() else null
            var currentDate = date
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = date
            
            for (i in 1..repeatCount) {
                val isFuture = currentDate > System.currentTimeMillis()
                val finalIsPaid = if (isFuture) false else isPaid
                
                financeRepository.insertTransaction(
                    TransactionEntity(
                        type = type.name,
                        amount = amountCentavos,
                        categoryId = categoryId,
                        accountId = accountId,
                        targetAccountId = targetAccountId,
                        description = if (repeatCount > 1) "$description ($i/$repeatCount)" else description,
                        date = currentDate,
                        groupId = groupId,
                        installmentNumber = i,
                        totalInstallments = repeatCount,
                        isPaid = finalIsPaid
                    )
                )
                if (repeatFrequency == "MONTHLY") {
                    calendar.add(java.util.Calendar.MONTH, 1)
                } else if (repeatFrequency == "WEEKLY") {
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                } else if (repeatFrequency == "BIWEEKLY") {
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 14)
                } else if (repeatFrequency == "DAILY") {
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                } else if (repeatFrequency == "YEARLY") {
                    calendar.add(java.util.Calendar.YEAR, 1)
                }
                currentDate = calendar.timeInMillis
            }
        }
    }

    fun updateTransaction(tx: TransactionEntity, mode: String = "THIS") {
        viewModelScope.launch(Dispatchers.IO) {
            val isRecurringBill = tx.recurrenceRule?.startsWith("RECURRING_ID:") == true
            val recurringId = if (isRecurringBill) tx.recurrenceRule?.removePrefix("RECURRING_ID:")?.toLongOrNull() else null

            if (mode == "THIS" || (tx.groupId == null && !isRecurringBill)) {
                financeRepository.updateTransaction(tx)
            } else if (mode.startsWith("COUNT:")) {
                val count = mode.removePrefix("COUNT:").toIntOrNull() ?: 1
                if (tx.groupId != null) {
                    val groupTxs = financeRepository.getTransactionsFromGroup(tx.groupId, tx.installmentNumber ?: 1).take(count)
                    for (txInGroup in groupTxs) {
                        financeRepository.updateTransaction(txInGroup.copy(
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            targetAccountId = tx.targetAccountId,
                            description = tx.description,
                            type = tx.type
                        ))
                    }
                } else if (isRecurringBill && tx.recurrenceRule != null) {
                    val recTxs = financeRepository.getTransactionsByRecurrenceRule(tx.recurrenceRule).filter { it.date >= tx.date }.take(count)
                    for (txInRec in recTxs) {
                        financeRepository.updateTransaction(txInRec.copy(
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            targetAccountId = tx.targetAccountId,
                            description = tx.description,
                            type = tx.type
                        ))
                    }
                }
            } else if (mode == "FUTURE") {
                if (tx.groupId != null) {
                    val groupTxs = financeRepository.getTransactionsFromGroup(tx.groupId, tx.installmentNumber ?: 1)
                    for (txInGroup in groupTxs) { 
                        financeRepository.updateTransaction(txInGroup.copy(
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            targetAccountId = tx.targetAccountId,
                            description = tx.description,
                            type = tx.type
                        ))
                    }
                } else if (isRecurringBill && tx.recurrenceRule != null) {
                    val recTxs = financeRepository.getTransactionsByRecurrenceRule(tx.recurrenceRule).filter { it.date >= tx.date }
                    for (txInRec in recTxs) {
                        financeRepository.updateTransaction(txInRec.copy(
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            targetAccountId = tx.targetAccountId,
                            description = tx.description,
                            type = tx.type
                        ))
                    }
                    if (recurringId != null) {
                        financeRepository.getRecurringById(recurringId)?.let { rec ->
                            financeRepository.updateRecurring(rec.copy(
                                name = tx.description.removePrefix("Pagamento: ").removePrefix("Recebimento: ").trim(),
                                amount = tx.amount,
                                categoryId = tx.categoryId,
                                accountId = tx.accountId
                            ))
                        }
                    }
                }
            } else if (mode == "ALL") {
                if (tx.groupId != null) {
                    val groupTxs = financeRepository.getAllTransactionsFromGroup(tx.groupId)
                    for (txInGroup in groupTxs) { 
                        financeRepository.updateTransaction(txInGroup.copy(
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            targetAccountId = tx.targetAccountId,
                            description = tx.description,
                            type = tx.type
                        ))
                    }
                } else if (isRecurringBill && tx.recurrenceRule != null) {
                    val recTxs = financeRepository.getTransactionsByRecurrenceRule(tx.recurrenceRule)
                    for (txInRec in recTxs) {
                        financeRepository.updateTransaction(txInRec.copy(
                            amount = tx.amount,
                            categoryId = tx.categoryId,
                            accountId = tx.accountId,
                            targetAccountId = tx.targetAccountId,
                            description = tx.description,
                            type = tx.type
                        ))
                    }
                    if (recurringId != null) {
                        financeRepository.getRecurringById(recurringId)?.let { rec ->
                            financeRepository.updateRecurring(rec.copy(
                                name = tx.description.removePrefix("Pagamento: ").removePrefix("Recebimento: ").trim(),
                                amount = tx.amount,
                                categoryId = tx.categoryId,
                                accountId = tx.accountId
                            ))
                        }
                    }
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity, mode: String = "THIS") {
        viewModelScope.launch(Dispatchers.IO) {
            val isRecurringBill = tx.recurrenceRule?.startsWith("RECURRING_ID:") == true
            val recurringId = if (isRecurringBill) tx.recurrenceRule?.removePrefix("RECURRING_ID:")?.toLongOrNull() else null

            if (mode == "THIS" || (tx.groupId == null && !isRecurringBill)) {
                financeRepository.deleteTransaction(tx)
            } else if (mode.startsWith("COUNT:")) {
                val count = mode.removePrefix("COUNT:").toIntOrNull() ?: 1
                if (tx.groupId != null) {
                    val groupTxs = financeRepository.getTransactionsFromGroup(tx.groupId, tx.installmentNumber ?: 1).take(count)
                    for (txInGroup in groupTxs) { financeRepository.deleteTransaction(txInGroup) }
                } else if (isRecurringBill && tx.recurrenceRule != null) {
                    val recTxs = financeRepository.getTransactionsByRecurrenceRule(tx.recurrenceRule).filter { it.date >= tx.date }.take(count)
                    for (txInRec in recTxs) { financeRepository.deleteTransaction(txInRec) }
                }
            } else if (mode == "FUTURE") {
                if (tx.groupId != null) {
                    val groupTxs = financeRepository.getTransactionsFromGroup(tx.groupId, tx.installmentNumber ?: 1)
                    for (txInGroup in groupTxs) { financeRepository.deleteTransaction(txInGroup) }
                } else if (isRecurringBill && tx.recurrenceRule != null) {
                    val recTxs = financeRepository.getTransactionsByRecurrenceRule(tx.recurrenceRule).filter { it.date >= tx.date }
                    for (txInRec in recTxs) { financeRepository.deleteTransaction(txInRec) }
                }
            } else if (mode == "ALL") {
                if (tx.groupId != null) {
                    val groupTxs = financeRepository.getAllTransactionsFromGroup(tx.groupId)
                    for (txInGroup in groupTxs) { financeRepository.deleteTransaction(txInGroup) }
                } else if (isRecurringBill && tx.recurrenceRule != null) {
                    val recTxs = financeRepository.getTransactionsByRecurrenceRule(tx.recurrenceRule)
                    for (txInRec in recTxs) { financeRepository.deleteTransaction(txInRec) }
                    if (recurringId != null) {
                        financeRepository.getRecurringById(recurringId)?.let { rec ->
                            financeRepository.deleteRecurring(rec)
                        }
                    }
                }
            }
        }
    }

    fun addAccount(
        name: String,
        type: String,
        initialBalanceCentavos: Long,
        creditLimit: Long = 0L,
        closingDay: Int = 1,
        dueDay: Int = 10
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.insertAccount(
                AccountEntity(
                    name = name,
                    type = type,
                    initialBalance = initialBalanceCentavos,
                    currentBalance = initialBalanceCentavos,
                    isActive = true,
                    creditLimit = creditLimit,
                    closingDay = closingDay,
                    dueDay = dueDay
                )
            )
        }
    }

    fun payRecurringBill(rec: RecurringTransactionEntity, accountId: Long = rec.accountId, paymentDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch(Dispatchers.IO) {
            val isIncome = rec.type == "INCOME"
            val txType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
            val prefix = if (isIncome) "Recebimento:" else "Pagamento:"
            val description = "$prefix ${rec.name}"

            financeRepository.insertTransaction(
                TransactionEntity(
                    type = txType.name,
                    amount = rec.amount,
                    categoryId = rec.categoryId,
                    accountId = accountId,
                    targetAccountId = null,
                    description = description,
                    date = paymentDate,
                    isRecurring = true,
                    isPaid = true,
                    recurrenceRule = "RECURRING_ID:${rec.id}"
                )
            )
        }
    }

    fun toggleTransactionPaid(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.insertTransaction(tx.copy(isPaid = !tx.isPaid))
        }
    }

    fun payCreditCardInvoice(cardId: Long, sourceAccountId: Long, amountCentavos: Long, cardName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val debtsCatId = allCategories.value.find { it.name == "Dívidas" || it.name == "Contas" }?.id ?: 1L
            financeRepository.insertTransaction(
                TransactionEntity(
                    type = "TRANSFER",
                    amount = amountCentavos,
                    categoryId = debtsCatId,
                    accountId = sourceAccountId,
                    targetAccountId = cardId,
                    description = "Pagamento Fatura: $cardName",
                    date = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.updateAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.deleteAccount(account)
            if (_uiState.value.primaryAccountId == account.id) {
                setPrimaryAccount(0L)
            }
        }
    }

    fun setPrimaryAccount(accountId: Long) {
        _uiState.value = _uiState.value.copy(primaryAccountId = if (accountId > 0) accountId else null)
        viewModelScope.launch(Dispatchers.IO) {
            if (accountId > 0) {
                financeRepository.setSetting("primary_account_id", accountId.toString())
            } else {
                financeRepository.setSetting("primary_account_id", "")
            }
        }
    }

    fun addRecurring(
        name: String,
        type: String,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        dueDay: Int,
        frequency: String = "MONTHLY"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.insertRecurring(
                RecurringTransactionEntity(
                    name = name,
                    type = type,
                    amount = amountCentavos,
                    categoryId = categoryId,
                    accountId = accountId,
                    frequency = frequency,
                    dueDay = dueDay,
                    nextDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteRecurring(rec: RecurringTransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.deleteRecurring(rec)
        }
    }

    fun addGoal(name: String, targetAmountCentavos: Long, initialAmountCentavos: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.insertGoal(
                FinancialGoalEntity(
                    name = name,
                    targetAmount = targetAmountCentavos,
                    currentAmount = initialAmountCentavos
                )
            )
        }
    }

    fun updateGoal(goal: FinancialGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.updateGoal(goal)
        }
    }

    fun updateGoalProgress(goal: FinancialGoalEntity, newAmountCentavos: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.updateGoal(goal.copy(currentAmount = newAmountCentavos))
        }
    }

    fun transferToGoal(goal: FinancialGoalEntity, accountId: Long, amountCentavos: Long, isDeposit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAmount = if (isDeposit) {
                goal.currentAmount + amountCentavos
            } else {
                (goal.currentAmount - amountCentavos).coerceAtLeast(0L)
            }
            financeRepository.updateGoal(goal.copy(currentAmount = newAmount))

            // Create corresponding transaction so account balance reflects the deposit or withdrawal
            val txType = if (isDeposit) TransactionType.EXPENSE else TransactionType.INCOME
            val description = if (isDeposit) "Guardado na caixinha: ${goal.name}" else "Resgate da caixinha: ${goal.name}"
            val defaultCategory = allCategories.value.find {
                if (isDeposit) it.type == "EXPENSE" else it.type == "INCOME"
            }?.id ?: 1L

            financeRepository.insertTransaction(
                TransactionEntity(
                    type = txType.name,
                    amount = amountCentavos,
                    categoryId = defaultCategory,
                    accountId = accountId,
                    targetAccountId = null,
                    description = description,
                    date = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteGoal(goal: FinancialGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.deleteGoal(goal)
        }
    }

    fun addBudget(categoryId: Long, amountCentavos: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.insertBudget(
                BudgetEntity(
                    categoryId = categoryId,
                    amount = amountCentavos
                )
            )
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.deleteBudget(budget)
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            financeRepository.clearAllData()
        }
    }
}
