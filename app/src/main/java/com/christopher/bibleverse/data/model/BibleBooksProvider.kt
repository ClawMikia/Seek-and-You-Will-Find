package com.christopher.bibleverse.data.model

/**
 * Static catalogue of the 66 canonical books, used to populate the
 * Testament -> Book -> Chapter -> Verse filter chain. [BibleBook.abbrev]
 * values match the keys used in the bundled offline JSON asset
 * (assets/bible_kjv.json), which lists books in this exact canonical
 * order — so no network lookup is ever needed to resolve a book.
 */
object BibleBooksProvider {

    val oldTestament: List<BibleBook> = listOf(
        BibleBook("GEN", "gn", "Genesis", Testament.OLD),
        BibleBook("EXO", "ex", "Exodus", Testament.OLD),
        BibleBook("LEV", "lv", "Leviticus", Testament.OLD),
        BibleBook("NUM", "nm", "Numbers", Testament.OLD),
        BibleBook("DEU", "dt", "Deuteronomy", Testament.OLD),
        BibleBook("JOS", "js", "Joshua", Testament.OLD),
        BibleBook("JDG", "jud", "Judges", Testament.OLD),
        BibleBook("RUT", "rt", "Ruth", Testament.OLD),
        BibleBook("1SA", "1sm", "1 Samuel", Testament.OLD),
        BibleBook("2SA", "2sm", "2 Samuel", Testament.OLD),
        BibleBook("1KI", "1kgs", "1 Kings", Testament.OLD),
        BibleBook("2KI", "2kgs", "2 Kings", Testament.OLD),
        BibleBook("1CH", "1ch", "1 Chronicles", Testament.OLD),
        BibleBook("2CH", "2ch", "2 Chronicles", Testament.OLD),
        BibleBook("EZR", "ezr", "Ezra", Testament.OLD),
        BibleBook("NEH", "ne", "Nehemiah", Testament.OLD),
        BibleBook("EST", "et", "Esther", Testament.OLD),
        BibleBook("JOB", "job", "Job", Testament.OLD),
        BibleBook("PSA", "ps", "Psalms", Testament.OLD),
        BibleBook("PRO", "prv", "Proverbs", Testament.OLD),
        BibleBook("ECC", "ec", "Ecclesiastes", Testament.OLD),
        BibleBook("SNG", "so", "Song of Solomon", Testament.OLD),
        BibleBook("ISA", "is", "Isaiah", Testament.OLD),
        BibleBook("JER", "jr", "Jeremiah", Testament.OLD),
        BibleBook("LAM", "lm", "Lamentations", Testament.OLD),
        BibleBook("EZK", "ez", "Ezekiel", Testament.OLD),
        BibleBook("DAN", "dn", "Daniel", Testament.OLD),
        BibleBook("HOS", "ho", "Hosea", Testament.OLD),
        BibleBook("JOL", "jl", "Joel", Testament.OLD),
        BibleBook("AMO", "am", "Amos", Testament.OLD),
        BibleBook("OBA", "ob", "Obadiah", Testament.OLD),
        BibleBook("JON", "jn", "Jonah", Testament.OLD),
        BibleBook("MIC", "mi", "Micah", Testament.OLD),
        BibleBook("NAM", "na", "Nahum", Testament.OLD),
        BibleBook("HAB", "hk", "Habakkuk", Testament.OLD),
        BibleBook("ZEP", "zp", "Zephaniah", Testament.OLD),
        BibleBook("HAG", "hg", "Haggai", Testament.OLD),
        BibleBook("ZEC", "zc", "Zechariah", Testament.OLD),
        BibleBook("MAL", "ml", "Malachi", Testament.OLD)
    )

    val newTestament: List<BibleBook> = listOf(
        BibleBook("MAT", "mt", "Matthew", Testament.NEW),
        BibleBook("MRK", "mk", "Mark", Testament.NEW),
        BibleBook("LUK", "lk", "Luke", Testament.NEW),
        BibleBook("JHN", "jo", "John", Testament.NEW),
        BibleBook("ACT", "act", "Acts", Testament.NEW),
        BibleBook("ROM", "rm", "Romans", Testament.NEW),
        BibleBook("1CO", "1co", "1 Corinthians", Testament.NEW),
        BibleBook("2CO", "2co", "2 Corinthians", Testament.NEW),
        BibleBook("GAL", "gl", "Galatians", Testament.NEW),
        BibleBook("EPH", "eph", "Ephesians", Testament.NEW),
        BibleBook("PHP", "ph", "Philippians", Testament.NEW),
        BibleBook("COL", "cl", "Colossians", Testament.NEW),
        BibleBook("1TH", "1ts", "1 Thessalonians", Testament.NEW),
        BibleBook("2TH", "2ts", "2 Thessalonians", Testament.NEW),
        BibleBook("1TI", "1tm", "1 Timothy", Testament.NEW),
        BibleBook("2TI", "2tm", "2 Timothy", Testament.NEW),
        BibleBook("TIT", "tt", "Titus", Testament.NEW),
        BibleBook("PHM", "phm", "Philemon", Testament.NEW),
        BibleBook("HEB", "hb", "Hebrews", Testament.NEW),
        BibleBook("JAS", "jm", "James", Testament.NEW),
        BibleBook("1PE", "1pe", "1 Peter", Testament.NEW),
        BibleBook("2PE", "2pe", "2 Peter", Testament.NEW),
        BibleBook("1JN", "1jo", "1 John", Testament.NEW),
        BibleBook("2JN", "2jo", "2 John", Testament.NEW),
        BibleBook("3JN", "3jo", "3 John", Testament.NEW),
        BibleBook("JUD", "jd", "Jude", Testament.NEW),
        BibleBook("REV", "re", "Revelation", Testament.NEW)
    )

    val allBooks: List<BibleBook> = oldTestament + newTestament

    fun booksFor(testament: Testament?): List<BibleBook> = when (testament) {
        Testament.OLD -> oldTestament
        Testament.NEW -> newTestament
        null -> allBooks
    }

    fun byAbbrev(abbrev: String): BibleBook? = allBooks.firstOrNull { it.abbrev == abbrev }
}
