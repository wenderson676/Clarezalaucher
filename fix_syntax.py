import re

with open('app/src/main/java/com/example/ui/components/QuickRegisterDialog.kt', 'r') as f:
    content = f.read()

# I will just replace the tail
replacement = """            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}"""

content = re.sub(r'            }\n        }\n    if \(showDatePicker\) \{.*$', replacement, content, flags=re.DOTALL)
content = content.replace("@OptIn(ExperimentalLayoutApi::class)", "@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)")
with open('app/src/main/java/com/example/ui/components/QuickRegisterDialog.kt', 'w') as f:
    f.write(content)
