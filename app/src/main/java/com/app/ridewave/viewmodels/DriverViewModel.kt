package com.app.ridewave.viewmodels

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.ridewave.models.DriverModel
import com.app.ridewave.models.RiderModel
import com.app.ridewave.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class DriverViewModel : ViewModel() {


    fun createDriverAccount(name: String, email: String, password:String , carPhoto: String, carDescription: String) : MutableLiveData<DriverModel>
    {
        var mutableLiveData : MutableLiveData<DriverModel> = MutableLiveData()

        // Create a Firebase Auth instance
        val auth = FirebaseAuth.getInstance()

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
                                    val driverModel = DriverModel(user.uid, name, email, carPhoto, carDescription)

                                    // Create a user document in Firestore
                                    db.collection(Constants.DRIVERS_COLLECTION).add(driverModel)
                                        .addOnCompleteListener { task1 ->

                                            // Check if the user document was created successfully
                                            if (task1.isSuccessful) {
                                                mutableLiveData.value = driverModel
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
                    mutableLiveData.value = DriverModel("account_exists", "", "", "", "")
                }
            }
            .addOnFailureListener { exception ->

                println("ErrorMessage: ${exception.message}")
                // Handle the error
                mutableLiveData.value = null
            }

        return mutableLiveData
    }





}