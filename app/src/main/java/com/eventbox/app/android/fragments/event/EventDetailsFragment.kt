package com.eventbox.app.android.fragments.event

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_fetching_event_error.view.retry
import kotlinx.android.synthetic.main.fragment_event.view.buttonTickets
import kotlinx.android.synthetic.main.fragment_event.view.container
import kotlinx.android.synthetic.main.fragment_event.view.eventErrorCard
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.common.EventClickListener
import com.eventbox.app.android.ui.common.FavoriteFabClickListener
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.utils.EventUtils.loadMapUrl
import com.eventbox.app.android.adapters.SimilarEventsListAdapter
import com.eventbox.app.android.adapters.FeedbackRecyclerAdapter
import com.eventbox.app.android.adapters.LIMITED_FEEDBACK_NUMBER
import com.eventbox.app.android.databinding.FragmentEventBinding
import com.eventbox.app.android.ui.event.EventDetailsViewModel
import com.eventbox.app.android.utils.*
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.extensions.nonNull
import kotlinx.android.synthetic.main.content_event.view.*
import kotlinx.android.synthetic.main.dialog_feedback.view.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val EVENT_DETAIL_FRAGMENT = "eventDetailFragment"

class EventDetailsFragment : Fragment() {

    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private val safeArgs: EventDetailsFragmentArgs by navArgs()
    private val feedbackAdapter = FeedbackRecyclerAdapter(true)
    private val similarEventsAdapter = SimilarEventsListAdapter()
    private var hasSimilarEvents: Boolean = false

