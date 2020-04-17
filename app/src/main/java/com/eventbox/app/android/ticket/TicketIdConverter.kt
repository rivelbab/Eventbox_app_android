package com.eventbox.app.android.ticket

import androidx.room.TypeConverter

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
