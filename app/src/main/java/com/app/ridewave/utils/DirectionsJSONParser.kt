package com.app.ridewave.utils

import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class DirectionsJSONParser {

    fun parse(jObject: JSONObject): List<List<LatLng>> {
        val routes = mutableListOf<List<LatLng>>()

        val routesArray = jObject.getJSONArray("routes")
        for (i in 0 until routesArray.length()) {
            val legs = routesArray.getJSONObject(i).getJSONArray("legs")
            val path = mutableListOf<LatLng>()

            for (j in 0 until legs.length()) {
                val steps = legs.getJSONObject(j).getJSONArray("steps")
                for (k in 0 until steps.length()) {
                    val points = steps.getJSONObject(k).getJSONObject("polyline").getString("points")
                    val decodedPoints = decodePolyline(points)
                    path.addAll(decodedPoints)
                }
            }
            routes.add(path)
        }

        return routes
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }
}
