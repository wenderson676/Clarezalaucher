import re

with open('app/src/main/java/com/example/ui/finance/FinanceDashboardScreen.kt', 'r') as f:
    content = f.read()

pattern = r'\s*// ==========================================\s*// QUICK ACTION PILLS.*?// ==========================================\s*// CARD 2: MINHAS CONTAS'
replacement = '\n\n        // ==========================================\n        // CARD 2: MINHAS CONTAS'
content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/finance/FinanceDashboardScreen.kt', 'w') as f:
    f.write(content)
