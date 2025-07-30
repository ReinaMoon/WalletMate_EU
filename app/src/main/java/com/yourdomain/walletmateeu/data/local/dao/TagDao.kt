package com.yourdomain.walletmateeu.data.local.dao

import androidx.room.*
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    // --- 수정된 부분 시작 ---
    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToTransaction(crossRef: TransactionTagCrossRef)

    @Query("DELETE FROM transaction_tag_cross_ref WHERE transactionId = :transactionId AND tagId = :tagId")
    suspend fun removeTagFromTransaction(transactionId: String, tagId: String)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: String)

    @Query("DELETE FROM transaction_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteTagCrossRefsByTagId(tagId: String)
    // --- 수정된 부분 끝 ---

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("DELETE FROM transaction_tag_cross_ref")
    suspend fun deleteAllTagCrossRefs()

    @Query("DELETE FROM transaction_tag_cross_ref WHERE transactionId = :transactionId")
    suspend fun deleteAllTagCrossRefsForTransaction(transactionId: String)
}