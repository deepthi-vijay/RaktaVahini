package com.example.raktha_vahini.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.raktha_vahini.R
import com.example.raktha_vahini.databinding.ActivityRegistrationBinding
import com.example.raktha_vahini.model.Donor
import com.example.raktha_vahini.repository.DonorRepository
import com.example.raktha_vahini.viewmodel.DonorViewModel
import com.example.raktha_vahini.viewmodel.DonorViewModelFactory

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var viewModel: DonorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = DonorRepository(this)
        val factory = DonorViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DonorViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val bloodGroup = binding.etBloodGroup.text.toString()
            val location = binding.etLocation.text.toString()
            val lastDonationDate = binding.etLastDonationDate.text.toString()
            val phone = binding.etPhone.text.toString()

            if (name.isNotEmpty() && bloodGroup.isNotEmpty() && location.isNotEmpty() && lastDonationDate.isNotEmpty() && phone.isNotEmpty()) {
                val donor = Donor(
                    id = (System.currentTimeMillis() % 10000).toInt(),
                    name = name,
                    bloodGroup = bloodGroup,
                    location = location,
                    lastDonationDate = lastDonationDate,
                    isAvailable = true,
                    phoneNumber = phone,
                    distance = 0,
                    latitude = 0.0,
                    longitude = 0.0,
                )
                viewModel.registerDonor(name, bloodGroup, location, lastDonationDate, phone)
                repository.saveCurrentUser(donor)
                Toast.makeText(this, getString(R.string.registered_successfully), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
