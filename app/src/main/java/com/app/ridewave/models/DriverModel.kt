package com.app.ridewave.models

data class DriverModel (val uid: String,var name: String, var emailAddress : String, var carPhoto : String, var carDescription: String )
{
    constructor() : this("","","","","")
}