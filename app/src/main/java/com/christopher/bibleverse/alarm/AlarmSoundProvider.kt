package com.christopher.bibleverse.alarm

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri

/**
 * Exposes the bundled `alarm.mp3` asset as a `content://` URI so the system's
 * notification sound service can read it. A FileProvider URI can't be used
 * here: the system process has no read grant on FileProvider URIs and will
 * silently skip the sound. This exported, read-only provider serves the asset
 * directly, which the sound service can open.
 */
class AlarmSoundProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? =
        context?.assets?.openFd(ASSET_NAME)

    override fun getType(uri: Uri): String = "audio/mpeg"

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    companion object {
        private const val ASSET_NAME = "alarm.mp3"
        const val AUTHORITY = "com.christopher.bibleverse.alarmsound"
        const val SOUND_URI = "content://$AUTHORITY/alarm"
    }
}
