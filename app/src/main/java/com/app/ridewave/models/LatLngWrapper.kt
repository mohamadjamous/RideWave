package com.app.ridewave.models

import com.google.maps.model.LatLng

data class LatLngWrapper(val latitude: Double, val longitude: Double) {

    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    companion object {
        fun fromLatLng(latLng: LatLng): LatLngWrapper {
            return LatLngWrapper(latLng.lat, latLng.lng)
        }
    }
}