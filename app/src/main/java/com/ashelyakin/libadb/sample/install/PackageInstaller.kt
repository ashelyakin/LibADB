package com.ashelyakin.libadb.sample.install

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

class PackageInstaller {

    companion object{
        private const val NAME = "mostly-unused"
        private const val PI_INSTALL = 3439
    }

    suspend fun installApk(context: Context, path: String, onLog: (String) -> Unit){
        try {
            onLog("start of installApk function")
            val installer = context.packageManager.packageInstaller
            val resolver = context.contentResolver
            val apkFile = File(path)
            if (!apkFile.exists()){
                onLog("apk file is not exists")
                return
            }
            val apkUri = Uri.fromFile(apkFile)
            onLog("apkUri: $apkUri")
            onLog("start install")
            resolver.openInputStream(apkUri)?.use { apkStream ->
                val length = DocumentFile.fromSingleUri(context, apkUri)?.length() ?: -1
                val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
                val sessionId = installer.createSession(params)
                val session = installer.openSession(sessionId)

                onLog("start of copying apk stream")
                session.openWrite(NAME, 0, length).use { sessionStream ->
                    apkStream.copyTo(sessionStream)
                    session.fsync(sessionStream)
                }
                onLog("end of copying apk stream")

                val intent = Intent(context, InstallReceiver::class.java)
                val pi = PendingIntent.getBroadcast(context, PI_INSTALL, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                onLog("intent created")
                session.commit(pi.intentSender)
                session.close()
            }
        } catch (e: Exception) {
            onLog(e.stackTraceToString())
        }
    }
}