package com.example.raktha_vahini.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.raktha_vahini.databinding.ActivitySearchBinding
import com.example.raktha_vahini.repository.DonorRepository
import com.example.raktha_vahini.ui.adapter.DonorAdapter
import com.example.raktha_vahini.viewmodel.DonorViewModel
import com.example.raktha_vahini.viewmodel.DonorViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: DonorViewModel
    private lateinit var adapter: DonorAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val repository = DonorRepository(this)
        val factory = DonorViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DonorViewModel::class.java]

        setupToolbar()
        setupFilters()
        setupRecyclerView()
        setupObservers()
        checkLocationPermission()

        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.loadDonors()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setUserLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupFilters() {
        val bloodGroups = listOf(getString(com.example.raktha_vahini.R.string.all), "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
        val adapterBloodGroup = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroups)
        binding.spinnerBloodGroup.setAdapter(adapterBloodGroup)
        binding.spinnerBloodGroup.setText(getString(com.example.raktha_vahini.R.string.all), false)

        val distances = listOf(getString(com.example.raktha_vahini.R.string.all), "10 km", "20 km", "50 km")
        val adapterDistance = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, distances)
        binding.spinnerDistance.setAdapter(adapterDistance)
        binding.spinnerDistance.setText(getString(com.example.raktha_vahini.R.string.all), false)

        binding.spinnerBloodGroup.setOnItemClickListener { _, _, _, _ -> applyFilters() }
        binding.spinnerDistance.setOnItemClickListener { _, _, _, _ -> applyFilters() }
        binding.switchEligible.setOnCheckedChangeListener { _, _ -> applyFilters() }
    }

    private fun applyFilters() {
        val bloodGroup = binding.spinnerBloodGroup.text.toString()
        val showOnlyEligible = binding.switchEligible.isChecked
        val maxDistance = when (binding.spinnerDistance.text.toString()) {
            "10 km" -> 10
            "20 km" -> 20
            "50 km" -> 50
            else -> -1
        }
        val allText = getString(com.example.raktha_vahini.R.string.all)
        viewModel.filterDonors(bloodGroup, showOnlyEligible, maxDistance, allText)
        
        // Ensure UI updates if list is empty
        viewModel.filteredDonors.observe(this) { donors ->
            if (donors.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DonorAdapter(emptyList())
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.filteredDonors.observe(this) { donors ->
            adapter.updateData(donors)
            if (donors.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE
            }
        }
    }
}
