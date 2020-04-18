package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailDescription
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailLogo
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailName
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailType
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.sponsor.Sponsor
import com.eventbox.app.android.utils.SponsorUtils
import com.eventbox.app.android.utils.Utils
import com.eventbox.app.android.utils.stripHtml

class SponsorsDetailAdapter : RecyclerView.Adapter<SponsorsDetailViewHolder>() {

    private val sponsorList = mutableListOf<Sponsor>()
    lateinit var onURLClickListener: SponsorURLClickListener

    fun addAll(newSponsors: List<Sponsor>) {
        if (sponsorList.isNotEmpty()) sponsorList.clear()
        sponsorList.addAll(
            SponsorUtils.sortSponsorByLevel(
                newSponsors
            )
        )
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SponsorsDetailViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sponsor_detail, parent, false)
        return SponsorsDetailViewHolder(
            itemView
        )
    }

    override fun getItemCount(): Int = sponsorList.size

    override fun onBindViewHolder(holder: SponsorsDetailViewHolder, position: Int) {
        holder.apply {
            bind(sponsorList[position])
            sponsorURLClickListener = onURLClickListener
        }
    }
}

class SponsorsDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var sponsorURLClickListener: SponsorURLClickListener
    private val resource = Resource()

    fun bind(sponsor: Sponsor) {
        Picasso.get()
            .load(sponsor.logoUrl)
            .placeholder(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .error(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .into(itemView.sponsorDetailLogo)

        itemView.sponsorDetailName.text = sponsor.name
        if (sponsor.type.isNullOrBlank()) {
            itemView.sponsorDetailType.isVisible = false
        } else {
            itemView.sponsorDetailType.text = resource.getString(R.string.sponsor_type, sponsor.type)
            itemView.sponsorDetailType.isVisible = true
        }
        if (sponsor.description.isNullOrBlank()) {
            itemView.sponsorDetailDescription.isVisible = false
        } else {
            itemView.sponsorDetailDescription.text = sponsor.description.stripHtml()
            itemView.sponsorDetailDescription.isVisible = true
        }

        itemView.setOnClickListener {
            sponsorURLClickListener.onClick(sponsor.url)
        }
    }
}

interface SponsorURLClickListener {
    fun onClick(sponsorURL: String?)
}

interface SponsorClickListener {
    fun onClick()
}
