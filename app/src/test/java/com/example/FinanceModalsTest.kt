package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.OutlinedTextField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.ui.components.CurrencyAmountVisualTransformation
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FinanceModalsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCurrencyInput() {
        composeTestRule.setContent {
            val text = remember { mutableStateOf("") }
            OutlinedTextField(
                value = text.value,
                onValueChange = { text.value = it.filter { char -> char.isDigit() } },
                visualTransformation = CurrencyAmountVisualTransformation()
            )
        }
        
        // Find by type or just the first text field
        composeTestRule.onNode(androidx.compose.ui.test.hasSetTextAction()).performTextInput("123")
    }
}
