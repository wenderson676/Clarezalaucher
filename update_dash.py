import re

with open('app/src/main/java/com/example/ui/finance/FinanceDashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("    onDeleteTransaction: (TransactionEntity) -> Unit,\n    onUpdateTransaction: (TransactionEntity) -> Unit = {},", "    onDeleteTransaction: (TransactionEntity, String) -> Unit,\n    onUpdateTransaction: (TransactionEntity, String) -> Unit,")

content = content.replace("""        EditTransactionDialog(
            transaction = tx,
            categories = categories,
            accounts = accounts,
            onSave = { updatedTx ->
                onUpdateTransaction(updatedTx)
                editingTransaction = null
            },
            onDelete = { txToDelete ->
                onDeleteTransaction(txToDelete)
                editingTransaction = null
            },
            onDismiss = { editingTransaction = null }
        )""", """        EditTransactionDialog(
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
        )""")

# Wait, there's another occurrence of onDeleteTransaction? Let's check FinanceTabs as well.

with open('app/src/main/java/com/example/ui/finance/FinanceDashboardScreen.kt', 'w') as f:
    f.write(content)
