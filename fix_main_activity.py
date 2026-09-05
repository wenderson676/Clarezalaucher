import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_lambda = """                            onSave = { type, amountCentavos, categoryId, accountId, targetAccountId, description, date ->
                                viewModel.addTransaction(
                                    type = type,
                                    amountCentavos = amountCentavos,
                                    categoryId = categoryId,
                                    accountId = accountId,
                                    targetAccountId = targetAccountId,
                                    description = description,
                                    date = date
                                )"""
                                
new_lambda = """                            onSave = { type, amountCentavos, categoryId, accountId, targetAccountId, description, date, repeatCount, repeatFreq ->
                                viewModel.addTransaction(
                                    type = type,
                                    amountCentavos = amountCentavos,
                                    categoryId = categoryId,
                                    accountId = accountId,
                                    targetAccountId = targetAccountId,
                                    description = description,
                                    date = date,
                                    repeatCount = repeatCount,
                                    repeatFrequency = repeatFreq
                                )"""

content = content.replace(old_lambda, new_lambda)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
