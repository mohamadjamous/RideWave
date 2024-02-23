package com.app.ridewave.views

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityProfileBinding
import com.app.ridewave.utils.Constants
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.viewmodels.DriverViewModel
import com.app.ridewave.viewmodels.RiderViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog
    private lateinit var deleteDialog: AlertDialog
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
        val userId = Helper.getUserId(context)
        val userType = Helper.getUserType(context)
        println("UserId: $userId")
        println("UserType: $userType")


        binding.back.setOnClickListener { finish() }

        binding.logout.setOnClickListener { logoutUser() }

        binding.deleteAccount.setOnClickListener {
            //rider
            if (userType == "0")
                deleteRider(userId)
            //driver
            else
                deleteDriver(userId)



        }

        //rider
        if (userType == "0")
            getRiderAccount(userId)
        //driver
        else
            getDriverAccount(userId)

    }

    private fun deleteDriver(userId: String) {

            showYesNoDialog(context, "Are you sure you want to delete your account?", {
                // Delete the item
                initializeDialog(getString(R.string.deleting_account))
                showDialog(true)

                driverViewModel.deleteDriverAccount(userId).observe(this)
                {
                    if (it.equals("error")) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }else
                    {
                        Helper.deleteUserIdFromSharedPreferences(context)
                        Helper.restart(this)
                    }
                    showDialog(false)

                }

            }, {
                // Do nothing
                deleteDialog.dismiss()
            })


        }

    fun deleteRider(id:String)
     {

         showYesNoDialog(context, "Are you sure you want to delete your account?", {
             // Delete the item
             initializeDialog(getString(R.string.deleting_account))

             riderViewModel.deleteRiderAccount(id).observe(this)
             {
                 if (it.equals("error")) {
                     Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                 }else
                 {
                     Helper.deleteUserIdFromSharedPreferences(context)
                     Helper.restart(this)
                 }
                 showDialog(false)

             }

         }, {
             // Do nothing
             deleteDialog.dismiss()
         })


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

        driverViewModel.getAccountInfo(id).observe(this)
        {
            if (it.equals("error")) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            } else {
                binding.name.text = it.split(",")[0]
                binding.description.text = it.split(",")[1]
                Glide.with(context).load(it.split(",")[2]).into(binding.carImage)
                binding.type.text = "Driver"
            }

            binding.progressBar.visibility = android.view.View.GONE
        }

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




    fun showYesNoDialog(context: Context, message: String, onYesClick: () -> Unit, onNoClick: () -> Unit ){
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                onYesClick()
            }
            .setNegativeButton("No") { _, _ ->
                onNoClick()
            }
            .show()

        deleteDialog = builder.create()

    }




}