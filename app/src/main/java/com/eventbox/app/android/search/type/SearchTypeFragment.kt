package com.eventbox.app.android.search.type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.fragment_search_type.view.eventTypesRecyclerView
import kotlinx.android.synthetic.main.fragment_search_type.view.eventTypesTextTitle
import kotlinx.android.synthetic.main.fragment_search_type.view.shimmerSearchEventTypes
import kotlinx.android.synthetic.main.fragment_search_type.view.toolbar
import com.eventbox.app.android.R
import com.eventbox.app.android.search.SEARCH_FILTER_FRAGMENT
import com.eventbox.app.android.search.SearchFilterFragmentArgs
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchTypeFragment : Fragment() {
    private val typesAdapter: SearchTypeAdapter = SearchTypeAdapter()
    private val searchTypeViewModel by viewModel<SearchTypeViewModel>()
    private val safeArgs: SearchTypeFragmentArgs by navArgs()
    private lateinit var rootView: View
    private val eventTypesList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search_type, container, false)
        setToolbar(activity, show = false)
        rootView.eventTypesRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.eventTypesRecyclerView.adapter = typesAdapter
        searchTypeViewModel.loadEventTypes()
        eventTypesList.add(getString(R.string.anything))

        searchTypeViewModel.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isConnected ->
                if (isConnected) {
                    searchTypeViewModel.loadEventTypes()
                    showNoInternetError(false)
                } else {
                    showNoInternetError(searchTypeViewModel.eventTypes.value == null)
                }
            })

        searchTypeViewModel.showShimmer
            .nonNull()
            .observe(viewLifecycleOwner, Observer { shouldShowShimmer ->
                if (shouldShowShimmer) {
                    rootView.shimmerSearchEventTypes.startShimmer()
                } else {
                    rootView.shimmerSearchEventTypes.stopShimmer()
                }
                rootView.shimmerSearchEventTypes.isVisible = shouldShowShimmer
            })

        searchTypeViewModel.eventTypes
            .nonNull()
            .observe(viewLifecycleOwner, Observer { list ->
                list.forEach {
                    eventTypesList.add(it.name)
                }
                setCurrentChoice(safeArgs.type)
                typesAdapter.addAll(eventTypesList)
                typesAdapter.notifyDataSetChanged()
            })

        val listener: TypeClickListener = object : TypeClickListener {
            override fun onClick(chosenType: String) {
                redirectToSearch(chosenType)
            }
        }
        typesAdapter.setListener(listener)

        rootView.retry.setOnClickListener {
            if (searchTypeViewModel.isConnected()) searchTypeViewModel.loadEventTypes()
        }

        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        return rootView
    }

    private fun redirectToSearch(type: String) {
        searchTypeViewModel.saveType(type)
        val destFragId = if (safeArgs.fromFragmentName == SEARCH_FILTER_FRAGMENT)
            R.id.action_search_type_to_search_filter
        else
            R.id.action_search_type_to_search

        val navArgs = if (safeArgs.fromFragmentName == SEARCH_FILTER_FRAGMENT) {
            SearchFilterFragmentArgs(
                query = safeArgs.query
            ).toBundle()
        } else
            null
        findNavController(rootView).navigate(destFragId, navArgs)
    }

    private fun setCurrentChoice(value: String?) {
        for (pos in 0 until eventTypesList.size) {
            if (eventTypesList[pos] == value) {
                typesAdapter.setCheckTypePosition(pos)
                return
            }
        }
    }

    private fun showNoInternetError(show: Boolean) {
        rootView.noInternetCard.isVisible = show
        rootView.eventTypesRecyclerView.isVisible = !show
        rootView.eventTypesTextTitle.isVisible = !show
    }
}
