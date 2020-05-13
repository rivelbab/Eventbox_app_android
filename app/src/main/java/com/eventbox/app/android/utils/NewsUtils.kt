package com.eventbox.app.android.utils

import android.content.Context
import android.content.Intent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.news.News

object NewsUtils {
    /**
     *  share news detail
     */
    fun share(news: News, context: Context) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, news.title)
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            news.content)
        context.startActivity(Intent.createChooser(sendIntent, "Share News Details"))
    }
}