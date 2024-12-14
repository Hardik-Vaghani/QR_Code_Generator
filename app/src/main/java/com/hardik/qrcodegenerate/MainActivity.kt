package com.hardik.qrcodegenerate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.hardik.qrcodegenerate.databinding.ActivityMainBinding
import com.hardik.qrcodegenerate.room.UserDatabase
import com.hardik.qrcodegenerate.room.entity.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var database: UserDatabase
//hi
    private lateinit var binding: ActivityMainBinding
    private lateinit var fullName: EditText
    private lateinit var mobileNo: EditText
    private lateinit var email: EditText
    private lateinit var imageUrl: EditText
    private lateinit var submit: Button
    private lateinit var goToList: Button
    private lateinit var scan: Button
    private lateinit var utilRepository: UtilRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = UserDatabase.getInstance(applicationContext)

        fullName = binding.fullNameTxt
        mobileNo = binding.mobileNoTxt
        email = binding.emailTxt
        imageUrl = binding.imageUrlTxt
        submit = binding.submitBtn
        goToList = binding.goToListBtn
        scan = binding.scanBtn
        utilRepository = UtilRepository()

        checkPermissions()

        goToList.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        submit.setOnClickListener {
            utilRepository.hideKeyboard(it, context = it.context)
            val enteredFullName = fullName.text.toString().trim()
            val enteredMobileNo = mobileNo.text.toString().trim()
            val enteredEmail = email.text.toString().trim()
            val enteredImageUrl = imageUrl.text.toString().trim()

            if (enteredFullName.isNotEmpty() && enteredMobileNo.isNotEmpty() && enteredEmail.isNotEmpty()) {
                if (utilRepository.isEmailValid(enteredEmail)) {
                    insertUserIntoDatabase(
                        enteredFullName,
                        enteredMobileNo,
                        enteredEmail,
                        enteredImageUrl
                    )
                    fullName.text.clear()
                    mobileNo.text.clear()
                    email.text.clear()
                    imageUrl.text.clear()
                } else {
                    Snackbar.make(it, "insert proper email", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(it, "input field is empty, please insert data", Snackbar.LENGTH_SHORT).show()
            }
        }

        scan.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun insertUserIntoDatabase(
        fullName: String,
        mobileNo: String,
        email: String,
        imageUrl: String
    ) {
        GlobalScope.launch {
            database.userDao.insertUser(User(fullName, mobileNo, email, imageUrl))
            val users: List<User> = database.userDao.getUsers()
            for (u in users) {
                Log.e(TAG, "insertUserIntoDatabase: ${u.email}")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    private fun checkPermissions() {
        PermissionUtil.checkMultiplePermission(this)
    }
}