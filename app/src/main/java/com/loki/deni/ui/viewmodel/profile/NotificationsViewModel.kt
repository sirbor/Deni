package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.ui.model.DeniNotification
import com.loki.deni.ui.model.NotifType
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<DeniNotification>>(emptyList())
    val notifications: StateFlow<List<DeniNotification>> = _notifications.asStateFlow()
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.currentUserId.collectLatest { userId ->
                if (userId.isNullOrBlank()) {
                    _notifications.value = emptyList()
                    _unreadCount.value = 0
                    return@collectLatest
                }
                loadNotifications()
                while (isActive) {
                    delay(30_000)
                    loadNotifications()
                }
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) {
                _notifications.value = emptyList()
                _unreadCount.value = 0
                return@launch
            }
            val tx = withContext(Dispatchers.IO) {
                repository.getTransactions(userId).first()
            }
            val mapped = tx.take(20).map { t ->
                DeniNotification(
                    id = t.transId,
                    type = when {
                        t.type.equals("debit", true) -> NotifType.REPAYMENT
                        t.status.equals("paid", true) -> NotifType.APPROVAL
                        else -> NotifType.SYSTEM
                    },
                    title = t.title.ifBlank { "Account update" },
                    body = "KES ${t.amount.toInt()} ${if (t.type.equals("debit", true)) "debited" else "credited"}",
                    timestamp = SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH).format(Date(t.timestamp)),
                    createdAt = t.timestamp,
                    refId = t.loanId ?: t.transId,
                    isRead = t.status.equals("completed", true),
                    iconEmoji = if (t.type.equals("debit", true)) "PAY" else "APP",
                )
            }
            _notifications.value = mapped
            _unreadCount.value = mapped.count { !it.isRead }
        }
    }

    fun markAsRead(id: Int) {
        _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0
    }

    fun deleteNotification(id: Int) {
        _notifications.value = _notifications.value.filterNot { it.id == id }
    }

    fun clearAll() {
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}
