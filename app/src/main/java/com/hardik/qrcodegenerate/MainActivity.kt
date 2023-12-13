package com.hardik.qrcodegenerate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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

    private lateinit var binding: ActivityMainBinding
    private lateinit var fullName: EditText
    private lateinit var mobileNo: EditText
    private lateinit var email: EditText
    private lateinit var imageUrl: EditText
    private lateinit var submit: Button
    private lateinit var goToList: Button

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

        checkPermissions()

        submit.setOnClickListener {
            hideKeyboard(it)
            val enteredFullName = fullName.text.toString().trim()
            val enteredMobileNo = mobileNo.text.toString().trim()
            val enteredEmail = email.text.toString().trim()
            val enteredImageUrl = imageUrl.text.toString().trim()

            if (enteredFullName.isNotEmpty() && enteredMobileNo.isNotEmpty() && enteredEmail.isNotEmpty()) {
                if (isEmailValid(enteredEmail)) {
                    insertUserIntoDatabase(
                        enteredFullName,
                        enteredMobileNo,
                        enteredEmail,
                        enteredImageUrl
                    )
                } else {
                    Snackbar.make(it, "insert proper email", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(it, "insert data", Snackbar.LENGTH_SHORT).show()
            }
        }
        goToList.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-]+)\\.([a-zA-Z]{2,5})$")
        return emailRegex.matches(email)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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