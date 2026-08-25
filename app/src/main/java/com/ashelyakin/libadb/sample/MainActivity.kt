package com.ashelyakin.libadb.sample

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.ashelyakin.libadb.AdbPairingRequiredException
import com.ashelyakin.libadb.AdbStream
import com.ashelyakin.libadb.LocalServices
import com.ashelyakin.libadb.sample.install.PackageInstaller
import com.ashelyakin.libadb.sample.ui.TextButtonWithStroke
import com.ashelyakin.libadb.sample.ui.TextFieldWithFilling
import com.ashelyakin.libadb.sample.ui.theme.LibADBTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.bartwell.exfilepicker.ExFilePicker
import ru.bartwell.exfilepicker.ExFilePicker.SortingType
import ru.bartwell.exfilepicker.data.ExFilePickerResult
import ru.bartwell.exfilepicker.ui.activity.ExFilePickerActivity
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.concurrent.Volatile

class MainActivity : ComponentActivity() {
    private val connectAdb = MutableLiveData<Boolean>()
    private var adbShellStream: AdbStream? = null
    private val commandOutput = MutableLiveData<CharSequence>()
    private val apkPath = MutableLiveData<String>("Выбрать файл .apk")

    private val permissions: Array<String> = mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray()
    private val packageInstaller = PackageInstaller()

    val manager = AdbConnectionManager("LibADB")

    companion object{
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE: Int = 10050
        private  const val EX_FILE_PICKER_RESULT = 7777
    }

    @Volatile
    private var clearEnabled = false
    private val outputGenerator = Runnable {
        try {
            BufferedReader(InputStreamReader(adbShellStream!!.openInputStream()))
                .use { reader ->
                    val sb = StringBuilder()
                    var s: String?
                    while ((reader.readLine().also { s = it }) != null) {
                        if (clearEnabled) {
                            sb.delete(0, sb.length)
                            clearEnabled = false
                        }
                        sb.append(s).append("\n")
                        commandOutput.postValue(sb)
                    }
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this,
            permissions,
            PERMISSIONS_REQUEST_CODE
        )

        connectAdb.observe(this){
            Toast.makeText(this, "adb connected", Toast.LENGTH_LONG).show()
        }

        autoConnectInternal()

        enableEdgeToEdge()
        setContent {
            LibADBTheme {
                Scaffold(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        TextButtonWithStroke(
                            "Connect"
                        ){
                            autoConnectInternal()
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        TextButtonWithStroke(
                            text = "Device owner"
                        ) {
                            executeCommand("dpm set-device-owner com.ashelyakin.libadb.sample/com.ashelyakin.libadb.sample.AdminReceiver")
                        }

                        val apkPathState = apkPath.observeAsState()
                        Spacer(modifier = Modifier.height(20.dp))
                        TextButtonWithStroke(
                            text = apkPathState.value.toString()
                        ) {
                            showFilePicker()
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        TextButtonWithStroke(
                            text = "Установить .apk"
                        ) {
                            lifecycleScope.launch {
                                packageInstaller.installApk(
                                    this@MainActivity,
                                    apkPathState.value.toString()
                                ) {
                                    commandOutput.postValue(it)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        TextButtonWithStroke(
                            text = "Перезагрузить устройство"
                        ) {
                            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                dpm.reboot(ComponentName(this@MainActivity, AdminReceiver::class.java))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        val output = commandOutput.observeAsState()
                        TextFieldWithFilling(
                            title = "Вывод консоли",
                            value = output.value.toString(),
                            fillingColor = Color.LightGray
                        ){}
                    }
                }
            }
        }
    }

    private fun showFilePicker(){
        val intent = Intent(this, ExFilePickerActivity::class.java)
        intent.putExtra(ExFilePickerActivity.EXTRA_CAN_CHOOSE_ONLY_ONE_ITEM, true)
        intent.putExtra(ExFilePickerActivity.EXTRA_SHOW_ONLY_EXTENSIONS, emptyArray<String>())
        intent.putExtra(ExFilePickerActivity.EXTRA_EXCEPT_EXTENSIONS, emptyArray<String>())
        intent.putExtra(ExFilePickerActivity.EXTRA_IS_NEW_FOLDER_BUTTON_DISABLED, true)
        intent.putExtra(ExFilePickerActivity.EXTRA_IS_SORT_BUTTON_DISABLED, false)
        intent.putExtra(ExFilePickerActivity.EXTRA_IS_QUIT_BUTTON_ENABLED, true)
        intent.putExtra(ExFilePickerActivity.EXTRA_CHOICE_TYPE, ExFilePicker.ChoiceType.FILES)
        intent.putExtra(ExFilePickerActivity.EXTRA_SORTING_TYPE, SortingType.NAME_ASC)
        intent.putExtra(ExFilePickerActivity.EXTRA_START_DIRECTORY, "")
        intent.putExtra(ExFilePickerActivity.EXTRA_USE_FIRST_ITEM_AS_UP_ENABLED, true)
        startActivityForResult(intent, EX_FILE_PICKER_RESULT)
    }

    private fun autoConnectInternal() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                var connected = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        connected = manager.autoConnect(application, 5000)
                    } catch (e: AdbPairingRequiredException) {
                        Log.e("MainActivity", e.stackTraceToString())
                        return@launch
                    } catch (th: Throwable) {
                        Log.e("MainActivity", th.stackTraceToString())
                    }
                }
                if (!connected) {
                    connected = manager.connect(5555)
                }
                if (connected) {
                    connectAdb.postValue(true)
                }
            } catch (th: Throwable) {
                Log.e("MainActivity", th.stackTraceToString())
            }
        }
    }

    private fun executeCommand(command: String){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (adbShellStream == null || adbShellStream?.isClosed == true) {
                    adbShellStream = manager.openStream(LocalServices.SHELL)
                    Thread(outputGenerator).start()
                }
                if (command == "clear") {
                    clearEnabled = true
                }
                adbShellStream?.openOutputStream()?.use { os ->
                    os.write(
                        String.format("%1\$s\n", command)
                            .toByteArray(StandardCharsets.UTF_8)
                    )
                    os.flush()
                    os.write("\n".toByteArray(StandardCharsets.UTF_8))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EX_FILE_PICKER_RESULT) {
            try {
                val result = ExFilePickerResult.getFromIntent(data)
                val apkName = result?.names?.firstOrNull()
                if (result != null && result.count > 0 && !apkName.isNullOrEmpty()) {
                    val apkFile = File(result.path, apkName)
                    if (apkFile.exists()) {
                        apkPath.postValue(apkFile.absolutePath)
                    } else {
                        Log.e(TAG,"Some troubles with choose directory for media files. path: ${result.path}, name $apkName")
                    }
                } else {
                    Log.e(TAG, "Some troubles with choose directory for media files. result path: ${result?.path}")
                }
            } catch (e: Exception){
                Log.e(TAG, e.stackTraceToString())
            }
        }
    }
}