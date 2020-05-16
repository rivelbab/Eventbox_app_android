package com.eventbox.app.android.networks.payloads.auth

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id

class ResetPasswordResponse(
    @Id
    val id: String = "",
    val email: String? = null,
    val name: String? = null
)
