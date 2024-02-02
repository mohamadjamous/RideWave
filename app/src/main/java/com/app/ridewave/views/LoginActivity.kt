package com.app.ridewave.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityLoginBinding
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.viewmodels.RiderViewModel
import com.google.firebase.auth.PhoneAuthProvider
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {


    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: RiderViewModel
    lateinit var context: Context
    lateinit var dialog: android.app.AlertDialog
    lateinit var verificationId: String
    lateinit var phoneNumber: String
    /*
     neutral phone number page state
     0 - neutral
     1 - login
     2 - signup
     */
    var phoneNumberPageState: Int = 0

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
            phoneNumberPageState = 0
            pageStates(0)
        }

        binding.loginPhoneNumber.setOnClickListener {
            phoneNumberPageState = 1
            pageStates(2)
        }

        binding.signupPhoneNumber.setOnClickListener {
            phoneNumberPageState = 2
            pageStates(2)
        }

        binding.back.setOnClickListener {
            pageStates(0)
        }

        binding.backOtp.setOnClickListener {
            pageStates(0)
        }

        binding.login.setOnClickListener {
            loginUser(binding.emailLogin.text.toString(), binding.passwordLogin.text.toString())
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }


        binding.next.setOnClickListener {
            sendOTPCode()
        }

        binding.verifyOtp.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

                if (p0.toString().length == 6) {
                    verifyOTP(p0.toString())
                }
            }
        })


        binding.resendCode.setOnClickListener {
            resendOTPCode()
        }
    }

    fun verifyOTP(otp: String) {

        initializeDialog(getString(R.string.verifying_otp_code))
        showDialog(true)

        viewModel.verifyOTPCode(verificationId, otp, phoneNumber, phoneNumberPageState).observe(this)
        {
            val response: String = it
            val message: String = response.split(":")[0]
            val content: String = response.split(":")[1]

            if (message == "success") {
                saveRiderId(message)
            } else {
                Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
            }

            showDialog(false)
        }
    }

    fun sendOTPCode() {

        val phoneNumber: String = binding.phoneNumber.text.toString()
        val countryCodeName: String = binding.countryPicker.selectedCountryNameCode.toString()
        val countryCode: String = binding.countryPicker.selectedCountryCodeWithPlus.toString()
        this.phoneNumber = countryCode + phoneNumber

        println("PhoneNumber: $phoneNumber")
        println("CountryCodeName: $countryCodeName")
        println("CountryCode: $countryCode")
        println("PhoneNumberValue: " + isPhoneNumberValid(phoneNumber, countryCodeName))

        if (!isPhoneNumberValid(phoneNumber, countryCodeName)) {
            return
        }

        initializeDialog(getString(R.string.loading))
        showDialog(true)


        viewModel.sendOTPCode(this.phoneNumber, this, phoneNumberPageState).observe(this) {

            val response: String = it
            println("ResponseValue: $response")
            val message: String = response.split(":")[0]
            val content: String = response.split(":")[1]
            println("ContentValue: $content")
            println("MessageValue: $message")

            when (message) {

                "no_account" -> {
                    Toast.makeText(this, "Account does not exist", Toast.LENGTH_SHORT).show()
                }

                "account_exists" -> {
                    Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show()
                }

                "error" -> {
                    Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
                }

                "smsCode" -> {

                    saveRiderId(message)
                }

                ("verificationId") -> {
                    Toast.makeText(this, "OTP code has been sent", Toast.LENGTH_SHORT).show()
                    this.verificationId = content
                    pageStates(3)
                }
            }
            showDialog(false)
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
            println("LoginResponse: $it")

            val response: List<String> = it.split(":")
            if (response[0] == "successful") {
                saveRiderId(response[1])

            } else {
                Toast.makeText(this, response[1], Toast.LENGTH_SHORT).show()
            }

            showDialog(false)
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
    * 3 -> verify  otp code
     */
    fun pageStates(page: Int) {

        when (page) {
            0 -> {
                binding.loginLayout.visibility = View.VISIBLE
                binding.signupLayout.visibility = View.GONE
                binding.phoneLoginLayout.visibility = View.GONE
                binding.phoneVerificationLayout.visibility = View.GONE
                binding.phoneNumber.setText("")
                binding.verifyOtp.setText("")
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
                binding.phoneVerificationLayout.visibility = View.GONE
                binding.phoneNumber.setText("")
                binding.verifyOtp.setText("")
                binding.emailSignup.setText("")
                binding.passwordSignup.setText("")
                binding.confirmPassword.setText("")
            }

            2 -> {
                binding.loginLayout.visibility = View.GONE
                binding.signupLayout.visibility = View.GONE
                binding.phoneLoginLayout.visibility = View.VISIBLE
                binding.phoneVerificationLayout.visibility = View.GONE
                binding.phoneNumber.setText("")
                binding.verifyOtp.setText("")
                binding.phoneNumber.setText("")
                binding.emailSignup.setText("")
                binding.passwordSignup.setText("")
                binding.confirmPassword.setText("")
            }

            3 -> {
                binding.loginLayout.visibility = View.GONE
                binding.signupLayout.visibility = View.GONE
                binding.phoneLoginLayout.visibility = View.GONE
                binding.phoneVerificationLayout.visibility = View.VISIBLE
                binding.phoneNumber.setText("")
                binding.emailSignup.setText("")
                binding.passwordSignup.setText("")
                binding.confirmPassword.setText("")
                binding.verifyOtp.setText("")
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

            if (it != null) {

                if (it.id == "account_exists") {
                    Toast.makeText(this, "Email address already exists", Toast.LENGTH_SHORT).show()

                } else {
                    saveRiderId(it.id)
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

    fun isPhoneNumberValid(phoneNumber: String, countryCode: String): Boolean {

        // Check if the phone number is empty
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return false
        }

        val phoneUtil = PhoneNumberUtil.getInstance()

        try {
            val numberProto = phoneUtil.parse(phoneNumber, countryCode)

            if (!phoneUtil.isValidNumber(numberProto)) {
                // Display a toast message
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
                return false
            }

        } catch (e: NumberParseException) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            System.err.println("NumberParseException: $e")
            return false
        }


        return true
    }


    private fun validateUsing_libphonenumber(countryCode: String, phNumber: String): Boolean {
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        val isoCode: String = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.toInt())
        var phoneNumber: Phonenumber.PhoneNumber? = null
        try {
            //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode)
        } catch (e: NumberParseException) {
            System.err.println(e)
        }

        val isValid: Boolean = phoneNumberUtil.isValidNumber(phoneNumber)
        if (isValid) {
            val internationalFormat: String =
                phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            Toast.makeText(this, "Phone Number is Valid $internationalFormat", Toast.LENGTH_LONG)
                .show()
            return true
        } else {
            Toast.makeText(this, "Phone Number is Invalid $phoneNumber", Toast.LENGTH_LONG).show()
            return false
        }
    }


    fun saveRiderId(id: String) {

        Helper.saveRiderId(id, context)
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }


    fun resendOTPCode() {

        val countryCodeName: String = binding.countryPicker.selectedCountryNameCode.toString()
        val countryCode: String = binding.countryPicker.selectedCountryCodeWithPlus.toString()

        println("PhoneNumber: " + this.phoneNumber)
        println("CountryCodeName: $countryCodeName")
        println("CountryCode: $countryCode")


        initializeDialog(getString(R.string.loading))
        showDialog(true)


        viewModel.resendOtpCode(this.phoneNumber, this).observe(this) {

            val response: String = it
            println("ResponseValue: $response")
            val message: String = response.split(":")[0]
            val content: String = response.split(":")[1]
            println("ContentValue: $content")
            println("MessageValue: $message")

            when (message) {

                "no_account" -> {
                    Toast.makeText(this, "Phone number does not exist", Toast.LENGTH_SHORT).show()
                }

                "account_exists" -> {
                    Toast.makeText(this, "Phone number already exists", Toast.LENGTH_SHORT).show()
                }

                "error" -> {
                    Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
                }

                "smsCode" -> {

                    saveRiderId(message)
                }

                ("verificationId") -> {
                    Toast.makeText(this, "OTP code has been sent", Toast.LENGTH_SHORT).show()
                    this.verificationId = content
                    pageStates(3)
                }
            }
            showDialog(false)
        }

    }


}