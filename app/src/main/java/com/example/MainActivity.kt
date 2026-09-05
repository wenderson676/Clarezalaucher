package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.UpcomingBill
import com.example.launcher.AppItem
import com.example.launcher.QuickParser
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.AppContextMenu
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.QuickRegisterDialog
import com.example.ui.drawer.AppDrawerScreen
import com.example.ui.finance.AddAccountDialog
import com.example.ui.finance.AddBudgetDialog
import com.example.ui.finance.AddGoalDialog
import com.example.ui.finance.AddRecurringDialog
import com.example.ui.finance.FinanceDashboardScreen
import com.example.ui.home.ConfigureHomeAppsDialog
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.VidaSimplesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    HOME, DRAWER, SEARCH, FINANCE, SETTINGS, ONBOARDING
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val transactions by viewModel.allTransactions.collectAsState()
            val accounts by viewModel.allAccounts.collectAsState()
            val categories by viewModel.allCategories.collectAsState()
            val recurring by viewModel.allRecurring.collectAsState()
            val goals by viewModel.allGoals.collectAsState()
            val budgets by viewModel.allBudgets.collectAsState()

            var currentScreen by remember(uiState.isOnboardingCompleted) {
                mutableStateOf(if (uiState.isOnboardingCompleted) Screen.HOME else Screen.ONBOARDING)
            }

            // Dialog / Sheet states
            var showQuickRegisterDialog by remember { mutableStateOf(false) }
            var quickRegisterInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
            var quickRegisterInitialAmount by remember { mutableStateOf(0L) }
            var quickRegisterInitialDescription by remember { mutableStateOf("") }
            var quickRegisterInitialCategoryId by remember { mutableStateOf<Long?>(null) }

            var selectedAppForMenu by remember { mutableStateOf<AppItem?>(null) }
            var showUpcomingBillsSheet by remember { mutableStateOf(false) }
            var showConfigureHomeAppsDialog by remember { mutableStateOf(false) }

            var showAddAccountDialog by remember { mutableStateOf(false) }
            var showAddRecurringDialog by remember { mutableStateOf(false) }
            var showAddGoalDialog by remember { mutableStateOf(false) }
            var showAddBudgetDialog by remember { mutableStateOf(false) }

            // Speech recognizer result contract
            val speechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val spokenText = result.data
                        ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        ?.firstOrNull()

                    if (!spokenText.isNullOrBlank()) {
                        val parsed = QuickParser.parse(spokenText, categories)
                        if (parsed != null) {
                            quickRegisterInitialType = parsed.type
                            quickRegisterInitialAmount = parsed.amountCentavos
                            quickRegisterInitialDescription = parsed.description
                            quickRegisterInitialCategoryId = parsed.suggestedCategory?.id
                            showQuickRegisterDialog = true
                        } else {
                            quickRegisterInitialDescription = spokenText
                            showQuickRegisterDialog = true
                        }
                    }
                }
            }

            fun launchVoiceRecognition() {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o valor e descrição (ex: 35 mercado)")
                    }
                    speechLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Reconhecimento de voz indisponível no momento.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Android Back button handling
            BackHandler(enabled = true) {
                when {
                    selectedAppForMenu != null -> selectedAppForMenu = null
                    showConfigureHomeAppsDialog -> showConfigureHomeAppsDialog = false
                    showQuickRegisterDialog -> showQuickRegisterDialog = false
                    showUpcomingBillsSheet -> showUpcomingBillsSheet = false
                    showAddAccountDialog -> showAddAccountDialog = false
                    showAddRecurringDialog -> showAddRecurringDialog = false
                    showAddGoalDialog -> showAddGoalDialog = false
                    showAddBudgetDialog -> showAddBudgetDialog = false
                    currentScreen != Screen.HOME -> currentScreen = Screen.HOME
                    else -> {
                        // On Home: stay on Home (standard launcher behavior)
                    }
                }
            }

            VidaSimplesTheme(themeMode = uiState.themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == Screen.DRAWER || targetState == Screen.FINANCE || targetState == Screen.SETTINGS) {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        },
                        label = "screen_transition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            Screen.ONBOARDING -> {
                                OnboardingScreen(
                                    onFinish = {
                                        viewModel.setOnboardingCompleted()
                                        currentScreen = Screen.HOME
                                    },
                                    onOpenDefaultLauncherSettings = {
                                        viewModel.appManager.openDefaultLauncherSettings()
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            Screen.HOME -> {
                                HomeScreen(
                                    onOpenRegisterIncome = {
                                        quickRegisterInitialType = TransactionType.INCOME
                                        quickRegisterInitialAmount = 0L
                                        quickRegisterInitialDescription = ""
                                        quickRegisterInitialCategoryId = null
                                        showQuickRegisterDialog = true
                                    },
                                    onOpenRegisterExpense = {
                                         quickRegisterInitialType = TransactionType.EXPENSE
                                         quickRegisterInitialAmount = 0L
                                         quickRegisterInitialDescription = ""
                                         quickRegisterInitialCategoryId = null
                                         showQuickRegisterDialog = true
                                    },
                                    availableBalanceCentavos = uiState.totalAvailableBalance,
                                    monthIncomeCentavos = uiState.monthIncomeCentavos,
                                    monthExpenseCentavos = uiState.monthExpenseCentavos,
                                    dailyLimitCentavos = uiState.spendingLimit.dailyLimit,
                                    spendingLimit = uiState.spendingLimit,
                                    upcomingBills = uiState.upcomingBills,
                                    transactions = transactions,
                                    isPrivacyEnabled = uiState.isPrivacyEnabled,
                                    showBattery = uiState.showBattery,
                                    isUltraSimpleMode = uiState.isUltraSimpleMode,
                                    isFocusMode = uiState.isFocusMode,
                                    favoriteApps = uiState.installedApps.filter { it.isFavorite },
                                    categories = categories,
                                    accounts = accounts,
                                    riskWarningDate = uiState.riskWarningDate,
                                    onTogglePrivacy = { viewModel.togglePrivacy() },
                                    onOpenFinance = { currentScreen = Screen.FINANCE },
                                    onOpenUpcomingBills = { showUpcomingBillsSheet = true },
                                    onOpenAppDrawer = { currentScreen = Screen.DRAWER },
                                    onOpenSearch = { currentScreen = Screen.DRAWER },
                                    onOpenSettings = { currentScreen = Screen.SETTINGS },
                                    onConfigureHomeApps = { showConfigureHomeAppsDialog = true },
                                    onLaunchApp = { app ->
                                        viewModel.recordAppLaunch(app.packageName)
                                        viewModel.appManager.launchApp(app)
                                    },
                                    onAppLongClick = { app -> selectedAppForMenu = app },
                                    onOpenFullRegister = {
                                        quickRegisterInitialType = TransactionType.EXPENSE
                                        quickRegisterInitialAmount = 0L
                                        quickRegisterInitialDescription = ""
                                        quickRegisterInitialCategoryId = null
                                        showQuickRegisterDialog = true
                                    },
                                    onVoiceRequest = { launchVoiceRecognition() },
                                    onQuickSave = { type, amountCentavos, categoryId, accountId, description ->
                                        viewModel.addTransaction(
                                            type = type,
                                            amountCentavos = amountCentavos,
                                            categoryId = categoryId,
                                            accountId = accountId,
                                            targetAccountId = null,
                                            description = description,
                                            date = System.currentTimeMillis()
                                        )
                                        Toast.makeText(this@MainActivity, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            Screen.DRAWER -> {
                                AppDrawerScreen(
                                    apps = uiState.installedApps,
                                    frequentlyUsedApps = uiState.frequentlyUsedApps,
                                    isFocusMode = uiState.isFocusMode,
                                    onLaunchApp = { app ->
                                        viewModel.recordAppLaunch(app.packageName)
                                        viewModel.appManager.launchApp(app)
                                    },
                                    onAppLongClick = { app -> selectedAppForMenu = app },
                                    onBack = { currentScreen = Screen.HOME },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            Screen.FINANCE -> {
                                FinanceDashboardScreen(
                                    transactions = transactions,
                                    accounts = accounts,
                                    categories = categories,
                                    recurring = recurring,
                                    goals = goals,
                                    budgets = budgets,
                                    totalAvailableBalance = uiState.totalAvailableBalance,
                                    totalNetWorth = uiState.totalNetWorth,
                                    upcomingBills = uiState.upcomingBills,
                                    spendingLimit = uiState.spendingLimit,
                                    isPrivacyEnabled = uiState.isPrivacyEnabled,
                                    primaryAccountId = uiState.primaryAccountId,
                                    themeMode = uiState.themeMode,
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    onTogglePrivacy = { viewModel.togglePrivacy() },
                                    onClearAllData = { viewModel.clearAllData() },
                                    onOpenLauncherSettings = { currentScreen = Screen.SETTINGS },
                                    onBack = { currentScreen = Screen.HOME },
                                    onOpenNewTransaction = {
                                        quickRegisterInitialType = TransactionType.EXPENSE
                                        quickRegisterInitialAmount = 0L
                                        quickRegisterInitialDescription = ""
                                        quickRegisterInitialCategoryId = null
                                        showQuickRegisterDialog = true
                                    },
                                    onOpenAddAccount = { showAddAccountDialog = true },
                                    onOpenAddRecurring = { showAddRecurringDialog = true },
                                    onOpenAddGoal = { showAddGoalDialog = true },
                                    onOpenAddBudget = { showAddBudgetDialog = true },
                                    onDeleteTransaction = { tx, mode -> viewModel.deleteTransaction(tx, mode) },
                                    onUpdateTransaction = { tx, mode -> viewModel.updateTransaction(tx, mode) },
                                    onUpdateAccount = { acc -> viewModel.updateAccount(acc) },
                                    onDeleteAccount = { acc -> viewModel.deleteAccount(acc) },
                                    onSetPrimaryAccount = { accId -> viewModel.setPrimaryAccount(accId) },
                                    onDeleteRecurring = { rec -> viewModel.deleteRecurring(rec) },
                                    onDeleteGoal = { goal -> viewModel.deleteGoal(goal) },
                                    onUpdateGoal = { goal -> viewModel.updateGoal(goal) },
                                    onDeleteBudget = { b -> viewModel.deleteBudget(b) },
                                    onUpdateBudget = { b -> viewModel.updateBudget(b) },
                                    onToggleTransactionPaid = { tx -> viewModel.toggleTransactionPaid(tx) },
                                    onPayRecurring = { rec -> viewModel.payRecurringBill(rec) },
                                    onTransferToGoal = { goal, accountId, amountCentavos, isDeposit ->
                                        viewModel.transferToGoal(goal, accountId, amountCentavos, isDeposit)
                                        val msg = if (isDeposit) "Depósito na caixinha realizado!" else "Resgate da caixinha realizado!"
                                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    onUpdateGoalProgress = { goal, newAmount ->
                                        viewModel.updateGoalProgress(goal, newAmount)
                                    },
                                    onQuickSave = { type, amountCentavos, categoryId, accountId, description ->
                                        viewModel.addTransaction(
                                            type = type,
                                            amountCentavos = amountCentavos,
                                            categoryId = categoryId,
                                            accountId = accountId,
                                            targetAccountId = null,
                                            description = description,
                                            date = System.currentTimeMillis()
                                        )
                                    },
                                    onTransferSave = { sourceAccountId, targetAccountId, amountCentavos, description ->
                                        val catId = categories.firstOrNull()?.id ?: 1L
                                        viewModel.addTransaction(
                                            type = TransactionType.TRANSFER,
                                            amountCentavos = amountCentavos,
                                            categoryId = catId,
                                            accountId = sourceAccountId,
                                            targetAccountId = targetAccountId,
                                            description = description,
                                            date = System.currentTimeMillis()
                                        )
                                        Toast.makeText(this@MainActivity, "Transferência realizada com sucesso!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            Screen.SETTINGS -> {
                                SettingsScreen(
                                    currentThemeMode = uiState.themeMode,
                                    isPrivacyDefault = uiState.isPrivacyEnabled,
                                    showBattery = uiState.showBattery,
                                    isUltraSimpleMode = uiState.isUltraSimpleMode,
                                    isFocusMode = uiState.isFocusMode,
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    onTogglePrivacyDefault = { viewModel.togglePrivacy() },
                                    onToggleShowBattery = { viewModel.setShowBattery(it) },
                                    onToggleUltraSimpleMode = { viewModel.setUltraSimpleMode(it) },
                                    onToggleFocusMode = { viewModel.setFocusMode(it) },
                                    onConfigureHomeApps = { showConfigureHomeAppsDialog = true },
                                    onOpenDefaultLauncherSettings = { viewModel.appManager.openDefaultLauncherSettings() },
                                    onClearAllData = { viewModel.clearAllData() },
                                    onBack = { currentScreen = Screen.HOME },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            Screen.SEARCH -> {
                                AppDrawerScreen(
                                    apps = uiState.installedApps,
                                    isFocusMode = uiState.isFocusMode,
                                    onLaunchApp = { app -> viewModel.appManager.launchApp(app) },
                                    onAppLongClick = { app -> selectedAppForMenu = app },
                                    onBack = { currentScreen = Screen.HOME },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }

                    // Quick Register Dialog Modal
                    if (showQuickRegisterDialog) {
                        QuickRegisterDialog(
                            categories = categories,
                            accounts = accounts,
                            initialType = quickRegisterInitialType,
                            initialAmountCentavos = quickRegisterInitialAmount,
                            initialDescription = quickRegisterInitialDescription,
                            initialCategoryId = quickRegisterInitialCategoryId,
                            initialAccountId = uiState.primaryAccountId,
                            onSave = { type, amountCentavos, categoryId, accountId, targetAccountId, description, date, repeatCount, repeatFreq, isPaid ->
                                viewModel.addTransaction(
                                    type = type,
                                    amountCentavos = amountCentavos,
                                    categoryId = categoryId,
                                    accountId = accountId,
                                    targetAccountId = targetAccountId,
                                    description = description,
                                    date = date,
                                    repeatCount = repeatCount,
                                    repeatFrequency = repeatFreq,
                                    isPaid = isPaid
                                )
                                Toast.makeText(this@MainActivity, "Movimentação salva!", Toast.LENGTH_SHORT).show()
                            },
                            onSaveRecurring = { name, type, amount, categoryId, accountId, dueDay, frequency ->
                                viewModel.addRecurring(
                                    name = name,
                                    type = type,
                                    amountCentavos = amount,
                                    categoryId = categoryId,
                                    accountId = accountId,
                                    dueDay = dueDay,
                                    frequency = frequency
                                )
                                Toast.makeText(this@MainActivity, "Conta fixa salva com sucesso!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showQuickRegisterDialog = false }
                        )
                    }

                    // App Context Menu Sheet
                    selectedAppForMenu?.let { app ->
                        AppContextMenu(
                            app = app,
                            onOpen = { viewModel.appManager.launchApp(app) },
                            onAppInfo = { viewModel.appManager.openAppInfo(app.packageName) },
                            onToggleFavorite = { viewModel.toggleFavorite(app.packageName) },
                            onToggleHideInFocus = { viewModel.toggleHideInFocus(app.packageName) },
                            onUninstall = { viewModel.appManager.uninstallApp(app.packageName) },
                            onDismiss = { selectedAppForMenu = null }
                        )
                    }

                    // Upcoming Bills Bottom Sheet
                    if (showUpcomingBillsSheet) {
                        UpcomingBillsModalSheet(
                            upcomingBills = uiState.upcomingBills,
                            isPrivacyEnabled = uiState.isPrivacyEnabled,
                            onDismiss = { showUpcomingBillsSheet = false }
                        )
                    }

                    // Add Account Dialog
                    if (showAddAccountDialog) {
                        AddAccountDialog(
                            onSave = { name, type, initialBalance, creditLimit, closingDay, dueDay ->
                                viewModel.addAccount(name, type, initialBalance, creditLimit, closingDay, dueDay)
                                Toast.makeText(this@MainActivity, "Conta adicionada!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showAddAccountDialog = false }
                        )
                    }

                    // Add Recurring Dialog
                    if (showAddRecurringDialog) {
                        AddRecurringDialog(
                            categories = categories,
                            accounts = accounts,
                            onSave = { name, type, amount, categoryId, accountId, dueDay ->
                                viewModel.addRecurring(name, type, amount, categoryId, accountId, dueDay)
                                Toast.makeText(this@MainActivity, "Conta fixa adicionada!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showAddRecurringDialog = false }
                        )
                    }

                    // Add Goal Dialog
                    if (showAddGoalDialog) {
                        AddGoalDialog(
                            onSave = { name, target, initial ->
                                viewModel.addGoal(name, target, initial)
                                Toast.makeText(this@MainActivity, "Meta criada!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showAddGoalDialog = false }
                        )
                    }

                    // Add Budget Dialog
                    if (showAddBudgetDialog) {
                        AddBudgetDialog(
                            categories = categories,
                            onSave = { categoryId, amount ->
                                viewModel.addBudget(categoryId, amount)
                                Toast.makeText(this@MainActivity, "Orçamento definido!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showAddBudgetDialog = false }
                        )
                    }

                    // Configure Home Apps Dialog
                    if (showConfigureHomeAppsDialog) {
                        ConfigureHomeAppsDialog(
                            installedApps = uiState.installedApps,
                            currentFavorites = uiState.favoritePackages,
                            onSaveFavorites = { newFavorites ->
                                viewModel.setFavoritePackages(newFavorites)
                                Toast.makeText(this@MainActivity, "Apps da tela inicial atualizados!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showConfigureHomeAppsDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshApps()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingBillsModalSheet(
    upcomingBills: List<UpcomingBill>,
    isPrivacyEnabled: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Contas Próximas do Vencimento",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (upcomingBills.isEmpty()) {
                Text(
                    text = "Nenhuma conta próxima nos próximos 30 dias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(upcomingBills, key = { it.id }) { bill ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = bill.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${bill.categoryName} • ${dateFormat.format(Date(bill.dueDate))} (${if (bill.daysUntilDue == 0) "Hoje" else "em ${bill.daysUntilDue} dias"})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "− " + CurrencyFormatter.formatCentavos(bill.amount, isPrivacyEnabled),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = FinanceExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

