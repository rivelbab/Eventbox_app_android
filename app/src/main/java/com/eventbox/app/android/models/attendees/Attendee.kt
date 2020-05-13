package com.eventbox.app.android.models.attendees

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.models.payment.TicketId

@Type("attendee")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity(foreignKeys = [
    (ForeignKey(entity = Event::class, parentColumns = ["id"],
        childColumns = ["event"], onDelete = ForeignKey.CASCADE)),
    (ForeignKey(entity = Ticket::class, parentColumns = ["id"],
        childColumns = ["ticket"], onDelete = ForeignKey.CASCADE))])
data class Attendee(
    @Id(IntegerIdHandler::class)
    @PrimaryKey
    val id: Long,
    val firstname: String? = null,
    val lastname: String? = null,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    val billingAddress: String? = null,
    val isCheckedIn: Boolean? = false,
    val checkinTimes: String? = null,
    val isCheckedOut: Boolean = false,
    val ticketId: String? = null,
    val checkedIn: String? = null,
    val checkedOut: String? = null,
    @ColumnInfo(index = true)
    @Relationship("event")
    var event: EventId? = null,
    @ColumnInfo(index = true)
    @Relationship("ticket")
    var ticket: TicketId? = null
)
