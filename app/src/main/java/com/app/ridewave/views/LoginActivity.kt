package com.app.ridewave.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityLoginBinding
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.viewmodels.RiderViewModel
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {


    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: RiderViewModel
    lateinit var context: Context
    lateinit var dialog: android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)




        viewModel = ViewModelProvider(this).get(RiderViewModel::class.java)
        context = this


        //check if rider id exists
        println("UserIdValue: " + Helper.getRiderId(context))
        println("UserIdValue: " + Helper.getRiderId(context).length)
        if (Helper.getRiderId(context) != "null") {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }


        pageStates(0)
        binding.signupText.setOnClickListener {
            binding.signupLayout.visibility = View.VISIBLE
            binding.loginLayout.visibility = View.GONE
            binding.phoneLoginLayout.visibility = View.GONE
        }


        binding.signup.setOnClickListener {
            createRiderAccount(
                binding.emailSignup.text.toString(),
                binding.passwordSignup.text.toString(),
                binding.confirmPassword.text.toString()
            )
        }

        binding.cancel.setOnClickListener {
            pageStates(0)
        }

        binding.loginPhoneNumber.setOnClickListener {
            pageStates(2)
        }

        binding.signupPhoneNumber.setOnClickListener {
            pageStates(2)
        }

        binding.back.setOnClickListener {
            pageStates(0)
        }

        binding.login.setOnClickListener {
            loginUser(binding.emailLogin.text.toString(), binding.passwordLogin.text.toString())
        }

        binding.forgotPassword.setOnClickListener {
//            startActivity(Intent(this, HomeActivity::class.java))
        }

    }

    fun loginUser(email: String, password: String): Boolean {
        // Check if the email address is empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password is empty
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the email address is a valid email address
        val emailPattern =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,64}")
        val emailMatcher = emailPattern.matcher(email)
        if (!emailMatcher.matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        initializeDialog(getString(R.string.logging_in))
        showDialog(true)
        viewModel.loginUser(email, password).observe(this)
        {

            val responseList: List<String> = it.split(":")
            if (responseList[0] == "successful") {
                Helper.saveRiderId(responseList[1], context)
            } else {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
            showDialog(true)
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


    /*
    * 0 -> login
    * 1 -> signup
    * 2 -> phone login
     */
    fun pageStates(page: Int) {

        when (page) {
            0 -> {
                binding.loginLayout.visibility = View.VISIBLE
                binding.signupLayout.visibility = View.GONE
                binding.phoneLoginLayout.visibility = View.GONE
                binding.emailLogin.setText("")
                binding.passwordLogin.setText("")
                binding.emailSignup.setText("")
                binding.passwordSignup.setText("")
                binding.confirmPassword.setText("")
            }

            1 -> {
                binding.loginLayout.visibility = View.GONE
                binding.signupLayout.visibility = View.VISIBLE
                binding.phoneLoginLayout.visibility = View.GONE
                binding.emailSignup.setText("")
                binding.passwordSignup.setText("")
                binding.confirmPassword.setText("")
            }

            else -> {
                binding.loginLayout.visibility = View.GONE
                binding.signupLayout.visibility = View.GONE
                binding.phoneLoginLayout.visibility = View.VISIBLE
                binding.phoneNumber.setText("")
                binding.emailSignup.setText("")
                binding.passwordSignup.setText("")
                binding.confirmPassword.setText("")
            }
        }

    }

    /**
     * Creates a new rider account.
     *
     * @param email The email address of the rider.
     * @param password The password for the rider account.
     * @param confirmPassword The confirmation password for the rider account.
     *
     */
    fun createRiderAccount(email: String, password: String, confirmPassword: String) {

        // Check if the email address is a valid email address
        val emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,64}")
        val emailMatcher = emailPattern.matcher(email)

        println("EmailValue: " + email)
        println("Password: " + password)
        println("ConfirmPassword: " + confirmPassword)
        println("EmailValidationValue: " + emailMatcher.matches())



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


        initializeDialog(getString(R.string.creating_account))
        showDialog(true)

        viewModel.createAccountEmailPassword(email, password).observe(this) {

            if (it != null)
            {
                Helper.saveRiderId(it.id, context)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            else
                Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show()

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
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password contains at least one uppercase letter
        if (!password.any { it.isUpperCase() }) {
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password contains at least one lowercase letter
        if (!password.any { it.isLowerCase() }) {
            Toast.makeText(this, "Password must contain at least one lowercase letter", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password contains at least one digit
        if (!password.any { it.isDigit() }) {
            Toast.makeText(this, "Password must contain at least one digit", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the password contains at least one special character
        if (!password.any { it.isLetterOrDigit() }) {
            Toast.makeText(this, "Password must contain at least one special character", Toast.LENGTH_SHORT).show()
            return false
        }

        // If all checks pass, return true
        return true
    }


}