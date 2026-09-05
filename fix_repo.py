import re

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'r') as f:
    content = f.read()

repo_add = """
    suspend fun getTransactionsFromGroup(groupId: String, startInstallment: Int) = transactionDao.getTransactionsFromGroup(groupId, startInstallment)
    suspend fun getAllTransactionsFromGroup(groupId: String) = transactionDao.getAllTransactionsFromGroup(groupId)
"""
content = content.replace("suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)", "suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)" + repo_add)

with open('app/src/main/java/com/example/data/repository/FinanceRepository.kt', 'w') as f:
    f.write(content)
