package com.hardik.qrcodegenerate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.loader.content.CursorLoader
import com.bumptech.glide.Glide
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.util.*

open class UtilRepository {

    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-]+)\\.([a-zA-Z]{2,5})$")
        return emailRegex.matches(email)
    }

    fun hideKeyboard(view: View, context: Context) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // generateQRCode function
    fun generateQRCode(text: String): Bitmap? {
        val width = 300 // Width of the QR code bitmap
        val height = 300 // Height of the QR code bitmap
        try {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //startQRCodeScanner function
    fun startQRCodeScanner(activity: Activity) {
        val integrator = IntentIntegrator(activity)
        integrator.setOrientationLocked(true) // Unlock orientation to allow custom orientation
        integrator.setPrompt("Scan a QR Code")
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
        integrator.setBarcodeImageEnabled(true)
        integrator.setTorchEnabled(false)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    //startBarCodeScanner function
    fun startBarCodeScanner(activity: Activity) {
        val integrator = IntentIntegrator(activity)
        integrator.setOrientationLocked(true) // Unlock orientation to allow custom orientation
        integrator.setPrompt("Scan a Bar Code")
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
        integrator.setBarcodeImageEnabled(true)
        integrator.setTorchEnabled(false)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    @SuppressLint("Recycle")
    fun getImagePath(uri: Uri?, context: Context): String? {
        var imagePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(context, uri!!, projection, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            imagePath = it.getString(columnIndex)
        }
        return imagePath
    }

    fun decodeQRCodeFromImage(imagePath: String): String? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(imagePath, options)

        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()

        try {
            val result = reader.decode(binaryBitmap)
            return result.text
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    // getTimeStamp function
    private fun getTimeStamp(): String? {
        val tsLong = System.currentTimeMillis() / 1000
        return tsLong.toString()
    }

    // saveImage function (in gallery)
    fun saveImage(img: Bitmap, context: Context): String {
        var savedImagePath: String? = null
        val imageFileName = "QR" + getTimeStamp() + ".jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "QRGenerator"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut = FileOutputStream(imageFile)
                img.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(savedImagePath)
            val contentUrl = Uri.fromFile(f)
            mediaScanIntent.data = contentUrl
            context.sendBroadcast(mediaScanIntent)
            Toast.makeText(
                context,
                "QR Image Saved in to the: QRGenerator in Gallery",
                Toast.LENGTH_SHORT
            ).show()
        }

        return savedImagePath!!
    }

    // setGlideImage function with bitmap
    fun setGlideImage(context: Context, generatedQRCode: Bitmap, bottomSheetImage: ImageView) {
        Glide.with(context)
            .load(generatedQRCode)
            .placeholder(R.drawable.qr_codegif)
            .error("https://png.pngitem.com/pimgs/s/31-314727_attention-clipart-hd-png-download.png")
            .into(bottomSheetImage)
    }

    // setGlideImage function with drawable
    fun setGlideImage(context: Context, drawableInt: Int, bottomSheetImage: ImageView) {
        Glide.with(context)
            .load(drawableInt)
            .placeholder(R.drawable.qr_codegif)
            .error("https://png.pngitem.com/pimgs/s/31-314727_attention-clipart-hd-png-download.png")
            .into(bottomSheetImage)
    }
}