package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Clareza Launcher", appName)
  }

  @Test
  fun `launch MainActivity`() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        assertTrue(activity != null)
      }
    }
  }

  @Test
  fun `verify database population`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context, this)
    val categories = database.categoryDao().getAllCategories().first()
    println("DATABASE CATEGORIES COUNT: ${categories.size}")
    assertTrue("Categories should be populated", categories.isNotEmpty())
    
    val accounts = database.accountDao().getAllAccounts().first()
    println("DATABASE ACCOUNTS COUNT: ${accounts.size}")
    assertTrue("Accounts should be populated", accounts.isNotEmpty())
  }
}
