package com.example.ui.finance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.SpendingLimitCalculation
import com.example.data.repository.UpcomingBill
import com.example.ui.components.CurrencyFormatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeoExpenseDark
import com.example.ui.theme.NeoExpenseRed
import com.example.ui.theme.NeoIncomeDark
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.SageGreen
import java.util.Calendar

enum class FinanceBottomNav {
    HOME,
    STATEMENT,
    FORECAST,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    recurring: List<RecurringTransactionEntity>,
    goals: List<FinancialGoalEntity>,
    budgets: List<BudgetEntity>,
    totalAvailableBalance: Long,
    totalNetWorth: Long,
    accountBalances: Map<Long, Long> = emptyMap(),
    upcomingBills: List<UpcomingBill>,
    spendingLimit: SpendingLimitCalculation,
    isPrivacyEnabled: Boolean,
    primaryAccountId: Long? = null,
    themeMode: AppThemeMode = AppThemeMode.DARK,
    onSetThemeMode: (AppThemeMode) -> Unit = {},
    onTogglePrivacy: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    onOpenLauncherSettings: () -> Unit = {},
    onBack: () -> Unit,
    onOpenNewTransaction: () -> Unit,
    onOpenAddAccount: () -> Unit,
    onOpenAddRecurring: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onOpenAddBudget: () -> Unit,
    onDeleteTransaction: (TransactionEntity, String) -> Unit,
    onUpdateTransaction: (TransactionEntity, String) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit = {},
    onDeleteAccount: (AccountEntity) -> Unit = {},
    onSetPrimaryAccount: (Long) -> Unit = {},
    onDeleteRecurring: (RecurringTransactionEntity) -> Unit,
    onDeleteGoal: (FinancialGoalEntity) -> Unit,
    onUpdateGoal: (FinancialGoalEntity) -> Unit = {},
    onDeleteBudget: (BudgetEntity) -> Unit,
    onUpdateBudget: (BudgetEntity) -> Unit = {},
    onTransferToGoal: (goal: FinancialGoalEntity, accountId: Long, amountCentavos: Long, isDeposit: Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateGoalProgress: (FinancialGoalEntity, Long) -> Unit = { _, _ -> },
    onQuickSave: (type: TransactionType, amountCentavos: Long, categoryId: Long, accountId: Long, description: String) -> Unit = { _, _, _, _, _ -> },
    onTransferSave: (sourceAccountId: Long, targetAccountId: Long, amountCentavos: Long, description: String) -> Unit = { _, _, _, _ -> },
    onToggleTransactionPaid: (TransactionEntity) -> Unit = {},
    onPayRecurring: (RecurringTransactionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedNav by remember { mutableStateOf(FinanceBottomNav.HOME) }

    // Dialog states
    var showLimitExplanationDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showExportCsvDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var editingGoal by remember { mutableStateOf<FinancialGoalEntity?>(null) }
    var transferGoalTarget by remember { mutableStateOf<FinancialGoalEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("finance_dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedNav) {
                            FinanceBottomNav.HOME -> "Painel Financeiro"
                            FinanceBottomNav.STATEMENT -> "Extrato de Movimentações"
                            FinanceBottomNav.FORECAST -> "Previsão de Saldo"
                            FinanceBottomNav.SETTINGS -> "Configurações"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar ao Launcher")
                    }
                },
                actions = {
                    // Privacy toggle button
                    IconButton(onClick = onTogglePrivacy) {
                        Icon(
                            imageVector = if (isPrivacyEnabled) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Privacidade",
                            tint = if (isPrivacyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // CSV export action when on statement tab
                    if (selectedNav == FinanceBottomNav.STATEMENT) {
                        IconButton(onClick = { showExportCsvDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Exportar CSV",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().testTag("finance_bottom_nav")
            ) {
                // 1. Tela Inicial (Esquerda)
                NavigationBarItem(
                    selected = selectedNav == FinanceBottomNav.HOME,
                    onClick = { selectedNav = FinanceBottomNav.HOME },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Tela Inicial"
                        )
                    },
                    label = { Text("Início", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                // 2. Extrato (ao lado)
                NavigationBarItem(
                    selected = selectedNav == FinanceBottomNav.STATEMENT,
                    onClick = { selectedNav = FinanceBottomNav.STATEMENT },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = "Extrato"
                        )
                    },
                    label = { Text("Extrato", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                // 3. Botão + para registros (Central destacado)
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenNewTransaction,
                    icon = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Novo Registro",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    label = { Text("Registro", maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                )

                // 4. Previsão (ao lado)
                NavigationBarItem(
                    selected = selectedNav == FinanceBottomNav.FORECAST,
                    onClick = { selectedNav = FinanceBottomNav.FORECAST },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AutoGraph,
                            contentDescription = "Previsão"
                        )
                    },
                    label = { Text("Previsão", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                // 5. Engrenagem de Configurações (ao lado direito)
                NavigationBarItem(
                    selected = selectedNav == FinanceBottomNav.SETTINGS,
                    onClick = { selectedNav = FinanceBottomNav.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configurações"
                        )
                    },
                    label = { Text("Ajustes", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedNav) {
                FinanceBottomNav.HOME -> {
                    FinanceHomeCardsContent(
                        totalAvailableBalance = totalAvailableBalance,
                        totalNetWorth = totalNetWorth,
                        spendingLimit = spendingLimit,
                        transactions = transactions,
                        categories = categories,
                        accounts = accounts,
                        accountBalances = accountBalances,
                        budgets = budgets,
                        goals = goals,
                        recurring = recurring,
                        upcomingBills = upcomingBills,
                        isPrivacyEnabled = isPrivacyEnabled,
                        primaryAccountId = primaryAccountId,
                        onOpenLimitDetails = { showLimitExplanationDialog = true },
                        onOpenAddAccount = onOpenAddAccount,
                        onOpenTransferBetweenAccounts = { showTransferDialog = true },
                        onEditAccount = { editingAccount = it },
                        onOpenAddGoal = onOpenAddGoal,
                        onEditGoal = { editingGoal = it },
                        onTransferToGoalClick = { transferGoalTarget = it },
                        onOpenAddBudget = onOpenAddBudget,
                        onEditBudget = { editingBudget = it },
                        onOpenAddRecurring = onOpenAddRecurring,
                        onDeleteRecurring = onDeleteRecurring,
                        onOpenNewTransaction = onOpenNewTransaction,
                        onNavigateToStatement = { selectedNav = FinanceBottomNav.STATEMENT }
                    )
                }

                FinanceBottomNav.STATEMENT -> {
                    StatementTabContent(
                        transactions = transactions,
                        recurring = recurring,
                        categories = categories,
                        accounts = accounts,
                        isPrivacyEnabled = isPrivacyEnabled,
                        onEditTransaction = { tx -> editingTransaction = tx },
                        onDeleteTransaction = onDeleteTransaction,
                        onTogglePaidStatus = { tx ->
                            if (tx.id < 0) {
                                // Virtual pending recurring bill -> pay/register it!
                                val recId = -tx.id - 100000L
                                val rec = recurring.find { it.id == recId }
                                if (rec != null) {
                                    onPayRecurring(rec)
                                }
                            } else {
                                // Real transaction -> toggle status!
                                onToggleTransactionPaid(tx)
                            }
                        }
                    )
                }

                FinanceBottomNav.FORECAST -> {
                    ForecastTabContent(
                        currentBalance = totalAvailableBalance,
                        recurring = recurring,
                        transactions = transactions,
                        isPrivacyEnabled = isPrivacyEnabled
                    )
                }

                FinanceBottomNav.SETTINGS -> {
                    FinanceSettingsTabContent(
                        themeMode = themeMode,
                        onSetThemeMode = onSetThemeMode,
                        isPrivacyEnabled = isPrivacyEnabled,
                        onTogglePrivacy = onTogglePrivacy,
                        onClearAllData = onClearAllData,
                        onOpenLauncherSettings = onOpenLauncherSettings
                    )
                }
            }
        }
    }

    // Dialog: Transfer Between Accounts
    if (showTransferDialog) {
        TransferBetweenAccountsDialog(
            accounts = accounts,
            onSave = { sourceId, targetId, amount, desc ->
                onTransferSave(sourceId, targetId, amount, desc)
                showTransferDialog = false
            },
            onDismiss = { showTransferDialog = false }
        )
    }

    // Dialog: Export CSV
    if (showExportCsvDialog) {
        val csvText = remember(transactions, categories, accounts) {
            buildCsvString(transactions, categories, accounts)
        }
        ExportCsvDialog(
            csvContent = csvText,
            onCopyCsv = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Extrato CSV", csvText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "CSV copiado com sucesso!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showExportCsvDialog = false }
        )
    }

    // Dialog: "Quanto posso gastar?" Explanation
    if (showLimitExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showLimitExplanationDialog = false },
            title = {
                Text(
                    text = "Como é calculado o limite diário?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Saldo Líquido Atual: ${CurrencyFormatter.formatCentavos(spendingLimit.currentBalance)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Contas Previstas no Mês: − ${CurrencyFormatter.formatCentavos(spendingLimit.upcomingBillsMonth)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (spendingLimit.reservedForGoals > 0) {
                        Text(
                            text = "• Reserva para Metas: − ${CurrencyFormatter.formatCentavos(spendingLimit.reservedForGoals)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "• Disponível Livre: ${CurrencyFormatter.formatCentavos(spendingLimit.netAvailable)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "• Dias restantes no mês: ${spendingLimit.daysRemainingInMonth} dias",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Limite diário seguro: ${CurrencyFormatter.formatCentavos(spendingLimit.dailyLimit)} por dia.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "O cálculo divide o dinheiro livre pelos dias restantes do mês para evitar surpresas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showLimitExplanationDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Dialog: Edit Transaction
    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            categories = categories,
            accounts = accounts,
            onSave = { updatedTx, mode ->
                onUpdateTransaction(updatedTx, mode)
                editingTransaction = null
            },
            onDelete = { txToDelete, mode ->
                onDeleteTransaction(txToDelete, mode)
                editingTransaction = null
            },
            onDismiss = { editingTransaction = null }
        )
    }

    // Dialog: Edit Account
    editingAccount?.let { acc ->
        EditAccountDialog(
            account = acc,
            isPrimary = primaryAccountId == acc.id,
            onSave = { updatedAcc ->
                onUpdateAccount(updatedAcc)
                editingAccount = null
            },
            onSetPrimary = {
                onSetPrimaryAccount(acc.id)
            },
            onDelete = { accToDelete ->
                onDeleteAccount(accToDelete)
                editingAccount = null
            },
            onDismiss = { editingAccount = null }
        )
    }

    // Dialog: Edit Budget
    editingBudget?.let { budget ->
        EditBudgetDialog(
            budget = budget,
            categories = categories,
            onSave = { updatedBudget ->
                onUpdateBudget(updatedBudget)
                editingBudget = null
            },
            onDelete = { budgetToDelete ->
                onDeleteBudget(budgetToDelete)
                editingBudget = null
            },
            onDismiss = { editingBudget = null }
        )
    }

    // Dialog: Edit Goal
    editingGoal?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onSave = { updatedGoal ->
                onUpdateGoal(updatedGoal)
                editingGoal = null
            },
            onDelete = { goalToDelete ->
                onDeleteGoal(goalToDelete)
                editingGoal = null
            },
            onDismiss = { editingGoal = null }
        )
    }

    // Dialog: Transfer to Goal (Caixinha)
    transferGoalTarget?.let { goal ->
        TransferToGoalDialog(
            goal = goal,
            accounts = accounts,
            onTransfer = { targetGoal, accountId, amountCentavos, isDeposit ->
                onTransferToGoal(targetGoal, accountId, amountCentavos, isDeposit)
                transferGoalTarget = null
            },
            onDismiss = { transferGoalTarget = null }
        )
    }
}

/**
 * Modern Card-based Home Screen for Finance:
 * Displays cards for Daily Safe Limit & Balance, Accounts, Goals/Caixinhas, Budgets, Recurring, and Monthly Summary.
 */
@Composable
private fun FinanceHomeCardsContent(
    totalAvailableBalance: Long,
    totalNetWorth: Long,
    spendingLimit: SpendingLimitCalculation,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    accountBalances: Map<Long, Long>,
    budgets: List<BudgetEntity>,
    goals: List<FinancialGoalEntity>,
    recurring: List<RecurringTransactionEntity>,
    upcomingBills: List<UpcomingBill>,
    isPrivacyEnabled: Boolean,
    primaryAccountId: Long?,
    onOpenLimitDetails: () -> Unit,
    onOpenAddAccount: () -> Unit,
    onOpenTransferBetweenAccounts: () -> Unit,
    onEditAccount: (AccountEntity) -> Unit,
    onOpenAddGoal: () -> Unit,
    onEditGoal: (FinancialGoalEntity) -> Unit,
    onTransferToGoalClick: (FinancialGoalEntity) -> Unit,
    onOpenAddBudget: () -> Unit,
    onEditBudget: (BudgetEntity) -> Unit,
    onOpenAddRecurring: () -> Unit,
    onDeleteRecurring: (RecurringTransactionEntity) -> Unit,
    onOpenNewTransaction: () -> Unit,
    onNavigateToStatement: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Current Month Transactions
    val currentMonthTxs = remember(transactions) {
        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }
    }

    val totalIncomeMonth = currentMonthTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpenseMonth = currentMonthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netMonthResult = totalIncomeMonth - totalExpenseMonth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // CARD 1: SALDO & LIMITE DIÁRIO SEGURO (Image 2 Hero Card)
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable { onOpenLimitDetails() }
                .testTag("spending_limit_card"),
            color = Color(0xFF0E1722),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF162534), Color(0xFF0B131C))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SALDO TOTAL DISPONÍVEL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = NeonCyan
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Detalhes",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8DA4B8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Como é calculado",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CurrencyFormatter.formatCentavos(totalAvailableBalance, isPrivacyEnabled),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Safe daily limit badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "💡 Limite seguro: ${CurrencyFormatter.formatCentavos(spendingLimit.dailyLimit, isPrivacyEnabled)} / dia  •  ${spendingLimit.daysRemainingInMonth}d restantes",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Saldo Livre", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8DA4B8))
                            Text(
                                CurrencyFormatter.formatCentavos(spendingLimit.netAvailable, isPrivacyEnabled),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Column {
                            Text("Patrimônio Total", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8DA4B8))
                            Text(
                                CurrencyFormatter.formatCentavos(totalNetWorth, isPrivacyEnabled),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Column {
                            Text("Resultado Mês", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8DA4B8))
                            Text(
                                CurrencyFormatter.formatCentavos(netMonthResult, isPrivacyEnabled),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (netMonthResult >= 0) NeonCyan else NeoExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3 OVERVIEW METRIC CARDS ROW (Image 2 aesthetic)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Contas
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFF101923),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x2E00E5BC))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CONTAS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = Color(0xFF8DA4B8)
                    )
                    Text(
                        text = "${accounts.size} ativas",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Card 2: Metas
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFF101923),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x2E00E5BC))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = SageGreen.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Savings,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "METAS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = Color(0xFF8DA4B8)
                    )
                    Text(
                        text = "${goals.size} caixinhas",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Card 3: Orçamento
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFF101923),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x2E00E5BC))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF64B5F6).copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ORÇAMENTO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = Color(0xFF8DA4B8)
                    )
                    Text(
                        text = "${budgets.size} tetos",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // ==========================================
        // CARD 2: MINHAS CONTAS
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Minhas Contas (${accounts.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onOpenTransferBetweenAccounts, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Transferir entre contas",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onOpenAddAccount, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Nova Conta",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (accounts.isEmpty()) {
                    Text(
                        text = "Nenhuma conta cadastrada. Toque em + para adicionar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    accounts.forEach { acc ->
                        val isPrimary = primaryAccountId == acc.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEditAccount(acc) },
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when (acc.type) {
                                            "CREDIT_CARD" -> Icons.Filled.CreditCard
                                            "INVESTMENT" -> Icons.Filled.Savings
                                            else -> Icons.Filled.AccountBalance
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = acc.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isPrimary) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "★ Principal",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text(
                                            text = when (acc.type) {
                                                "CREDIT_CARD" -> "Cartão de Crédito"
                                                "INVESTMENT" -> "Investimento"
                                                "CASH" -> "Dinheiro Físico"
                                                else -> "Conta Corrente"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyFormatter.formatCentavos(
                                            accountBalances[acc.id] ?: acc.initialBalance,
                                            isPrivacyEnabled
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if ((accountBalances[acc.id] ?: acc.initialBalance) >= 0) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else FinanceExpenseRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Editar Conta",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 3: METAS & CAIXINHAS DE GUARDAR DINHEIRO
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Savings,
                            contentDescription = null,
                            tint = SageGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Metas & Caixinhas (${goals.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onOpenAddGoal, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Nova Meta",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (goals.isEmpty()) {
                    Text(
                        text = "Nenhuma caixinha criada. Crie metas para guardar dinheiro para seus objetivos!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    goals.forEach { goal ->
                        val progress = if (goal.targetAmount > 0) {
                            (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        val percent = (progress * 100).toInt()

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = goal.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$percent%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { onEditGoal(goal) }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Editar Meta",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = SageGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Guardado: ${CurrencyFormatter.formatCentavos(goal.currentAmount, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SageGreen
                                    )
                                    Text(
                                        text = "Alvo: ${CurrencyFormatter.formatCentavos(goal.targetAmount, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Button to deposit or withdraw from caixinha
                                Button(
                                    onClick = { onTransferToGoalClick(goal) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SageGreen.copy(alpha = 0.18f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SwapHoriz,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Guardar / Resgatar Dinheiro",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 4: ORÇAMENTOS POR CATEGORIA
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Orçamentos do Mês (${budgets.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onOpenAddBudget, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Definir Limite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (budgets.isEmpty()) {
                    Text(
                        text = "Nenhum limite configurado. Defina limites de gastos para suas categorias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    budgets.forEach { budget ->
                        val cat = categories.find { it.id == budget.categoryId }
                        val spent = currentMonthTxs
                            .filter { it.type == "EXPENSE" && it.categoryId == budget.categoryId }
                            .sumOf { it.amount }
                        val progress = if (budget.amount > 0) (spent.toFloat() / budget.amount.toFloat()).coerceIn(0f, 1f) else 0f
                        val isExceeded = spent > budget.amount

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEditBudget(budget) },
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat?.name ?: "Categoria",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isExceeded) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🔴 Excedeu",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FinanceExpenseRed
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Editar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Editar Orçamento",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isExceeded) FinanceExpenseRed else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Gasto: ${CurrencyFormatter.formatCentavos(spent, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isExceeded) FinanceExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Limite: ${CurrencyFormatter.formatCentavos(budget.amount, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 5: CONTAS FIXAS & RECORRENTES
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contas Fixas & Recorrentes (${recurring.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onOpenAddRecurring, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Nova Recorrente",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (recurring.isEmpty()) {
                    Text(
                        text = "Nenhuma conta fixa cadastrada. Cadastre aluguel, assinaturas ou salários recorrentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    recurring.forEach { rec ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rec.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Vence todo dia ${rec.dueDay} • ${if (rec.type == "INCOME") "Receita" else "Despesa"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${if (rec.type == "INCOME") "+" else "−"} ${CurrencyFormatter.formatCentavos(rec.amount, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (rec.type == "INCOME") FinanceIncomeGreen else FinanceExpenseRed
                                    )
                                    IconButton(
                                        onClick = { onDeleteRecurring(rec) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Excluir Recorrente",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 6: MAIORES GASTOS DO MÊS
        // ==========================================
        val categoryBreakdown = remember(currentMonthTxs, categories, budgets) {
            val expenseTxs = currentMonthTxs.filter { it.type == "EXPENSE" }
            val totalMonthExp = expenseTxs.sumOf { it.amount }
            val grouped = expenseTxs.groupBy { it.categoryId }

            categories.filter { it.type == "EXPENSE" }.mapNotNull { cat ->
                val spent = grouped[cat.id]?.sumOf { it.amount } ?: 0L
                val catBudget = budgets.find { it.categoryId == cat.id }?.amount
                if (spent == 0L && catBudget == null) return@mapNotNull null

                val pctOfTotal = if (totalMonthExp > 0) spent.toFloat() / totalMonthExp.toFloat() else 0f
                Triple(cat, spent, pctOfTotal)
            }.sortedByDescending { it.second }
        }

        if (categoryBreakdown.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Maiores Gastos por Categoria",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    categoryBreakdown.take(4).forEach { (cat, spent, pctOfTotal) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${CurrencyFormatter.formatCentavos(spent, isPrivacyEnabled)} (${(pctOfTotal * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pctOfTotal.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Settings tab embedded directly into Finance bottom navigation
 */
@Composable
private fun FinanceSettingsTabContent(
    themeMode: AppThemeMode,
    onSetThemeMode: (AppThemeMode) -> Unit,
    isPrivacyEnabled: Boolean,
    onTogglePrivacy: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenLauncherSettings: () -> Unit
) {
    val context = LocalContext.current
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Selection Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tema Visual", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(12.dp))
                listOf(
                    AppThemeMode.DARK to "Tema Escuro (OLED Minimalista)",
                    AppThemeMode.LIGHT to "Tema Claro",
                    AppThemeMode.SYSTEM to "Padrão do Sistema"
                ).forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetThemeMode(mode) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = { onSetThemeMode(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Privacy & Security Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Modo Privacidade", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Oculta valores com ••••••", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isPrivacyEnabled,
                        onCheckedChange = { onTogglePrivacy() },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Launcher Settings Link
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenLauncherSettings() },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Configurações do Launcher", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Apps da tela inicial, launcher padrão e foco", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Clear All Financial Data
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = FinanceExpenseRed.copy(alpha = 0.08f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null, tint = FinanceExpenseRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zona de Perigo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinanceExpenseRed)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apaga todas as movimentações, contas, metas e orçamentos cadastrados no dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showClearDataConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Limpar Todos os Dados")
                }
            }
        }

        // Section: Feedbacks & Sugestões (Abaixo da Zona de Perigo)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Feedback & Sugestões",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sua opinião é fundamental para aprimorar o app. Envie suas críticas, elogios, dúvidas ou ideias de novas funções diretamente pelo WhatsApp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val phone = "5531983470840"
                            val msg = "Olá Wenderson! Estou usando o Clareza Launcher e gostaria de enviar um feedback / sugestão:"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(msg)}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp não encontrado. Telefone: (31) 98347-0840", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("WhatsApp Feedback", "31983470840")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Número (31) 98347-0840 copiado!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copiar número",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Section: Apoio & Doações (Abaixo da Zona de Perigo)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF4D6D),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apoie o Projeto (Doações)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ajude a manter o Clareza atualizado e com novas melhorias. Qualquer contribuição é uma ajuda voluntária, pois o aplicativo tende a se manter sempre gratuito e sem anúncios para todos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chave Pix (E-mail):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "gwenderson400@gmail.com",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Chave Pix", "gwenderson400@gmail.com")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Chave Pix copiada: gwenderson400@gmail.com", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copiar chave Pix",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Chave Pix", "gwenderson400@gmail.com")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Chave Pix (gwenderson400@gmail.com) copiada com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Chave Pix")
                }
            }
        }

        // Section: Sobre o Criador & Direitos Autorais
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "WG",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Criado por Wenderson Gomes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Clareza Launcher • Versão 2.4",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Desenvolvido com carinho para simplificar sua rotina e transformar o controle financeiro em um hábito prático e seguro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "© 2026 Clareza Launcher • Desenvolvido por Wenderson Gomes. Este aplicativo possui direitos autorais reservados.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            title = { Text("Zerar Banco de Dados?") },
            text = { Text("Esta ação é irreversível. Todas as transações, contas e metas serão apagadas permanentemente.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataConfirmDialog = false
                        Toast.makeText(context, "Dados resetados com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Apagar Tudo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun buildCsvString(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>
): String {
    val sb = StringBuilder("Data,Tipo,Descrição,Categoria,Conta,Valor (R$)\n")
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    for (tx in transactions) {
        val catName = categories.find { it.id == tx.categoryId }?.name ?: "Geral"
        val accName = accounts.find { it.id == tx.accountId }?.name ?: "Conta"
        val typeLabel = when (tx.type) {
            "INCOME" -> "RECEITA"
            "EXPENSE" -> "DESPESA"
            else -> "TRANSFERENCIA"
        }
        val amountFormatted = String.format(java.util.Locale.US, "%.2f", tx.amount / 100.0)
        val signedAmount = if (tx.type == "EXPENSE") "-$amountFormatted" else amountFormatted
        val dateStr = sdf.format(java.util.Date(tx.date))
        val descEscaped = tx.description.replace("\"", "'")
        sb.append("\"$dateStr\",\"$typeLabel\",\"$descEscaped\",\"$catName\",\"$accName\",$signedAmount\n")
    }
    return sb.toString()
}
