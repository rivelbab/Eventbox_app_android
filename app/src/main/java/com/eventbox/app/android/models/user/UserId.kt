package com.eventbox.app.android.models.user

import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import org.jetbrains.annotations.NotNull

@Type("user")
data class UserId(
    @Id
    @NotNull
    val id: String
)
