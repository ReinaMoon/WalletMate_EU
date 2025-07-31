package com.yourdomain.walletmateeu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourdomain.walletmateeu.data.local.dao.CategoryDao
import com.yourdomain.walletmateeu.data.local.dao.TagDao
import com.yourdomain.walletmateeu.data.local.dao.TransactionDao
import com.yourdomain.walletmateeu.data.local.model.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
        TitleCategoryMap::class // <<--- 이 줄이 추가되었습니다
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN imageUri TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tags ADD COLUMN color TEXT NOT NULL DEFAULT '#808080'")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `title_category_map` (
                `title` TEXT NOT NULL, 
                `categoryId` TEXT NOT NULL, 
                PRIMARY KEY(`title`)
            )
        """)
    }
}