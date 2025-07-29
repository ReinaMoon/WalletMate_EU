package com.yourdomain.walletmateeu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourdomain.walletmateeu.data.local.dao.CategoryDao
import com.yourdomain.walletmateeu.data.local.dao.TagDao
import com.yourdomain.walletmateeu.data.local.dao.TransactionDao
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionTagCrossRef

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class
    ],
    version = 2, // <<--- 버전을 1에서 2로 올립니다!
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
}

// --- 아래 Migration 객체를 파일 하단에 추가합니다 ---

/**
 * 데이터베이스 버전 1에서 2로 마이그레이션합니다.
 * 'categories' 테이블에 'type' 열을 추가합니다.
 * 기본값으로 모든 기존 카테고리를 'EXPENSE'로 설정합니다.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE categories ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
    }
}