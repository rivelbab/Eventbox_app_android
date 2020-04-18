package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import com.eventbox.app.android.R
import com.eventbox.app.android.models.event.SocialLink
import com.eventbox.app.android.ui.event.SocialLinksViewHolder

class SocialLinksRecyclerAdapter : RecyclerView.Adapter<SocialLinksViewHolder>() {

    private val socialLinks = ArrayList<SocialLink>()

    fun addAll(socialLinkList: List<SocialLink>) {
        if (socialLinkList.isNotEmpty())
            this.socialLinks.clear()
        this.socialLinks.addAll(socialLinkList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialLinksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_social_link, parent, false)
        return SocialLinksViewHolder(
            view,
            parent.context
        )
    }

    override fun onBindViewHolder(holder: SocialLinksViewHolder, position: Int) {
        val socialLink = socialLinks[position]
        holder.bind(socialLink)
    }

    override fun getItemCount(): Int {
        return socialLinks.size
    }
}
