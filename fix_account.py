import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

content = re.sub(r'var initialBalanceInput by remember \{\s*mutableStateOf\(account\.initialBalance\.toString\(\)\)\s*\}', r'''var initialBalanceInput by remember {
        val dec = account.initialBalance / 100.0
        mutableStateOf(if (account.initialBalance > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }''', content)
    
content = re.sub(r'var creditLimitInput by remember \{\s*mutableStateOf\(if \(account\.creditLimit > 0L\) account\.creditLimit\.toString\(\) else ""\)\s*\}', r'''var creditLimitInput by remember {
        val dec = account.creditLimit / 100.0
        mutableStateOf(if (account.creditLimit > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }''', content)

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
