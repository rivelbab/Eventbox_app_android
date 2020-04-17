package com.eventbox.app.android.auth.change

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class Password(val oldPassword: String, val newPassword: String)
