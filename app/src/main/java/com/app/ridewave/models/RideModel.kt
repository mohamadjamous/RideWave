package com.app.ridewave.models

import com.google.android.gms.maps.model.LatLng


// state = -1: canceled, 0: active, 1: completed, 2: arrived
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
