package cn.zr

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityManager
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import cn.zr.contentProviderPreference.RemotePreferences
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.reflect.Modifier


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, AccessibilityManager.AccessibilityStateChangeListener, CompoundButton.OnCheckedChangeListener {

    //private lateinit var remotePreferences: RemotePreferences
    private fun initPreferences() {
        //remotePreferences = RemotePreferences(this@MainActivity, "cn.zr.preferences", "other_preferences")
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            switchSuspensionWindow(getBoolean("is_show_suspension_window_key", false))
            registerOnSharedPreferenceChangeListener(this@MainActivity)
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {

        when (key) {
            "is_show_suspension_window_key" -> {
                switchSuspensionWindow(sharedPreferences.getBoolean(key, false))
            }
        }

    }

    private fun switchSuspensionWindow(b: Boolean) {
        if (b) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(applicationContext)) {
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), REQUEST_CODE_DRAW_OVERLAYS_PERMISSION)
                } else {
                    SuspensionWindow.showSuspensionWindow(this)
                }
            } else {
                SuspensionWindow.showSuspensionWindow(this)
            }
        } else {
            SuspensionWindow.dismissSuspensionWindow()
        }
    }

    private lateinit var mainSwitch: Switch

    private lateinit var accessibilityManager: AccessibilityManager
    private var isAccessibility: Boolean = false


    private fun getResult(): String {
        return "zrd"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getResult()
        Log.d(TAG, "onCreate")

        //Other().equals("")
        /*DebugHelper.init()
        DebugHelper.copyAssistFile(this, "zr.so", "arm64-v8a/")
        //DebugHelper.runAssistFile(this,"share.so","arm64-v8a/")
        //DebugHelper.copyAssistFile(this, "test.so", "arm64-v8a/")

        DebugHelper.initExec(this)
        DebugHelper.exec()

        bn.setOnClickListener {
            DebugHelper.exec()
        }

        bn0.setOnClickListener {
            DebugHelper.check()
        }*/
        //dx  --dex --output jar.dex dex.jar

        accessibilityManager = (getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).apply {
            addAccessibilityStateChangeListener(this@MainActivity)
        }


        Switch(this@MainActivity).apply {
            mainSwitch = this
            isAccessibility = isStart().apply {
                isChecked = this
            }
            setOnCheckedChangeListener(this@MainActivity)
        }
        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            customView = mainSwitch
        }


        initPreferences()

    }


    override fun onAccessibilityStateChanged(enabled: Boolean) {
        Log.d(TAG, "$enabled onAccessibilityStateChanged")
        isAccessibility = enabled
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this)
    }


    override fun onResume() {
        super.onResume()
        mainSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = isAccessibility
            setOnCheckedChangeListener(this@MainActivity)
        }

        isStart()
    }

    private fun isStart(): Boolean {


        Log.d(TAG, "isStart")
        accessibilityManager.apply {

            Log.d(TAG, "-->" + this.hashCode())
        }.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)?.also {
            for (i in it) {
                Log.d(TAG, "-->")
                Log.d(TAG, "-->" + i.id + "  " + i.packageNames[0])
                if (i.id == "$packageName/.MyAccessibilityService") {
                    Toast.makeText(this@MainActivity, i.packageNames[0], Toast.LENGTH_SHORT).show()
                    return true
                }

            }

        }

        return false
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_DRAW_OVERLAYS_PERMISSION ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(applicationContext)) {
                        Toast.makeText(this, getString(R.string.no_suspension_windows_permission), Toast.LENGTH_SHORT).show()
                    } else {
                        SuspensionWindow.showSuspensionWindow(this)
                    }
                }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_DRAW_OVERLAYS_PERMISSION = 1000

    }


}
