package com.eventbox.app.android.fragments.user

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import com.squareup.picasso.Picasso
import com.eventbox.app.android.BottomIconDoubleClick
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.utils.CircleTransform
import com.eventbox.app.android.PLAY_STORE_BUILD_FLAVOR
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.auth.LoginViewModel
import com.eventbox.app.android.auth.SmartAuthUtil
import com.eventbox.app.android.auth.SmartAuthViewModel
import com.eventbox.app.android.fragments.auth.MINIMUM_PASSWORD_LENGTH
import com.eventbox.app.android.utils.Utils
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.requireDrawable
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.VERIFICATION_TOKEN
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.nullToEmpty
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.ui.user.ProfileViewModel
import kotlinx.android.synthetic.main.dialog_delete_account.view.*
import kotlinx.android.synthetic.main.dialog_change_password.view.*
import kotlinx.android.synthetic.main.dialog_confirm_delete_account.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val PROFILE_FRAGMENT = "profileFragment"

class ProfileFragment : Fragment(), BottomIconDoubleClick {
    private val profileViewModel by viewModel<ProfileViewModel>()
    private val smartAuthViewModel by viewModel<SmartAuthViewModel>()
    private val loginViewModel by viewModel<LoginViewModel>()

    private lateinit var rootView: View
    private var emailSettings: String = ""
    private var user: User? = null

    private fun redirectToLogin() {
        findNavController(rootView).navigate(
            ProfileFragmentDirections.actionProfileToAuth(
                PROFILE_FRAGMENT, showSkipButton = true
            )
        )
    }

    private fun redirectToEventsFragment() {
        findNavController(rootView).popBackStack(R.id.eventsFragment, false)
    }

    override fun onStart() {
        super.onStart()
        handleLayoutVisibility(profileViewModel.isLoggedIn())
    }

