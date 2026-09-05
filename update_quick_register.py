import re

with open('app/src/main/java/com/example/ui/components/QuickRegisterDialog.kt', 'r') as f:
    content = f.read()

# Add imports for DatePicker, Dropdown, etc.
imports = """
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
"""
content = content.replace("package com.example.ui.components", "package com.example.ui.components" + imports)

# Update signature
old_sig = """    onSave: (
        type: TransactionType,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        targetAccountId: Long?,
        description: String,
        date: Long
    ) -> Unit,"""

new_sig = """    onSave: (
        type: TransactionType,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        targetAccountId: Long?,
        description: String,
        date: Long,
        repeatCount: Int,
        repeatFrequency: String
    ) -> Unit,"""
content = content.replace(old_sig, new_sig)

# Add state variables
state_vars = """
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var isRepeating by remember { mutableStateOf(false) }
    var repeatCountInput by remember { mutableStateOf("1") }
    var repeatFrequency by remember { mutableStateOf("MONTHLY") }
    var repeatFreqExpanded by remember { mutableStateOf(false) }
"""
content = content.replace("var description by remember { mutableStateOf(initialDescription) }", "var description by remember { mutableStateOf(initialDescription) }" + state_vars)

# Theme color logic
theme_color = """
    val themeColor = when (selectedType) {
        TransactionType.INCOME -> FinanceIncomeGreen
        TransactionType.EXPENSE -> FinanceExpenseRed
        TransactionType.TRANSFER -> FinanceTransferBlue
    }
"""
content = content.replace("    Dialog(", theme_color + "\n    Dialog(")
content = content.replace("color = MaterialTheme.colorScheme.surface,", "color = themeColor.copy(alpha = 0.05f),")

# Date Picker UI
date_ui = """
                // Date Picker
                Spacer(modifier = Modifier.height(14.dp))
                val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDateMillis)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
"""
content = content.replace("                Spacer(modifier = Modifier.height(14.dp))\n                // Category Selection", date_ui + "\n                Spacer(modifier = Modifier.height(14.dp))\n                // Category Selection")

# Repeating UI
repeat_ui = """
                // Repeat/Installments
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isRepeating,
                        onCheckedChange = { isRepeating = it },
                        colors = CheckboxDefaults.colors(checkedColor = themeColor)
                    )
                    Text("Repetir / Parcelar", style = MaterialTheme.typography.bodyMedium)
                }
                if (isRepeating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = repeatCountInput,
                            onValueChange = { repeatCountInput = it.filter { char -> char.isDigit() } },
                            label = { Text("Qtd (Parcelas/Vezes)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = repeatFreqExpanded,
                            onExpandedChange = { repeatFreqExpanded = !repeatFreqExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val freqLabels = mapOf("DAILY" to "Diário", "WEEKLY" to "Semanal", "MONTHLY" to "Mensal", "YEARLY" to "Anual")
                            OutlinedTextField(
                                value = freqLabels[repeatFrequency] ?: "Mensal",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatFreqExpanded) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("Frequência") }
                            )
                            ExposedDropdownMenu(
                                expanded = repeatFreqExpanded,
                                onDismissRequest = { repeatFreqExpanded = false }
                            ) {
                                freqLabels.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            repeatFrequency = key
                                            repeatFreqExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
"""

content = content.replace("                Spacer(modifier = Modifier.height(20.dp))\n                // Save Button", repeat_ui + "\n                Spacer(modifier = Modifier.height(20.dp))\n                // Save Button")

save_button_old = """                            onSave(
                                selectedType,
                                amountCentavos,
                                selectedCategoryId,
                                selectedAccountId,
                                if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null,
                                description.ifEmpty { if (selectedType == TransactionType.INCOME) "Receita" else "Despesa" },
                                System.currentTimeMillis()
                            )"""
save_button_new = """                            onSave(
                                selectedType,
                                amountCentavos,
                                selectedCategoryId,
                                selectedAccountId,
                                if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null,
                                description.ifEmpty { if (selectedType == TransactionType.INCOME) "Receita" else "Despesa" },
                                selectedDateMillis,
                                if (isRepeating) repeatCountInput.toIntOrNull() ?: 1 else 1,
                                if (isRepeating) repeatFrequency else "MONTHLY"
                            )"""
content = content.replace(save_button_old, save_button_new)
content = content.replace("containerColor = MaterialTheme.colorScheme.primary", "containerColor = themeColor")


date_picker_dialog = """
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
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
"""

content = re.sub(r'    }\n}$', date_picker_dialog, content)

with open('app/src/main/java/com/example/ui/components/QuickRegisterDialog.kt', 'w') as f:
    f.write(content)
