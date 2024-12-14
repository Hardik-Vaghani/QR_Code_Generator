package com.hardik.qrcodegenerate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

class ScannerActivity : AppCompatActivity() {
    private lateinit var btnScan: Button
    private lateinit var btnGallery: Button
    private lateinit var txtResult: TextView
    private lateinit var imgResult: ImageView
    private lateinit var utilRepository: UtilRepository

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        initializeViews()
        btnScan.setOnClickListener {
            utilRepository.startQRCodeScanner(activity = this)
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
                    imgResult.setImageBitmap(utilRepository.generateQRCode(result.contents))//set image when you scan
                }
            }
        } else if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == RESULT_OK && data != null) {
                val intentData: Intent? = data
                intentData?.data?.let { uri ->

                    imgResult.setImageURI(uri)
                    // Handle the selected image URI here and decode the QR code
                    val imagePath = utilRepository.getImagePath(uri, context = this)
                    val result = utilRepository.decodeQRCodeFromImage(imagePath!!)

                    txtResult.text = result ?: "Unable to decode QR code"
                }
            }
        }
    }


    private fun initializeViews() {
        btnScan = findViewById(R.id.btnScan)
        btnGallery = findViewById(R.id.btnGoToGallery)
        txtResult = findViewById(R.id.txtResult)
        imgResult = findViewById(R.id.imgResult)
        utilRepository = UtilRepository()
    }


    private fun openGalleryForQrCode() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 101
    }


}
