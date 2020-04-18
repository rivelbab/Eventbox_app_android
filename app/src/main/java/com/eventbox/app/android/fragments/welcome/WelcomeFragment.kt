package com.eventbox.app.android.fragments.welcome

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_welcome.view.currentLocation
import kotlinx.android.synthetic.main.fragment_welcome.view.locationProgressBar
import kotlinx.android.synthetic.main.fragment_welcome.view.pickCityButton
import kotlinx.android.synthetic.main.fragment_welcome.view.skip
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.location.GeoLocationViewModel
import com.eventbox.app.android.ui.event.search.SAVED_LOCATION
import com.eventbox.app.android.utils.Utils.isLocationEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOCATION_PERMISSION_REQUEST = 1000
const val WELCOME_FRAGMENT = "welcomeFragment"

class WelcomeFragment : Fragment() {
    private lateinit var rootView: View
    private val geoLocationViewModel by viewModel<GeoLocationViewModel>()
    val preference = Preference()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_welcome, container, false)
        val thisActivity = activity
        if (thisActivity is AppCompatActivity)
            thisActivity.supportActionBar?.hide()
        rootView.pickCityButton.setOnClickListener {
            Navigation.findNavController(rootView)
                .navigate(WelcomeFragmentDirections.actionWelcomeToSearch(WELCOME_FRAGMENT))
        }

        rootView.currentLocation.setOnClickListener {
            if (isLocationEnabled(requireContext())) {
                rootView.locationProgressBar.isVisible = true
                checkLocationPermission()
            }
        }

        rootView.skip.setOnClickListener {
            redirectToAuth()
        }

        geoLocationViewModel.location.observe(viewLifecycleOwner, Observer {
            preference.putString(SAVED_LOCATION, it)
            redirectToAuth()
        })

        geoLocationViewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            rootView.locationProgressBar.isVisible = false
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
        })

        return rootView
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    geoLocationViewModel.configure()
                } else {
                    Snackbar.make(rootView, "Cannot fetch location!", Snackbar.LENGTH_SHORT).show()
                    rootView.locationProgressBar.isVisible = false
                }
            }
        }
    }

    private fun checkLocationPermission() {
        val permission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST
            )
        } else {
            geoLocationViewModel.configure()
        }
    }

    private fun redirectToAuth() {
        Navigation.findNavController(rootView).navigate(WelcomeFragmentDirections.actionWelcomeToAuth(
                redirectedFrom = WELCOME_FRAGMENT, showSkipButton = true)
        )
    }
}
