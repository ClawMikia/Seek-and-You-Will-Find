package com.christopher.bibleverse.data.model

data class VerseDetail(
    val bookId: String,
    val bookName: String,
    val testament: Testament,
    val chapter: Int,
    val verseNumber: Int,
    val reference: String,
    val text: String,
    val translationId: String,
    val translationName: String,
    val savedAtEpochMillis: Long = System.currentTimeMillis()
)
