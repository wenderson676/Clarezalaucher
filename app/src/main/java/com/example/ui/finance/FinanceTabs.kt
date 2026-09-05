package com.example.ui.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.data.repository.FinanceRepository
import com.example.ui.components.CurrencyFormatter
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.FinanceTransferBlue
import com.example.ui.theme.FinanceWarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StatementTabContent(
    transactions: List<TransactionEntity>,
    recurring: List<com.example.data.model.RecurringTransactionEntity> = emptyList(),
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    isPrivacyEnabled: Boolean,
    onDeleteTransaction: (TransactionEntity, String) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit = {},
    onTogglePaidStatus: (TransactionEntity) -> Unit = {},
    onExportCsv: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, TRANSFER
    val dateFormat = remember { SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")) }

    // Dynamically blend active unpaid recurring bills for the current month into displayTransactions
    val displayTransactions = remember(transactions, recurring) {
        val list = transactions.toMutableList()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        // Find active recurring bills that don't have a transaction paid/registered in this month yet
        for (rec in recurring.filter { it.isActive }) {
            val alreadyRegisteredThisMonth = transactions.any { tx ->
                tx.recurrenceRule == "RECURRING_ID:${rec.id}" && {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
                }()
            }
            if (!alreadyRegisteredThisMonth) {
                // Determine due date in current month
                val dueCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, rec.dueDay.coerceIn(1, 28))
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                }
                
                list.add(
                    TransactionEntity(
                        id = -100000L - rec.id, // Negative virtual ID to mark as virtual recurring
                        type = rec.type,
                        amount = rec.amount,
                        categoryId = rec.categoryId,
                        accountId = rec.accountId,
                        targetAccountId = null,
                        description = "🔄 [Conta Fixa] ${rec.name}",
                        date = dueCal.timeInMillis,
                        isPaid = false,
                        isRecurring = true,
                        recurrenceRule = "RECURRING_ID:${rec.id}"
                    )
                )
            }
        }
        
        // Sort all by date descending
        list.sortedByDescending { it.date }
    }

    val filtered = remember(displayTransactions, selectedFilter) {
        when (selectedFilter) {
            "INCOME" -> displayTransactions.filter { it.type == "INCOME" }
            "EXPENSE" -> displayTransactions.filter { it.type == "EXPENSE" }
            "TRANSFER" -> displayTransactions.filter { it.type == "TRANSFER" }
            else -> displayTransactions
        }
    }

    // Group by Date String
    val grouped = remember(filtered) {
        val today = dateFormat.format(Date())
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = dateFormat.format(calYesterday.time)

        filtered.groupBy { tx ->
            val dateStr = dateFormat.format(Date(tx.date))
            when (dateStr) {
                today -> "Hoje"
                yesterday -> "Ontem"
                else -> dateStr
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Action Row with Filter Chips and Export Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val filters = listOf("ALL" to "Todos", "EXPENSE" to "Despesas", "INCOME" to "Receitas", "TRANSFER" to "Transf.")
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Button(
                onClick = onExportCsv,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("CSV", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Nenhuma movimentação encontrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (dateHeader, txList) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(txList, key = { it.id }) { tx ->
                        val cat = categories.find { it.id == tx.categoryId }
                        val acc = accounts.find { it.id == tx.accountId }
                        val targetAcc = if (tx.targetAccountId != null) accounts.find { it.id == tx.targetAccountId } else null

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEditTransaction(tx) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Clickable status check circle
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (tx.isPaid) FinanceIncomeGreen.copy(alpha = 0.12f) else Color.Transparent)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (tx.isPaid) FinanceIncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                            .clickable { onTogglePaidStatus(tx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (tx.isPaid) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Efetivada",
                                                tint = FinanceIncomeGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))

                                    val (icon, tint) = when (tx.type) {
                                        "INCOME" -> Icons.Filled.ArrowUpward to FinanceIncomeGreen
                                        "EXPENSE" -> Icons.Filled.ArrowDownward to FinanceExpenseRed
                                        else -> Icons.Filled.SwapHoriz to FinanceTransferBlue
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tx.description,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val subtitle = when (tx.type) {
                                            "TRANSFER" -> "${acc?.name ?: "Origem"} → ${targetAcc?.name ?: "Destino"}"
                                            else -> "${cat?.name ?: "Geral"} • ${acc?.name ?: "Conta"}"
                                        }
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val prefix = when (tx.type) {
                                        "INCOME" -> "+ "
                                        "EXPENSE" -> "− "
                                        else -> ""
                                    }
                                    val amountColor = when (tx.type) {
                                        "INCOME" -> FinanceIncomeGreen
                                        "EXPENSE" -> FinanceExpenseRed
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = prefix + CurrencyFormatter.formatCentavos(tx.amount, isPrivacyEnabled),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = amountColor
                                        )
                                        if (!tx.isPaid) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Surface(
                                                color = FinanceExpenseRed.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Pendente",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                                    color = FinanceExpenseRed,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { onEditTransaction(tx) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteTransaction(tx, "THIS") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Excluir",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun AccountsTabContent(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    isPrivacyEnabled: Boolean,
    primaryAccountId: Long? = null,
    onAddAccount: () -> Unit,
    onEditAccount: (AccountEntity) -> Unit = {},
    onTransfer: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Minhas Contas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onTransfer,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transferir", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onAddAccount,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nova Conta", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(accounts, key = { it.id }) { acc ->
                // Calculate dynamic balance
                var balance = acc.initialBalance
                for (tx in transactions) {
                    if (tx.type == "INCOME" && tx.accountId == acc.id) balance += tx.amount
                    if (tx.type == "EXPENSE" && tx.accountId == acc.id) balance -= tx.amount
                    if (tx.type == "TRANSFER") {
                        if (tx.accountId == acc.id) balance -= tx.amount
                        if (tx.targetAccountId == acc.id) balance += tx.amount
                    }
                }

                val isPrimary = primaryAccountId == acc.id

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEditAccount(acc) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isPrimary) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "⭐ Principal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = acc.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = CurrencyFormatter.formatCentavos(balance, isPrivacyEnabled),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (balance >= 0) MaterialTheme.colorScheme.onSurface else FinanceExpenseRed
                            )
                            IconButton(
                                onClick = { onEditAccount(acc) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Editar Conta",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun RecurringTabContent(
    recurring: List<RecurringTransactionEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity> = emptyList(),
    isPrivacyEnabled: Boolean,
    onAddRecurring: () -> Unit,
    onDeleteRecurring: (RecurringTransactionEntity) -> Unit,
    onPayRecurring: (RecurringTransactionEntity) -> Unit = {}
) {
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Despesas e Receitas Recorrentes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = onAddRecurring,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (recurring.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Nenhuma conta recorrente cadastrada.\nAdicione suas contas fixas para previsão automática!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recurring, key = { it.id }) { rec ->
                    val cat = categories.find { it.id == rec.categoryId }
                    val isIncome = rec.type == "INCOME"

                    val isPaidThisMonth = transactions.any { tx ->
                        if (tx.type != rec.type) return@any false
                        val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                        if (cal.get(Calendar.MONTH) != currentMonth || cal.get(Calendar.YEAR) != currentYear) return@any false
                        tx.description.contains(rec.name, ignoreCase = true) ||
                        tx.description.startsWith("Pagamento: ${rec.name}", ignoreCase = true) ||
                        tx.description.startsWith("Recebimento: ${rec.name}", ignoreCase = true)
                    }

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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Vence todo dia ${rec.dueDay} • ${cat?.name ?: "Geral"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPaidThisMonth) {
                                    Surface(
                                        color = FinanceIncomeGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isIncome) "✓ Recebido" else "✓ Pago",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = FinanceIncomeGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { onPayRecurring(rec) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isIncome) FinanceIncomeGreen.copy(alpha = 0.2f) else FinanceExpenseRed.copy(alpha = 0.2f),
                                            contentColor = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed
                                        ),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isIncome) "Receber" else "Pagar",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Text(
                                    text = (if (isIncome) "+ " else "− ") + CurrencyFormatter.formatCentavos(rec.amount, isPrivacyEnabled),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed
                                )

                                IconButton(
                                    onClick = { onDeleteRecurring(rec) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Excluir",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun BudgetTabContent(
    budgets: List<BudgetEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    isPrivacyEnabled: Boolean,
    onAddBudget: () -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onEditBudget: (BudgetEntity) -> Unit = {}
) {
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val currentMonthExpenses = remember(transactions) {
        transactions.filter { tx ->
            if (tx.type != "EXPENSE") return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Orçamentos por Categoria",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = onAddBudget,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Definir Limite", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Nenhum orçamento definido.\nDefina limites para Alimentação, Lazer ou Transporte para manter o controle!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(budgets, key = { it.id }) { budget ->
                    val cat = categories.find { it.id == budget.categoryId }
                    val spent = currentMonthExpenses.filter { it.categoryId == budget.categoryId }.sumOf { it.amount }
                    val progress = if (budget.amount > 0) (spent.toFloat() / budget.amount.toFloat()).coerceIn(0f, 1f) else 0f
                    val isExceeded = spent > budget.amount

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEditBudget(budget) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat?.name ?: "Categoria",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
                                    IconButton(
                                        onClick = { onEditBudget(budget) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteBudget(budget) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Excluir",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
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
                                color = if (isExceeded) FinanceExpenseRed else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

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

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun GoalsTabContent(
    goals: List<FinancialGoalEntity>,
    isPrivacyEnabled: Boolean,
    emergencyFundRecommended: Long = 0L,
    onAddGoal: () -> Unit,
    onDeleteGoal: (FinancialGoalEntity) -> Unit,
    onUpdateGoalProgress: (FinancialGoalEntity, Long) -> Unit,
    onEditGoal: (FinancialGoalEntity) -> Unit = {},
    onTransferToGoal: (FinancialGoalEntity) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Metas & Caixinhas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = onAddGoal,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nova Meta", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Emergency Fund Recommendation Banner
        if (emergencyFundRecommended > 0L) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Reserva de Emergência Recomendada",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Objetivo sugerido: ${CurrencyFormatter.formatCentavos(emergencyFundRecommended, isPrivacyEnabled)} (cobre 6 meses de contas fixas e essenciais).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Nenhuma meta criada.\nCrie metas e caixinhas para guardar dinheiro para seus objetivos!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
                    val percent = (progress * 100).toInt()

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onEditGoal(goal) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteGoal(goal) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Excluir",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
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
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${CurrencyFormatter.formatCentavos(goal.currentAmount, isPrivacyEnabled)} ($percent%)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Alvo: ${CurrencyFormatter.formatCentavos(goal.targetAmount, isPrivacyEnabled)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (goal.deadline != null && goal.deadline > System.currentTimeMillis()) {
                                val now = System.currentTimeMillis()
                                val daysRem = (goal.deadline - now) / (1000L * 60 * 60 * 24)
                                val monthsRem = ((daysRem / 30) + 1).coerceAtLeast(1)
                                val amountRem = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
                                val monthlySugg = amountRem / monthsRem

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Prazo: $monthsRem meses • Aporte sugerido: ${CurrencyFormatter.formatCentavos(monthlySugg, isPrivacyEnabled)}/mês",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { onTransferToGoal(goal) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guardar / Resgatar Dinheiro",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun ForecastTabContent(
    currentBalance: Long,
    recurring: List<RecurringTransactionEntity>,
    transactions: List<TransactionEntity> = emptyList(),
    isPrivacyEnabled: Boolean
) {
    var monthOffset by remember { mutableStateOf(0) } // 0 = current month, 1 = next month, etc.

    val currentCal = remember { Calendar.getInstance() }
    val todayYear = currentCal.get(Calendar.YEAR)
    val todayMonth = currentCal.get(Calendar.MONTH)
    val todayDay = currentCal.get(Calendar.DAY_OF_MONTH)

    val viewCal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
    }
    val viewYear = viewCal.get(Calendar.YEAR)
    val viewMonth = viewCal.get(Calendar.MONTH)

    val monthNames = arrayOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
    val monthTitle = "${monthNames.getOrElse(viewMonth) { "Mês" }} $viewYear"

    // Days in month
    val daysInMonth = remember(viewYear, viewMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, viewYear)
            set(Calendar.MONTH, viewMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // First day of week (1 = Sunday, 2 = Monday, ...) -> offset 0 to 6
    val firstDayOfWeekOffset = remember(viewYear, viewMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, viewYear)
            set(Calendar.MONTH, viewMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    var selectedDay by remember(viewMonth, viewYear) {
        mutableStateOf<Int?>(if (monthOffset == 0) todayDay else null)
    }

    val forecast = remember(currentBalance, recurring, transactions) {
        FinanceRepository.calculateForecast(currentBalance, recurring, transactions)
    }

    // Month summary calculations
    val monthRecurringExpenses = remember(recurring) {
        recurring.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val monthRecurringIncome = remember(recurring) {
        recurring.filter { it.type == "INCOME" }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Previsão & Calendário",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visualização por vencimentos e fluxo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Month Navigation Controls
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0x221E3349))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (monthOffset > -1) monthOffset-- },
                            enabled = monthOffset > -1,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChevronLeft,
                                contentDescription = "Mês Anterior",
                                modifier = Modifier.size(18.dp),
                                tint = if (monthOffset > -1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = { if (monthOffset < 12) monthOffset++ },
                            enabled = monthOffset < 12,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Próximo Mês",
                                modifier = Modifier.size(18.dp),
                                tint = if (monthOffset < 12) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        if (forecast.riskWarningDate != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = FinanceWarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = FinanceWarningAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Atenção: Saldo pode ficar negativo por volta de ${forecast.riskWarningDate}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceWarningAmber
                        )
                    }
                }
            }
        }

        // Main Calendar Grid Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x221E3349))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Week Day Headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid (6 rows max)
                    val totalSlots = firstDayOfWeekOffset + daysInMonth
                    val rowsCount = (totalSlots + 6) / 7

                    for (rowIndex in 0 until rowsCount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (colIndex in 0 until 7) {
                                val slotIndex = rowIndex * 7 + colIndex
                                val dayNumber = slotIndex - firstDayOfWeekOffset + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val isToday = (monthOffset == 0 && dayNumber == todayDay)
                                    val isSelected = (selectedDay == dayNumber)

                                    // Check recurring due on this day
                                    val dayRecurring = recurring.filter { it.dueDay == dayNumber }
                                    val dayPendingTransactions = transactions.filter { tx ->
                                        if (tx.isPaid) return@filter false
                                        val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                                        cal.get(Calendar.YEAR) == viewYear &&
                                        cal.get(Calendar.MONTH) == viewMonth &&
                                        cal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                    }
                                    val hasExpenseDue = dayRecurring.any { it.type == "EXPENSE" } || dayPendingTransactions.any { it.type == "EXPENSE" }
                                    val hasIncomeDue = dayRecurring.any { it.type == "INCOME" } || dayPendingTransactions.any { it.type == "INCOME" }

                                    // Check if transactions exist on this day in viewMonth/viewYear
                                    val dayTransactions = transactions.filter { tx ->
                                        val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                                        cal.get(Calendar.YEAR) == viewYear &&
                                        cal.get(Calendar.MONTH) == viewMonth &&
                                        cal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedDay = dayNumber },
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            hasExpenseDue && hasIncomeDue -> Color(0xFF1E2638)
                                            hasExpenseDue -> FinanceExpenseRed.copy(alpha = 0.10f)
                                            hasIncomeDue -> FinanceIncomeGreen.copy(alpha = 0.10f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.5.dp,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                else -> Color(0x1875889C)
                                            }
                                        )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp
                                                ),
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            // Event Indicator Dots
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                if (hasIncomeDue) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(FinanceIncomeGreen)
                                                    )
                                                }
                                                if (hasExpenseDue) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(FinanceExpenseRed)
                                                    )
                                                }
                                                if (dayTransactions.isNotEmpty() && !hasExpenseDue && !hasIncomeDue) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Empty slot
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FinanceExpenseRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Conta a pagar", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FinanceIncomeGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receita prevista", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hoje / Selecionado", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Selected Day Details Card
        selectedDay?.let { sDay ->
            val dayRecurring = recurring.filter { it.dueDay == sDay }
            val dayPendingTransactions = transactions.filter { tx ->
                if (tx.isPaid) return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                cal.get(Calendar.YEAR) == viewYear &&
                cal.get(Calendar.MONTH) == viewMonth &&
                cal.get(Calendar.DAY_OF_MONTH) == sDay
            }
            val dayTransactions = transactions.filter { tx ->
                if (!tx.isPaid) return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                cal.get(Calendar.YEAR) == viewYear &&
                cal.get(Calendar.MONTH) == viewMonth &&
                cal.get(Calendar.DAY_OF_MONTH) == sDay
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dia $sDay de ${monthNames.getOrElse(viewMonth) { "" }}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (monthOffset == 0 && sDay == todayDay) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "HOJE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (dayRecurring.isEmpty() && dayPendingTransactions.isEmpty() && dayTransactions.isEmpty()) {
                            Text(
                                text = "Nenhuma conta ou receita recorrente com vencimento neste dia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Recurring items due on this day
                            dayRecurring.forEach { rec ->
                                val isIncome = rec.type == "INCOME"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = rec.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isIncome) "Receita prevista" else "Despesa recorrente",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = (if (isIncome) "+ " else "− ") + CurrencyFormatter.formatCentavos(rec.amount, isPrivacyEnabled),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed
                                    )
                                }
                            }

                            // Pending transactions (unpaid) due on this day
                            dayPendingTransactions.forEach { tx ->
                                val isIncome = tx.type == "INCOME"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = tx.description.ifBlank { "Sem descrição" },
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isIncome) "Receita prevista" else "Despesa prevista",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = (if (isIncome) "+ " else "− ") + CurrencyFormatter.formatCentavos(tx.amount, isPrivacyEnabled),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed
                                    )
                                }
                            }

                            // Past recorded transactions on this day
                            if (dayTransactions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lançamentos Registrados:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                dayTransactions.take(3).forEach { tx ->
                                    val isExp = tx.type == "EXPENSE"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = tx.description.ifBlank { "Sem descrição" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = (if (isExp) "− " else "+ ") + CurrencyFormatter.formatCentavos(tx.amount, isPrivacyEnabled),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isExp) FinanceExpenseRed else FinanceIncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Month Totals & 30d/90d Telemetry Cards
        item {
            Text(
                text = "Resumo do Mês e Projeções:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val projItems = listOf(
                    "Em 7 dias" to forecast.projected7Days,
                    "Em 30 dias" to forecast.projected30Days,
                    "Em 90 dias" to forecast.projected90Days
                )
                projItems.forEach { (label, value) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.formatCentavos(value, isPrivacyEnabled),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = if (value >= 0) MaterialTheme.colorScheme.onSurface else FinanceExpenseRed
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

