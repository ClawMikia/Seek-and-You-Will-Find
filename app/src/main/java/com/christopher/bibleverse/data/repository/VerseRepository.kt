package com.christopher.bibleverse.data.repository

import android.util.Log
import com.christopher.bibleverse.data.local.AlarmPreferences
import com.christopher.bibleverse.data.local.AlarmState
import com.christopher.bibleverse.data.local.OfflineBibleSource
import com.christopher.bibleverse.data.local.dao.FavoriteVerseDao
import com.christopher.bibleverse.data.local.entity.FavoriteVerseEntity
import com.christopher.bibleverse.data.model.BibleBook
import com.christopher.bibleverse.data.model.BibleBooksProvider
import com.christopher.bibleverse.data.model.Testament
import com.christopher.bibleverse.data.model.VerseDetail
import com.christopher.bibleverse.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class VerseRepository @Inject constructor(
    private val offlineBible: OfflineBibleSource,
    private val dao: FavoriteVerseDao,
    private val alarmPreferences: AlarmPreferences
) {

    /**
     * Resolves a verse entirely offline from the bundled KJV dataset — no
     * network access is ever used. Any of [testament], [bookId], [chapter],
     * or [verseNumber] left null is chosen at random within whatever's
     * already been narrowed down (e.g. a random chapter within a chosen
     * book, or a fully random verse from all 66 books).
     */
    suspend fun fetchVerse(
        testament: Testament?,
        bookId: String?,
        chapter: Int?,
        verseNumber: Int?
    ): Resource<VerseDetail> {
        return try {
            val candidateBooks: List<BibleBook> = if (bookId != null) {
                listOfNotNull(BibleBooksProvider.allBooks.firstOrNull { it.id == bookId })
            } else {
                BibleBooksProvider.booksFor(testament)
            }
            val book = candidateBooks.randomOrNull(Random)
                ?: return Resource.Error("invalid_reference")

            val chapterCount = offlineBible.chapterCount(book.abbrev)
            if (chapterCount == 0) return Resource.Error("invalid_reference")
            val resolvedChapter = chapter?.takeIf { it in 1..chapterCount }
                ?: Random.nextInt(1, chapterCount + 1)

            val verseCount = offlineBible.verseCount(book.abbrev, resolvedChapter)
            if (verseCount == 0) return Resource.Error("invalid_reference")
            val resolvedVerse = verseNumber?.takeIf { it in 1..verseCount }
                ?: Random.nextInt(1, verseCount + 1)

            val text = offlineBible.verseText(book.abbrev, resolvedChapter, resolvedVerse)
            if (text.isNullOrBlank()) {
                Resource.Error("invalid_reference")
            } else {
                Resource.Success(
                    VerseDetail(
                        bookId = book.id,
                        bookName = book.displayName,
                        testament = book.testament,
                        chapter = resolvedChapter,
                        verseNumber = resolvedVerse,
                        reference = "${book.displayName} $resolvedChapter:$resolvedVerse",
                        text = text,
                        translationId = OfflineBibleSource.TRANSLATION_ID,
                        translationName = OfflineBibleSource.TRANSLATION_NAME
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Offline verse resolution failed", e)
            Resource.Error("invalid_reference")
        }
    }

    suspend fun chapterCount(bookId: String): Int {
        val book = BibleBooksProvider.allBooks.firstOrNull { it.id == bookId } ?: return 0
        return offlineBible.chapterCount(book.abbrev)
    }

    suspend fun verseCount(bookId: String, chapter: Int): Int {
        val book = BibleBooksProvider.allBooks.firstOrNull { it.id == bookId } ?: return 0
        return offlineBible.verseCount(book.abbrev, chapter)
    }

    fun observeFavorite(): Flow<VerseDetail?> = dao.observe().map { it?.toDomain() }

    suspend fun saveFavorite(verse: VerseDetail) {
        dao.upsert(
            FavoriteVerseEntity(
                bookId = verse.bookId,
                bookName = verse.bookName,
                testament = verse.testament.name,
                chapter = verse.chapter,
                verseNumber = verse.verseNumber,
                reference = verse.reference,
                text = verse.text,
                translationId = verse.translationId,
                translationName = verse.translationName,
                savedAtEpochMillis = verse.savedAtEpochMillis
            )
        )
    }

    suspend fun getFavoriteOnce(): VerseDetail? = dao.getOnce()?.toDomain()

    // --- Alarm ---
    val alarmState: Flow<AlarmState> = alarmPreferences.alarmStateFlow
    suspend fun setAlarm(hour: Int, minute: Int) = alarmPreferences.setAlarm(hour, minute)
    suspend fun clearAlarm() = alarmPreferences.clearAlarm()
    suspend fun currentAlarmState(): AlarmState = alarmPreferences.currentState()

    private fun FavoriteVerseEntity.toDomain() = VerseDetail(
        bookId = bookId,
        bookName = bookName,
        testament = runCatching { Testament.valueOf(testament) }.getOrDefault(Testament.NEW),
        chapter = chapter,
        verseNumber = verseNumber,
        reference = reference,
        text = text,
        translationId = translationId,
        translationName = translationName,
        savedAtEpochMillis = savedAtEpochMillis
    )

    companion object {
        private const val TAG = "VerseRepository"
    }
}
