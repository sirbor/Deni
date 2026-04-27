package com.loki.deni.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.domain.repository.DeniRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            runCatching {
                val userId = preferences.currentUserId.first().orEmpty()
                if (userId.isBlank()) return@runCatching
                repository.reconcileLoanStatuses(userId)
                // Trigger remote pulls so app cache/state stays warm.
                repository.getUserProfile(userId).first()
                repository.getLoans(userId).first()
                repository.getTransactions(userId).first()
            }.fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() },
            )
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "deni_background_sync"
    }
}
