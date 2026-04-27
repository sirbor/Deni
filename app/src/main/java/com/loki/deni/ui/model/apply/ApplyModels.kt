package com.loki.deni.ui.model

enum class LoanPurpose {
    PERSONAL,
    BUSINESS,
    EMERGENCY,
}

enum class EmploymentStatus {
    EMPLOYED,
    SELF_EMPLOYED,
    BUSINESS_OWNER,
    FREELANCER,
    STUDENT,
}

data class PersonalDetails(
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val nationalId: String = "",
    val phone: String = "",
    val county: String = "",
)

data class EmploymentDetails(
    val status: EmploymentStatus = EmploymentStatus.EMPLOYED,
    val employerName: String = "",
    val monthlyIncome: Int = 0,
    val mpesaNumber: String = "",
)

data class LoanApplication(
    val personal: PersonalDetails,
    val employment: EmploymentDetails,
    val purpose: LoanPurpose,
    val notes: String,
    val requestedAmount: Int,
    val tenureMonths: Int,
)

data class CreditOffer(
    val creditScore: Int,
    val scoreLabel: String,
    val approvedLimit: Int,
    val minAmount: Int,
    val interestRate: Double,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalRepayment: Double,
)

sealed interface ApplyUiState {
    data object Idle : ApplyUiState
    data object Validating : ApplyUiState
    data object AnalyzingCredit : ApplyUiState
    data class OfferReady(val offer: CreditOffer) : ApplyUiState
    data class Error(val msg: String) : ApplyUiState
}
