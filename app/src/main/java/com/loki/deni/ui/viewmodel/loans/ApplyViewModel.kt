package com.loki.deni.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loki.deni.data.local.UserPreferencesStore
import com.loki.deni.domain.repository.DeniRepository
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.model.ApplyUiState
import com.loki.deni.ui.model.CreditOffer
import com.loki.deni.ui.model.EmploymentDetails
import com.loki.deni.ui.model.EmploymentStatus
import com.loki.deni.ui.model.LoanApplication
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.model.LoanPurpose
import com.loki.deni.ui.model.PersonalDetails
import com.loki.deni.util.AuthEvents
import com.loki.deni.util.core.CreditPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ApplyViewModel @Inject constructor(
    private val repository: DeniRepository,
    private val preferences: UserPreferencesStore,
) : ViewModel() {
    private var salaryRange: String = ""
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _uiState = MutableStateFlow<ApplyUiState>(ApplyUiState.Idle)
    val uiState: StateFlow<ApplyUiState> = _uiState.asStateFlow()

    private val _personal = MutableStateFlow(PersonalDetails())
    val personal: StateFlow<PersonalDetails> = _personal.asStateFlow()

    private val _employment = MutableStateFlow(EmploymentDetails())
    val employment: StateFlow<EmploymentDetails> = _employment.asStateFlow()

    private val _selectedPurpose = MutableStateFlow<LoanPurpose?>(null)
    val selectedPurpose: StateFlow<LoanPurpose?> = _selectedPurpose.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedAmount = MutableStateFlow(15000)
    val selectedAmount: StateFlow<Int> = _selectedAmount.asStateFlow()

    private val _selectedTenure = MutableStateFlow(3)
    val selectedTenure: StateFlow<Int> = _selectedTenure.asStateFlow()

    private val _creditOffer = MutableStateFlow<CreditOffer?>(null)
    val creditOffer: StateFlow<CreditOffer?> = _creditOffer.asStateFlow()

    private val _personalErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val personalErrors: StateFlow<Map<String, String>> = _personalErrors.asStateFlow()

    private val _employmentErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val employmentErrors: StateFlow<Map<String, String>> = _employmentErrors.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) return@launch
            val user = repository.getUserProfile(userId).first() ?: return@launch
            val names = user.name.split(" ")
            val displayPhone = when {
                user.phone.startsWith("0") -> user.phone
                user.phone.length == 9 -> "0${user.phone}"
                user.phone.startsWith("254") && user.phone.length >= 12 -> "0${user.phone.removePrefix("254").take(9)}"
                else -> user.phone
            }
            _personal.value = _personal.value.copy(
                firstName = names.firstOrNull().orEmpty(),
                lastName = names.drop(1).joinToString(" "),
                nationalId = "",
                phone = displayPhone,
                dateOfBirth = user.dateOfBirth ?: "",
                county = user.county ?: "",
            )
            _employment.value = _employment.value.copy(
                mpesaNumber = displayPhone,
                employerName = user.employerName ?: "",
                monthlyIncome = user.monthlyIncome ?: 0,
                status = when (user.employmentStatus) {
                    "SELF_EMPLOYED" -> EmploymentStatus.SELF_EMPLOYED
                    "BUSINESS_OWNER" -> EmploymentStatus.BUSINESS_OWNER
                    "FREELANCER" -> EmploymentStatus.FREELANCER
                    "STUDENT" -> EmploymentStatus.STUDENT
                    else -> EmploymentStatus.EMPLOYED
                },
            )
            salaryRange = user.salaryRange.orEmpty()
        }
    }

    fun updatePersonal(field: String, value: String) {
        _personal.value = when (field) {
            "firstName" -> _personal.value.copy(firstName = value)
            "lastName" -> _personal.value.copy(lastName = value)
            "dateOfBirth" -> _personal.value.copy(dateOfBirth = value)
            "nationalId" -> _personal.value.copy(nationalId = value.filter(Char::isDigit))
            "phone" -> _personal.value.copy(phone = value.filter(Char::isDigit).take(10))
            "county" -> _personal.value.copy(county = value)
            else -> _personal.value
        }
        _personalErrors.value = _personalErrors.value - field
    }

    fun updateEmployment(field: String, value: String) {
        _employment.value = when (field) {
            "status" -> _employment.value.copy(status = EmploymentStatus.valueOf(value))
            "employerName" -> _employment.value.copy(employerName = value)
            "monthlyIncome" -> _employment.value.copy(monthlyIncome = value.filter(Char::isDigit).toIntOrNull() ?: 0)
            "mpesaNumber" -> _employment.value.copy(mpesaNumber = value.filter(Char::isDigit).take(10))
            else -> _employment.value
        }
        _employmentErrors.value = _employmentErrors.value - field
    }

    fun selectPurpose(purpose: LoanPurpose) {
        _selectedPurpose.value = purpose
    }

    fun updateNotes(notes: String) {
        _notes.value = notes
    }

    fun updateAmount(amount: Int) {
        _selectedAmount.value = amount
        recalculate()
    }

    fun updateTenure(months: Int) {
        _selectedTenure.value = months
        recalculate()
    }

    fun validatePersonal(): Boolean {
        val p = _personal.value
        val errors = mutableMapOf<String, String>()
        if (p.firstName.isBlank()) errors["firstName"] = "First name is required"
        if (p.lastName.isBlank()) errors["lastName"] = "Last name is required"
        if (!p.nationalId.matches(Regex("^\\d{8}$"))) errors["nationalId"] = "National ID must be exactly 8 digits"
        if (!isValidKePhone(p.phone)) errors["phone"] = "Enter a valid Kenyan phone number"
        if (p.dateOfBirth.isBlank()) errors["dateOfBirth"] = "Date of birth is required"
        if (p.county.isBlank()) errors["county"] = "County is required"
        _personalErrors.value = errors
        return errors.isEmpty()
    }

    fun validateEmployment(): Boolean {
        val e = _employment.value
        val errors = mutableMapOf<String, String>()
        if (e.employerName.isBlank()) errors["employerName"] = "Employer or business name is required"
        if (e.monthlyIncome <= 0) errors["monthlyIncome"] = "Monthly income must be greater than 0"
        if (!isValidKePhone(e.mpesaNumber)) errors["mpesaNumber"] = "Enter a valid Kenyan phone number"
        _employmentErrors.value = errors
        return errors.isEmpty()
    }

    fun goToStep(step: Int) {
        when (_currentStep.value) {
            1 -> if (!validatePersonal()) return
            2 -> if (!validateEmployment()) return
            3 -> {
                if (_selectedPurpose.value == null) {
                    _uiState.value = ApplyUiState.Error("Please select a loan purpose")
                    return
                }
                analyzeCredit()
                return
            }
        }
        _currentStep.value = step.coerceIn(1, 4)
    }

    fun goBack() {
        _currentStep.value = (_currentStep.value - 1).coerceAtLeast(1)
    }

    fun analyzeCredit() {
        viewModelScope.launch {
            _uiState.value = ApplyUiState.AnalyzingCredit
            delay(2000)
            val userId = preferences.currentUserId.first()
            if (userId.isNullOrBlank()) {
                AuthEvents.emitSessionExpired()
                _uiState.value = ApplyUiState.Error("Session expired. Sign in again.")
                return@launch
            }
            val profile = repository.getUserProfile(userId).first()
            val loans = repository.getLoans(userId).first()
            val paidLoans = loans.count { it.isPaid }
            val effectiveSalaryRange = profile?.salaryRange ?: salaryRange
            val creditScore = (profile?.creditScore ?: CreditPolicy.starterScoreForSalaryRange(effectiveSalaryRange))
                .coerceIn(300, 850)
            val approvedLimit = CreditPolicy.resolveApprovedLimit(
                salaryRange = effectiveSalaryRange,
                creditScore = creditScore,
                paidLoansCount = paidLoans,
            )
            val scoreLabel = "${CreditPolicy.scoreBand(creditScore)} Credit Standing"
            val emi = LoanCalculator.calculateEMI(
                principal = _selectedAmount.value.toDouble(),
                annualRate = 0.15,
                tenureMonths = _selectedTenure.value,
            )
            val totalRepayment = LoanCalculator.totalRepayment(_selectedAmount.value.toDouble(), 0.15, _selectedTenure.value)
            val totalInterest = totalRepayment - _selectedAmount.value
            val offer = CreditOffer(
                creditScore = creditScore,
                scoreLabel = scoreLabel,
                approvedLimit = approvedLimit,
                minAmount = 1000,
                interestRate = 0.15,
                monthlyEmi = emi,
                totalInterest = totalInterest,
                totalRepayment = totalRepayment,
            )
            _creditOffer.value = offer
            _uiState.value = ApplyUiState.OfferReady(offer)
            _currentStep.value = 4
        }
    }

    fun recalculate() {
        val offer = _creditOffer.value ?: return
        val emi = LoanCalculator.calculateEMI(_selectedAmount.value.toDouble(), offer.interestRate, _selectedTenure.value)
        val totalRepayment = LoanCalculator.totalRepayment(_selectedAmount.value.toDouble(), offer.interestRate, _selectedTenure.value)
        val totalInterest = totalRepayment - _selectedAmount.value
        val updated = offer.copy(
            monthlyEmi = emi,
            totalInterest = totalInterest,
            totalRepayment = totalRepayment,
        )
        _creditOffer.value = updated
        _uiState.value = ApplyUiState.OfferReady(updated)
    }

    fun validateAndSubmit(): Boolean {
        val validPersonal = validatePersonal()
        val validEmployment = validateEmployment()
        val hasPurpose = _selectedPurpose.value != null
        val validNumbers = _selectedAmount.value > 0 && _selectedTenure.value > 0
        return validPersonal && validEmployment && hasPurpose && validNumbers
    }

    fun buildApplication(): LoanApplication? {
        val purpose = _selectedPurpose.value ?: return null
        return LoanApplication(
            personal = _personal.value,
            employment = _employment.value,
            purpose = purpose,
            notes = _notes.value,
            requestedAmount = _selectedAmount.value,
            tenureMonths = _selectedTenure.value,
        )
    }

    private fun isValidKePhone(phone: String): Boolean {
        val digits = phone.filter(Char::isDigit)
        val normalized = when {
            digits.startsWith("254") && digits.length >= 12 -> "0${digits.removePrefix("254").take(9)}"
            digits.length == 9 && (digits.startsWith("7") || digits.startsWith("1")) -> "0$digits"
            else -> digits
        }
        return normalized.length == 10 && (normalized.startsWith("07") || normalized.startsWith("01"))
    }

}
