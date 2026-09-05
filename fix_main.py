import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },", "onDeleteTransaction = { tx, mode -> viewModel.deleteTransaction(tx, mode) },")
content = content.replace("onUpdateTransaction = { tx -> viewModel.updateTransaction(tx) },", "onUpdateTransaction = { tx, mode -> viewModel.updateTransaction(tx, mode) },")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content2 = f.read()

# Update updateTransaction and deleteTransaction
new_update = """    fun updateTransaction(tx: TransactionEntity, mode: String = "THIS") {
        viewModelScope.launch(Dispatchers.IO) {
            if (tx.groupId == null || mode == "THIS") {
                financeRepository.updateTransaction(tx)
            } else if (mode == "FUTURE") {
                val groupTxs = financeRepository.getTransactionsFromGroup(tx.groupId, tx.installmentNumber ?: 1)
                groupTxs.forEach { 
                    financeRepository.updateTransaction(it.copy(
                        amount = tx.amount,
                        categoryId = tx.categoryId,
                        accountId = tx.accountId,
                        targetAccountId = tx.targetAccountId,
                        description = tx.description,
                        type = tx.type
                    ))
                }
            } else if (mode == "ALL") {
                val groupTxs = financeRepository.getAllTransactionsFromGroup(tx.groupId)
                groupTxs.forEach { 
                    financeRepository.updateTransaction(it.copy(
                        amount = tx.amount,
                        categoryId = tx.categoryId,
                        accountId = tx.accountId,
                        targetAccountId = tx.targetAccountId,
                        description = tx.description,
                        type = tx.type
                    ))
                }
            }
        }
    }"""
content2 = re.sub(r'    fun updateTransaction\(tx: TransactionEntity\) \{\s*viewModelScope\.launch\(Dispatchers\.IO\) \{\s*financeRepository\.updateTransaction\(tx\)\s*\}\s*\}', new_update, content2)

new_delete = """    fun deleteTransaction(tx: TransactionEntity, mode: String = "THIS") {
        viewModelScope.launch(Dispatchers.IO) {
            if (tx.groupId == null || mode == "THIS") {
                financeRepository.deleteTransaction(tx)
            } else if (mode == "FUTURE") {
                val groupTxs = financeRepository.getTransactionsFromGroup(tx.groupId, tx.installmentNumber ?: 1)
                groupTxs.forEach { financeRepository.deleteTransaction(it) }
            } else if (mode == "ALL") {
                val groupTxs = financeRepository.getAllTransactionsFromGroup(tx.groupId)
                groupTxs.forEach { financeRepository.deleteTransaction(it) }
            }
        }
    }"""
content2 = re.sub(r'    fun deleteTransaction\(tx: TransactionEntity\) \{\s*viewModelScope\.launch\(Dispatchers\.IO\) \{\s*financeRepository\.deleteTransaction\(tx\)\s*\}\s*\}', new_delete, content2)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content2)

