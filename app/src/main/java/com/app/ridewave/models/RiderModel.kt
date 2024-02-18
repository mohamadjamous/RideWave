package com.app.ridewave.models

data class RiderModel(var id: String, var email: String, var name: String) {

    constructor(id: String, email: String, phoneNumber: String, name:String) : this(id, email, name) {
        this.phoneNumber = phoneNumber
    }

    var phoneNumber: String? = null

}
