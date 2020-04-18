package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eventbox.app.android.models.payment.Order
import io.reactivex.Single

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrders(orders: List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: Order)

    @Query("SELECT * FROM `order`")
    fun getAllOrders(): Single<List<Order>>

    @Query("SELECT * FROM `order` WHERE isExpired = :expired")
    fun getOrders(expired: Boolean): Single<List<Order>>

    @Query("DELETE FROM `order`")
    fun deleteAllOrders()

    @Query("SELECT * FROM `order` WHERE id = :orderId")
    fun getOrderById(orderId: Long): Single<Order>
}
