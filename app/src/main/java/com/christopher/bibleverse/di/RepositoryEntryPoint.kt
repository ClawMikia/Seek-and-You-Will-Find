package com.christopher.bibleverse.di

import com.christopher.bibleverse.data.repository.VerseRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import android.content.Context

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun verseRepository(): VerseRepository
}

fun Context.verseRepositoryEntryPoint(): VerseRepository =
    EntryPointAccessors.fromApplication(applicationContext, RepositoryEntryPoint::class.java)
        .verseRepository()
