package com.loki.deni.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AuthEvent {
    data object SessionExpired : AuthEvent
}

object AuthEvents {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emitSessionExpired() {
        _events.tryEmit(AuthEvent.SessionExpired)
    }
}
