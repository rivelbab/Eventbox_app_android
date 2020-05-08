package com.eventbox.app.android.fragments.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteEventsRecycler
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteProgressBar
import kotlinx.android.synthetic.main.fragment_favorite.view.findText
import kotlinx.android.synthetic.main.fragment_favorite.view.likesNumber
import kotlinx.android.synthetic.main.fragment_favorite.view.likesTitle
import kotlinx.android.synthetic.main.fragment_favorite.view.monthChip
import kotlinx.android.synthetic.main.fragment_favorite.view.noLikedLL
import kotlinx.android.synthetic.main.fragment_favorite.view.scrollView
import kotlinx.android.synthetic.main.fragment_favorite.view.todayChip
import kotlinx.android.synthetic.main.fragment_favorite.view.tomorrowChip
import kotlinx.android.synthetic.main.fragment_favorite.view.toolbarLayout
import kotlinx.android.synthetic.main.fragment_favorite.view.weekendChip
import com.eventbox.app.android.BottomIconDoubleClick
import com.eventbox.app.android.R
import com.eventbox.app.android.adapters.FavoriteEventsListAdapter
import com.eventbox.app.android.ui.common.EventClickListener
import com.eventbox.app.android.ui.common.FavoriteFabClickListener
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.fragments.message.MESSAGE_FRAGMENT
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.utils.EventUtils.getEventDateTime
import com.eventbox.app.android.ui.event.search.SAVED_LOCATION
import com.eventbox.app.android.ui.event.FavoriteEventsViewModel
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.hideWithFading
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.extensions.showWithFading
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class EventJoinFragment : Fragment(), BottomIconDoubleClick {
    private val favoriteEventViewModel by viewModel<FavoriteEventsViewModel>()
    private lateinit var rootView: View
    private val favoriteEventsRecyclerAdapter =
        FavoriteEventsListAdapter()

    override fun onStart() {
        super.onStart()
        if (!favoriteEventViewModel.isLoggedIn())
            redirectToLogin()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //setPostponeSharedElementTransition()
        rootView = inflater.inflate(R.layout.fragment_event_join, container, false)
        rootView.favoriteEventsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.favoriteEventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.favoriteEventsRecycler.isNestedScrollingEnabled = false
        rootView.viewTreeObserver.addOnDrawListener {
            //setStartPostponedEnterTransition()
        }

        rootView.findText.setOnClickListener {
            findNavController(rootView).navigate(FavoriteFragmentDirections.actionFavouriteToSearch())
        }

        rootView.todayChip.setOnClickListener {
            openSearchResult(rootView.todayChip.text.toString())
        }
        rootView.tomorrowChip.setOnClickListener {
            openSearchResult(rootView.tomorrowChip.text.toString())
        }
        rootView.weekendChip.setOnClickListener {
            openSearchResult(rootView.weekendChip.text.toString())
        }
        rootView.monthChip.setOnClickListener {
            openSearchResult(rootView.monthChip.text.toString())
        }

        favoriteEventViewModel.events
            .nonNull()
            .observe(viewLifecycleOwner, Observer { list ->
                favoriteEventsRecyclerAdapter.submitList(list.sortedBy { getEventDateTime(it.startsAt, it.timezone) })
                rootView.likesNumber.text = resources.getQuantityString(R.plurals.events_number, list.size, list.size)
                showEmptyMessage(list.size)
                Timber.d("Fetched events of size %s", list.size)
            })

        favoriteEventViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        favoriteEventViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.favoriteProgressBar.isIndeterminate = it
                rootView.favoriteProgressBar.isVisible = it
            })

        favoriteEventViewModel.loadInterestedEvents()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long, imageView: ImageView) {
                findNavController(rootView).navigate(
                    FavoriteFragmentDirections.actionFavouriteToEventDetails(
                        eventID
                    ),
                        FragmentNavigatorExtras(imageView to "eventDetailImage"))
            }
        }

        val favFabClickListener: FavoriteFabClickListener = object : FavoriteFabClickListener {
            override fun onClick(event: Event, itemPosition: Int) {
                favoriteEventViewModel.setInterested(event, false)
                favoriteEventsRecyclerAdapter.notifyItemChanged(itemPosition)
                rootView.snackbar(getString(R.string.removed_from_liked, event.name),
                    getString(R.string.undo)) {
                    favoriteEventViewModel.setInterested(event, true)
                    favoriteEventsRecyclerAdapter.notifyItemChanged(itemPosition)
                }
            }
        }

        favoriteEventsRecyclerAdapter.apply {
            onEventClick = eventClickListener
            onFavFabClick = favFabClickListener
        }

        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.likesTitle.y && !rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.showWithFading()
            else if (scrollY < rootView.likesTitle.y && rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.hideWithFading()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favoriteEventsRecyclerAdapter.apply {
            onEventClick = null
            onFavFabClick = null
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbar(activity, show = false)
    }

    override fun doubleClick() = rootView.scrollView.smoothScrollTo(0, 0)

    private fun showEmptyMessage(itemCount: Int) {
        rootView.noLikedLL.isVisible = (itemCount == 0)
    }

    private fun redirectToLogin() {
        findNavController(rootView).navigate(
            FavoriteFragmentDirections.actionFavouriteToAuth(
                getString(R.string.log_in_first),
                MESSAGE_FRAGMENT
            )
        )
    }

    private fun openSearchResult(time: String) {
        findNavController(rootView).navigate(
            FavoriteFragmentDirections.actionFavouriteToSearchResults(
                query = "",
                location = Preference().getString(SAVED_LOCATION).toString(),
                type = getString(R.string.anything),
                date = time
            )
        )
    }
}
