package com.eventbox.app.android.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.ui.event.search.SAVED_LOCATION
import kotlinx.android.synthetic.main.fragment_welcome.view.*

const val WELCOME_FRAGMENT = "welcomeFragment"
const val LOCATION = "Rechercher..."

class WelcomeFragment : Fragment() {
    private lateinit var rootView: View
    val preference = Preference()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_welcome, container, false)
        val thisActivity = activity
        if (thisActivity is AppCompatActivity)
            thisActivity.supportActionBar?.hide()

        rootView.welcomeLetsGoBtn.setOnClickListener {
            preference.putString(SAVED_LOCATION, LOCATION)
            redirectToAuth()
        }

        return rootView
    }

    private fun redirectToAuth() {
        Navigation.findNavController(rootView).navigate(WelcomeFragmentDirections.actionWelcomeToAuth(
                redirectedFrom = WELCOME_FRAGMENT, showSkipButton = true)
        )
    }
}
