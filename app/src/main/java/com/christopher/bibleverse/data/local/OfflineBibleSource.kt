package com.christopher.bibleverse.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Matches the shape of assets/bible_kjv.json: one entry per book, in canonical order. */
private data class BookJson(
    val abbrev: String,
    val chapters: List<List<String>>
)

/**
 * Parses the bundled, fully offline King James Version JSON asset into
 * memory once and keeps it cached for the lifetime of the process. No
 * network access is ever required to resolve a verse.
 */
@Singleton
class OfflineBibleSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()
    private var cache: Map<String, List<List<String>>>? = null

    private suspend fun booksByAbbrev(): Map<String, List<List<String>>> {
        cache?.let { return it }
        return mutex.withLock {
            cache?.let { return it }
            val loaded = withContext(Dispatchers.IO) {
                context.assets.open(ASSET_FILE_NAME).use { stream ->
                    val reader = stream.bufferedReader(Charsets.UTF_8)
                    val type = object : TypeToken<List<BookJson>>() {}.type
                    val books: List<BookJson> = Gson().fromJson(reader, type)
                    books.associate { it.abbrev to it.chapters }
                }
            }
            cache = loaded
            loaded
        }
    }

    /** Number of chapters in the given book, or 0 if the book isn't found. */
    suspend fun chapterCount(bookAbbrev: String): Int =
        booksByAbbrev()[bookAbbrev]?.size ?: 0

    /** Number of verses in the given chapter (1-indexed), or 0 if not found. */
    suspend fun verseCount(bookAbbrev: String, chapter: Int): Int {
        val chapters = booksByAbbrev()[bookAbbrev] ?: return 0
        if (chapter < 1 || chapter > chapters.size) return 0
        return chapters[chapter - 1].size
    }

    /** Verse text for a specific reference (1-indexed chapter/verse), or null if out of range. */
    suspend fun verseText(bookAbbrev: String, chapter: Int, verse: Int): String? {
        val chapters = booksByAbbrev()[bookAbbrev] ?: return null
        val chapterVerses = chapters.getOrNull(chapter - 1) ?: return null
        return chapterVerses.getOrNull(verse - 1)?.trim()
    }

    companion object {
        const val ASSET_FILE_NAME = "bible_kjv.json"
        const val TRANSLATION_ID = "kjv"
        const val TRANSLATION_NAME = "King James Version"
    }
}
