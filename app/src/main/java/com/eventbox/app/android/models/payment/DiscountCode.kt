package com.eventbox.app.android.models.payment

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.models.event.EventId

@Type("discount-code")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class DiscountCode(
    @Id(IntegerIdHandler::class)
    val id: Int,
    val code: String,
    val validFrom: String? = null,
    val minQuantity: Int? = null,
    val createdAt: String? = null,
    val ticketsNumber: Int? = null,
    val value: Float? = null,
    val maxQuantity: Int? = null,
    val isActive: Boolean = false,
    val usedFor: String,
    val validTill: String? = null,
    val discountUrl: String? = null,
    val type: String,
    @Relationship("event")
    val eventId: EventId? = null,
    @Relationship("tickets")
    val tickets: List<TicketId>? = null
)
