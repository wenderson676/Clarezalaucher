import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

content = re.sub(r'var targetInput by remember \{\s*mutableStateOf\(goal\.targetAmount\.toString\(\)\)\s*\}', r'''var targetInput by remember {
        val dec = goal.targetAmount / 100.0
        mutableStateOf(if (goal.targetAmount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }''', content)
    
content = re.sub(r'var currentInput by remember \{\s*mutableStateOf\(goal\.currentAmount\.toString\(\)\)\s*\}', r'''var currentInput by remember {
        val dec = goal.currentAmount / 100.0
        mutableStateOf(if (goal.currentAmount > 0L) String.format(java.util.Locale.US, "%.2f", dec).replace(".", "") else "")
    }''', content)

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