    private fun handleLayoutVisibility(isLoggedIn: Boolean) {
        rootView.login.isVisible = !isLoggedIn
        rootView.logout.isVisible = isLoggedIn
        rootView.profileSettingContainer.isVisible = isLoggedIn
        rootView.accountInfoContainer.isVisible = isLoggedIn
        rootView.fabEditProfile.isVisible = isLoggedIn
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        setToolbar(activity, show = false)
        if (profileViewModel.isLoggedIn()) {
            profileViewModel.getProfile()
            profileViewModel.syncProfile()
        }

        val token = arguments?.getString(VERIFICATION_TOKEN)
        if (token != null)
            profileViewModel.verifyProfile(token)

        /*val progressDialog = progressDialog(context, getString(R.string.loading_message))
        profileViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })*/

        profileViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        profileViewModel.accountDeleted
            .nonNull()
            .observe(viewLifecycleOwner, Observer { accountDeleted ->
                if (accountDeleted) {
                    rootView.snackbar(getString(R.string.success_deleting_account_message))
                    profileViewModel.logout()
                    redirectToEventsFragment()
                } else {
                    openDeleteAccountFailDialog()
                }
            })

        profileViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                user = it
                updateProfile(it)
            })

        profileViewModel.updatedPassword
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
                    smartAuthViewModel.saveCredential(emailSettings, it,
                        SmartAuthUtil.getCredentialsClient(
                            requireActivity()
                        )
                    )
                }
            })

        profileViewModel.updatedUser
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                user = it
                updateProfile(it)
            })

        rootView.editProfile.setOnClickListener {
            findNavController(rootView).navigate(ProfileFragmentDirections.actionProfileToEditProfile())
        }

        rootView.fabEditProfile.setOnClickListener {
            findNavController(rootView).navigate(ProfileFragmentDirections.actionProfileToEditProfile())
        }
        return rootView
    }

    private fun updateProfile(it: User) {
        rootView.accountName.text =  it.name
        rootView.accountEmail.text = it.email
        emailSettings = it.email.nullToEmpty()
        rootView.accountNotVerified.isVisible = !it.emailVerified
        rootView.accountVerified.isVisible = it.emailVerified
        Picasso.get()
            .load(it.avatarUrl)
            .placeholder(requireDrawable(requireContext(), R.drawable.ic_account_circle_grey))
            .transform(CircleTransform())
            .into(rootView.accountAvatar)
    }

    override fun doubleClick() = rootView.profileScrollView.smoothScrollTo(0, 0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView.manageEvents.setOnClickListener { startOrgaApp("com.eventbox.organizer") }

        rootView.settingToolbar.setOnClickListener {
            findNavController(rootView).navigate(
                ProfileFragmentDirections.actionProfileToSettings(
                    emailSettings
                )
            )
        }

        rootView.ticketIssues.setOnClickListener {
            Utils.openUrl(requireContext(), resources.getString(R.string.ticket_issues_url))
        }

        rootView.logout.setOnClickListener { showLogoutDialog() }
        rootView.login.setOnClickListener { redirectToLogin() }

        rootView.accountNotVerified.setOnClickListener {
            val userEmail = user?.email
            if (userEmail != null) {
                profileViewModel.resendVerificationEmail(userEmail)
            } else {
                rootView.snackbar(getString(R.string.error))
            }
        }
        rootView.changePassword.setOnClickListener {
            handleChangePassword()
        }

        rootView.deleteAccount.setOnClickListener {
            openDeleteAccountDialog()
        }
    }

    private fun openDeleteAccountFailDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.delete_account_fail_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    private fun openDeleteAccountDialog() {
        val deleteLayout = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        deleteLayout.confirmEmailLayout.isVisible = true

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setView(deleteLayout)
            .create()

        deleteLayout.cancelButton.setOnClickListener {
            dialog.cancel()
        }

        deleteLayout.confirmEmailButton.setOnClickListener {
            deleteLayout.confirmDeleteLayout.isVisible = true
            deleteLayout.confirmEmailLayout.isVisible = false
        }

        deleteLayout.deleteButton.setOnClickListener {
            dialog.cancel()
            confirmDeleteAccount()
        }

        deleteLayout.confirmDeleteCheckbox.setOnCheckedChangeListener { _, isChecked ->
            deleteLayout.deleteButton.isEnabled = isChecked
        }

        deleteLayout.email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                deleteLayout.confirmEmailButton.isEnabled = user?.email == s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Do Nothing */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Do Nothing */ }
        })

        dialog.show()
    }

    private fun confirmDeleteAccount() {
        val progressDialog = progressDialog(context, getString(R.string.deleting_account))

        loginViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        loginViewModel.validPassword
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    profileViewModel.deleteProfile()
                else
                    rootView.snackbar(getString(R.string.invalid_password_to_delete_account_message))
            })

        val layout = layoutInflater.inflate(R.layout.dialog_confirm_delete_account, null)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setView(layout)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                user?.email?.let {
                    loginViewModel.checkValidPassword(it, layout.password.text.toString())
                }
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    private fun handleChangePassword() {
        val layout = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.title_change_password))
            .setView(layout)
            .setPositiveButton(getString(R.string.change)) { _, _ ->
                profileViewModel.changePassword(layout.oldPassword.text.toString(), layout.newPassword.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layout.newPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!layout.textInputLayoutNewPassword.isEndIconVisible) {
                    layout.textInputLayoutNewPassword.isEndIconVisible = true
                }

                if (layout.newPassword.text.toString().length >= MINIMUM_PASSWORD_LENGTH) {
                    layout.textInputLayoutNewPassword.error = null
                    layout.textInputLayoutNewPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutNewPassword.error = getString(R.string.invalid_password_message)
                }
                if (layout.confirmNewPassword.text.toString() == layout.newPassword.text.toString()) {
                    layout.textInputLayoutConfirmNewPassword.error = null
                    layout.textInputLayoutConfirmNewPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutConfirmNewPassword.error =
                        getString(R.string.invalid_confirm_password_message)
                }
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < MINIMUM_PASSWORD_LENGTH) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothing*/ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothing*/ }
        })

        layout.confirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!layout.textInputLayoutConfirmNewPassword.isEndIconVisible) {
                    layout.textInputLayoutConfirmNewPassword.isEndIconVisible = true
                }

                if (layout.confirmNewPassword.text.toString() == layout.newPassword.text.toString()) {
                    layout.textInputLayoutConfirmNewPassword.error = null
                    layout.textInputLayoutConfirmNewPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutConfirmNewPassword.error =
                        getString(R.string.invalid_confirm_password_message)
                }
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < MINIMUM_PASSWORD_LENGTH) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothing*/ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothiing*/ }
        })

        layout.oldPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < MINIMUM_PASSWORD_LENGTH) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothing*/ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothing*/ }
        })
    }

    private fun startOrgaApp(packageName: String) {
        val manager = activity?.packageManager
        try {
            val intent = manager?.getLaunchIntentForPackage(packageName)
                    ?: throw ActivityNotFoundException()
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showInMarket(packageName)
        }
    }

    private fun showInMarket(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun showLogoutDialog() {
            AlertDialog.Builder(requireContext()).setMessage(resources.getString(R.string.message))
            .setPositiveButton(resources.getString(R.string.logout)) { _, _ ->
                if (profileViewModel.isLoggedIn()) {
                    profileViewModel.logout()
                    redirectToEventsFragment()
                }
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .show()
    }
}
