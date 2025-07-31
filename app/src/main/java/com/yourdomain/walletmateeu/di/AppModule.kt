package com.yourdomain.walletmateeu.di

import android.content.Context
import androidx.room.Room
import com.yourdomain.walletmateeu.data.local.AppDatabase
import com.yourdomain.walletmateeu.data.local.MIGRATION_3_4
import com.yourdomain.walletmateeu.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "walletmate_db"
        )
            .addMigrations(MIGRATION_3_4) // <<--- 이 줄을 추가하세요
            .build()
    }
    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase) = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase) = db.categoryDao()

    @Provides
    @Singleton
    fun provideTagDao(db: AppDatabase) = db.tagDao()

    // --- 이 함수가 추가되었습니다 ---
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}