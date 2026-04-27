package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SummaryViewModel : ViewModel() {
    private val _acceptedTerms = MutableStateFlow(false)
    val acceptedTerms = _acceptedTerms.asStateFlow()

    fun setAccepted(accepted: Boolean) {
        _acceptedTerms.value = accepted
    }
}
