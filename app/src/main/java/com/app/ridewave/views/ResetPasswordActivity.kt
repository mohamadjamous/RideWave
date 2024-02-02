package com.app.ridewave.views

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityResetPasswordBinding
import com.app.ridewave.utils.CustomProgressDialog
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern


class ResetPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityResetPasswordBinding
    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.send.setOnClickListener {
            sendPasswordResetEmail(binding.email.text.toString())
        }

    }

    fun sendPasswordResetEmail(email: String) {

        // Check if the email address is a valid email address
        val emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,64}")
        val emailMatcher = emailPattern.matcher(email)

        // Check if the email address is empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!emailMatcher.matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }


        // Get the Firebase Auth instance
        val firebaseAuth = FirebaseAuth.getInstance()

        initializeDialog(getString(R.string.loading))
        showDialog(true)
        // Send the password reset email
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    // The password reset email has been sent
                } else {
                    // The password reset email could not be sent
                    Toast.makeText(this, "Error sending email", Toast.LENGTH_SHORT).show()
                }
                showDialog(false)
            }
    }


    fun initializeDialog(text: String) {
        dialog = CustomProgressDialog.showCustomDialog(this, text, R.color.white);
    }


    fun showDialog(show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }

    }

}