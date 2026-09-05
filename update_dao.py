import re

with open('app/src/main/java/com/example/data/dao/AppDaos.kt', 'r') as f:
    content = f.read()

new_dao = """    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE groupId = :groupId AND installmentNumber >= :startInstallment")
    suspend fun getTransactionsFromGroup(groupId: String, startInstallment: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE groupId = :groupId")
    suspend fun getAllTransactionsFromGroup(groupId: String): List<TransactionEntity>"""

content = content.replace('    @Query("SELECT * FROM transactions WHERE id = :id")\n    suspend fun getTransactionById(id: Long): TransactionEntity?', new_dao)

with open('app/src/main/java/com/example/data/dao/AppDaos.kt', 'w') as f:
    f.write(content)
