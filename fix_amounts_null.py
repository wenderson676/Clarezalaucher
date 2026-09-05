import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

# Fix the parsing where the field might be empty -> this can cause crashes or logic errors if it expects a number but gets empty
content = re.sub(r'val amountCentavos = remember\(amountInput\) \{ amountInput\.toLongOrNull\(\) \?: 0L \}', r'val amountCentavos = remember(amountInput) { amountInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val currentCentavos = remember\(currentInput\) \{ currentInput\.toLongOrNull\(\) \?: 0L \}', r'val currentCentavos = remember(currentInput) { currentInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val targetCentavos = remember\(targetInput\) \{ targetInput\.toLongOrNull\(\) \?: 0L \}', r'val targetCentavos = remember(targetInput) { targetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val initialBalanceCentavos = remember\(initialBalanceInput\) \{ initialBalanceInput\.toLongOrNull\(\) \?: 0L \}', r'val initialBalanceCentavos = remember(initialBalanceInput) { initialBalanceInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val creditLimitCentavos = remember\(creditLimitInput\) \{ creditLimitInput\.toLongOrNull\(\) \?: 0L \}', r'val creditLimitCentavos = remember(creditLimitInput) { creditLimitInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }', content)


with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
