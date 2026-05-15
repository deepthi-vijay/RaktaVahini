package com.example.raktha_vahini.model

data class Donor(
    val id: Int,
    val name: String,
    val bloodGroup: String,
    val location: String,
    var lastDonationDate: String, // Changed to var for updates
    var isAvailable: Boolean,     // Changed to var for updates
    val phoneNumber: String = "9876543210",
    val distance: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
