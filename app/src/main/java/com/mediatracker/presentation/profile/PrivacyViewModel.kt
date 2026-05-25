package com.mediatracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.firestore.FirestoreDataSource
import com.mediatracker.data.firestore.PrivacySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
) : ViewModel() {

    private val _settings = MutableStateFlow(PrivacySettings())
    val settings: StateFlow<PrivacySettings> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            firestoreDataSource.getPrivacySettings()
                .onSuccess { _settings.value = it }
                .onFailure { Timber.w(it, "Failed to load privacy settings, using defaults") }
            _isLoading.value = false
        }
    }

    fun onPublicProfileChanged(enabled: Boolean) {
        updateSettings(_settings.value.copy(publicProfile = enabled))
    }

    fun onShowStatsChanged(enabled: Boolean) {
        updateSettings(_settings.value.copy(showStats = enabled))
    }

    fun onShowLibraryChanged(enabled: Boolean) {
        updateSettings(_settings.value.copy(showLibrary = enabled))
    }

    fun onShareActivityChanged(enabled: Boolean) {
        updateSettings(_settings.value.copy(shareActivity = enabled))
    }

    private fun updateSettings(newSettings: PrivacySettings) {
        _settings.value = newSettings
        viewModelScope.launch {
            firestoreDataSource.updatePrivacySettings(newSettings)
                .onFailure { Timber.e(it, "Failed to save privacy settings") }
        }
    }
}
