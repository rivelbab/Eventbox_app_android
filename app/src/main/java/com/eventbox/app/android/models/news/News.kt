package com.eventbox.app.android.models.news


import androidx.room.PrimaryKey
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("news")
data class News (
    @Id(LongIdHandler::class)
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val publishedOn: String = "",
    val createdAt: String = "",
    val visible: Boolean = false
)