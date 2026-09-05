import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("groupTxs.forEach { ", "for (txInGroup in groupTxs) { ")
content = content.replace("financeRepository.updateTransaction(it.copy(", "financeRepository.updateTransaction(txInGroup.copy(")
content = content.replace("financeRepository.deleteTransaction(it)", "financeRepository.deleteTransaction(txInGroup)")

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
