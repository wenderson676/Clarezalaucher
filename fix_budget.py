import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

content = re.sub(r'var amountInput by remember \{\s*mutableStateOf\(budget\.amount\.toString\(\)\)\s*\}', r'''var amountInput by remember {
        val dec = budget.amount / 100.0
        mutableStateOf(if (budget.amount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }''', content)

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
