package com.eventbox.app.android.models.user

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("user")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class User(
    @Id
    @PrimaryKey
    val id: String = "",
    val email: String? = null,
    val name: String? = null,
    val details: String? = null,
    val phone: String? = null,
    //== image details
    val avatarUrl: String? = null,
    //=== security details
    val emailVerified: Boolean = false
)
