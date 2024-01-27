package com.app.ridewave.utils

import android.content.Context
import android.preference.PreferenceManager

object Helper {


    fun saveRiderId(riderId: String, context: Context) {
        // Get the shared preferences object
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        // Create an editor for the shared preferences object
        val editor = sharedPreferences.edit()

        // Put the user ID in the shared preferences object
        editor.putString(Constants.RIDER_ID, riderId)

        // Commit the changes to the shared preferences object
        editor.apply()
    }

    fun getRiderId(context: Context): String {
        // Get the shared preferences object
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Get the user ID from the shared preferences object
        val riderId: String = sharedPreferences.getString(Constants.RIDER_ID, null).toString()

        // Return the user ID
        return riderId
    }


}