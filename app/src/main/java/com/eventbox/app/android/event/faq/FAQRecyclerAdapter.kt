package com.eventbox.app.android.event.faq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.R
import com.eventbox.app.android.event.faq.FAQViewHolder

class FAQRecyclerAdapter : RecyclerView.Adapter<FAQViewHolder>() {
    val faqList = ArrayList<EventFAQ>()

    fun addAll(faqList: List<EventFAQ>) {
        if (faqList.isNotEmpty())
            this.faqList.clear()
        this.faqList.addAll(faqList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faq = faqList[position]

        holder.bind(faq)
    }

    override fun getItemCount(): Int {
        return faqList.size
    }
}
