import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

content = content.replace("onSave(updated)\n                                onDismiss()", "onSave(updated, updateMode)\n                                onDismiss()")
content = content.replace("onDelete(transaction)\n                            onDismiss()", "onDelete(transaction, updateMode)\n                            onDismiss()")

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
