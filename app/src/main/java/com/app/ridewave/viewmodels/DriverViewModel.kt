package com.app.ridewave.viewmodels

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.ridewave.models.DriverModel
import com.app.ridewave.models.RiderModel
import com.app.ridewave.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class DriverViewModel : ViewModel() {


    fun createDriverAccount(
        name: String,
        email: String,
        password: String,
        carPhoto: String,
        carDescription: String
    ): MutableLiveData<DriverModel> {
        var mutableLiveData: MutableLiveData<DriverModel> = MutableLiveData()

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
                                    val driverModel =
                                        DriverModel(user.uid, name, email, carPhoto, carDescription)

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


    fun getAccountInfo(id: String): MutableLiveData<String> {
        // Create a MutableLiveData object to store the Rider object
        val mutableLiveData: MutableLiveData<String> = MutableLiveData()
        val db = FirebaseFirestore.getInstance()

        println("CurrentUserId: " + id)

        db.collection(Constants.DRIVERS_COLLECTION).whereEqualTo("uid", id)
            .get()
            .addOnSuccessListener {

                var documentSnapshot: DocumentSnapshot? = null
                if (it.equals(null)) {
                    mutableLiveData.value = "error"
                } else {
                    documentSnapshot = it.documents[0]
                }

                if (documentSnapshot != null) {

                    val finalValue = documentSnapshot.getString("name") +
                            "," + documentSnapshot.getString("carDescription") +
                            "," + documentSnapshot.getString("carPhoto")

                    mutableLiveData.value = finalValue
                } else {

                    mutableLiveData.value = "error"
                }

            }
            .addOnFailureListener {
                // Handle any errors
                println("ErrorMessage: ${it.message}")
                mutableLiveData.value = "error"

            }


        return mutableLiveData
    }


    fun loginUser(email: String, password: String): MutableLiveData<String> {
        // Create a MutableLiveData object to store the Rider object
        val mutableLiveData = MutableLiveData<String>()

        // Create a Firebase Auth instance
        val auth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()


        // Sign in the user with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Get the current user
                    val user = auth.currentUser

                    db.collection(Constants.DRIVERS_COLLECTION)
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


    fun deleteDriverAccount(id: String): MutableLiveData<String> {
        val mutableLiveData: MutableLiveData<String> = MutableLiveData()
        // Delete the Firebase Auth account
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()

        // Delete the Firestore account
        val db = FirebaseFirestore.getInstance()
        // Query to find documents with "uid" field equal to the specified value
        db.collection(Constants.DRIVERS_COLLECTION)
            .whereEqualTo("uid", id).get().addOnSuccessListener { documents ->
                if (documents.size() >0) {
                    // Delete each document found
                    documents.documents.get(0).reference.delete().addOnSuccessListener {
                        // Document successfully deleted
                        mutableLiveData.value = "success"
                    }.addOnFailureListener { e ->
                        // Error deleting document
                        println("ErrorMessage: ${e.message}")
                        mutableLiveData.value = "error"
                    }
                }else
                {
                    mutableLiveData.value = "error"
                }
            }
            .addOnFailureListener { e ->
                // Error querying documents
                println("ErrorMessage: ${e.message}")
                mutableLiveData.value = "error"
            }
        return mutableLiveData
    }


}