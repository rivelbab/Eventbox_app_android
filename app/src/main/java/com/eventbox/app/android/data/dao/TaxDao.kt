package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eventbox.app.android.models.payment.Tax
import io.reactivex.Single

@Dao
interface TaxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTax(tax: Tax)

    @Query("SELECT * from Tax WHERE eventId = :eventId")
    fun getTaxDetails(eventId: Long): Single<Tax>
}
