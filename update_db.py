import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

replacement = """    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val notes: String? = null,
    val groupId: String? = null,
    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null
)"""
content = re.sub(r'    val isRecurring: Boolean = false,\n    val recurrenceRule: String\? = null,\n    val notes: String\? = null\n\)', replacement, content)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(content)
