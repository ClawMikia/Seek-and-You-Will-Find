package com.christopher.bibleverse.di

import android.content.Context
import androidx.room.Room
import com.christopher.bibleverse.data.local.AlarmPreferences
import com.christopher.bibleverse.data.local.AppDatabase
import com.christopher.bibleverse.data.local.dao.FavoriteVerseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteVerseDao(database: AppDatabase): FavoriteVerseDao =
        database.favoriteVerseDao()

    @Provides
    @Singleton
    fun provideAlarmPreferences(@ApplicationContext context: Context): AlarmPreferences =
        AlarmPreferences(context)
}
