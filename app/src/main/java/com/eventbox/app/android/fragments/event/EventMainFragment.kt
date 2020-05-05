package com.eventbox.app.android.fragments.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.eventbox.app.android.BottomIconDoubleClick

import com.eventbox.app.android.R
import com.eventbox.app.android.adapters.EventMainAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_favorite.view.*

class EventMainFragment : Fragment(), BottomIconDoubleClick {

    private lateinit var eventPagerAdapter: EventMainAdapter
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
         rootView = inflater.inflate(R.layout.fragment_event_main, container, false)


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventPagerAdapter = EventMainAdapter(childFragmentManager)
        viewPager = view.findViewById(R.id.eventViewPager)
        tabLayout = view.findViewById(R.id.tabLayout)
        configureTabLayout(tabLayout, viewPager)
        viewPager!!.adapter = eventPagerAdapter
        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
    }

    private fun configureTabLayout(tabLayout: TabLayout?, viewPager: ViewPager?) {

        tabLayout!!.addTab(tabLayout.newTab().setText(R.string.tab_favorite))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_saved))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_created))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        setupTabIcons(tabLayout)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun setupTabIcons(tabLayout: TabLayout?) {
        tabLayout!!.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_favorite_white)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_star_white)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_baseline_calendar_white)
    }

    override fun doubleClick() = rootView.scrollView.smoothScrollTo(0, 0)

}