    private lateinit var rootView: View
    private lateinit var binding: FragmentEventBinding
    private val LINE_COUNT: Int = 3
    private var menuActionBar: Menu? = null
    private var currentEvent: Event? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)
        val progressDialog = progressDialog(context, getString(R.string.loading_message))
        rootView = binding.root
        setToolbar(activity)
        setHasOptionsMenu(true)

        setupOrder()
        setupEventOverview()
        setupFeedback()
        setupSimilarEvents()

        rootView.buttonTickets.setOnClickListener {
            val ticketUrl = currentEvent?.ticketUrl
            if (!ticketUrl.isNullOrEmpty() &&
                Uri.parse(ticketUrl).host != getString(R.string.FRONTEND_HOST)) {
                Utils.openUrl(requireContext(), ticketUrl)
            } else {
                loadTicketFragment()
            }
        }

        eventViewModel.popMessage
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                showEventErrorScreen(it == getString(R.string.error_fetching_event_message))
            })

        eventViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        rootView.retry.setOnClickListener {
            currentEvent?.let { eventViewModel.loadEvent(it.id) }
        }

        return rootView
    }

    private fun setupOrder() {
        if (eventViewModel.orders.value == null) eventViewModel.loadOrders()
        eventViewModel.orders
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                it.forEach { order ->
                    if (order.event?.id == safeArgs.eventId) {
                        rootView.alreadyRegisteredLayout.isVisible = true
                        rootView.alreadyRegisteredLayout.setOnClickListener {
                            order.identifier?.let { identifier ->
                                EventDetailsFragmentDirections.actionEventDetailsToOrderDetail(
                                    eventId = safeArgs.eventId,
                                    orderId = order.id,
                                    orderIdentifier = identifier
                                )
                            }?.let { navigation ->
                                findNavController(rootView).navigate(navigation)
                            }
                        }
                        return@forEach
                    }
                }
            })
    }

    private fun setupEventOverview() {
        eventViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                currentEvent = it
                loadEvent(it)
                if (eventViewModel.similarEvents.value == null) {
                    val eventTypeId = it.eventType?.id ?: 0
                    val eventLocation = it.locationName
                    eventViewModel.fetchSimilarEvents(it.id, eventTypeId, eventLocation)
                }
                if (eventViewModel.eventFeedback.value == null) eventViewModel.fetchEventFeedback(it.id)

                if (eventViewModel.priceRange.value == null) eventViewModel.syncTickets(it)

                // Update favorite icon and external event url menu option
                activity?.invalidateOptionsMenu()

                Timber.d("Fetched events of id ${it.id}")
                showEventErrorScreen(false)
                setHasOptionsMenu(true)
            })

        val eventIdentifier = arguments?.getString(EVENT_IDENTIFIER)
        val event = eventViewModel.event.value
        when {
            event != null -> {
                currentEvent = event
                loadEvent(event)
            }
            !eventIdentifier.isNullOrEmpty() -> eventViewModel.loadEventByIdentifier(eventIdentifier)
            else -> eventViewModel.loadEvent(safeArgs.eventId)
        }

        // Set toolbar title to event name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootView.nestedContentEventScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY > rootView.eventName.height + rootView.eventImage.height)
                /*Toolbar title set to name of Event if scrolled more than
                combined height of eventImage and eventName views*/
                    setToolbar(activity, eventViewModel.event.value?.name ?: "")
                else
                // Toolbar title set to an empty string
                    setToolbar(activity)
            }
        }
    }

    private fun setupFeedback() {
        rootView.feedbackRv.layoutManager = LinearLayoutManager(context)
        rootView.feedbackRv.adapter = feedbackAdapter
        eventViewModel.feedbackProgress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.feedbackProgress.isVisible = it
                rootView.feedbackBtn.isVisible = !it
            })

        eventViewModel.eventFeedback.observe(viewLifecycleOwner, Observer {
            feedbackAdapter.addAll(it)
            if (it.isEmpty()) {
                rootView.feedbackRv.isVisible = false
                rootView.noFeedBackTv.isVisible = true
                rootView.seeFeedbackTextView.isVisible = false
            } else {
                rootView.feedbackRv.isVisible = true
                rootView.noFeedBackTv.isVisible = false
                rootView.seeFeedbackTextView.isVisible = it.size >= LIMITED_FEEDBACK_NUMBER
            }
        })

        eventViewModel.submittedFeedback
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (feedbackAdapter.itemCount < LIMITED_FEEDBACK_NUMBER)
                    feedbackAdapter.add(it)
                else
                    rootView.seeFeedbackTextView.isVisible = true
                rootView.feedbackRv.isVisible = true
                rootView.noFeedBackTv.isVisible = false
            })

        rootView.feedbackBtn.setOnClickListener {
            checkForAuthentication()
        }
    }

    private fun setupSimilarEvents() {
        eventViewModel.similarEventsProgress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.shimmerSimilarEvents.isVisible = it
                if (it) {
                    rootView.shimmerSimilarEvents.startShimmer()
                    rootView.similarEventsContainer.isVisible = true
                } else {
                    rootView.shimmerSimilarEvents.stopShimmer()
                    if (!similarEventsAdapter.currentList.isNullOrEmpty()) {
                        hasSimilarEvents = true
                    }
                    rootView.similarEventsContainer.isVisible = hasSimilarEvents
                }
            })

        val similarLinearLayoutManager = LinearLayoutManager(context)
        similarLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.similarEventsRecycler.layoutManager = similarLinearLayoutManager
        rootView.similarEventsRecycler.adapter = similarEventsAdapter

        eventViewModel.similarEvents
            .nonNull()
            .observe(viewLifecycleOwner, Observer { similarEvents ->
                similarEventsAdapter.submitList(similarEvents)
        })
    }

    private fun loadEvent(event: Event) {
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)
        binding.event = event
        binding.executePendingBindings()

        // Set Cover Image
        Picasso.get()
            .load(event.originalImageUrl)
            .placeholder(R.drawable.header)
            .into(rootView.eventImage)

        // About event on-click
        val aboutEventOnClickListener = View.OnClickListener {
            currentEvent?.let {
                findNavController(rootView).navigate(
                    EventDetailsFragmentDirections.actionEventDetailsToAboutEvent(
                        it.id
                    )
                )
            }
        }

        // Event Description Section
        val description = event.description.nullToEmpty().stripHtml()
        if (!description.isNullOrEmpty()) {

            rootView.eventDescription.post {
                if (rootView.eventDescription.lineCount > LINE_COUNT) {
                    rootView.seeMore.isVisible = true
                    // start about fragment
                    rootView.eventDescription.setOnClickListener(aboutEventOnClickListener)
                    rootView.seeMore.setOnClickListener(aboutEventOnClickListener)
                }
            }
        }

        // attendee count
        rootView.attendeeCount.text = "0"

        // event availability
        rootView.eventDispo.text = "places limitées !"

        // Date and Time section
        rootView.eventDateDetailsFirst.text = EventUtils.getFormattedEventDateTimeRange(startsAt, endsAt)
        rootView.eventDateDetailsSecond.text = EventUtils.getFormattedEventDateTimeRangeSecond(startsAt, endsAt)

        // Time section
        rootView.eventTimeDetailsFirst.text = EventUtils.getFormattedEventTimeRange(startsAt, endsAt)
        rootView.eventTimeDetailsSecond.text = EventUtils.getFormattedEventTimeRangeSecond(startsAt, endsAt)

        // Add event to Calendar
        val dateClickListener = View.OnClickListener { startCalendar(event) }
        rootView.eventTimingLinearLayout.setOnClickListener(dateClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventViewModel.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isConnected ->
                if (isConnected) {
                    val currentFeedback = eventViewModel.eventFeedback.value
                    if (currentFeedback == null) {
                        currentEvent?.let { eventViewModel.fetchEventFeedback(it.id) }
                    } else {
                        feedbackAdapter.addAll(currentFeedback)
                        if (currentFeedback.isEmpty()) {
                            rootView.feedbackRv.isVisible = false
                            rootView.noFeedBackTv.isVisible = true
                        } else {
                            rootView.feedbackRv.isVisible = true
                            rootView.noFeedBackTv.isVisible = false
                        }
                    }
                }
            })

        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long, imageView: ImageView) {
                findNavController(rootView)
                    .navigate(
                        EventDetailsFragmentDirections.actionSimilarEventsToEventDetails(
                            eventID
                        ),
                        FragmentNavigatorExtras(imageView to "eventDetailImage"))
            }
        }

        val redirectToLogin = object :
            RedirectToLogin {
            override fun goBackToLogin() {
                redirectToLogin()
            }
        }

        val favFabClickListener: FavoriteFabClickListener = object : FavoriteFabClickListener {
            override fun onClick(event: Event, itemPosition: Int) {
                if (eventViewModel.isLoggedIn()) {
                    event.favorite = !event.favorite
                    eventViewModel.setFavorite(event, event.favorite)
                    similarEventsAdapter.notifyItemChanged(itemPosition)
                } else {
                    EventUtils.showLoginToLikeDialog(
                        requireContext(),
                        layoutInflater, redirectToLogin, event.originalImageUrl, event.name
                    )
                }
            }
        }

        similarEventsAdapter.apply {
            onEventClick = eventClickListener
            onFavFabClick = favFabClickListener
        }
        rootView.seeFeedbackTextView.setOnClickListener {
            currentEvent?.let {
                findNavController(rootView).navigate(
                    EventDetailsFragmentDirections.actionEventDetailsToFeedback(
                        it.id
                    )
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.add_to_calendar -> {
                // Add event to Calendar
                currentEvent?.let { startCalendar(it) }
                true
            }
            R.id.report_event -> {
                currentEvent?.let { reportEvent(it) }
                true
            }
            R.id.favorite_event -> {
                currentEvent?.let {
                    if (eventViewModel.isLoggedIn()) {
                        it.favorite = !it.favorite
                        eventViewModel.setFavorite(it, it.favorite)
                        currentEvent = it
                    } else {
                        EventUtils.showLoginToLikeDialog(
                            requireContext(),
                            layoutInflater,
                            object :
                                RedirectToLogin {
                                override fun goBackToLogin() {
                                    redirectToLogin()
                                }
                            },
                            it.originalImageUrl,
                            it.name
                        )
                    }
                }
                true
            }
            R.id.event_share -> {
                currentEvent?.let { EventUtils.share(it, requireContext()) }
                return true
            }
            R.id.code_of_conduct -> {
                currentEvent?.let { event ->
                        findNavController(rootView)
                            .navigate(
                                EventDetailsFragmentDirections.actionEventDetailsToConductCode(
                                    event.id
                                )
                            )
                }
                return true
            }
            R.id.open_faqs -> {
                currentEvent?.let {
                    findNavController(rootView).navigate(
                        EventDetailsFragmentDirections.actionEventDetailsToFaq(
                            it.id
                        )
                    )
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description?.stripHtml())
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.locationName)
        intent.putExtra(CalendarContract.Events.CALENDAR_TIME_ZONE, event.timezone)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
            EventUtils.getTimeInMilliSeconds(event.startsAt, event.timezone)
        )
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
            EventUtils.getTimeInMilliSeconds(event.endsAt, event.timezone)
        )
        startActivity(intent)
    }

    private fun reportEvent(event: Event) {
        val email = "support@eventbox.com"
        val subject = "Report of ${event.name} (${event.identifier})"
        val body = "Let us know what's wrong"
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)

        startActivity(Intent.createChooser(emailIntent, "Chooser Title"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_details, menu)
        menuActionBar = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        currentEvent?.let { currentEvent ->
            if (currentEvent.codeOfConduct.isNullOrBlank())
                menu.findItem(R.id.code_of_conduct).isVisible = false
            setFavoriteIconFilled(currentEvent.favorite)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Picasso.get().cancelRequest(rootView.eventImage)
        similarEventsAdapter.apply {
            onEventClick = null
            onFavFabClick = null
        }
    }

    private fun loadTicketFragment() {
        val currency = currentEvent?.paymentCurrency ?: "USD"
        currentEvent?.let {
            findNavController(rootView).navigate(
                EventDetailsFragmentDirections.actionEventDetailsToTickets(
                    it.id,
                    currency
                )
            )
        }
    }

    private fun startMap(event: Event) {
        // start map intent
        val mapUrl = loadMapUrl(event)
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        val packageManager = activity?.packageManager
        if (packageManager != null && mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }

    private fun setFavoriteIconFilled(filled: Boolean) {
        val id = when {
            filled -> R.drawable.ic_baseline_favorite_white
            else -> R.drawable.ic_baseline_favorite_border_white
        }
        menuActionBar?.findItem(R.id.favorite_event)?.icon = ContextCompat.getDrawable(requireContext(), id)
    }

    private fun showEventErrorScreen(show: Boolean) {
        rootView.container.isVisible = !show
        rootView.eventErrorCard.isVisible = show
        val menuItemSize = menuActionBar?.size() ?: 0
        for (i in 0 until menuItemSize) {
            menuActionBar?.getItem(i)?.isVisible = !show
        }
    }

    private fun checkForAuthentication() {
        if (eventViewModel.isLoggedIn())
            writeFeedback()
        else {
            rootView.nestedContentEventScroll.longSnackbar(getString(R.string.log_in_first))
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        findNavController(rootView).navigate(
            EventDetailsFragmentDirections.actionEventDetailsToAuth(
                getString(R.string.log_in_first),
                EVENT_DETAIL_FRAGMENT
            )
        )
    }

    private fun writeFeedback() {
        val layout = layoutInflater.inflate(R.layout.dialog_feedback, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.submit_feedback))
            .setView(layout)
            .setPositiveButton(getString(R.string.submit)) { _, _ ->
                currentEvent?.let {
                    eventViewModel.submitFeedback(layout.feedback.text.toString(),
                        layout.feedbackrating.rating,
                        it.id)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }

            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        layout.feedback.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                    if (layout.feedback.text.toString().isNotEmpty()) {
                        layout.feedbackTextInputLayout.error = null
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        layout.feedbackTextInputLayout.isErrorEnabled = false
                    } else {
                        layout.feedbackTextInputLayout.error = getString(R.string.cant_be_empty)
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
            })
    }
}
