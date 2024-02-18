package com.app.ridewave.views

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityResetPasswordBinding
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.viewmodels.RiderViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern


class ResetPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityResetPasswordBinding
    lateinit var dialog: AlertDialog
    lateinit var viewModel : RiderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel = ViewModelProvider(this).get(RiderViewModel::class.java)

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

        initializeDialog(getString(R.string.loading))
        showDialog(true)

        viewModel.sendResetPasswordEmail(email).observe(this) {

            if (it.equals("success"))
            {
                Toast.makeText(this, "Email sent successfully", Toast.LENGTH_SHORT).show()
            }else if (it.equals("error"))
            {
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