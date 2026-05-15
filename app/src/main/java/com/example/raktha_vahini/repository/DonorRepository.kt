package com.example.raktha_vahini.repository

import android.content.Context
import androidx.core.content.edit
import com.example.raktha_vahini.model.Donor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DonorRepository(private val context: Context) {
    private val gson = Gson()
    private val sharedPrefs = context.getSharedPreferences("donor_prefs", Context.MODE_PRIVATE)

    fun getMockDonors(): List<Donor> {
        return try {
            val jsonString = context.assets.open("donors.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Donor>>() {}.type
            gson.fromJson(jsonString, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDonor(donor: Donor) {
        val currentDonors = getRegisteredDonors().toMutableList()
        currentDonors.add(donor)
        val json = gson.toJson(currentDonors)
        sharedPrefs.edit {
            putString("registered_donors", json)
        }
    }

    fun getRegisteredDonors(): List<Donor> {
        val json = sharedPrefs.getString("registered_donors", null) ?: return emptyList()
        val listType = object : TypeToken<List<Donor>>() {}.type
        return gson.fromJson(json, listType)
    }

    fun getAllDonors(): List<Donor> {
        return getMockDonors() + getRegisteredDonors()
    }

    fun getCurrentUser(): Donor? {
        val json = sharedPrefs.getString("current_user", null) ?: return null
        return gson.fromJson(json, Donor::class.java)
    }

    fun saveCurrentUser(donor: Donor) {
        val json = gson.toJson(donor)
        sharedPrefs.edit {
            putString("current_user", json)
        }
    }
}
