package com.example.raktha_vahini.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.raktha_vahini.model.Donor
import com.example.raktha_vahini.repository.DonorRepository
import com.example.raktha_vahini.utils.DateUtils
import com.example.raktha_vahini.utils.LocationUtils
import java.text.SimpleDateFormat
import java.util.*

class DonorViewModel(private val repository: DonorRepository) : ViewModel() {

    private val _donors = MutableLiveData<List<Donor>>()
    val donors: LiveData<List<Donor>> get() = _donors

    private val _filteredDonors = MutableLiveData<List<Donor>>()
    val filteredDonors: LiveData<List<Donor>> get() = _filteredDonors

    private val _currentUser = MutableLiveData<Donor?>()
    val currentUser: LiveData<Donor?> get() = _currentUser

    private var userLat: Double = 0.0
    private var userLon: Double = 0.0

    private val _dashboardStats = MutableLiveData<Triple<Int, Int, Int>>() // Total, Eligible, Nearby
    val dashboardStats: LiveData<Triple<Int, Int, Int>> get() = _dashboardStats

    fun loadDonors() {
        val allDonors = repository.getAllDonors()
        updateDashboardStats(allDonors)
        updateDistancesAndPost(allDonors)
    }

    private fun updateDashboardStats(allDonors: List<Donor>) {
        val total = allDonors.size
        val eligible = allDonors.count { DateUtils.isEligibleToDonate(it.lastDonationDate) }
        val nearby = allDonors.count { donor ->
            val dist = if (userLat != 0.0 && userLon != 0.0) {
                LocationUtils.calculateDistance(userLat, userLon, donor.latitude, donor.longitude).toInt()
            } else 0
            dist <= 10 && dist != 0
        }
        _dashboardStats.value = Triple(total, eligible, nearby)
    }

    fun setUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        loadDonors()
    }

    private fun updateDistancesAndPost(allDonors: List<Donor>) {
        val donorsWithDistances = allDonors.map { donor ->
            val dist = if (userLat != 0.0 && userLon != 0.0) {
                LocationUtils.calculateDistance(userLat, userLon, donor.latitude, donor.longitude).toInt()
            } else {
                0
            }
            donor.copy(distance = dist)
        }.filter { it.isAvailable } // Only show available donors in general list
        
        _donors.value = donorsWithDistances
        _filteredDonors.value = donorsWithDistances
    }

    fun loadCurrentUser() {
        _currentUser.value = repository.getCurrentUser()
    }

    fun registerDonor(name: String, bloodGroup: String, location: String, lastDonationDate: String, phoneNumber: String) {
        val newDonor = Donor(
            id = (System.currentTimeMillis() % 10000).toInt(),
            name = name,
            bloodGroup = bloodGroup,
            location = location,
            lastDonationDate = lastDonationDate,
            isAvailable = true,
            phoneNumber = phoneNumber,
            distance = 0,
            latitude = 0.0,
            longitude = 0.0
        )
        repository.saveDonor(newDonor)
        loadDonors()
    }

    fun updateAvailability(isAvailable: Boolean) {
        val user = repository.getCurrentUser() ?: return
        user.isAvailable = isAvailable
        repository.saveCurrentUser(user)
        _currentUser.value = user
    }

    fun markDonated() {
        val user = repository.getCurrentUser() ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        user.lastDonationDate = sdf.format(Date())
        user.isAvailable = false // Automatically unavailable after donation
        repository.saveCurrentUser(user)
        _currentUser.value = user
        loadDonors() // Refresh dashboard and main list instantly
    }

    fun filterDonors(bloodGroup: String, showOnlyEligible: Boolean, maxDistance: Int, allText: String = "All") {
        val allDonors = repository.getAllDonors()
        val filtered = allDonors.filter { donor ->
            val dist = if (userLat != 0.0 && userLon != 0.0) {
                LocationUtils.calculateDistance(userLat, userLon, donor.latitude, donor.longitude).toInt()
            } else {
                -1
            }
            
            val matchBloodGroup = if (bloodGroup == allText) true else donor.bloodGroup == bloodGroup
            val matchEligibility = if (showOnlyEligible) DateUtils.isEligibleToDonate(donor.lastDonationDate) else true
            val matchDistance = if (maxDistance == -1) true else dist <= maxDistance && dist != -1
            
            // STRICT ENFORCEMENT: Only show donors who are available
            val matchAvailable = donor.isAvailable
            
            matchBloodGroup && matchEligibility && matchDistance && matchAvailable
        }.map { donor ->
            val dist = if (userLat != 0.0 && userLon != 0.0) {
                LocationUtils.calculateDistance(userLat, userLon, donor.latitude, donor.longitude).toInt()
            } else {
                0
            }
            donor.copy(distance = dist)
        }
        _filteredDonors.value = filtered
    }
}
