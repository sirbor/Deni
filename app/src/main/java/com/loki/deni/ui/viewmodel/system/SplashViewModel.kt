package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    suspend fun resolveNextRoute(): String {
        val isLoggedIn = preferences.isLoggedIn.first()
        return if (isLoggedIn) Routes.HOME else Routes.WELCOME
    }
}
