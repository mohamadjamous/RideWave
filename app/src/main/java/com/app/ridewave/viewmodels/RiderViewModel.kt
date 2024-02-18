package com.app.ridewave.viewmodels

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.ridewave.models.RiderModel
import com.app.ridewave.utils.Constants
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class RiderViewModel : ViewModel() {

    lateinit var token: ForceResendingToken

    fun createAccountEmailPassword(email: String, password: String, name :String): MutableLiveData<RiderModel> {

        // Create a Firebase Auth instance
        val auth = FirebaseAuth.getInstance()
        val mutableLiveData = MutableLiveData<RiderModel>()

        // Create a Firebase Firestore instance
        val db = FirebaseFirestore.getInstance()


        // Create a reference to the users collection
        val usersCollection = db.collection(Constants.RIDER_COLLECTION)

        // Query the users collection for the user with the given email address
        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->

                // Check if the query snapshot is empty
                if (querySnapshot.isEmpty) {

                    // The user does not exist
                    // Create a user with email and password
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {
                                // Get the current user
                                val user = auth.currentUser

                                if (user != null) {
                                    val riderModel = RiderModel(user.uid, email, name)

                                    // Create a user document in Firestore
                                    db.collection(Constants.RIDER_COLLECTION).add(riderModel)
                                        .addOnCompleteListener { task1 ->

                                            // Check if the user document was created successfully
                                            if (task1.isSuccessful) {
                                                mutableLiveData.value = riderModel
                                            } else {
                                                mutableLiveData.value = null
                                            }
                                        }

                                } else {
                                    mutableLiveData.value = null
                                }

                            } else {
                                // Handle the error
                                mutableLiveData.value = null
                            }
                        }

                } else {

                    // The user exists
                    // Handle the error
                    mutableLiveData.value = RiderModel("account_exists", "", "")
                }
            }
            .addOnFailureListener { exception ->

                println("ErrorMessage: ${exception.message}")
                // Handle the error
                mutableLiveData.value = null
            }

        return mutableLiveData
    }


    fun loginUser(email: String, password: String): MutableLiveData<String> {
        // Create a MutableLiveData object to store the Rider object
        val mutableLiveData = MutableLiveData<String>()

        // Create a Firebase Auth instance
        val auth = FirebaseAuth.getInstance()

        // Sign in the user with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Get the current user
                    val user = auth.currentUser

                    // Set the Rider object in the MutableLiveData object
                    mutableLiveData.value = "successful:" + user?.uid

                } else {
                    // Set an error message in the MutableLiveData object
                    mutableLiveData.value = "error: ${task.exception?.message}"
                }
            }

        // Return the MutableLiveData object
        return mutableLiveData
    }


    /*
    send otp code
    verify otp code
    sign in with phone number
    save user info to firestore
     */
    fun sendOTPCode(
        phoneNumber: String,
        activity: Activity,
        pageState: Int
    ): MutableLiveData<String> {

        // Create a MutableLiveData object to store the Rider object
        val mutableLiveData = MutableLiveData<String>()
        // Get the Firebase Phone Auth instance
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()


        // Create a reference to the users collection
        val usersCollection = db.collection(Constants.RIDER_COLLECTION)

        // Query the users collection for the user with the given phone number
        usersCollection.whereEqualTo("phoneNumber", phoneNumber).get()
            .addOnSuccessListener { querySnapshot ->

                // login
                if (pageState == 1) {
                    // Check if the query snapshot is empty
                    if (!querySnapshot.isEmpty) {

                        // Create a PhoneAuthOptions object
                        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object :
                                PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                                    // The OTP code has been automatically retrieved
                                    // You can use the credential to sign in the user
                                    println("ResponseMessage: " + credential.smsCode)

                                    // The user has been signed in successfully
                                    mutableLiveData.value = "smsCode:${credential.smsCode}"


                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    // The OTP code retrieval failed
                                    println("ResponseMessage1: " + e.message)
                                    mutableLiveData.value = "error:${e.message}"
                                }

                                override fun onCodeSent(
                                    verificationId: String,
                                    token: ForceResendingToken
                                ) {

                                    // The OTP code has been sent to the user's phone number
                                    // You can use the verificationId and token to resend the code if needed
                                    mutableLiveData.value = "verificationId:$verificationId"
                                    this@RiderViewModel.token = token
                                    println("ResponseMessage2: " + verificationId)
                                }
                            })
                            .build()

                        // Start the phone number verification process
                        PhoneAuthProvider.verifyPhoneNumber(options)

                    } else {

                        // The user exists
                        // Handle the error
                        mutableLiveData.value = "no_account:"
                    }
                }
                // signup
                else {

                    // Check if the query snapshot is empty
                    if (querySnapshot.isEmpty) {

                        // Create a PhoneAuthOptions object
                        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object :
                                PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                                    // The OTP code has been automatically retrieved
                                    // You can use the credential to sign in the user
                                    println("ResponseMessage: " + credential.smsCode)


                                    // Get the current user
                                    val user = firebaseAuth.currentUser


                                    if (user != null) {

                                        val riderModel = RiderModel(user.uid, "", phoneNumber)

                                        // Create a user document in Firestore
                                        db.collection(Constants.RIDER_COLLECTION).add(riderModel)
                                            .addOnCompleteListener { task1 ->

                                                // Check if the user document was created successfully
                                                if (task1.isSuccessful) {

                                                    // The user has been signed in successfully
                                                    mutableLiveData.value = "smsCode:${user.uid}"

                                                } else {
                                                    mutableLiveData.value =
                                                        "error:error creating account"
                                                }
                                            }

                                    } else {
                                        mutableLiveData.value = "error:error creating account"
                                    }

                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    // The OTP code retrieval failed
                                    println("ResponseMessage1: " + e.message)
                                    mutableLiveData.value = "error:${e.message}"
                                }

                                override fun onCodeSent(
                                    verificationId: String,
                                    token: ForceResendingToken
                                ) {

                                    // The OTP code has been sent to the user's phone number
                                    // You can use the verificationId and token to resend the code if needed
                                    mutableLiveData.value = "verificationId:$verificationId"
                                    this@RiderViewModel.token = token
                                    println("ResponseMessage2: " + verificationId)
                                }
                            })
                            .build()

                        // Start the phone number verification process
                        PhoneAuthProvider.verifyPhoneNumber(options)

                    } else {

                        // The user exists
                        // Handle the error
                        mutableLiveData.value = "account_exists:"
                    }

                }


            }



        return mutableLiveData
    }

    fun verifyOTPCode(
        verificationId: String,
        otpCode: String,
        phoneNumber: String,
        pageState: Int
    ): MutableLiveData<String> {

        // Get the Firebase Phone Auth instance
        val firebaseAuth = FirebaseAuth.getInstance()
        val mutableLiveData: MutableLiveData<String> = MutableLiveData()
        // Create a Firebase Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Create a PhoneAuthCredential object
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)

        // Sign in the user with the credential
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Get the current user
                    val user = firebaseAuth.currentUser

                    if (user != null) {

                        // login
                        if (pageState == 1) {
                            // The user has been signed in successfully
                            mutableLiveData.value = "success:${user.uid}"
                        }
                        // signup
                        else if (pageState == 2) {
                            val riderModel = RiderModel(user.uid, "", phoneNumber)
                            // Create a user document in Firestore
                            db.collection(Constants.RIDER_COLLECTION).add(riderModel)
                                .addOnCompleteListener { task1 ->

                                    // Check if the user document was created successfully
                                    if (task1.isSuccessful) {

                                        // The user has been signed in successfully
                                        mutableLiveData.value = "success:${user.uid}"

                                    } else {
                                        mutableLiveData.value = "error:error creating account"
                                    }
                                }
                        }


                    } else {
                        mutableLiveData.value = "error:error creating account"
                    }


                }
            }.addOnFailureListener {

                mutableLiveData.value = "error:${it.message}"
            }
        return mutableLiveData
    }

    fun resendOtpCode(phoneNumber: String, activity: Activity): MutableLiveData<String> {

        // Get the Firebase Phone Auth instance
        val firebaseAuth = FirebaseAuth.getInstance()
        val mutableLiveData: MutableLiveData<String> = MutableLiveData()

        // Create a PhoneAuthOptions object
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object :
                PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // The OTP code has been automatically retrieved
                    // You can use the credential to sign in the user
                    println("ResponseMessage: " + credential.smsCode)

                    // The user has been signed in successfully
                    mutableLiveData.value = "smsCode:${credential.smsCode}"


                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // The OTP code retrieval failed
                    println("ResponseMessage1: " + e.message)
                    mutableLiveData.value = "error:${e.message}"
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: ForceResendingToken
                ) {

                    // The OTP code has been sent to the user's phone number
                    // You can use the verificationId and token to resend the code if needed
                    mutableLiveData.value = "verificationId:$verificationId"
                    println("ResponseMessage2: " + verificationId)
                }
            })
            .setForceResendingToken(this@RiderViewModel.token)
            .build()

        // Start the phone number verification process
        PhoneAuthProvider.verifyPhoneNumber(options)
        return mutableLiveData
    }


    fun sendResetPasswordEmail(email: String): MutableLiveData<String> {
        var mutableLiveData: MutableLiveData<String> = MutableLiveData()
        val firebaseAuth = FirebaseAuth.getInstance()


        // Send the password reset email
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    mutableLiveData.value = "success"

                } else {

                    mutableLiveData.value = "error"
                }

            }

        return mutableLiveData
    }


}