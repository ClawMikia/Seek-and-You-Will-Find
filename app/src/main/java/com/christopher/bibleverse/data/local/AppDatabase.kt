package com.christopher.bibleverse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.christopher.bibleverse.data.local.dao.FavoriteVerseDao
import com.christopher.bibleverse.data.local.entity.FavoriteVerseEntity

@Database(entities = [FavoriteVerseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteVerseDao(): FavoriteVerseDao

    companion object {
        const val DATABASE_NAME = "bible_verse_db"
    }
}
