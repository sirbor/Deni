package com.loki.deni.util.core

object CreditPolicy {
    fun starterScoreForSalaryRange(salaryRange: String?): Int = when (salaryRange) {
        "0-25k" -> 420
        "25-50k" -> 500
        "50-100k" -> 580
        "above 100k" -> 650
        else -> 500
    }

    fun scoreBand(score: Int): String = when (score.coerceIn(300, 850)) {
        in 700..850 -> "Good"
        in 600..699 -> "Fair"
        else -> "Building"
    }

    fun resolveApprovedLimit(
        salaryRange: String?,
        creditScore: Int,
        paidLoansCount: Int,
    ): Int {
        val growthCycles = (paidLoansCount / 3).coerceAtLeast(0)
        val base = baseLimitForSalaryRange(salaryRange) ?: fallbackLimitForScore(creditScore)
        val increment = growthIncrementForSalaryRange(salaryRange)
        return (base + (growthCycles * increment)).coerceIn(1_000, 15_000)
    }

    private fun baseLimitForSalaryRange(salaryRange: String?): Int? = when (salaryRange) {
        "0-25k" -> 4_000
        "25-50k" -> 8_000
        "50-100k" -> 12_000
        "above 100k" -> 15_000
        else -> null
    }

    private fun growthIncrementForSalaryRange(salaryRange: String?): Int = when (salaryRange) {
        "0-25k" -> 1_200
        "25-50k" -> 1_800
        "50-100k" -> 2_400
        "above 100k" -> 3_000
        else -> 1_200
    }

    private fun fallbackLimitForScore(score: Int): Int = when (score) {
        in 750..850 -> 15_000
        in 600..749 -> 10_000
        else -> 5_000
    }
}
