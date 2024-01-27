package com.app.ridewave.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.app.ridewave.R


object CustomProgressDialog {
    fun showCustomDialog(context: Context?, message: String?, backgroundColor: Int): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.custom_dialog_layout, null)
        builder.setView(dialogView)

        // Customize background color
//        dialogView.setBackgroundColor(backgroundColor);

        // Customize progress bar color
//        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
//        progressBar.getIndeterminateDrawable().setColorFilter(progressColor, android.graphics.PorterDuff.Mode.MULTIPLY);

        // Customize message text
        val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = message

        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        return dialog
    }
}
