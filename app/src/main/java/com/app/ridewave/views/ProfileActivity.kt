package com.app.ridewave.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityProfileBinding
import com.app.ridewave.utils.Constants
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.viewmodels.DriverViewModel
import com.app.ridewave.viewmodels.RiderViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Driver


class ProfileActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog
    lateinit var binding: ActivityProfileBinding
    val context: Context = this
    lateinit var riderViewModel: RiderViewModel
    lateinit var driverViewModel: DriverViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        riderViewModel = ViewModelProvider(this).get(RiderViewModel::class.java)
        driverViewModel = ViewModelProvider(this).get(DriverViewModel::class.java)
        val userId = Helper.getRiderId(context)
        val userType = Helper.getUserType(context)
        println("UserId: $userId")
        println("UserType: $userType")


        binding.back.setOnClickListener { finish() }

        binding.logout.setOnClickListener { logoutUser() }

        //rider
        if (userType == "0")
            getRiderAccount(userId)
        //driver
        else
            getDriverAccount(userId)

    }

    fun getRiderAccount(userId: String) {

        binding.vehicleInfo.visibility = android.view.View.GONE
        binding.progressBar.visibility = android.view.View.VISIBLE

        riderViewModel.getAccountInfo(userId).observe(this)
        {
            if (it.equals("error")) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            } else {
                binding.name.text = it
                binding.type.text = "Rider"
            }

            binding.progressBar.visibility = android.view.View.GONE
        }

    }


    fun getDriverAccount(id: String) {
        binding.vehicleInfo.visibility = android.view.View.VISIBLE
        binding.progressBar.visibility = android.view.View.VISIBLE

    }


    fun logoutUser() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        Helper.deleteUserIdFromSharedPreferences(context)
        Helper.restart(this)
    }


    fun initializeDialog(text: String) {
        dialog = CustomProgressDialog.showCustomDialog(context, text, R.color.white)
    }


    fun showDialog(show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }

    }

}