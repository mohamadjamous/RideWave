package com.app.ridewave.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityDriverDashboardBinding
import com.app.ridewave.models.RideModel
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.viewmodels.DriverViewModel
import com.app.ridewave.viewmodels.RideViewModel

class DriverDashboard : AppCompatActivity() {

    lateinit var rideViewModel: RideViewModel
    private lateinit var dialog: android.app.AlertDialog
    private lateinit var currentRide: RideModel
    private val context: Context = this
    lateinit var driverViewModel: DriverViewModel
    lateinit var binding: ActivityDriverDashboardBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        driverViewModel = ViewModelProvider(this)[DriverViewModel::class.java]
        rideViewModel = ViewModelProvider(this)[RideViewModel::class.java]
        val id = intent.getStringExtra("id")

        binding.progressLayout.visibility = View.VISIBLE
        binding.selectRiderLayout.visibility = View.GONE

        binding.profileCard.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        driverViewModel.getAccountInfo(id!!).observe(this)
        {
            if (it.equals(null)) {
                Toast.makeText(context, "Error Loading User", Toast.LENGTH_SHORT).show()
                binding.progressLayout.visibility = View.GONE
//                setPageState(0)
            } else {
                //search for on going ride
                driverViewModel.getRide(it.uid, 1).observe(this)
                { rideModel ->

                    //no active ride
                    if (rideModel == null) {
                        searchForRides()
                    } else {
                        //active ride
                        binding.progressLayout.visibility = View.GONE
                        binding.selectRiderLayout.visibility = View.VISIBLE
                        binding.selectRiderButton.visibility = View.GONE
                        binding.cancel.visibility = View.GONE
                        currentRide = rideModel
                        binding.riderName.text = currentRide.rider.name
                        binding.description.text = currentRide.pickUpAddress + "\n" + currentRide.dropOffAddress
                    }
                }
            }
        }

        binding.selectRiderButton.setOnClickListener {
            updateRide(0, 1)
        }

        binding.cancel.setOnClickListener {
            updateRide(1, -1)
        }
    }


    fun searchForRides() {

        if (binding.progressLayout.visibility == View.GONE)
        {
            binding.progressLayout.visibility = View.VISIBLE
        }

        // search for rides
        driverViewModel.getRide(Helper.getUserId(context), 0).observe(this)
        {
            if (it == null) {

                //keep searching for rides
                searchForRides()

            } else {
                println("RideModelId: " + it.id)
                binding.progressLayout.visibility = View.GONE
                binding.selectRiderLayout.visibility = View.VISIBLE
                binding.selectRiderButton.visibility = View.VISIBLE
                binding.cancel.visibility = View.VISIBLE
                currentRide = it
                println("RideModelId: " + currentRide.rider.name)
                println("RideModelId: " + currentRide.pickUpAddress)
                println("RideModelId: " + currentRide.dropOffAddress)
                binding.riderName.text = currentRide.rider.name
                binding.description.text =
                    currentRide.pickUpAddress + "\n" + currentRide.dropOffAddress
            }
        }
    }


    //type 0 = accept ride, 1 = cancel ride
    fun updateRide(type: Int, state: Int) {

        initializeDialog(getString(R.string.loading))
        showDialog(true)

        println("CurrentIdValue: ${currentRide.id}")

        rideViewModel.updateRide(currentRide.id, state).observe(this)
        {
            if (it.equals("success")) {

                if (type == 1) {
                    binding.selectRiderLayout.visibility = View.GONE
                    searchForRides()
                } else {
                    binding.cancel.visibility = View.GONE
                    binding.selectRiderButton.visibility = View.GONE
                }

            } else {
                Toast.makeText(context, "Error Finishing Ride", Toast.LENGTH_SHORT).show()
            }
            showDialog(false)
        }

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