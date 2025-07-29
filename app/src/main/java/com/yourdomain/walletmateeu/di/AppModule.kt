package com.yourdomain.walletmateeu.di

import android.content.Context
import androidx.room.Room
import com.yourdomain.walletmateeu.data.local.AppDatabase
import com.yourdomain.walletmateeu.data.local.MIGRATION_1_2
import com.yourdomain.walletmateeu.data.local.MIGRATION_2_3 // <<--- 새 Migration 임포트
import com.yourdomain.walletmateeu.data.local.dao.CategoryDao
import com.yourdomain.walletmateeu.data.local.dao.TagDao
import com.yourdomain.walletmateeu.data.local.dao.TransactionDao
import com.yourdomain.walletmateeu.data.repository.AppRepository
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // <<--- 새 Migration 추가
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(appDatabase: AppDatabase): TagDao {
        return appDatabase.tagDao()
    }

    // --- 아래 함수를 추가합니다 ---
    /**
     * AppRepository 인스턴스를 직접 생성하여 제공합니다.
     * TransactionDao와 CategoryDao에 의존합니다.
     * Hilt는 이 함수를 보고, 위에 있는 provideTransactionDao와 provideCategoryDao를 호출하여
     * 필요한 부품들을 가져온 뒤, AppRepository를 조립합니다.
     */
    @Provides
    @Singleton
    fun provideAppRepository(
        transactionDao: TransactionDao,
        categoryDao: CategoryDao,
        tagDao: TagDao // <<--- 1. 여기에 파라미터 추가
    ): AppRepository {
        return AppRepository(
            transactionDao = transactionDao,
            categoryDao = categoryDao,
            tagDao = tagDao // <<--- 2. 여기에 생성자로 전달
        )
    }
}