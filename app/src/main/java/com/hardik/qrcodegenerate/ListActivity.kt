package com.hardik.qrcodegenerate

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hardik.qrcodegenerate.adapter.UserListAdapter
import com.hardik.qrcodegenerate.room.UserDatabase
import com.hardik.qrcodegenerate.room.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class ListActivity : AppCompatActivity(), UserListAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserListAdapter // Assuming you have created a UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.hasFixedSize()
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
        val bottomSheetSaveQRCode = dialogView.findViewById<TextView>(R.id.bottomSheetSaveQRCodeTxt)

        val utilRepository = UtilRepository()
        // Concatenate user details into a single string
        val userDetailsString =
            "${user.fullName}\n${user.mobileNo}\n${user.email}\n${user.imageUrl}"
        val generatedQRCode = utilRepository.generateQRCode(userDetailsString)


        if (generatedQRCode != null) {
            // Use the generatedQRCode bitmap where needed, such as setting it to an ImageView
            utilRepository.setGlideImage(context = this, generatedQRCode, bottomSheetImage)
        } else {
            // Handle error if QR code generation fails
            utilRepository.setGlideImage(context = this, R.drawable.qr_codegif, bottomSheetImage)

        }

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)
//        dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent) // Set your background drawable here
//        dialog.window?.attributes?.apply {
//            width = resources.displayMetrics.widthPixels - (48 * resources.displayMetrics.density).toInt() // Adjust width
//            height = WindowManager.LayoutParams.WRAP_CONTENT // Adjust height as needed
//            gravity = Gravity.CENTER // Adjust the gravity as needed

            // Set margins and padding
//            horizontalMargin = 16 * resources.displayMetrics.density
//            verticalMargin = 16 * resources.displayMetrics.density

            // Set padding
//            dialogView.setPadding(
//                24 * resources.displayMetrics.density.toInt(),
//                24 * resources.displayMetrics.density.toInt(),
//                24 * resources.displayMetrics.density.toInt(),
//                24 * resources.displayMetrics.density.toInt()
//            )
//        }

        dialog.show()

        //save QR Code in gallery
        bottomSheetSaveQRCode.setOnClickListener {
            if (generatedQRCode != null) {
                utilRepository.saveImage(generatedQRCode, it.context)
                dialog.dismiss()
//                Toast.makeText(it.context, "QR Image Saved in to the: QRGenerator in Gallery", Toast.LENGTH_SHORT).show()
//                Handler().postDelayed({dialog.dismiss()}, 500)
            }
        }
    }
}
