package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import java.net.UnknownHostException

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.showErrorToast(e: Throwable, length: Int = Toast.LENGTH_SHORT) {
    when (e) {
        is UnknownHostException -> {
            Toast.makeText(this, getString(R.string.network_error), length).show()
        }
        else -> {
            Toast.makeText(this, getString(R.string.error), length).show()
            Log.e("ERROR", e.javaClass.simpleName)
        }
    }
}

fun Context.isConnectedInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnectedOrConnecting ?: false
}