package com.app.ridewave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.ridewave.models.DriverModel
import com.app.ridewave.models.RideModel
import com.app.ridewave.models.RiderModel
import com.app.ridewave.utils.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class RideViewModel : ViewModel() {

    val db = FirebaseFirestore.getInstance()


    fun createRide(rideModel: RideModel): MutableLiveData<String> {
        val ride = MutableLiveData<String>()
        val rideCollection = db.collection(Constants.RIDES_COLLECTION)
        rideCollection.add(rideModel)
            .addOnSuccessListener { documentReference ->
                // Ride created successfully
                documentReference.update("id", documentReference.id).addOnSuccessListener {
                    ride.value = "success"
                }.addOnFailureListener {
                    println("ErrorMessage ${it.message}")
                    ride.value = "error"
                }

            }
            .addOnFailureListener { e ->
                // Error creating ride
                println("ErrorMessage: ${e.message}")
                ride.value = "error"
            }
        return ride
    }

    fun getRide(riderId: String): MutableLiveData<RideModel> {
        val mutableLiveData = MutableLiveData<RideModel>()
        db.collection(Constants.RIDES_COLLECTION).whereEqualTo("rider.id", riderId)
            .whereEqualTo("state", 0).get().addOnSuccessListener {

            var documentSnapshot: DocumentSnapshot? = null
            if (it.equals(null)) {
                mutableLiveData.value = null
            } else {
                if (it.documents.isNotEmpty()) {
                    documentSnapshot = it.documents[0]
                } else {
                    mutableLiveData.value = null
                }

            }

            if (documentSnapshot != null) {
                val rideModel = RideModel()
                rideModel.id = documentSnapshot.getString("id").toString()
                val driver = DriverModel()
                driver.carPhoto = documentSnapshot.getString("driver.carPhoto").toString()
                driver.carDescription =
                    documentSnapshot.getString("driver.carDescription").toString()
                rideModel.driver = driver

                mutableLiveData.value = rideModel
            } else {

                mutableLiveData.value = null
            }

        }.addOnFailureListener {
            println("ErrorMessage: ${it.message}")
            mutableLiveData.value = null
        }

        return mutableLiveData
    }


    fun finishRide(id: String, state: Int): MutableLiveData<String> {
        val mutableLiveData = MutableLiveData<String>()

        db.collection(Constants.RIDES_COLLECTION).document(id).update("state", state)
            .addOnSuccessListener {
                mutableLiveData.value = "success"
            }
            .addOnFailureListener { e ->
                // Error creating ride
                println("ErrorMessage: ${e.message}")
                mutableLiveData.value = "error"
            }

        return mutableLiveData
    }


}