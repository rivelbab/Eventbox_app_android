package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.payment.TicketId

class TicketIdConverter {

    @TypeConverter
    fun fromTicketId(ticketId: TicketId?): Long? {
        return ticketId?.id
    }

    @TypeConverter
    fun toTicketId(id: Long?): TicketId? {
        return id?.let {
            TicketId(id)
        }
    }
}
