package com.christopher.bibleverse.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.christopher.bibleverse.alarm.AlarmScheduler
import com.christopher.bibleverse.data.local.Reminder
import com.christopher.bibleverse.data.model.Testament
import com.christopher.bibleverse.data.model.VerseDetail
import com.christopher.bibleverse.data.repository.VerseRepository
import com.christopher.bibleverse.util.Resource
import com.christopher.bibleverse.widget.BibleVerseWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VerseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val alarmScheduler = AlarmScheduler(context)

    val favoriteVerse: LiveData<VerseDetail?> = repository.observeFavorite().asLiveData()
    val reminders: LiveData<List<Reminder>> = repository.reminders.asLiveData()

    private val _fetchResult = MutableLiveData<Resource<VerseDetail>?>(null)
    val fetchResult: LiveData<Resource<VerseDetail>?> = _fetchResult

    fun fetchVerse(testament: Testament?, bookId: String?, chapter: Int?, verseNumber: Int?) {
        _fetchResult.value = Resource.Loading
        viewModelScope.launch {
            _fetchResult.value = repository.fetchVerse(testament, bookId, chapter, verseNumber)
        }
    }

    suspend fun chapterCount(bookId: String): Int = repository.chapterCount(bookId)

    suspend fun verseCount(bookId: String, chapter: Int): Int = repository.verseCount(bookId, chapter)

    fun clearFetchResult() {
        _fetchResult.value = null
    }

    fun saveFavorite(verse: VerseDetail) {
        viewModelScope.launch {
            repository.saveFavorite(verse)
            BibleVerseWidgetProvider.requestUpdateAll(context)
        }
    }

    fun addReminder(hour: Int, minute: Int) {
        viewModelScope.launch {
            val reminder = repository.addReminder(hour, minute)
            alarmScheduler.schedule(reminder)
        }
    }

    fun editReminder(id: Long, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateReminder(id, hour, minute)
            // Re-schedule with the same id: the request code is stable, so the
            // existing one-shot alarm is replaced with the new time.
            alarmScheduler.schedule(Reminder(id, hour, minute))
        }
    }

    fun removeReminder(id: Long) {
        viewModelScope.launch {
            repository.removeReminder(id)
            alarmScheduler.cancel(id)
        }
    }
}
