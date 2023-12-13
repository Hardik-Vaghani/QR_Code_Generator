package com.hardik.qrcodegenerate.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    val fullName: String,
    val mobileNo: String,
    val email: String
)
