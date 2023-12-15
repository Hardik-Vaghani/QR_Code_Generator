package com.hardik.qrcodegenerate

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator

class ScannerActivity : AppCompatActivity() {
    private lateinit var btnScan: Button
    private lateinit var btnGallery: Button
    private lateinit var txtResult: TextView
    private lateinit var imgResult: ImageView

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        initializeViews()
        btnScan.setOnClickListener {
            startQRCodeScanner()
        }

        btnGallery.setOnClickListener {
            openGalleryForQrCode()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    txtResult.text = "Scan cancelled"
                } else {
                    txtResult.text = result.contents
                    imgResult.setImageBitmap(ListActivity().generateQRCode(result.contents))//set image when you scan
                }
            }
        } else if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == RESULT_OK && data != null) {
                val intentData: Intent? = data
                intentData?.data?.let { uri ->

                    imgResult.setImageURI(uri)
                    // Handle the selected image URI here and decode the QR code
                    val imagePath = getImagePath(uri)
                    val result = decodeQRCodeFromImage(imagePath!!)

                    txtResult.text = result ?: "Unable to decode QR code"
                }
            }
        }
    }


    private fun initializeViews() {
        btnScan = findViewById(R.id.btnScan)
        btnGallery = findViewById(R.id.btnGoToGallery)
        txtResult = findViewById(R.id.txtResult)
        imgResult= findViewById(R.id.imgResult)
    }

    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true) // Unlock orientation to allow custom orientation
        integrator.setPrompt("Scan a QR Code")
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
        integrator.setBarcodeImageEnabled(true)
        integrator.setTorchEnabled(false)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    private fun startBarCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true) // Unlock orientation to allow custom orientation
        integrator.setPrompt("Scan a Bar Code")
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
        integrator.setBarcodeImageEnabled(true)
        integrator.setTorchEnabled(false)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    private fun openGalleryForQrCode() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 101
    }

    @SuppressLint("Recycle")
    private fun getImagePath(uri: Uri?): String? {
        var imagePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(this, uri!!, projection, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            imagePath = it.getString(columnIndex)
        }
        return imagePath
    }

    private fun decodeQRCodeFromImage(imagePath: String): String? {
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

}
