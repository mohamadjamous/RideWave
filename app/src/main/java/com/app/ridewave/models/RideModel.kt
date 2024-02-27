package com.app.ridewave.models

import com.google.android.gms.maps.model.LatLng


data class RideModel(var id: String, var pickUp: LatLng?, var dropOff: LatLng?, var pickUpAddress: String,
                     var dropOffAddress:String, var rider:RiderModel, var driver:DriverModel, var state: Int)
{
    constructor() : this(
        "",
        null,
        null,
        "",
        "",
        RiderModel(),
        DriverModel(),
        0
    )
}
