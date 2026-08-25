package com.ashelyakin.libadb.sample.install

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

class InstallReceiver : BroadcastReceiver() {

    private val TAG = "InstallReceiver"

    override fun onReceive(context: Context, intent: Intent) {

        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                Log.i(TAG,"$TAG: need user action")
            }
            PackageInstaller.STATUS_SUCCESS ->{
                Log.i(TAG,"$TAG: install successful")
            }
            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.i(TAG,"$TAG: received $status and $msg")
            }
        }
    }
}