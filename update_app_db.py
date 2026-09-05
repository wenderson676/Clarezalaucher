import re

with open('app/src/main/java/com/example/data/db/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace("version = 2,", "version = 3,")
content = content.replace("import androidx.sqlite.db.SupportSQLiteDatabase", "import androidx.sqlite.db.SupportSQLiteDatabase\nimport androidx.room.migration.Migration")

migration_code = """    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN groupId TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN installmentNumber INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN totalInstallments INTEGER")
            }
        }

        @Volatile"""

content = content.replace("    companion object {\n        @Volatile", migration_code)
content = content.replace(".fallbackToDestructiveMigration()", ".addMigrations(MIGRATION_2_3)\n                    .fallbackToDestructiveMigration()")

with open('app/src/main/java/com/example/data/db/AppDatabase.kt', 'w') as f:
    f.write(content)
