package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    val darkModeEnabled: StateFlow<Boolean> = preferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleDarkMode() {
        viewModelScope.launch {
            preferences.setDarkMode(!darkModeEnabled.value)
        }
    }
}
