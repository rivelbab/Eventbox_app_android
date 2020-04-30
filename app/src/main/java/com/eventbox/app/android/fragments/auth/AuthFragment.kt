package com.eventbox.app.android.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_auth.view.email
import kotlinx.android.synthetic.main.fragment_auth.view.emailLayout
import kotlinx.android.synthetic.main.fragment_auth.view.getStartedButton
import kotlinx.android.synthetic.main.fragment_auth.view.rootLayout
import kotlinx.android.synthetic.main.fragment_auth.view.skipTextView
import kotlinx.android.synthetic.main.fragment_auth.view.toolbar
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.ComplexBackPressFragment
import com.eventbox.app.android.PLAY_STORE_BUILD_FLAVOR
import com.eventbox.app.android.R
import com.eventbox.app.android.auth.SmartAuthUtil
import com.eventbox.app.android.auth.SmartAuthViewModel
import com.eventbox.app.android.fragments.event.EVENT_DETAIL_FRAGMENT
import com.eventbox.app.android.ui.event.search.ORDER_COMPLETED_FRAGMENT
import com.eventbox.app.android.fragments.event.search.SEARCH_RESULTS_FRAGMENT
import com.eventbox.app.android.fragments.event.search.SEARCH_LOCATION_FRAGMENT
import com.eventbox.app.android.fragments.speakers.SPEAKERS_CALL_FRAGMENT
import com.eventbox.app.android.fragments.payment.TICKETS_FRAGMENT
import com.eventbox.app.android.utils.Utils.hideSoftKeyboard
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.extensions.setSharedElementEnterTransition
import com.eventbox.app.android.fragments.welcome.WELCOME_FRAGMENT
import com.eventbox.app.android.fragments.user.PROFILE_FRAGMENT
import com.eventbox.app.android.ui.auth.AuthViewModel
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthFragment : Fragment(), ComplexBackPressFragment {
    private lateinit var rootView: View
    private val authViewModel by viewModel<AuthViewModel>()
    private val safeArgs: AuthFragmentArgs by navArgs()
    private val smartAuthViewModel by sharedViewModel<SmartAuthViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_auth, container, false)
        setSharedElementEnterTransition()
        setupToolbar()
        checkCredentials()

        val progressDialog = progressDialog(context)

        val snackbarMessage = safeArgs.snackbarMessage
        if (!snackbarMessage.isNullOrEmpty()) rootView.snackbar(snackbarMessage)

        val email = safeArgs.email
        if (email != null) {
            rootView.email.setText(email)
        }

        rootView.skipTextView.isVisible = safeArgs.showSkipButton
        rootView.skipTextView.setOnClickListener {
            findNavController(rootView).navigate(
                AuthFragmentDirections.actionAuthToEventsPop()
            )
        }

        rootView.getStartedButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            if (!Patterns.EMAIL_ADDRESS.matcher(rootView.email.text.toString()).matches()) {
                rootView.emailLayout.error = getString(R.string.invalid_email_message)
                return@setOnClickListener
            }
            authViewModel.checkUser(rootView.email.text.toString())
        }

        rootView.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do Nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do Nothing*/ }
            override fun afterTextChanged(s: Editable?) {
                rootView.emailLayout.error = null
            }
        })

        authViewModel.isUserExists
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    redirectToLogin(rootView.email.text.toString())
                else
                    redirectToSignUp()
                authViewModel.mutableStatus.postValue(null)
            })

        authViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        smartAuthViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        authViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.rootLayout.longSnackbar(it)
            })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rootView.email.viewTreeObserver.addOnGlobalLayoutListener {
            startPostponedEnterTransition()
        }
    }

    private fun checkCredentials() {
        if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
            smartAuthViewModel.requestCredentials(
                SmartAuthUtil.getCredentialsClient(
                    requireActivity()
                )
            )
            smartAuthViewModel.isCredentialStored
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    if (it) redirectToLogin()
                })
        }
    }

    private fun setupToolbar() {
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun redirectToLogin(email: String = "") {
        findNavController(rootView).navigate(
            AuthFragmentDirections.actionAuthToLogIn(
                email,
                safeArgs.redirectedFrom,
                true
            ),
            FragmentNavigatorExtras(rootView.email to "emailLoginTransition"))
    }

    private fun redirectToSignUp() {
        findNavController(rootView).navigate(
            AuthFragmentDirections.actionAuthToSignUp(
                rootView.email.text.toString(),
                safeArgs.redirectedFrom
            ),
                FragmentNavigatorExtras(rootView.email to "emailSignUpTransition"))
    }

    override fun handleBackPress() {
        when (safeArgs.redirectedFrom) {
            TICKETS_FRAGMENT -> findNavController(rootView).popBackStack(R.id.ticketsFragment, false)
            EVENT_DETAIL_FRAGMENT -> findNavController(rootView).popBackStack(R.id.eventDetailsFragment, false)
            WELCOME_FRAGMENT -> findNavController(rootView).popBackStack(R.id.welcomeFragment, false)
            SEARCH_LOCATION_FRAGMENT -> findNavController(rootView).popBackStack(R.id.searchLocationFragment, false)
            PROFILE_FRAGMENT -> findNavController(rootView).popBackStack(R.id.profileFragment, false)
            SEARCH_RESULTS_FRAGMENT -> findNavController(rootView).popBackStack(R.id.searchResultsFragment, false)
            ORDER_COMPLETED_FRAGMENT -> findNavController(rootView).popBackStack(R.id.orderCompletedFragment, false)
            SPEAKERS_CALL_FRAGMENT -> findNavController(rootView).popBackStack(R.id.speakersCallFragment, false)
            else -> findNavController(rootView).navigate(AuthFragmentDirections.actionAuthToEventsPop())
        }
    }
}
