package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.eventbox.app.android.models.user.User
import io.reactivex.Single

@Dao
interface UserDao {
    @Insert(onConflict = REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM User WHERE id = :id")
    fun deleteUser(id: String)

    @Query("SELECT * from User WHERE id = :id")
    fun getUser(id: String): Single<User>
}
