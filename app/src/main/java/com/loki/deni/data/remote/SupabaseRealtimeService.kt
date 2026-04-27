package com.loki.deni.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class SupabaseRealtimeService @Inject constructor() {
    private val okHttp = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var refCounter = 1
    private var currentUserId: String? = null
    private var currentToken: String? = null
    private val callbacks = LinkedHashSet<(String) -> Unit>()

    fun connect(userId: String, jwt: String, onTableChanged: (String) -> Unit) {
        callbacks.add(onTableChanged)
        if (currentUserId == userId && currentToken == jwt && webSocket != null) return
        currentUserId = userId
        currentToken = jwt
        webSocket?.close(1000, "reconnect")
        val request = Request.Builder()
            .url("wss://gigxwidiwfteigolfpma.supabase.co/realtime/v1/websocket?apikey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdpZ3h3aWRpd2Z0ZWlnb2xmcG1hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxMzcxNjYsImV4cCI6MjA5MjcxMzE2Nn0.-vNtOX1K-aRn5B6NlSTyFQKXzFnRw5NrvhoarGpkj9s&vsn=1.0.0")
            .build()
        webSocket = okHttp.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    joinTable(webSocket, "users", userId, jwt)
                    joinTable(webSocket, "loans", userId, jwt)
                    joinTable(webSocket, "transactions", userId, jwt)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (!text.contains("\"event\":\"postgres_changes\"")) return
                    val table = extractTableName(text) ?: return
                    callbacks.forEach { it(table) }
                }
            },
        )
    }

    private fun joinTable(webSocket: WebSocket, table: String, userId: String, jwt: String) {
        val filter = if (table == "users") "id=eq.$userId" else "user_id=eq.$userId"
        val payload = JSONObject()
            .put("config", JSONObject()
                .put("broadcast", JSONObject().put("self", false))
                .put("presence", JSONObject().put("key", ""))
                .put(
                    "postgres_changes",
                    JSONArray().put(
                        JSONObject()
                            .put("event", "*")
                            .put("schema", "public")
                            .put("table", table)
                            .put("filter", filter),
                    ),
                ),
            )
            .put("access_token", jwt)
        val envelope = JSONObject()
            .put("topic", "realtime:public:$table")
            .put("event", "phx_join")
            .put("payload", payload)
            .put("ref", (refCounter++).toString())
        webSocket.send(envelope.toString())
    }

    private fun extractTableName(message: String): String? {
        return runCatching {
            val json = JSONObject(message)
            json.optString("topic")
                .substringAfterLast(":")
                .ifBlank { null }
        }.getOrNull()
    }
}
