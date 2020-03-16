package pl.edu.pw.meil.knr.classes

import android.content.Context
import android.net.ConnectivityManager

object DetectConnection {
    fun checkInternetConnection(context: Context): Boolean {
        val conManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return (conManager.activeNetworkInfo != null && conManager.activeNetworkInfo.isAvailable
                && conManager.activeNetworkInfo.isConnected)
    }
}