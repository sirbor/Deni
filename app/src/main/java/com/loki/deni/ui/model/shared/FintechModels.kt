package com.loki.deni.ui.model

enum class NotifType {
    REMINDER,
    APPROVAL,
    OFFER,
    REPAYMENT,
    SYSTEM,
}

data class DeniNotification(
    val id: Int,
    val type: NotifType,
    val title: String,
    val body: String,
    val timestamp: String,
    val createdAt: Long,
    val refId: Int? = null,
    val isRead: Boolean,
    val iconEmoji: String,
)

enum class ScheduleStatus {
    PAID,
    UPCOMING,
    OVERDUE,
}

data class ScheduleItem(
    val installmentNumber: Int,
    val dueDate: String,
    val emiAmount: Double,
    val principal: Double,
    val interest: Double,
    val balance: Double,
    val status: ScheduleStatus,
)

data class ScorePoint(
    val month: String,
    val score: Int,
)

enum class LoyaltyTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
}

data class LoginEvent(
    val device: String,
    val location: String,
    val time: String,
    val isCurrent: Boolean,
)

data class LinkedAccount(
    val id: Int,
    val provider: String,
    val accountLabel: String,
    val maskedNumber: String,
    val isPrimary: Boolean,
)

data class KycChecklistItem(
    val title: String,
    val subtitle: String,
    val verified: Boolean,
)

data class AppLockSettings(
    val pinEnabled: Boolean,
    val biometricsEnabled: Boolean,
    val timeoutMinutes: Int,
)

data class PaymentReceiptData(
    val txId: Int,
    val amount: Int,
    val method: String,
    val reference: String,
    val timestamp: String,
    val remainingBalance: Int,
)

data class PaymentFeedItem(
    val paymentId: Int,
    val loanId: String,
    val title: String,
    val date: String,
    val amountKes: Int,
    val isRepayment: Boolean,
    val statusLabel: String,
)

data class UpcomingPaymentItem(
    val id: Int,
    val date: String,
    val daysAway: Int,
    val title: String,
    val loanId: String,
    val amountKes: Int,
)

data class BarEntry(
    val month: String,
    val amount: Int,
    val isCurrent: Boolean,
)
