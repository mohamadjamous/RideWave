package com.app.ridewave.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
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
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class ProfileActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var carPhoto: String
    private lateinit var dialog: AlertDialog
    private lateinit var deleteDialog: AlertDialog
    lateinit var binding: ActivityProfileBinding
    val context: Context = this
    lateinit var riderViewModel: RiderViewModel
    lateinit var driverViewModel: DriverViewModel
    private val REQUEST_TAKE_PHOTO: Int = 1
    private val REQUEST_SELECT_IMAGE_FROM_GALLERY: Int = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        riderViewModel = ViewModelProvider(this).get(RiderViewModel::class.java)
        driverViewModel = ViewModelProvider(this).get(DriverViewModel::class.java)
        userId = Helper.getUserId(context)
        val userType = Helper.getUserType(context)
        println("UserId: $userId")
        println("UserType: $userType")

        binding.activeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            driverViewModel.updateFirebaseUserFieldById(userId, "online", isChecked)
                .observe(this) {
                    if (it.equals("success")) {

                        if (binding.activeSwitch.isChecked) {
                            binding.activeSwitch.text = "active"
                        } else {
                            binding.activeSwitch.text = "inactive"
                        }
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(context, "Error Update Status", Toast.LENGTH_SHORT)
                            .show()
                    }
                }


        }


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

        binding.carImage.setOnClickListener {
            showImagePickerDialog()
        }

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
                } else {
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

    fun deleteRider(id: String) {

        showYesNoDialog(context, "Are you sure you want to delete your account?", {
            // Delete the item
            initializeDialog(getString(R.string.deleting_account))

            riderViewModel.deleteRiderAccount(id).observe(this)
            {
                if (it.equals("error")) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                } else {
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
        binding.activeSwitch.visibility = android.view.View.GONE

        riderViewModel.getAccountInfo(userId).observe(this)
        {
            if (it.equals(null)) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            } else {
                binding.name.text = it.name
                binding.type.text = "Rider"
            }

            binding.progressBar.visibility = android.view.View.GONE
        }

    }


    fun getDriverAccount(id: String) {
        binding.vehicleInfo.visibility = android.view.View.VISIBLE
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.activeSwitch.visibility = View.VISIBLE

        driverViewModel.getAccountInfo(id).observe(this)
        {
            if (it.equals("error")) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            } else {
                binding.name.text = it.name
                binding.description.text = it.carDescription
                binding.activeSwitch.text = if (it.online) "active" else "inactive"
                binding.activeSwitch.isChecked = it.online
                Glide.with(context).load(it.carPhoto).into(binding.carImage)
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


    fun showYesNoDialog(
        context: Context,
        message: String,
        onYesClick: () -> Unit,
        onNoClick: () -> Unit
    ) {
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


    /////// uploading photo ////////

    fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        android.app.AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera(this)
                    1 -> openGallery(this)
                }
            }
            .show()
    }

    private fun openGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, REQUEST_SELECT_IMAGE_FROM_GALLERY)
    }

    private fun openCamera(activity: Activity) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity.startActivityForResult(intent, REQUEST_TAKE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")

        if (requestCode == REQUEST_SELECT_IMAGE_FROM_GALLERY
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {


            // Get the Uri of data
            val file_uri = data.data
            uploadImageToFirebase(file_uri)
            binding.carImage.setImageURI(file_uri)
        } else {
            binding.progressBar.visibility = View.VISIBLE
        }

    }


    fun uploadImageToFirebase(fileUri: Uri?) {

        if (fileUri != null) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

            refStorage.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        carPhoto = imageUrl
                        driverViewModel.updateFirebaseUserFieldById(userId, "carPhoto", carPhoto)
                            .observe(this)
                            { it ->
                                if (it.equals("success")) {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this,
                                        "Image uploaded successfully",
                                        Toast.LENGTH_SHORT
                                    )
                                } else {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this,
                                        "Failed to upload image",
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }

                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    println("ErrorMessage: " + e.message)
                    carPhoto = ""
                    binding.progressBar.visibility = View.GONE
                }
        } else {
            carPhoto = ""
            binding.progressBar.visibility = View.GONE
        }
    }

    /////// uploading photo ////////


}