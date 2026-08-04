package com.christopher.bibleverse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Only one row of this table will ever exist ([id] is pinned to
 * [SINGLE_ROW_ID]) because the app only ever remembers exactly one
 * favorite verse at a time. Saving a new verse overwrites this row.
 */
@Entity(tableName = "favorite_verse")
data class FavoriteVerseEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val bookId: String,
    val bookName: String,
    val testament: String,
    val chapter: Int,
    val verseNumber: Int,
    val reference: String,
    val text: String,
    val translationId: String,
    val translationName: String,
    val savedAtEpochMillis: Long
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
