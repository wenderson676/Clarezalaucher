import re

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'r') as f:
    content = f.read()

new_repo = """    suspend fun getTransactionById(id: Long): TransactionEntity? = transactionDao.getTransactionById(id)
    suspend fun getTransactionsFromGroup(groupId: String, startInstallment: Int) = transactionDao.getTransactionsFromGroup(groupId, startInstallment)
    suspend fun getAllTransactionsFromGroup(groupId: String) = transactionDao.getAllTransactionsFromGroup(groupId)"""

content = content.replace('    suspend fun getTransactionById(id: Long): TransactionEntity? = transactionDao.getTransactionById(id)', new_repo)

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'w') as f:
    f.write(content)
