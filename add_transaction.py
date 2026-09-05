import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

new_add_transaction = """    fun addTransaction(
        type: TransactionType,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        targetAccountId: Long?,
        description: String,
        date: Long,
        repeatCount: Int = 1,
        repeatFrequency: String = "MONTHLY"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val groupId = if (repeatCount > 1) java.util.UUID.randomUUID().toString() else null
            var currentDate = date
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = date
            
            for (i in 1..repeatCount) {
                financeRepository.insertTransaction(
                    TransactionEntity(
                        type = type.name,
                        amount = amountCentavos,
                        categoryId = categoryId,
                        accountId = accountId,
                        targetAccountId = targetAccountId,
                        description = if (repeatCount > 1) "$description ($i/$repeatCount)" else description,
                        date = currentDate,
                        groupId = groupId,
                        installmentNumber = i,
                        totalInstallments = repeatCount
                    )
                )
                if (repeatFrequency == "MONTHLY") {
                    calendar.add(java.util.Calendar.MONTH, 1)
                } else if (repeatFrequency == "WEEKLY") {
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                } else if (repeatFrequency == "DAILY") {
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                } else if (repeatFrequency == "YEARLY") {
                    calendar.add(java.util.Calendar.YEAR, 1)
                }
                currentDate = calendar.timeInMillis
            }
        }
    }"""

pattern = r'    fun addTransaction\((.*?)\s*\) \{\s*viewModelScope\.launch\(Dispatchers\.IO\) \{\s*financeRepository\.insertTransaction\((.*?)\)\s*\}\s*\}'
content = re.sub(pattern, new_add_transaction, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
