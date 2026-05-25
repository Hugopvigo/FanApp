package com.mediatracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.NotificationEntity
import com.mediatracker.data.local.NotificationPreferencesRepository
import com.mediatracker.data.local.NotificationPreferencesRepository.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val preferencesRepository: NotificationPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<NotificationPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationPreferences())

    val notifications: StateFlow<List<NotificationEntity>> = notificationDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onPushEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updatePushEnabled(enabled) }
    }

    fun onNewReleasesChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateNewReleasesEnabled(enabled) }
    }

    fun onRecommendationsChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateRecommendationsEnabled(enabled) }
    }

    fun onWeeklySummaryChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateWeeklySummaryEnabled(enabled) }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch { notificationDao.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { notificationDao.markAllAsRead() }
    }
}
