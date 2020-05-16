package com.eventbox.app.android.fragments.event

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventContent
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventDetails
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventImage
import kotlinx.android.synthetic.main.fragment_about_event.view.appBar
import kotlinx.android.synthetic.main.fragment_about_event.view.detailsHeader
import kotlinx.android.synthetic.main.fragment_about_event.view.eventName
import kotlinx.android.synthetic.main.fragment_about_event.view.progressBarAbout
import com.eventbox.app.android.R
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.utils.EventUtils
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.stripHtml
import com.eventbox.app.android.ui.event.AboutEventViewModel
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutEventFragment : Fragment() {
    private lateinit var rootView: View
    private val aboutEventViewModel by viewModel<AboutEventViewModel>()
    private lateinit var eventExtra: Event
    private val safeArgs: AboutEventFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = layoutInflater.inflate(R.layout.fragment_about_event, container, false)

        setToolbar(activity)
        setHasOptionsMenu(true)

        aboutEventViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEvent(it)
            })

        aboutEventViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        aboutEventViewModel.progressAboutEvent
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBarAbout.isVisible = it
            })

        aboutEventViewModel.loadEvent(safeArgs.eventId)

        return rootView
    }

    private fun loadEvent(event: Event) {
        eventExtra = event
        rootView.aboutEventContent.movementMethod = LinkMovementMethod.getInstance()
        rootView.aboutEventContent.text = event.description?.stripHtml()
        val startsAt = EventUtils.getEventDateTime(event.startsAt, "UTC")
        val endsAt = EventUtils.getEventDateTime(event.endsAt, "UTC")

        rootView.aboutEventDetails.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        rootView.eventName.text = event.name
        Picasso.get()
            .load(event.originalImageUrl)
            .placeholder(R.drawable.header)
            .into(rootView.aboutEventImage)

        rootView.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
            if (Math.abs(offset) == appBarLayout.totalScrollRange) {
                rootView.detailsHeader.isVisible = false
                setToolbar(activity, event.name)
            } else {
                rootView.detailsHeader.isVisible = true
                setToolbar(activity)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
