package com.eventbox.app.android.models.event

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Event(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    //== basic details
    var name: String,
    val identifier: String,
    var description: String? = null,
    var locationName: String? = null,
    var startsAt: String,
    var endsAt: String,
    val codeOfConduct: String? = null,
    val createdAt: String? = null,
    val deletedAt: String? = null,
    val isComplete: Boolean = false,
    var privacy: String = "public",
    //== image url
    var thumbnailImageUrl: String? = null,
    var originalImageUrl: String? = null,
    var largeImageUrl: String? = null,
    //== owner details
    val ownerDescription: String? = null,
    var ownerName: String? = null,
    val hasOwnerInfo: Boolean = false,
    //== geolocation details
    val timezone: String = "UTC",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isMapShown: Boolean = false,
    //== favorite and interested
    var favorite: Boolean = false,
    var favoriteEventId: Long? = null,
    var interested: Boolean = false,
    var interestedEventId: Long? = null,
    //== payment details
    val paymentCurrency: String? = null,
    val paymentCountry: String? = null,
    val paypalEmail: String? = null,
    val ticketUrl: String? = null,
    val refundPolicy: String? = null,
    val canPayByStripe: Boolean = false,
    val canPayByBank: Boolean = false,
    val canPayByPaypal: Boolean = false,
    val canPayOnsite: Boolean = false,
    val isTicketingEnabled: Boolean = false,
    val isTaxEnabled: Boolean = false,

    @ColumnInfo(index = true)
    @Relationship("event-type", resolve = true)
    val eventType: EventType? = null
) {
    constructor(
        name: String,
        description: String,
        locationName: String,
        startsAt: String,
        endsAt: String,
        thumbnailImageUrl: String,
        originalImageUrl: String,
        largeImageUrl: String,
        privacy: String,
        ownerName: String
    ) : this(
        Long.MIN_VALUE,
        name,
        "event-" + Long.MIN_VALUE,
        description,
        locationName,
        startsAt,
        endsAt,
        null,
        null,
        null,
        false,
        privacy,
        thumbnailImageUrl,
        originalImageUrl,
        largeImageUrl,
        null,
        ownerName,
        false,
        "UTC",
        null,
        null,
        false,
        false,
        null
    )
}
