package com.eventbox.app.android.fragments.payment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.filter
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.filterToolbar
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.findMyTickets
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.noTicketsScreen
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ordersRecycler
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.pastEvent
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.scrollView
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.shimmerSearch
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.swipeRefresh
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ticketsNumber
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.ticketsTitle
import kotlinx.android.synthetic.main.fragment_orders_under_user.view.toolbarLayout
import com.eventbox.app.android.BottomIconDoubleClick
import com.eventbox.app.android.adapters.OrdersPagedListAdapter
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.payment.OrdersUnderUserViewModel
import com.eventbox.app.android.utils.Utils
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.hideWithFading
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.extensions.showWithFading
import kotlinx.android.synthetic.main.dialog_filter_order.view.*
import org.jetbrains.anko.design.longSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val ORDERS_FRAGMENT = "ordersFragment"

class OrdersUnderUserFragment : Fragment(), BottomIconDoubleClick {

    private lateinit var rootView: View
    private val ordersUnderUserVM by viewModel<OrdersUnderUserViewModel>()
    private val ordersPagedListAdapter =
        OrdersPagedListAdapter()

    override fun onStart() {
        super.onStart()
        if (!ordersUnderUserVM.isLoggedIn()) {
            redirectToLogin()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_orders_under_user, container, false)
        setToolbar(activity, show = false)

        ordersPagedListAdapter.setShowExpired(false)
        rootView.ordersRecycler.adapter = ordersPagedListAdapter
        rootView.ordersRecycler.isNestedScrollingEnabled = false

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rootView.ordersRecycler.layoutManager = linearLayoutManager

        ordersUnderUserVM.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isConnected ->
                val currentItems = ordersUnderUserVM.eventAndOrderPaged.value
                if (currentItems != null) {
                    showNoInternetScreen(false)
                    showNoTicketsScreen(currentItems.size == 0)
                    ordersPagedListAdapter.submitList(currentItems)
                } else {
                    ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = true)
                }
            })

        ordersUnderUserVM.numOfTickets
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.ticketsNumber.text = resources.getQuantityString(R.plurals.numOfOrders, it, it)
                showNoTicketsScreen(it == 0 && !rootView.shimmerSearch.isVisible)
            })

        ordersUnderUserVM.showShimmerResults
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.shimmerSearch.startShimmer()
                    showNoTicketsScreen(false)
                    showNoInternetScreen(false)
                } else {
                    rootView.shimmerSearch.stopShimmer()
                    rootView.swipeRefresh.isRefreshing = false
                }
                rootView.shimmerSearch.isVisible = it
            })

        ordersUnderUserVM.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        ordersUnderUserVM.eventAndOrderPaged
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                ordersPagedListAdapter.submitList(it)
            })

        rootView.swipeRefresh.setColorSchemeColors(Color.BLUE)
        rootView.swipeRefresh.setOnRefreshListener {
            if (ordersUnderUserVM.isConnected()) {
                ordersUnderUserVM.clearOrders()
                ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = false)
            } else {
                rootView.swipeRefresh.isRefreshing = false
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewClickListener = object : OrdersPagedListAdapter.OrderClickListener {
            override fun onClick(eventID: Long, orderIdentifier: String, orderId: Long) {
                findNavController(rootView).navigate(
                    OrdersUnderUserFragmentDirections.actionOrderUserToOrderDetails(
                        eventID,
                        orderIdentifier,
                        orderId
                    )
                )
            }
        }
        ordersPagedListAdapter.setListener(recyclerViewClickListener)

        rootView.pastEvent.setOnClickListener {
            findNavController(rootView).navigate(OrdersUnderUserFragmentDirections.actionOrderUserToOrderExpired())
        }
        rootView.findMyTickets.setOnClickListener {
            Utils.openUrl(requireContext(), resources.getString(R.string.ticket_issues_url))
        }
        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.ticketsTitle.y && !rootView.toolbarLayout.isVisible) {
                rootView.toolbarLayout.showWithFading()
            } else if (scrollY < rootView.ticketsTitle.y && rootView.toolbarLayout.isVisible) {
                rootView.toolbarLayout.hideWithFading()
            }
        }
        rootView.filter.setOnClickListener {
            showFilterDialog()
        }
        rootView.filterToolbar.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val filterLayout = layoutInflater.inflate(R.layout.dialog_filter_order, null)
        filterLayout.completedOrdersCheckBox.isChecked = ordersUnderUserVM.filter.isShowingCompletedOrders
        filterLayout.pendingOrdersCheckBox.isChecked = ordersUnderUserVM.filter.isShowingPendingOrders
        filterLayout.placedOrdersCheckBox.isChecked = ordersUnderUserVM.filter.isShowingPlacedOrders
        if (ordersUnderUserVM.filter.isSortingOrdersByDate)
            filterLayout.dateRadioButton.isChecked = true
        else
            filterLayout.orderStatusRadioButton.isChecked = true

        AlertDialog.Builder(requireContext())
            .setView(filterLayout)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }.setPositiveButton(getString(R.string.apply)) { _, _ ->
                ordersUnderUserVM.filter.isShowingCompletedOrders = filterLayout.completedOrdersCheckBox.isChecked
                ordersUnderUserVM.filter.isShowingPendingOrders = filterLayout.pendingOrdersCheckBox.isChecked
                ordersUnderUserVM.filter.isShowingPlacedOrders = filterLayout.placedOrdersCheckBox.isChecked
                ordersUnderUserVM.filter.isSortingOrdersByDate = filterLayout.dateRadioButton.isChecked
                applyFilter()
            }.create().show()
    }

    private fun applyFilter() {
        ordersUnderUserVM.clearOrders()
        ordersPagedListAdapter.clear()
        if (ordersUnderUserVM.isConnected()) {
            ordersUnderUserVM.getOrdersAndEventsOfUser(showExpired = false, fromDb = false)
        } else {
            showNoInternetScreen(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView.swipeRefresh.setOnRefreshListener(null)
        ordersPagedListAdapter.setListener(null)
    }

    private fun showNoTicketsScreen(show: Boolean) {
        rootView.noTicketsScreen.isVisible = show
    }

    private fun redirectToLogin() {
        findNavController(rootView).navigate(
            OrdersUnderUserFragmentDirections.actionOrderUserToAuth(
                getString(R.string.log_in_first),
                ORDERS_FRAGMENT
            )
        )
    }

    private fun showNoInternetScreen(show: Boolean) {
        if (show) {
            rootView.shimmerSearch.isVisible = false
            showNoTicketsScreen(false)
            ordersPagedListAdapter.clear()
        }
        rootView.noInternetCard.isVisible = show
    }

    override fun doubleClick() = rootView.scrollView.smoothScrollTo(0, 0)

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
