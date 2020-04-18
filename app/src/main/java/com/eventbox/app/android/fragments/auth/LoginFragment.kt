package com.eventbox.app.android.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_login.email
import kotlinx.android.synthetic.main.fragment_login.loginButton
import kotlinx.android.synthetic.main.fragment_login.password
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_login.view.email
import kotlinx.android.synthetic.main.fragment_login.view.skipTextView
import kotlinx.android.synthetic.main.fragment_login.view.toolbar
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.PLAY_STORE_BUILD_FLAVOR
import com.eventbox.app.android.R
import com.eventbox.app.android.auth.SmartAuthUtil
import com.eventbox.app.android.auth.SmartAuthViewModel
import com.eventbox.app.android.fragments.event.EVENT_DETAIL_FRAGMENT
import com.eventbox.app.android.fragments.event.FAVORITE_FRAGMENT
import com.eventbox.app.android.fragments.notification.NOTIFICATION_FRAGMENT
import com.eventbox.app.android.fragments.payment.ORDERS_FRAGMENT
import com.eventbox.app.android.ui.event.search.ORDER_COMPLETED_FRAGMENT
import com.eventbox.app.android.fragments.event.search.SEARCH_RESULTS_FRAGMENT
import com.eventbox.app.android.fragments.speakers.SPEAKERS_CALL_FRAGMENT
import com.eventbox.app.android.fragments.payment.TICKETS_FRAGMENT
import com.eventbox.app.android.utils.Utils.hideSoftKeyboard
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.Utils.showNoInternetDialog
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.extensions.setSharedElementEnterTransition
import com.eventbox.app.android.fragments.user.PROFILE_FRAGMENT
import com.eventbox.app.android.ui.auth.LoginViewModel
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    private val loginViewModel by viewModel<LoginViewModel>()
    private val smartAuthViewModel by sharedViewModel<SmartAuthViewModel>()
    private lateinit var rootView: View
    private val safeArgs: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        val progressDialog = progressDialog(context)
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        rootView.settings.setOnClickListener {
            findNavController(rootView).navigate(LoginFragmentDirections.actionLoginToSetting())
        }

        if (loginViewModel.isLoggedIn())
            popBackStack()

        rootView.loginButton.setOnClickListener {
            loginViewModel.login(email.text.toString(), password.text.toString())
            hideSoftKeyboard(context, rootView)
        }

        rootView.skipTextView.isVisible = safeArgs.showSkipButton
        rootView.skipTextView.setOnClickListener {
            findNavController(rootView).navigate(
                LoginFragmentDirections.actionLoginToEventsPop()
            )
        }

        if (safeArgs.email.isNotEmpty()) {
            setSharedElementEnterTransition()
            rootView.email.text = SpannableStringBuilder(safeArgs.email)
            rootView.email.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (s.toString() != safeArgs.email) {
                        findNavController(rootView).navigate(
                            LoginFragmentDirections.actionLoginToAuthPop(
                                redirectedFrom = safeArgs.redirectedFrom,
                                email = s.toString()
                            )
                        )
                        rootView.email.removeTextChangedListener(this)
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { /*Do Nothing*/ }
                override fun onTextChanged(email: CharSequence, start: Int, before: Int, count: Int) { /*Do Nothing*/ }
            })
        } else if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {

            smartAuthViewModel.requestCredentials(
                SmartAuthUtil.getCredentialsClient(
                    requireActivity()
                )
            )

            smartAuthViewModel.id
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    email.text = SpannableStringBuilder(it)
                })

            smartAuthViewModel.password
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    password.text = SpannableStringBuilder(it)
                })

            smartAuthViewModel.apiExceptionCodePair.nonNull().observe(viewLifecycleOwner, Observer {
                SmartAuthUtil.handleResolvableApiException(
                    it.first, requireActivity(), it.second
                )
            })

            smartAuthViewModel.progress
                .nonNull()
                .observe(viewLifecycleOwner, Observer {
                    progressDialog.show(it)
                })
        }

        loginViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        loginViewModel.showNoInternetDialog
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                showNoInternetDialog(context)
            })

        loginViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.loginCoordinatorLayout.longSnackbar(it)
            })

        loginViewModel.loggedIn
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loginViewModel.fetchProfile()
            })

        rootView.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(password: CharSequence, start: Int, before: Int, count: Int) {
                    loginButton.isEnabled = password.isNotEmpty()
            }
        })

        loginViewModel.requestTokenSuccess
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it.status) {
                    rootView.mailSentTextView.text = it.message
                    rootView.sentEmailLayout.isVisible = true
                    rootView.tick.isVisible = true
                    rootView.loginLayout.isVisible = false
                    rootView.toolbar.isVisible = true
                } else {
                    rootView.toolbar.isVisible = false
                }
            })

        rootView.tick.setOnClickListener {
            rootView.sentEmailLayout.isVisible = false
            rootView.tick.isVisible = false
            rootView.toolbar.isVisible = true
            rootView.loginLayout.isVisible = true
        }

        rootView.forgotPassword.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            loginViewModel.sendResetPasswordEmail(email.text.toString())
        }

        loginViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
                    smartAuthViewModel.saveCredential(
                        email.text.toString(), password.text.toString(),
                        SmartAuthUtil.getCredentialsClient(
                            requireActivity()
                        )
                    )
                }
                popBackStack()
            })

        return rootView
    }

    private fun popBackStack() {
        val destinationId =
        when (safeArgs.redirectedFrom) {
            PROFILE_FRAGMENT -> R.id.profileFragment
            EVENT_DETAIL_FRAGMENT -> R.id.eventDetailsFragment
            ORDERS_FRAGMENT -> R.id.orderUnderUserFragment
            TICKETS_FRAGMENT -> R.id.ticketsFragment
            NOTIFICATION_FRAGMENT -> R.id.notificationFragment
            SPEAKERS_CALL_FRAGMENT -> R.id.speakersCallFragment
            FAVORITE_FRAGMENT -> R.id.favoriteFragment
            SEARCH_RESULTS_FRAGMENT -> R.id.searchResultsFragment
            ORDER_COMPLETED_FRAGMENT -> R.id.orderCompletedFragment
            else -> R.id.eventsFragment
        }
        if (destinationId == R.id.eventsFragment) {
            findNavController(rootView).navigate(LoginFragmentDirections.actionLoginToEventsPop())
        } else {
            findNavController(rootView).popBackStack(destinationId, false)
        }
        rootView.snackbar(R.string.welcome_back)
    }
}
