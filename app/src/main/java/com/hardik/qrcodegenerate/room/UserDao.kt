package com.hardik.qrcodegenerate.room

import androidx.room.*
import com.hardik.qrcodegenerate.room.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Transaction
    @Query("SELECT * FROM user")
    suspend fun getUsers() : List<User>
}
