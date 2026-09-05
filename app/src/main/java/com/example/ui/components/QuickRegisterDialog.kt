package com.example.ui.components
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api


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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.FinanceTransferBlue

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuickRegisterDialog(
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    initialType: TransactionType = TransactionType.EXPENSE,
    initialAmountCentavos: Long = 0L,
    initialDescription: String = "",
    initialCategoryId: Long? = null,
    initialAccountId: Long? = null,
    onSave: (
        type: TransactionType,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        targetAccountId: Long?,
        description: String,
        date: Long,
        repeatCount: Int,
        repeatFrequency: String,
        isPaid: Boolean
    ) -> Unit,
    onSaveRecurring: ((
        name: String,
        type: String,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        dueDay: Int,
        frequency: String
    ) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var rawAmountInput by remember {
        mutableStateOf(if (initialAmountCentavos > 0) CurrencyFormatter.formatPlainNumber(initialAmountCentavos) else "")
    }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isPaid by remember { mutableStateOf(true) }
    
    var isRepeating by remember { mutableStateOf(false) }
    var isRecurringFixedMode by remember { mutableStateOf(false) }
    var repeatCount by remember { androidx.compose.runtime.mutableIntStateOf(2) }
    var repeatFrequency by remember { mutableStateOf("MONTHLY") }

    
    val filteredCategories = remember(selectedType, categories) {
        val typeStr = when (selectedType) {
            TransactionType.INCOME -> "INCOME"
            TransactionType.EXPENSE -> "EXPENSE"
            TransactionType.TRANSFER -> "EXPENSE"
        }
        categories.filter { it.type == typeStr }
    }

    var selectedCategoryId by remember(filteredCategories) {
        mutableLongStateOf(
            initialCategoryId ?: filteredCategories.firstOrNull()?.id ?: 1L
        )
    }

    var selectedAccountId by remember(accounts) {
        mutableLongStateOf(
            initialAccountId ?: accounts.firstOrNull()?.id ?: 1L
        )
    }

    var selectedTargetAccountId by remember(accounts) {
        mutableLongStateOf(
            accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 1L
        )
    }

    val amountCentavos = remember(rawAmountInput) {
        CurrencyFormatter.parseToCentavos(rawAmountInput)
    }


    val themeColor = when (selectedType) {
        TransactionType.INCOME -> FinanceIncomeGreen
        TransactionType.EXPENSE -> FinanceExpenseRed
        TransactionType.TRANSFER -> FinanceTransferBlue
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, themeColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .testTag("quick_register_dialog"),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova Movimentação",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.btn_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val types = listOf(
                        TransactionType.EXPENSE to "Despesa",
                        TransactionType.INCOME to "Receita",
                        TransactionType.TRANSFER to "Transf."
                    )
                    types.forEach { (type, label) ->
                        val isSelected = selectedType == type
                        val activeColor = when (type) {
                            TransactionType.EXPENSE -> FinanceExpenseRed
                            TransactionType.INCOME -> FinanceIncomeGreen
                            TransactionType.TRANSFER -> FinanceTransferBlue
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeColor else Color.Transparent)
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field (Large, centered)
                Text(
                    text = stringResource(R.string.amount_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = rawAmountInput,
                    onValueChange = { rawAmountInput = it.filter { char -> char.isDigit() } }, visualTransformation = CurrencyAmountVisualTransformation(),
                    placeholder = { Text("0,00", style = MaterialTheme.typography.headlineMedium) },
                    prefix = {
                        Text("R$ ", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold))
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        cursorColor = themeColor
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = stringResource(R.string.description_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Ex: Supermercado, Aluguel...", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        cursorColor = themeColor
                    )
                )

                if (selectedType != TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.category_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredCategories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) themeColor.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) themeColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selection
                Text(
                    text = if (selectedType == TransactionType.TRANSFER) "Conta de Origem" else stringResource(R.string.account_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) themeColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) themeColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // If transfer, show target account selection
                if (selectedType == TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.target_account_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                            val isSelected = selectedTargetAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) themeColor.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) themeColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedTargetAccountId = acc.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Selection Section
                Text(
                    text = "DATA DO REGISTRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

                    // Hoje Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isToday) themeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isToday) themeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedDateMillis = System.currentTimeMillis() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hoje",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isToday) themeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Ontem Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isYesterday) themeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isYesterday) themeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedDateMillis = System.currentTimeMillis() - 24 * 60 * 60 * 1000L }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ontem",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isYesterday) themeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Calendar Picker Button
                    val isOtherDate = !isToday && !isYesterday
                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isOtherDate) themeColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isOtherDate) themeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isOtherDate) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedDateStr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isOtherDate) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isOtherDate) themeColor else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Repetir Lançamento Section (Collapsible & Compact)
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Repetir este lançamento",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Contas fixas, parcelamentos ou recorrentes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = isRepeating,
                                onCheckedChange = { isRepeating = it },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = themeColor,
                                    checkedTrackColor = themeColor.copy(alpha = 0.3f)
                                )
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(visible = isRepeating) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Frequency Selection Label
                                Text(
                                    text = "Frequência de Repetição",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Frequency Segmented Choice Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val frequencies = listOf(
                                        "WEEKLY" to "Semanal",
                                        "BIWEEKLY" to "Quinzenal",
                                        "MONTHLY" to "Mensal"
                                    )
                                    frequencies.forEach { (freqValue, label) ->
                                        val isFreqSelected = repeatFrequency == freqValue
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { repeatFrequency = freqValue },
                                            color = if (isFreqSelected) {
                                                themeColor.copy(alpha = 0.15f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            },
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = if (isFreqSelected) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isFreqSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (selectedType != TransactionType.TRANSFER) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Tipo de Recorrência",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val modes = listOf(
                                            false to "Lançamentos Parcelados",
                                            true to "Conta Fixa Recorrente"
                                        )
                                        modes.forEach { (isFixed, label) ->
                                            val isSel = isRecurringFixedMode == isFixed
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { isRecurringFixedMode = isFixed },
                                                color = if (isSel) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = 1.dp,
                                                    color = if (isSel) themeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isSel) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    isRecurringFixedMode = false
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (!isRecurringFixedMode) {
                                    // Repeat Count Counter
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Repetir quantas vezes?",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Quantidade de parcelas/lançamentos",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }

                                        // Compact Row for quantity selector (- Value +)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable(enabled = repeatCount > 2) { repeatCount-- },
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "−",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (repeatCount > 2) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = repeatCount.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.widthIn(min = 20.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            Surface(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { repeatCount++ },
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "+",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    val freqText = when (repeatFrequency) {
                                        "WEEKLY" -> "semanais"
                                        "BIWEEKLY" -> "quinzenais"
                                        else -> "mensais"
                                    }
                                    Text(
                                        text = "💡 Serão gerados $repeatCount lançamentos $freqText no sistema.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = themeColor
                                    )
                                } else {
                                    val freqText = when (repeatFrequency) {
                                        "WEEKLY" -> "Semanal"
                                        "BIWEEKLY" -> "Quinzenal"
                                        else -> "Mensal"
                                    }
                                    Text(
                                        text = "💡 Uma nova Conta Fixa ($freqText) será cadastrada e monitorada no painel de contas.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = themeColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Status Selector (Efetivada vs Pendente)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Status do Lançamento",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) {
                                        if (statusValue) FinanceIncomeGreen else FinanceExpenseRed
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 0.6f else 0.35f)
                                    }
                                )
                            }
                        }
                    }
                }
                if (isFutureDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ℹ️ Por padrão, lançamentos futuros são identificados como pendentes, mas você pode marcar como efetivada se preferir.",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinanceExpenseRed
                    )
                }

                } // End of Scrollable Column

                Spacer(modifier = Modifier.height(14.dp))

                // Save Button
                Button(
                    onClick = {
                        if (amountCentavos > 0) {
                            if (isRepeating && isRecurringFixedMode && onSaveRecurring != null && selectedType != TransactionType.TRANSFER) {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                val dueDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                onSaveRecurring(
                                    description.ifEmpty { if (selectedType == TransactionType.INCOME) "Receita Fixa" else "Despesa Fixa" },
                                    if (selectedType == TransactionType.INCOME) "INCOME" else "EXPENSE",
                                    amountCentavos,
                                    selectedCategoryId,
                                    selectedAccountId,
                                    dueDay,
                                    repeatFrequency
                                )
                            } else {
                                onSave(
                                    selectedType,
                                    amountCentavos,
                                    selectedCategoryId,
                                    selectedAccountId,
                                    if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null,
                                    description.ifEmpty { if (selectedType == TransactionType.INCOME) "Receita" else "Despesa" },
                                    selectedDateMillis,
                                    if (isRepeating) repeatCount else 1,
                                    if (isRepeating) repeatFrequency else "MONTHLY",
                                    isPaid
                                )
                            }
                            onDismiss()
                        }
                    },
                    enabled = amountCentavos > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_save),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                        if (it > System.currentTimeMillis()) {
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
}

}
