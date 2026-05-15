package com.example.raktha_vahini.ui.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.raktha_vahini.R
import com.example.raktha_vahini.databinding.ItemDonorBinding
import com.example.raktha_vahini.model.Donor
import com.example.raktha_vahini.utils.DateUtils

class DonorAdapter(private var donors: List<Donor>) : RecyclerView.Adapter<DonorAdapter.DonorViewHolder>() {

    class DonorViewHolder(val binding: ItemDonorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val binding = ItemDonorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DonorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donors[position]
        val context = holder.itemView.context
        
        // Animation
        holder.itemView.animation = AnimationUtils.loadAnimation(context, R.anim.item_fade_in)

        holder.binding.apply {
            tvAvatar.text = donor.name.take(1).uppercase()
            tvDonorName.text = donor.name
            tvBloodGroupBadge.text = donor.bloodGroup
            tvLocation.text = "${donor.location} • ${donor.distance} km"
            
            val isEligible = DateUtils.isEligibleToDonate(donor.lastDonationDate)
            if (isEligible) {
                tvEligibility.text = context.getString(R.string.status_eligible)
                tvEligibility.setTextColor(Color.parseColor("#2E7D32"))
                tvEligibility.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
                tvEligibility.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
            } else {
                tvEligibility.text = context.getString(R.string.status_not_eligible)
                tvEligibility.setTextColor(Color.parseColor("#757575"))
                tvEligibility.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_warning, 0, 0, 0)
                tvEligibility.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#757575"))
            }
            
            // Highlight for Emergency (dist <= 10 and eligible)
            if (isEligible && donor.distance <= 10 && donor.distance != 0) {
                cardDonor.strokeColor = Color.parseColor("#B71C1C")
                cardDonor.strokeWidth = 6
                cardDonor.cardElevation = 6f
            } else {
                cardDonor.strokeColor = Color.parseColor("#E0E0E0")
                cardDonor.strokeWidth = 2
                cardDonor.cardElevation = 2f
            }

            btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${donor.phoneNumber}")
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = donors.size

    fun updateData(newDonors: List<Donor>) {
        donors = newDonors
        notifyDataSetChanged()
    }
}
