package com.app.ridewave.models

data class Rider(var id: String, var email: String) {

    constructor(id: String, email: String, phoneNumber: String) : this(id, email) {
        this.phoneNumber = phoneNumber
    }

    var phoneNumber: String? = null

}
