package com.loki.deni.ui.navigation

object Routes {
    const val WELCOME = "welcome"
    const val AUTH_PHONE = "auth_phone"
    const val AUTH_PIN_NEW = "auth_pin_new/{phone}"
    const val AUTH_PROFILE_SETUP = "auth_profile_setup"
    const val AUTH_SUCCESS = "auth_success"

    const val HOME = "home"
    const val LOANS = "loans"
    const val TRANSACTIONS = "transactions"
    const val PROFILE = "profile"

    const val LOAN_DETAIL = "loan_detail/{loanId}"
    const val BORROW_REVIEW = "borrow_review/{amount}/{tenure}/{loanType}"
    const val BORROW_SUCCESS = "borrow_success/{loanId}"
    const val REPAY = "repay/{loanId}"
    const val LOAN_SCHEDULE = "loan_schedule/{loanId}"
    const val REPAY_SUCCESS = "repay_success/{loanId}/{receiptRef}"

    const val EDIT_PROFILE = "edit_profile"
    const val PERSONAL_INFO = "personal_info"
    const val PAYMENT_METHODS = "payment_methods"
    const val KYC = "kyc"
    const val PRICING = "pricing"
    const val SETTINGS = "settings"
    const val SUPPORT = "support"
    const val SECURITY = "security"
    const val CLOSE_ACCOUNT = "close_account"
    const val NOTIFICATIONS = "notifications"
    const val RECEIPT = "receipt/{paymentId}"

    fun authPinNew(phone: String): String = "auth_pin_new/$phone"
    fun loanDetail(loanId: Int): String = "loan_detail/$loanId"
    fun borrowReview(amount: Int, tenure: Int, loanType: String): String = "borrow_review/$amount/$tenure/$loanType"
    fun borrowSuccess(loanId: Int): String = "borrow_success/$loanId"
    fun repay(loanId: Int): String = "repay/$loanId"
    fun loanSchedule(loanId: Int): String = "loan_schedule/$loanId"
    fun repaySuccess(loanId: Int, receiptRef: String): String = "repay_success/$loanId/$receiptRef"
    fun receipt(paymentId: Int): String = "receipt/$paymentId"
}
