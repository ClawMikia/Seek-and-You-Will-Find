package com.christopher.bibleverse.data.model

/**
 * @param id short display id (e.g. "GEN", "JHN")
 * @param abbrev the lookup key used in the bundled offline KJV JSON asset
 *   (assets/bible_kjv.json), e.g. "gn", "jo"
 * @param displayName human readable book name shown in the filter UI
 */
data class BibleBook(
    val id: String,
    val abbrev: String,
    val displayName: String,
    val testament: Testament
)
