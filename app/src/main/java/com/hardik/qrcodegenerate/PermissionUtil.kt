package com.hardik.qrcodegenerate
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object PermissionUtil {

    private const val MULTIPLE_PERMISSION_ID = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
    } else {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    private fun doOperation(activity: Activity) {
        Toast.makeText(
            activity,
            "All Permissions Granted Successfully!",
            Toast.LENGTH_LONG
        ).show()
        // Perform operations requiring permissions
    }

    @JvmStatic
    fun checkMultiplePermission(activity: Activity): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                listPermissionNeeded.toTypedArray(),
                MULTIPLE_PERMISSION_ID
            )
            return false
        }
        return true
    }

    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MULTIPLE_PERMISSION_ID) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                        break
                    }
                }
                if (isGrant) {
                    // All permissions granted successfully
                    doOperation(activity)
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    activity,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                                break
                            }
                        }
                    }
                    if (someDenied) {
                        // Open app settings as some permissions were permanently denied
                        appSettingOpen(activity)
                    } else {
                        // Show permission warning dialog
                        warningPermissionDialog(activity) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission(activity)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun appSettingOpen(context: Context) {
        Toast.makeText(
            context,
            "Go to Settings and Enable All Permissions",
            Toast.LENGTH_LONG
        ).show()

        val settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingIntent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(settingIntent)
    }

    private fun warningPermissionDialog(context: Context, listener: DialogInterface.OnClickListener) {
        MaterialAlertDialogBuilder(context)
            .setMessage("All Permissions are Required for this App")
            .setCancelable(false)
            .setPositiveButton("Ok", listener)
            .create()
            .show()
    }
}
