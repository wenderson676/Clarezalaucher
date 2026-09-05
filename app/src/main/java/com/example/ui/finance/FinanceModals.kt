package com.example.ui.finance
import com.example.ui.components.CurrencyAmountVisualTransformation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.foundation.border
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CurrencyFormatter
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.FinanceTransferBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountDialog(
    onSave: (name: String, type: String, initialBalanceCentavos: Long, creditLimit: Long, closingDay: Int, dueDay: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var initialBalanceInput by remember { mutableStateOf("") }
    var creditLimitInput by remember { mutableStateOf("") }
    var closingDayInput by remember { mutableStateOf("1") }
    var dueDayInput by remember { mutableStateOf("10") }

    val initialCentavos = remember(initialBalanceInput) {
        CurrencyFormatter.parseToCentavos(initialBalanceInput)
    }
    val creditLimitCentavos = remember(creditLimitInput) {
        CurrencyFormatter.parseToCentavos(creditLimitInput)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Nova Conta",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta (ex: Nubank, Carteira)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Tipo de Conta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AccountType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedType == AccountType.CREDIT_CARD) {
                    OutlinedTextField(
                        value = creditLimitInput,
                        onValueChange = { creditLimitInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                        label = { Text("Limite Total do Cartão (R$)") },
                        placeholder = { Text("2000,00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = closingDayInput,
                            onValueChange = { closingDayInput = it },
                            label = { Text("Dia Fechamento") },
                            placeholder = { Text("1 a 31") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = dueDayInput,
                            onValueChange = { dueDayInput = it },
                            label = { Text("Dia Vencimento") },
                            placeholder = { Text("1 a 31") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = initialBalanceInput,
                        onValueChange = { initialBalanceInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                        label = { Text("Saldo Inicial (R$)") },
                        placeholder = { Text("0,00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val cDay = closingDayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1
                            val dDay = dueDayInput.toIntOrNull()?.coerceIn(1, 31) ?: 10
                            onSave(name.trim(), selectedType.name, initialCentavos, creditLimitCentavos, cDay, dDay)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Conta")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRecurringDialog(
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    onSave: (name: String, type: String, amountCentavos: Long, categoryId: Long, accountId: Long, dueDay: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var amountInput by remember { mutableStateOf("") }
    var dueDayInput by remember { mutableStateOf("5") }

    val filteredCategories = remember(isExpense, categories) {
        val t = if (isExpense) "EXPENSE" else "INCOME"
        categories.filter { it.type == t }
    }
    var selectedCategoryId by remember(filteredCategories) {
        mutableLongStateOf(filteredCategories.firstOrNull()?.id ?: 1L)
    }
    var selectedAccountId by remember(accounts) {
        mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L)
    }

    val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val dueDay = dueDayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Nova Conta Fixa / Recorrente",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isExpense) FinanceExpenseRed else Color.Transparent)
                            .clickable { isExpense = true }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Despesa Fixa", style = MaterialTheme.typography.labelMedium, color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isExpense) FinanceIncomeGreen else Color.Transparent)
                            .clickable { isExpense = false }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Receita Fixa", style = MaterialTheme.typography.labelMedium, color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex: Aluguel, Netflix, Internet)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                        label = { Text("Valor (R$)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDayInput,
                        onValueChange = { dueDayInput = it },
                        label = { Text("Dia Venc.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Categoria", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredCategories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(cat.name, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && amountCentavos > 0) {
                            onSave(name.trim(), if (isExpense) "EXPENSE" else "INCOME", amountCentavos, selectedCategoryId, selectedAccountId, dueDay)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank() && amountCentavos > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Recorrência")
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onSave: (name: String, targetAmountCentavos: Long, initialAmountCentavos: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var initialInput by remember { mutableStateOf("") }

    val targetCentavos = remember(targetInput) { targetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val initialCentavos = remember(initialInput) { CurrencyFormatter.parseToCentavos(initialInput) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Nova Meta Financeira",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta (ex: Reserva de Emergência)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Valor Alvo (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = initialInput,
                    onValueChange = { initialInput = it },
                    label = { Text("Já guardado (R$) - Opcional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && targetCentavos > 0) {
                            onSave(name.trim(), targetCentavos, initialCentavos)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank() && targetCentavos > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Meta")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBudgetDialog(
    categories: List<CategoryEntity>,
    onSave: (categoryId: Long, amountCentavos: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val expenseCategories = remember(categories) { categories.filter { it.type == "EXPENSE" } }
    var selectedCategoryId by remember(expenseCategories) {
        mutableLongStateOf(expenseCategories.firstOrNull()?.id ?: 1L)
    }
    var amountInput by remember { mutableStateOf("") }
    val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Definir Limite de Orçamento",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Categoria", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    expenseCategories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(cat.name, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Limite Mensal Máximo (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (amountCentavos > 0) {
                            onSave(selectedCategoryId, amountCentavos)
                            onDismiss()
                        }
                    },
                    enabled = amountCentavos > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Orçamento")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransferBetweenAccountsDialog(
    accounts: List<AccountEntity>,
    onSave: (sourceAccountId: Long, targetAccountId: Long, amountCentavos: Long, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var sourceAccountId by remember(accounts) { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var targetAccountId by remember(accounts) {
        val target = accounts.find { it.id != sourceAccountId } ?: accounts.firstOrNull()
        mutableLongStateOf(target?.id ?: 2L)
    }
    var amountInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("Transferência entre contas") }

    val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val isValid = amountCentavos > 0 && sourceAccountId != targetAccountId && sourceAccountId != 0L && targetAccountId != 0L

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Transferência entre Contas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Conta de Origem (Sai Dinheiro)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = sourceAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FinanceExpenseRed else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    sourceAccountId = acc.id
                                    if (targetAccountId == acc.id) {
                                        targetAccountId = accounts.find { it.id != acc.id }?.id ?: acc.id
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(acc.name, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Conta de Destino (Entra Dinheiro)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = targetAccountId == acc.id
                        val isDisabled = sourceAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) FinanceIncomeGreen
                                    else if (isDisabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable(enabled = !isDisabled) { targetAccountId = acc.id }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                acc.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else if (isDisabled) Color.Gray else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Valor a Transferir (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Motivo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isValid) {
                            onSave(sourceAccountId, targetAccountId, amountCentavos, description.trim())
                            onDismiss()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Transferência")
                }
            }
        }
    }
}

@Composable
fun ExportCsvDialog(
    csvContent: String,
    onCopyCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Exportar Extrato (CSV)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Copie o texto abaixo para colar em planilhas como Excel ou Google Sheets:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Text(
                        text = csvContent,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onCopyCsv()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copiar CSV")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    onSave: (TransactionEntity, String) -> Unit, // Add String for edit mode
    onDelete: (TransactionEntity, String) -> Unit, // Add String for delete mode
    onDismiss: () -> Unit
) {
    var typeStr by remember { mutableStateOf(transaction.type) }
    var description by remember { mutableStateOf(transaction.description) }
    var amountInput by remember {
        val dec = transaction.amount / 100.0
        mutableStateOf(if (transaction.amount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }
    var selectedCategoryId by remember { mutableLongStateOf(transaction.categoryId) }
    var selectedAccountId by remember { mutableLongStateOf(transaction.accountId) }
    var selectedTargetAccountId by remember { mutableLongStateOf(transaction.targetAccountId ?: 0L) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isRepeatOrRecurring = transaction.groupId != null || transaction.isRecurring || !transaction.recurrenceRule.isNullOrEmpty()
    var updateMode by remember { mutableStateOf("THIS") } // THIS, ALL, SOME
    var someCount by remember { mutableIntStateOf(2) }
    var showScopePromptDialog by remember { mutableStateOf(false) }
    var isDeleteAction by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(transaction.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isPaid by remember { mutableStateOf(transaction.isPaid) }


    val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val filteredCategories = remember(typeStr, categories) {
        if (typeStr == "TRANSFER") emptyList()
        else categories.filter { it.type == typeStr }
    }

    val editThemeColor = when (typeStr) {
        "EXPENSE" -> FinanceExpenseRed
        "INCOME" -> FinanceIncomeGreen
        else -> FinanceTransferBlue
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Editar Lançamento",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    val types = listOf("EXPENSE" to "Despesa", "INCOME" to "Receita", "TRANSFER" to "Transferência")
                    types.forEach { (tKey, tLabel) ->
                        val isSelected = typeStr == tKey
                        val bgColor = when {
                            !isSelected -> Color.Transparent
                            tKey == "EXPENSE" -> FinanceExpenseRed
                            tKey == "INCOME" -> FinanceIncomeGreen
                            else -> FinanceTransferBlue
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgColor)
                                .clickable {
                                    typeStr = tKey
                                    if (tKey != "TRANSFER" && filteredCategories.none { it.id == selectedCategoryId }) {
                                        selectedCategoryId = categories.find { it.type == tKey }?.id ?: 1L
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = editThemeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        cursorColor = editThemeColor,
                        focusedLabelColor = editThemeColor
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Valor (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = editThemeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        cursorColor = editThemeColor,
                        focusedLabelColor = editThemeColor
                    )
                )

                if (typeStr != "TRANSFER") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Categoria", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredCategories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) editThemeColor.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) editThemeColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) editThemeColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(if (typeStr == "TRANSFER") "Conta de Origem" else "Conta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) editThemeColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) editThemeColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) editThemeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (typeStr == "TRANSFER") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Conta de Destino", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = selectedTargetAccountId == acc.id
                            val isDisabled = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) editThemeColor.copy(alpha = 0.15f)
                                        else if (isDisabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) editThemeColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = !isDisabled) { selectedTargetAccountId = acc.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) editThemeColor else if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Data do Lançamento", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
                    val selectedDateStr = remember(selectedDateMillis) { dateFormatter.format(Date(selectedDateMillis)) }

                    val isToday = remember(selectedDateMillis) {
                        val calSelected = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val calToday = java.util.Calendar.getInstance()
                        calSelected.get(java.util.Calendar.YEAR) == calToday.get(java.util.Calendar.YEAR) &&
                        calSelected.get(java.util.Calendar.DAY_OF_YEAR) == calToday.get(java.util.Calendar.DAY_OF_YEAR)
                    }

                    val isYesterday = remember(selectedDateMillis) {
                        val calSelected = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val calYesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
                        calSelected.get(java.util.Calendar.YEAR) == calYesterday.get(java.util.Calendar.YEAR) &&
                        calSelected.get(java.util.Calendar.DAY_OF_YEAR) == calYesterday.get(java.util.Calendar.DAY_OF_YEAR)
                    }

                    val themeColor = when (typeStr) {
                        "INCOME" -> FinanceIncomeGreen
                        "EXPENSE" -> FinanceExpenseRed
                        else -> FinanceTransferBlue
                    }

                    // Hoje Button
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isToday) themeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isToday) themeColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedDateMillis = System.currentTimeMillis() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hoje",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal),
                            color = if (isToday) themeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Ontem Button
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isYesterday) themeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isYesterday) themeColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedDateMillis = System.currentTimeMillis() - 24 * 60 * 60 * 1000L }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ontem",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Normal),
                            color = if (isYesterday) themeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Calendar Picker Button
                    val isOtherDate = !isToday && !isYesterday
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isOtherDate) themeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isOtherDate) themeColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isOtherDate) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedDateStr,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isOtherDate) FontWeight.Bold else FontWeight.Normal),
                                color = if (isOtherDate) themeColor else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Status Selector (Efetivada vs Pendente)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Status do Lançamento",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val isFutureDate = selectedDateMillis > System.currentTimeMillis()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = listOf(
                        true to "Efetivada",
                        false to "Pendente"
                    )
                    statuses.forEach { (statusValue, label) ->
                        val isSelected = (isPaid == statusValue)
                        val isEnabled = true

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = isEnabled) { isPaid = statusValue },
                            color = if (isSelected) {
                                if (statusValue) FinanceIncomeGreen.copy(alpha = 0.15f) else FinanceExpenseRed.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) {
                                    if (statusValue) FinanceIncomeGreen else FinanceExpenseRed
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) {
                                        if (statusValue) FinanceIncomeGreen else FinanceExpenseRed
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    }
                                )
                            }
                        }
                    }
                }
                if (isFutureDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ℹ️ Por padrão, lançamentos futuros são sugeridos como pendentes, mas você pode marcar como efetivada se desejar.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isRepeatOrRecurring) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = editThemeColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, editThemeColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                tint = editThemeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lançamento Repetido / Conta Fixa\nAo salvar ou excluir, você escolherá o alcance das alterações.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isRepeatOrRecurring) {
                                isDeleteAction = true
                                showScopePromptDialog = true
                            } else {
                                onDelete(transaction, "THIS")
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excluir", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (description.isNotBlank() && amountCentavos > 0) {
                                if (isRepeatOrRecurring) {
                                    isDeleteAction = false
                                    showScopePromptDialog = true
                                } else {
                                    val updated = transaction.copy(
                                        type = typeStr,
                                        description = description.trim(),
                                        amount = amountCentavos,
                                        categoryId = selectedCategoryId,
                                        accountId = selectedAccountId,
                                        targetAccountId = if (typeStr == "TRANSFER") selectedTargetAccountId else null,
                                        date = selectedDateMillis,
                                        isPaid = isPaid,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    onSave(updated, "THIS")
                                    onDismiss()
                                }
                            }
                        },
                        enabled = description.isNotBlank() && amountCentavos > 0,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                            if (it > System.currentTimeMillis() && !transaction.isPaid) {
                                isPaid = false
                            }
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showScopePromptDialog) {
            val effectiveMode = when (updateMode) {
                "THIS" -> "THIS"
                "ALL" -> "ALL"
                "SOME" -> "COUNT:$someCount"
                else -> "THIS"
            }
            AlertDialog(
                onDismissRequest = { showScopePromptDialog = false },
                title = {
                    Text(
                        text = if (isDeleteAction) "Excluir Lançamento Repetido" else "Alterar Lançamento Repetido",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isDeleteAction)
                                "Este lançamento faz parte de uma repetição ou conta fixa. A exclusão será somente para o registro selecionado, para todos ou somente alguns?"
                            else
                                "Este lançamento faz parte de uma repetição ou conta fixa. A alteração será somente para o registro selecionado, para todos ou somente alguns?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val options = listOf(
                            "THIS" to Pair("Somente para o registro selecionado", "Aplica apenas para este lançamento pontual."),
                            "ALL" to Pair("Para todos", "Aplica para todos os registros da série/recorrência."),
                            "SOME" to Pair("Somente alguns", "Permite selecionar quantos registros serão alterados.")
                        )

                        options.forEach { (modeVal, texts) ->
                            val isSelected = updateMode == modeVal
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { updateMode = modeVal },
                                color = if (isSelected) editThemeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) editThemeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { updateMode = modeVal },
                                            colors = RadioButtonDefaults.colors(selectedColor = editThemeColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = texts.first,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = texts.second,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 36.dp)
                                    )

                                    if (modeVal == "SOME" && isSelected) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .padding(start = 36.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Quantos alterar?",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable(enabled = someCount > 2) { someCount-- },
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "−",
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (someCount > 2) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = someCount.toString(),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = editThemeColor,
                                                modifier = Modifier.widthIn(min = 20.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            Surface(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { someCount++ },
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "+",
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "💡 Serão alterados $someCount lançamentos (este selecionado e os próximos ${someCount - 1}).",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = editThemeColor,
                                            modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showScopePromptDialog = false
                            if (isDeleteAction) {
                                onDelete(transaction, effectiveMode)
                                onDismiss()
                            } else {
                                val updated = transaction.copy(
                                    type = typeStr,
                                    description = description.trim(),
                                    amount = amountCentavos,
                                    categoryId = selectedCategoryId,
                                    accountId = selectedAccountId,
                                    targetAccountId = if (typeStr == "TRANSFER") selectedTargetAccountId else null,
                                    date = selectedDateMillis,
                                    isPaid = isPaid,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updated, effectiveMode)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeleteAction) FinanceExpenseRed else editThemeColor
                        )
                    ) {
                        Text(if (isDeleteAction) "Confirmar Exclusão" else "Confirmar e Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScopePromptDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditAccountDialog(
    account: AccountEntity,
    isPrimary: Boolean,
    onSave: (AccountEntity) -> Unit,
    onSetPrimary: () -> Unit,
    onDelete: (AccountEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var selectedType by remember {
        mutableStateOf(
            try { AccountType.valueOf(account.type) } catch (e: Exception) { AccountType.BANK }
        )
    }
    var initialBalanceInput by remember {
        val dec = account.initialBalance / 100.0
        mutableStateOf(if (account.initialBalance > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }
    var creditLimitInput by remember {
        val dec = account.creditLimit / 100.0
        mutableStateOf(if (account.creditLimit > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }
    var closingDayInput by remember { mutableStateOf(account.closingDay.toString()) }
    var dueDayInput by remember { mutableStateOf(account.dueDay.toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val initialCentavos = remember(initialBalanceInput) { CurrencyFormatter.parseToCentavos(initialBalanceInput) }
    val creditLimitCentavos = remember(creditLimitInput) { creditLimitInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Editar Conta",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Tipo de Conta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AccountType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedType == AccountType.CREDIT_CARD) {
                    OutlinedTextField(
                        value = creditLimitInput,
                        onValueChange = { creditLimitInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                        label = { Text("Limite Total do Cartão (R$)") },
                        placeholder = { Text("2000,00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = closingDayInput,
                            onValueChange = { closingDayInput = it },
                            label = { Text("Dia Fechamento") },
                            placeholder = { Text("1 a 31") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = dueDayInput,
                            onValueChange = { dueDayInput = it },
                            label = { Text("Dia Vencimento") },
                            placeholder = { Text("1 a 31") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = initialBalanceInput,
                        onValueChange = { initialBalanceInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                        label = { Text("Saldo Inicial (R$)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Account Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { if (!isPrimary) onSetPrimary() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPrimary) "Conta Principal (Padrão para lançamentos)" else "Definir como Conta Principal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excluir", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val cDay = closingDayInput.toIntOrNull()?.coerceIn(1, 31) ?: account.closingDay
                                val dDay = dueDayInput.toIntOrNull()?.coerceIn(1, 31) ?: account.dueDay
                                onSave(
                                    account.copy(
                                        name = name.trim(),
                                        type = selectedType.name,
                                        initialBalance = initialCentavos,
                                        creditLimit = creditLimitCentavos,
                                        closingDay = cDay,
                                        dueDay = dDay
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Conta?") },
            text = { Text("Tem certeza que deseja excluir a conta '${account.name}'? Os lançamentos associados permanecerão no extrato.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(account)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditBudgetDialog(
    budget: BudgetEntity,
    categories: List<CategoryEntity>,
    onSave: (BudgetEntity) -> Unit,
    onDelete: (BudgetEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val currentCategory = remember(budget, categories) {
        categories.find { it.id == budget.categoryId }
    }
    var amountInput by remember {
        val dec = budget.amount / 100.0
        mutableStateOf(if (budget.amount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }
    val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Editar Orçamento",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Categoria: ${currentCategory?.name ?: "Categoria Desconhecida"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Limite Mensal Máximo (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excluir", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (amountCentavos > 0) {
                                onSave(budget.copy(amount = amountCentavos))
                                onDismiss()
                            }
                        },
                        enabled = amountCentavos > 0,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Orçamento?") },
            text = { Text("Deseja remover o limite de orçamento para esta categoria?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(budget)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EditGoalDialog(
    goal: FinancialGoalEntity,
    onSave: (FinancialGoalEntity) -> Unit,
    onDelete: (FinancialGoalEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(goal.name) }
    var targetInput by remember {
        val dec = goal.targetAmount / 100.0
        mutableStateOf(if (goal.targetAmount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }
    var currentInput by remember {
        val dec = goal.currentAmount / 100.0
        mutableStateOf(if (goal.currentAmount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val targetCentavos = remember(targetInput) { targetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val currentCentavos = remember(currentInput) { currentInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Editar Meta / Caixinha",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Valor Alvo (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { currentInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text("Valor Atual Guardado (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excluir", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && targetCentavos > 0) {
                                onSave(
                                    goal.copy(
                                        name = name.trim(),
                                        targetAmount = targetCentavos,
                                        currentAmount = currentCentavos
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank() && targetCentavos > 0,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Meta?") },
            text = { Text("Tem certeza que deseja excluir a meta '${goal.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(goal)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransferToGoalDialog(
    goal: FinancialGoalEntity,
    accounts: List<AccountEntity>,
    onTransfer: (goal: FinancialGoalEntity, accountId: Long, amountCentavos: Long, isDeposit: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var isDeposit by remember { mutableStateOf(true) } // true = Guardar (Depósito), false = Resgatar (Retirada)
    var selectedAccountId by remember(accounts) {
        mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L)
    }
    var amountInput by remember { mutableStateOf("") }
    val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }

    val isValid = amountCentavos > 0 && selectedAccountId != 0L && (!isDeposit || amountCentavos <= 1_000_000_000L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isDeposit) "Guardar na Caixinha" else "Resgatar da Caixinha",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Meta: ${goal.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Saldo da caixinha: ${CurrencyFormatter.formatCentavos(goal.currentAmount)} de ${CurrencyFormatter.formatCentavos(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Guardar vs Resgatar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDeposit) FinanceIncomeGreen else Color.Transparent)
                            .clickable { isDeposit = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Guardar Dinheiro",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDeposit) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isDeposit) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { isDeposit = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Resgatar Saldo",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (!isDeposit) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isDeposit) "Conta de Origem (Sai o dinheiro)" else "Conta de Destino (Recebe o dinheiro)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    label = { Text(if (isDeposit) "Quanto deseja guardar? (R$)" else "Quanto deseja resgatar? (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isDeposit && amountCentavos > goal.currentAmount) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Valor maior do que o saldo atual da caixinha (${CurrencyFormatter.formatCentavos(goal.currentAmount)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceExpenseRed
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isValid && (isDeposit || amountCentavos <= goal.currentAmount)) {
                            onTransfer(goal, selectedAccountId, amountCentavos, isDeposit)
                            onDismiss()
                        }
                    },
                    enabled = isValid && (isDeposit || amountCentavos <= goal.currentAmount),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeposit) FinanceIncomeGreen else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isDeposit) "Confirmar e Guardar" else "Confirmar Resgate")
                }
            }
        }
    }
}



