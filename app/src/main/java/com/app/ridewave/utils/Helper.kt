package com.app.ridewave.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.app.ridewave.views.HomeActivity
import com.app.ridewave.views.LoginActivity


object Helper {


    fun saveRiderId(id: String, userType: String, context: Context) {
        // Get the shared preferences object
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

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
        val id: String = sharedPreferences.getString(Constants.USER_ID, null).toString()

        // Return the user ID
        return id
    }


    fun getUserType(context: Context): String {
        // Get the shared preferences object
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Get the user ID from the shared preferences object
        val userType: String = sharedPreferences.getString(Constants.USER_TYPE, null).toString()

        // Return the user ID
        return userType
    }


    fun restart(activity: Activity) {
        // Restart the app by launching the main activity
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)

        // Finish the current activity to prevent it from remaining in the back stack
        activity.finish()
    }


    fun deleteUserIdFromSharedPreferences(context: Context) {
        // Get the shared preferences object
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().clear().apply()

    }


}