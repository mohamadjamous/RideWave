package com.app.ridewave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.ridewave.models.Rider
import com.app.ridewave.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiderViewModel : ViewModel() {


    fun createAccountEmailPassword(email: String, password: String): MutableLiveData<Rider> {
        // Create a Firebase Auth instance
        val auth = FirebaseAuth.getInstance()
        val mutableLiveData = MutableLiveData<Rider>()

        // Create a Firebase Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Create a user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    // Get the current user
                    val user = auth.currentUser

                    if (user != null) {
                        val riderModel = Rider(user.uid, email)
                        // Create a user document in Firestore
                        db.collection(Constants.RIDER_COLLECTION).add(riderModel).addOnCompleteListener { task1 ->

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
                    mutableLiveData.value = "error"
                }
            }

        // Return the MutableLiveData object
        return mutableLiveData
    }


}