package com.eventbox.app.android.models.news

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("news")
data class News (
    @Id
    val id: String? = null,
    val title: String,
    val content: String,
    val author: String,
    val publishedOn: String? = null,
    val createdAt: String? = null,
    val visible: Boolean = false,
    val isNanterreNews: Boolean = false
)