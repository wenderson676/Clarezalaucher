import re

with open('app/src/main/java/com/example/ui/finance/FinanceTabs.kt', 'r') as f:
    content = f.read()

content = content.replace("    onDeleteTransaction: (TransactionEntity) -> Unit,\n    onEditTransaction: (TransactionEntity) -> Unit = {},", "    onDeleteTransaction: (TransactionEntity, String) -> Unit,\n    onEditTransaction: (TransactionEntity) -> Unit = {},")

with open('app/src/main/java/com/example/ui/finance/FinanceTabs.kt', 'w') as f:
    f.write(content)
