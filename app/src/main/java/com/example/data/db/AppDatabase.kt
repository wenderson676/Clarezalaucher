package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.data.dao.AccountDao
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.GoalDao
import com.example.data.dao.RecurringDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SettingEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        RecurringTransactionEntity::class,
        FinancialGoalEntity::class,
        BudgetEntity::class,
        SettingEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringDao(): RecurringDao
    abstract fun goalDao(): GoalDao
    abstract fun budgetDao(): BudgetDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, categoryId INTEGER NOT NULL, amount INTEGER NOT NULL, period TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS financial_goals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, targetAmount INTEGER NOT NULL, currentAmount INTEGER NOT NULL, deadline INTEGER, createdAt INTEGER NOT NULL)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN groupId TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN installmentNumber INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN totalInstallments INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 1")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vidasimples_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.beginTransaction()
                try {
                    // Populate Categories
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Salário', 'INCOME', 'payments', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Trabalho', 'INCOME', 'work', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Renda Extra', 'INCOME', 'trending_up', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Outras Receitas', 'INCOME', 'account_balance_wallet', 1, 0)")

                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Alimentação', 'EXPENSE', 'restaurant', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Moradia', 'EXPENSE', 'home', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Transporte', 'EXPENSE', 'directions_car', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Contas', 'EXPENSE', 'receipt_long', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Saúde', 'EXPENSE', 'local_hospital', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Educação', 'EXPENSE', 'school', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Lazer', 'EXPENSE', 'sports_esports', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Compras', 'EXPENSE', 'shopping_bag', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Dívidas', 'EXPENSE', 'credit_card', 1, 0)")
                    db.execSQL("INSERT INTO categories (name, type, icon, isDefault, isArchived) VALUES ('Outras Despesas', 'EXPENSE', 'category', 1, 0)")

                    // Populate Account
                    db.execSQL("INSERT INTO accounts (name, type, initialBalance, currentBalance, isActive, colorHex, creditLimit, closingDay, dueDay) VALUES ('Minha Conta', 'BANK', 0, 0, 1, '#2E7D5C', 0, 1, 10)")

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }
}
