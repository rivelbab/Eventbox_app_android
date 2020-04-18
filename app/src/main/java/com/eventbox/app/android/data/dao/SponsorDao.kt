package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import com.eventbox.app.android.models.sponsor.Sponsor

@Dao
interface SponsorDao {

    @Insert(onConflict = REPLACE)
    fun insertSponsor(sponsor: Sponsor)
}
