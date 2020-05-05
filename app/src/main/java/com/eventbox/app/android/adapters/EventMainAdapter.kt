package com.eventbox.app.android.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.eventbox.app.android.fragments.event.CreatedEventFragment
import com.eventbox.app.android.fragments.event.FavoriteFragment
import com.eventbox.app.android.fragments.event.InterestedEventFragment

class EventMainAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    private val totalTabs = 3

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment = FavoriteFragment()

        when (position) {
            0 -> fragment = FavoriteFragment()
            1 -> fragment = InterestedEventFragment()
            2 -> fragment = CreatedEventFragment()
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