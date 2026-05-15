package com.example.raktha_vahini.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.raktha_vahini.R
import com.example.raktha_vahini.databinding.ActivityProfileBinding
import com.example.raktha_vahini.model.Donor
import com.example.raktha_vahini.repository.DonorRepository
import com.example.raktha_vahini.utils.DateUtils
import com.example.raktha_vahini.viewmodel.DonorViewModel
import com.example.raktha_vahini.viewmodel.DonorViewModelFactory
import com.google.android.material.snackbar.Snackbar

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: DonorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = DonorRepository(this)
        val factory = DonorViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DonorViewModel::class.java]

        setupObservers()
        setupListeners()

        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.loadCurrentUser()
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                updateUI(user)
            } else {
                // For demo purposes, if no user exists, create a dummy one
                val dummyUser = Donor(101, "Test User", "O+", "Mumbai", "2024-01-01", true, "9999999999")
                repository().saveCurrentUser(dummyUser)
                viewModel.loadCurrentUser()
            }
        }
    }

    private fun repository() = DonorRepository(this)

    private fun updateUI(user: Donor) {
        binding.apply {
            tvAvatarLarge.text = user.name.take(1).uppercase()
            tvProfileName.text = user.name
            tvProfileBloodGroup.text = user.bloodGroup
            tvLastDonation.text = getString(R.string.last_donated_label, user.lastDonationDate)
            
            val isEligible = DateUtils.isEligibleToDonate(user.lastDonationDate)
            if (isEligible) {
                tvEligibilityStatus.text = getString(R.string.status_eligible)
                tvEligibilityStatus.setTextColor(Color.parseColor("#2E7D32"))
                tvEligibilityStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
                tvEligibilityStatus.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
            } else {
                tvEligibilityStatus.text = getString(R.string.status_not_eligible)
                tvEligibilityStatus.setTextColor(Color.parseColor("#757575"))
                tvEligibilityStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0)
                tvEligibilityStatus.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#757575"))
            }

            switchAvailability.isChecked = user.isAvailable
        }
    }

    private fun setupListeners() {
        binding.switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAvailability(isChecked)
        }

        binding.btnMarkDonated.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.confirm_donation_title)
                .setMessage(R.string.confirm_donation_msg)
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.markDonated()
                    Snackbar.make(binding.root, getString(R.string.thank_you_message), Snackbar.LENGTH_LONG).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}
