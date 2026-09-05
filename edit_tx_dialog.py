import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

new_edit_tx_sig = """@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    onSave: (TransactionEntity, String) -> Unit, // Add String for edit mode
    onDelete: (TransactionEntity, String) -> Unit, // Add String for delete mode
    onDismiss: () -> Unit
) {"""

content = re.sub(r'@OptIn\(ExperimentalLayoutApi::class\)\n@Composable\nfun EditTransactionDialog\(\n    transaction: TransactionEntity,\n    categories: List<CategoryEntity>,\n    accounts: List<AccountEntity>,\n    onSave: \(TransactionEntity\) -> Unit,\n    onDelete: \(TransactionEntity\) -> Unit,\n    onDismiss: \(\) -> Unit\n\) \{', new_edit_tx_sig, content)

# Replace the state initialization and add the update mode selector
state_vars = """
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var updateMode by remember { mutableStateOf("THIS") } // THIS, FUTURE, ALL
"""
content = content.replace("    var selectedTargetAccountId by remember { mutableLongStateOf(transaction.targetAccountId ?: 0L) }", "    var selectedTargetAccountId by remember { mutableLongStateOf(transaction.targetAccountId ?: 0L) }" + state_vars)

# Update Save and Delete Buttons to pass `updateMode` if the transaction is part of a group
# Add update mode UI before the buttons
mode_ui = """
                if (transaction.groupId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Esta é uma transação repetida (${transaction.installmentNumber}/${transaction.totalInstallments})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { updateMode = "THIS" }) {
                            androidx.compose.material3.RadioButton(selected = updateMode == "THIS", onClick = { updateMode = "THIS" })
                            Text("Apenas esta", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { updateMode = "FUTURE" }) {
                            androidx.compose.material3.RadioButton(selected = updateMode == "FUTURE", onClick = { updateMode = "FUTURE" })
                            Text("Esta e futuras", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { updateMode = "ALL" }) {
                            androidx.compose.material3.RadioButton(selected = updateMode == "ALL", onClick = { updateMode = "ALL" })
                            Text("Todas", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
"""

content = content.replace("                Spacer(modifier = Modifier.height(24.dp))\n                Row(", mode_ui + "\n                Spacer(modifier = Modifier.height(24.dp))\n                Row(")

content = content.replace("onSave(transaction.copy(", "onSave(transaction.copy(")
content = content.replace("                    onSave(updatedTx)\n                    onDismiss()", "                    onSave(updatedTx, updateMode)\n                    onDismiss()")
content = content.replace("                            onDelete(transaction)\n                            onDismiss()", "                            onDelete(transaction, updateMode)\n                            onDismiss()")

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
