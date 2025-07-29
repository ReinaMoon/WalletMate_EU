package com.yourdomain.walletmateeu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    /**
     * 새로운 카테고리를 데이터베이스에 삽입합니다.
     * 만약 동일한 ID를 가진 카테고리가 이미 존재한다면, 덮어씁니다.
     * @param category 삽입할 카테고리 엔티티
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    /**
     * 데이터베이스에 있는 모든 카테고리를 이름순으로 정렬하여 가져옵니다.
     * Flow를 반환하므로, 카테고리 목록에 변경이 생기면 자동으로 새로운 목록을 방출합니다.
     * @return 카테고리 엔티티 목록의 Flow
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * 주어진 ID를 가진 카테고리를 삭제합니다.
     * @param categoryId 삭제할 카테고리의 ID
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

}