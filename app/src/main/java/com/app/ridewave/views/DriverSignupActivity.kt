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
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityDriverSignupBinding
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.viewmodels.DriverViewModel
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.util.regex.Pattern


class DriverSignupActivity : AppCompatActivity() {


    private val REQUEST_TAKE_PHOTO: Int = 1
    private val REQUEST_SELECT_IMAGE_FROM_GALLERY: Int = 10
    lateinit var context: Context
    lateinit var dialog: AlertDialog
    lateinit var binding: ActivityDriverSignupBinding
    lateinit var viewModel: DriverViewModel
    var carPhoto: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this
        viewModel = ViewModelProvider(this).get(DriverViewModel::class.java)
        binding.close.setOnClickListener { Helper.restart(this) }
        binding.signup.setOnClickListener {
            createDriverAccount(
                binding.emailAddress.text.toString(),
                binding.password.text.toString(),
                binding.confirmPassword.text.toString(),
                binding.name.text.toString(),
                carPhoto,
                binding.carDescription.text.toString()
            )
        }
        binding.progressBar.visibility = View.GONE
        binding.carImage.setOnClickListener { showImagePickerDialog() }

    }


    /**
     * Creates a new driver account.
     *@param email The email address of the driver.
     * @param password The password of the driver.
     * @param confirmPassword The confirm password of the driver.
     * @param name The name of the driver.
     */
    fun createDriverAccount(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        carPhoto: String,
        carDescription: String
    ) {


        // Check if the email address is a valid email address
        val emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,64}")
        val emailMatcher = emailPattern.matcher(email)

        println("EmailValue: " + email)
        println("Password: " + password)
        println("ConfirmPassword: " + confirmPassword)
        println("EmailValidationValue: " + emailMatcher.matches())


        // Check if the email address is empty
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the email address is empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if password is valid
        if (!isPasswordValid(password))
            return

        // Check if the confirm password is empty
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
            return
        }

        if (!emailMatcher.matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the password and confirm password match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the confirm password is empty
        if (carDescription.isEmpty()) {
            Toast.makeText(this, "Please enter a car description", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the confirm password is empty
        if (carPhoto.isEmpty()) {
            Toast.makeText(this, "Please select a car photo", Toast.LENGTH_SHORT).show()
            return
        }

        initializeDialog(getString(R.string.creating_account))
        showDialog(true)

        viewModel.createDriverAccount(name, email, password, carPhoto, carDescription)
            .observe(this) {

                if (it != null) {

                    if (it.uid == "account_exists") {
                        Toast.makeText(this, "Email address already exists", Toast.LENGTH_SHORT)
                            .show()

                    } else {

                        Helper.saveUserId(it.uid, "driver", context)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show()

                }

                showDialog(false)
            }
    }

    fun isPasswordValid(password: String): Boolean {

        // Check if the password is empty
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password is at least 8 characters long
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        // Check if the password contains at least one uppercase letter
        if (!password.any { it.isUpperCase() }) {
            Toast.makeText(
                this,
                "Password must contain at least one uppercase letter",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Check if the password contains at least one lowercase letter
        if (!password.any { it.isLowerCase() }) {
            Toast.makeText(
                this,
                "Password must contain at least one lowercase letter",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Check if the password contains at least one digit
        if (!password.any { it.isDigit() }) {
            Toast.makeText(this, "Password must contain at least one digit", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        // Check if the password contains at least one special character
        if (!password.any { it.isLetterOrDigit() }) {
            Toast.makeText(
                this,
                "Password must contain at least one special character",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // If all checks pass, return true
        return true
    }


    fun initializeDialog(text: String) {
        dialog = CustomProgressDialog.showCustomDialog(context, text, R.color.white);
    }


    fun showDialog(show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }


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
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT)
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

}


