package com.example.raktha_vahini.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private fun getDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun isEligibleToDonate(lastDonationDateStr: String): Boolean {
        return try {
            val lastDonationDate = getDateFormat().parse(lastDonationDateStr) ?: return false
            val currentDate = Date()
            val diffInMillies = currentDate.time - lastDonationDate.time
            val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
            diffInDays > 90
        } catch (e: Exception) {
            false
        }
    }
}
