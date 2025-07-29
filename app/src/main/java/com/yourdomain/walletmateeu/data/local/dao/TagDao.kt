package com.yourdomain.walletmateeu.data.local.dao

import androidx.room.*
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToTransaction(crossRef: TransactionTagCrossRef)

    @Query("DELETE FROM transaction_tag_cross_ref WHERE transactionId = :transactionId AND tagId = :tagId")
    suspend fun removeTagFromTransaction(transactionId: String, tagId: String)

    // --- 아래 함수 추가 ---
    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("DELETE FROM transaction_tag_cross_ref")
    suspend fun deleteAllTagCrossRefs()
}