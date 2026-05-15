package com.example.raktha_vahini

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.raktha_vahini.databinding.ActivityMainBinding
import com.example.raktha_vahini.repository.DonorRepository
import com.example.raktha_vahini.model.Donor
import com.example.raktha_vahini.ui.activity.RegistrationActivity
import com.example.raktha_vahini.ui.activity.ProfileActivity
import com.example.raktha_vahini.ui.adapter.DonorAdapter
import com.example.raktha_vahini.viewmodel.DonorViewModel
import com.example.raktha_vahini.viewmodel.DonorViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DonorViewModel
    private lateinit var adapter: DonorAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val repository = DonorRepository(this)
        val factory = DonorViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DonorViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        checkLocationPermission()
        
        binding.fabRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        binding.cvEmergency.setOnClickListener {
            showLoading(true)
            viewModel.filterDonors("All", true, 10)
            Snackbar.make(binding.root, R.string.nearest_eligible_donors, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.loadDonors()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getUserLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setUserLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DonorAdapter(emptyList())
        binding.rvDonors.layoutManager = LinearLayoutManager(this)
        binding.rvDonors.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.donors.observe(this) { donors ->
            showLoading(false)
            updateUIState(donors)
            adapter.updateData(donors)
        }
        viewModel.filteredDonors.observe(this) { donors ->
            showLoading(false)
            updateUIState(donors)
            adapter.updateData(donors)
        }
        viewModel.dashboardStats.observe(this) { stats ->
            binding.tvTotalDonorsCount.text = stats.first.toString()
            binding.tvEligibleDonorsCount.text = stats.second.toString()
            binding.tvNearbyDonorsCount.text = stats.third.toString()
        }
    }

    private fun updateUIState(donors: List<Donor>) {
        if (donors.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvDonors.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvDonors.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDonors() // Refresh list after registration
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, com.example.raktha_vahini.ui.activity.SearchActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
