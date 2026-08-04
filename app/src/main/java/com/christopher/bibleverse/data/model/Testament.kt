package com.christopher.bibleverse.data.model

/**
 * Represents which half of the canon a book belongs to.
 * [apiToken] is what bible-api.com expects for its random-verse
 * filter endpoint: /data/{translation}/random/{OT|NT|bookId}
 */
enum class Testament(val displayName: String, val apiToken: String) {
    OLD("Old Testament", "OT"),
    NEW("New Testament", "NT");
}
