import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

# Fix transaction edit
content = re.sub(r'var amountInput by remember \{\s* \s*mutableStateOf\(if\(dec != null\) "0" else "0"\) \s*\}', 'var amountInput by remember { mutableStateOf(transaction.amount.toString()) }', content)

# Fix budget edit
content = re.sub(r'val dec = budget\.amount / 100\.0\s*mutableStateOf\(if\(dec != null\) "0" else "0"\) ', 'mutableStateOf(budget.amount.toString())', content)

# Fix account edit - initialBalance
content = re.sub(r'var initialBalanceInput by remember \{\s*val dec = account\.initialBalance / 100\.0\s*mutableStateOf\(if\(dec != null\) "0" else "0"\) \s*\}', 'var initialBalanceInput by remember { mutableStateOf(account.initialBalance.toString()) }', content)

# Fix account edit - creditLimit
content = re.sub(r'var creditLimitInput by remember \{\s*val dec = account\.creditLimit / 100\.0\s*mutableStateOf\(if \(account\.creditLimit > 0L\) String\.format\(java\.util\.Locale\.US, "%\.2f", dec\) else ""\)\s*\}', 'var creditLimitInput by remember { mutableStateOf(if (account.creditLimit > 0L) account.creditLimit.toString() else "") }', content)

# Fix goal edit
content = re.sub(r'var targetInput by remember \{\s*val dec = goal\.targetAmount / 100\.0\s*mutableStateOf\(if\(dec != null\) "0" else "0"\) \s*\}', 'var targetInput by remember { mutableStateOf(goal.targetAmount.toString()) }', content)
content = re.sub(r'var currentInput by remember \{\s*val dec = goal\.currentAmount / 100\.0\s*mutableStateOf\(if\(dec != null\) "0" else "0"\) \s*\}', 'var currentInput by remember { mutableStateOf(goal.currentAmount.toString()) }', content)

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
