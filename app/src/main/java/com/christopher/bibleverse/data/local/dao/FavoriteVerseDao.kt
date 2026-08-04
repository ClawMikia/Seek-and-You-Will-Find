package com.christopher.bibleverse.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.christopher.bibleverse.data.local.entity.FavoriteVerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteVerseDao {

    @Upsert
    suspend fun upsert(verse: FavoriteVerseEntity)

    @Query("SELECT * FROM favorite_verse WHERE id = :id LIMIT 1")
    fun observe(id: Int = FavoriteVerseEntity.SINGLE_ROW_ID): Flow<FavoriteVerseEntity?>

    @Query("SELECT * FROM favorite_verse WHERE id = :id LIMIT 1")
    suspend fun getOnce(id: Int = FavoriteVerseEntity.SINGLE_ROW_ID): FavoriteVerseEntity?

    @Query("DELETE FROM favorite_verse")
    suspend fun clear()
}
