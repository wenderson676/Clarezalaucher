import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

# Restore var amountInput initialization logic back to normal String (non-formatted).
# Replace: var amountInput by remember { mutableStateOf(transaction.amount.toString()) } -> val dec = transaction.amount / 100.0; mutableStateOf(if (transaction.amount > 0) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")

def format_replacer(match):
    prefix = match.group(1)
    obj = match.group(2)
    prop = match.group(3)
    
    return f"""var {prefix} by remember {{
        val dec = {obj}.{prop} / 100.0
        mutableStateOf(if ({obj}.{prop} > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }}"""

content = re.sub(r'var (amountInput|initialBalanceInput|creditLimitInput|targetInput|currentInput) by remember \{ mutableStateOf\(([a-zA-Z0-9_]+)\.([a-zA-Z0-9_]+)\.toString\(\)\) \}', format_replacer, content)


with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
