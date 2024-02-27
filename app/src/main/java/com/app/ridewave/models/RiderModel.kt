package com.app.ridewave.models

data class RiderModel(var id: String, var email: String, var name: String) {


    constructor() : this("","","") {
        this.phoneNumber = phoneNumber
    }

    var phoneNumber: String? = null

}
