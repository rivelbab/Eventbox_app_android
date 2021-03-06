package com.eventbox.app.android.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.eventbox.app.android.fragments.event.MyEventFragment
import com.eventbox.app.android.fragments.event.FavoriteFragment
import com.eventbox.app.android.fragments.event.EventJoinFragment

class EventMainAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    private val totalTabs = 3

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment = FavoriteFragment()

        when (position) {
            0 -> fragment = FavoriteFragment()
            1 -> fragment = EventJoinFragment()
            2 -> fragment = MyEventFragment()
        }
        return fragment
    }

    override fun getCount(): Int {
        return totalTabs
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return super.getPageTitle(position)
    }
}