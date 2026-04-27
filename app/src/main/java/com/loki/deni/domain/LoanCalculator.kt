package com.loki.deni.domain

import kotlin.math.pow

object LoanCalculator {
    private const val ANNUAL_INTEREST_RATE = 0.15
    private const val SIXTY_DAY_RATE = 0.20
    private const val PROCESSING_FEE_RATE = 0.03

    // EMI formula: P × r × (1+r)^n / ((1+r)^n - 1)
    fun monthlyPayment(principal: Double, tenureMonths: Int): Double {
        return calculateEMI(principal, ANNUAL_INTEREST_RATE, tenureMonths)
    }

    // EMI formula: P × r × (1+r)^n / ((1+r)^n - 1)
    fun calculateEMI(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        if (principal <= 0.0 || tenureMonths <= 0) return 0.0
        val monthlyRate = annualRate / 12.0
        val growth = (1 + monthlyRate).pow(tenureMonths)
        return principal * monthlyRate * growth / (growth - 1)
    }

    fun totalRepayment(principal: Double, tenureMonths: Int): Double {
        return monthlyPayment(principal, tenureMonths) * tenureMonths
    }

    fun totalRepayment(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        return calculateEMI(principal, annualRate, tenureMonths) * tenureMonths
    }

    fun totalInterest(principal: Double, tenureMonths: Int): Double {
        return totalRepayment(principal, tenureMonths) - principal
    }

    fun totalInterest(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        return totalRepayment(principal, annualRate, tenureMonths) - principal
    }

    fun annualInterestRate(): Double = ANNUAL_INTEREST_RATE

    fun proratedRateForDays(days: Int): Double {
        val normalizedDays = days.coerceAtLeast(1)
        return SIXTY_DAY_RATE * (normalizedDays / 60.0)
    }

    fun totalRepaymentForDays(principal: Double, days: Int): Double {
        val interest = interestForDays(principal, days)
        val fee = processingFeeForAmount(principal)
        return principal + interest + fee
    }

    fun interestForDays(principal: Double, days: Int): Double {
        val rate = proratedRateForDays(days)
        return principal * rate
    }

    fun processingFeeForAmount(principal: Double): Double {
        return principal * PROCESSING_FEE_RATE
    }

    fun maxTenureDaysForUserLimit(userLimit: Int): Int = when {
        userLimit <= 4_000 -> 14
        userLimit <= 8_000 -> 30
        userLimit > 15_000 -> 60
        else -> 45
    }

    fun allowedTenureDaysFor(userLimit: Int, principal: Int): List<Int> {
        val limitMax = maxTenureDaysForUserLimit(userLimit)
        val amountMax = if (principal < 8_000) 30 else 60
        val maxDays = minOf(limitMax, amountMax)
        return listOf(14, 30, 45, 60).filter { it <= maxDays }
    }
}
