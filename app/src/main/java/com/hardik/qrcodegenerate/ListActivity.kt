package com.hardik.qrcodegenerate

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.hardik.qrcodegenerate.adapter.UserListAdapter
import com.hardik.qrcodegenerate.room.UserDatabase
import com.hardik.qrcodegenerate.room.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ListActivity : AppCompatActivity(), UserListAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserListAdapter // Assuming you have created a UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Launch a coroutine to fetch user data asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            val userList: List<User> = getUserList()
            userAdapter = UserListAdapter(
                userList,
                this@ListActivity
            ) // Replace with your actual list of users
            recyclerView.adapter = userAdapter
        }
    }

    private suspend fun getUserList(): List<User> {
        return withContext(Dispatchers.IO) {
            UserDatabase.getInstance(applicationContext).userDao.getUsers()
        }
    }

    // onItemClick method
    override fun onItemClick(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        val bottomSheetImage = dialogView.findViewById<ImageView>(R.id.bottomSheetImage)

        val width = 300 // Width of the QR code bitmap
        val height = 300 // Height of the QR code bitmap

        // Concatenate user details into a single string
        val userDetailsString = "${user.fullName}\n${user.mobileNo}\n${user.email}"
        val generatedQRCode = generateQRCode(userDetailsString, width, height)


        if (generatedQRCode != null) {
            // Use the generatedQRCode bitmap where needed, such as setting it to an ImageView
            Glide.with(this)
                .load(generatedQRCode)
                .placeholder(R.drawable.qr_codegif)
                .error("https://png.pngitem.com/pimgs/s/31-314727_attention-clipart-hd-png-download.png")
                .into(bottomSheetImage)
        } else {
            // Handle error if QR code generation fails
            // For example, load a placeholder image
            Glide.with(this)
                .load(R.drawable.qr_codegif)
                .placeholder(R.drawable.qr_codegif)
                .error("https://png.pngitem.com/pimgs/s/31-314727_attention-clipart-hd-png-download.png")
                .into(bottomSheetImage)
        }

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)
        dialog.show()
    }

    // generateQRCode function
    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
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


}
