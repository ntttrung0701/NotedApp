package com.example.appquizlet.BroadcastReceiver

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class BroadcastReceiverCheckInternet : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent?.action)) {
            if (isNetworkAvailable(context!!)) {
//                Toast.makeText(context, "Connected to network", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show()
                showNetworkDialog(context)
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun showNetworkDialog(context: Context) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)

        builder.setTitle("Network Connection")
            .setMessage("You are not connected to the internet.\\nMake sure wifi is on, internet is on, Airplane mode is off and try again.")
            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}