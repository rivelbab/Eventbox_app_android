package com.eventbox.app.android.networks.payloads.auth

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class LoginResponse(val accessToken: String)
