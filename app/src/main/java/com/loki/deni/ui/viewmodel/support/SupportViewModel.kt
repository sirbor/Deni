package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class SupportMessage(
    val text: String,
    val fromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)

data class SupportTicketItem(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: Long,
)

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private val _messages = MutableStateFlow(
        listOf(
            SupportMessage("Hello Dominic, welcome to Deni Support.", fromUser = false),
            SupportMessage("How can we help you today?", fromUser = false),
        ),
    )
    val messages: StateFlow<List<SupportMessage>> = _messages.asStateFlow()

    private val _tickets = MutableStateFlow<List<SupportTicketItem>>(emptyList())
    val tickets: StateFlow<List<SupportTicketItem>> = _tickets.asStateFlow()

    init {
        observeTickets()
    }

    fun sendMessage(input: String) {
        val text = input.trim()
        if (text.isBlank()) return
        val now = System.currentTimeMillis()
        _messages.value = _messages.value + SupportMessage(text = text, fromUser = true, timestamp = now)
        _messages.value = _messages.value + SupportMessage(
            text = "Thanks, your message has been received. A support agent will reply shortly.",
            fromUser = false,
            timestamp = now + 1,
        )
    }

    fun submitTicket(category: String, subject: String, description: String): Boolean {
        val normalizedSubject = subject.trim()
        val normalizedDesc = description.trim()
        if (normalizedSubject.length < 4 || normalizedDesc.length < 8) return false
        val item = SupportTicketItem(
            id = "TK-${System.currentTimeMillis().toString().takeLast(6)}",
            category = category,
            subject = normalizedSubject,
            description = normalizedDesc,
            status = "Open",
            createdAt = System.currentTimeMillis(),
        )
        val updated = listOf(item) + _tickets.value
        _tickets.value = updated
        persistTickets(updated)
        return true
    }

    private fun observeTickets() {
        viewModelScope.launch {
            preferences.supportTicketsRaw.collect { raw ->
                if (raw.isBlank()) {
                    _tickets.value = emptyList()
                } else {
                    _tickets.value = decodeTickets(raw)
                }
            }
        }
    }

    private fun persistTickets(tickets: List<SupportTicketItem>) {
        viewModelScope.launch {
            preferences.setSupportTicketsRaw(encodeTickets(tickets))
        }
    }

    private fun encodeTickets(tickets: List<SupportTicketItem>): String {
        val arr = JSONArray()
        tickets.forEach { ticket ->
            arr.put(
                JSONObject()
                    .put("id", ticket.id)
                    .put("category", ticket.category)
                    .put("subject", ticket.subject)
                    .put("description", ticket.description)
                    .put("status", ticket.status)
                    .put("createdAt", ticket.createdAt),
            )
        }
        return arr.toString()
    }

    private fun decodeTickets(raw: String): List<SupportTicketItem> {
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        SupportTicketItem(
                            id = obj.optString("id"),
                            category = obj.optString("category"),
                            subject = obj.optString("subject"),
                            description = obj.optString("description"),
                            status = obj.optString("status", "Open"),
                            createdAt = obj.optLong("createdAt"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
