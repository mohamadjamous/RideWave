package com.app.ridewave.utils

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager

object Helper {


    fun saveRiderId(id: String, userType: String, context: Context) {
        // Get the shared preferences object
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        // Create an editor for the shared preferences object
        val editor = sharedPreferences.edit()

        // Put the user ID in the shared preferences object
        editor.putString(Constants.USER_ID, id)
        editor.putString(Constants.USER_TYPE, userType)

        // Commit the changes to the shared preferences object
        editor.apply()
    }

    fun getRiderId(context: Context): String {
        // Get the shared preferences object
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Get the user ID from the shared preferences object
        val riderId: String = sharedPreferences.getString(Constants.USER_TYPE, null).toString()

        // Return the user ID
        return riderId
    }

    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }


}