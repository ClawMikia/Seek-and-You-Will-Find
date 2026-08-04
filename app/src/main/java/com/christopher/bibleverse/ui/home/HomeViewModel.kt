package com.christopher.bibleverse.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.christopher.bibleverse.alarm.AlarmScheduler
import com.christopher.bibleverse.data.local.AlarmState
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
    val alarmState: LiveData<AlarmState> = repository.alarmState.asLiveData()

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

    fun setAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setAlarm(hour, minute)
            alarmScheduler.schedule(hour, minute)
        }
    }

    fun clearAlarm() {
        viewModelScope.launch {
            repository.clearAlarm()
            alarmScheduler.cancel()
        }
    }
}
