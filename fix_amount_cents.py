import re

with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'r') as f:
    content = f.read()

# Fix parsing
# val amountCentavos = remember(amountInput) { CurrencyFormatter.parseToCentavos(amountInput) } -> val amountCentavos = remember(amountInput) { amountInput.toLongOrNull() ?: 0L }

content = re.sub(r'val amountCentavos = remember\(amountInput\) \{ CurrencyFormatter\.parseToCentavos\(amountInput\) \}', r'val amountCentavos = remember(amountInput) { amountInput.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val currentCentavos = remember\(currentInput\) \{ CurrencyFormatter\.parseToCentavos\(currentInput\) \}', r'val currentCentavos = remember(currentInput) { currentInput.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val targetCentavos = remember\(targetInput\) \{ CurrencyFormatter\.parseToCentavos\(targetInput\) \}', r'val targetCentavos = remember(targetInput) { targetInput.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val initialBalanceCentavos = remember\(initialBalanceInput\) \{ CurrencyFormatter\.parseToCentavos\(initialBalanceInput\) \}', r'val initialBalanceCentavos = remember(initialBalanceInput) { initialBalanceInput.toLongOrNull() ?: 0L }', content)
content = re.sub(r'val creditLimitCentavos = remember\(creditLimitInput\) \{ CurrencyFormatter\.parseToCentavos\(creditLimitInput\) \}', r'val creditLimitCentavos = remember(creditLimitInput) { creditLimitInput.toLongOrNull() ?: 0L }', content)


with open('app/src/main/java/com/example/ui/finance/FinanceModals.kt', 'w') as f:
    f.write(content)
