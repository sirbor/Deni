package com.loki.deni.ui.navigation

sealed class DeniRoutes(val route: String) {
    data object Splash : DeniRoutes("splash")
    data object Onboarding : DeniRoutes("onboarding")
    data object AuthPhone : DeniRoutes("auth_phone")
    data object AuthOtp : DeniRoutes("auth_otp/{phone}") {
        fun createRoute(phone: String): String = "auth_otp/$phone"
    }

    data object Home : DeniRoutes("home")
    data object Loans : DeniRoutes("loans")
    data object Transactions : DeniRoutes("transactions?focus={focus}") {
        fun createRoute(focus: String = ""): String = "transactions?focus=$focus"
    }
    data object TransactionsFeed : DeniRoutes("transactions_feed")
    data object Apply : DeniRoutes("apply")
    data object BorrowAmount : DeniRoutes("borrow_amount")
    data object BorrowTenure : DeniRoutes("borrow_tenure/{amount}") {
        fun createRoute(amount: Int): String = "borrow_tenure/$amount"
    }
    data object BorrowSummary : DeniRoutes("borrow_summary/{amount}/{tenure}") {
        fun createRoute(amount: Int, tenure: Int): String = "borrow_summary/$amount/$tenure"
    }
    data object BorrowReview : DeniRoutes("borrow_review/{amount}/{tenure}/{loanType}") {
        fun createRoute(amount: Int, tenure: Int, loanType: String): String = "borrow_review/$amount/$tenure/$loanType"
    }
    data object LoanSuccess : DeniRoutes("borrow_success/{loanId}") {
        fun createRoute(loanId: Int): String = "borrow_success/$loanId"
    }
    data object Summary : DeniRoutes("summary/{amount}/{tenure}") {
        fun createRoute(amount: Int, tenure: Int): String = "summary/$amount/$tenure"
    }

    data object Success : DeniRoutes("success/{amount}") {
        fun createRoute(amount: Int): String = "success/$amount"
    }

    data object Repay : DeniRoutes("repay")
    data object RepayByLoan : DeniRoutes("repay/{loanId}") {
        fun createRoute(loanId: Int): String = "repay/$loanId"
    }
    data object RepayAmount : DeniRoutes("repay_amount/{loanId}") {
        fun createRoute(loanId: Int): String = "repay_amount/$loanId"
    }
    data object RepayConfirm : DeniRoutes("repay_confirm/{loanId}/{amount}") {
        fun createRoute(loanId: Int, amount: Int): String = "repay_confirm/$loanId/$amount"
    }
    data object RepaySuccess : DeniRoutes("repay_success/{loanId}/{receiptRef}") {
        fun createRoute(loanId: Int, receiptRef: String): String = "repay_success/$loanId/$receiptRef"
    }
    data object History : DeniRoutes("history")
    data object HistoryDetail : DeniRoutes("history_detail/{loanId}") {
        fun createRoute(loanId: Int): String = "history_detail/$loanId"
    }
    data object Profile : DeniRoutes("profile")
    data object Notifications : DeniRoutes("notifications")
    data object LoanDetail : DeniRoutes("loan_detail/{loanId}") {
        fun createRoute(loanId: Int): String = "loan_detail/$loanId"
    }
    data object LoanSchedule : DeniRoutes("loan_schedule/{loanId}") {
        fun createRoute(loanId: Int): String = "loan_schedule/$loanId"
    }
    data object Insights : DeniRoutes("insights")
    data object PaymentReceipt : DeniRoutes("payment_receipt/{txId}") {
        fun createRoute(txId: Int): String = "payment_receipt/$txId"
    }
    data object Receipt : DeniRoutes("receipt/{paymentId}") {
        fun createRoute(paymentId: Int): String = "receipt/$paymentId"
    }
    data object KycStatus : DeniRoutes("kyc_status")
    data object Kyc : DeniRoutes("kyc")
    data object Referral : DeniRoutes("referral")
    data object LinkedAccounts : DeniRoutes("linked_accounts")
    data object ChangePin : DeniRoutes("change_pin")
    data object LoginActivity : DeniRoutes("login_activity")
    data object AppLock : DeniRoutes("app_lock")
    data object Support : DeniRoutes("support")
    data object SupportChat : DeniRoutes("support_chat")
    data object SupportTicket : DeniRoutes("support_ticket")
    data object Settings : DeniRoutes("settings")
    data object LoanTopup : DeniRoutes("loan_topup/{loanId}") {
        fun createRoute(loanId: Int): String = "loan_topup/$loanId"
    }
    data object LoanTopupReview : DeniRoutes("loan_topup_review/{loanId}/{amount}/{days}/{purpose}") {
        fun createRoute(loanId: Int, amount: Int, days: Int, purpose: String): String =
            "loan_topup_review/$loanId/$amount/$days/${android.net.Uri.encode(purpose)}"
    }
}
