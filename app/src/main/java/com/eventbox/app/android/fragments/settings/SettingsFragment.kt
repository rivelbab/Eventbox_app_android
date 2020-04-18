package com.eventbox.app.android.fragments.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.webkit.URLUtil
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.FDROID_BUILD_FLAVOR
import com.eventbox.app.android.PLAY_STORE_BUILD_FLAVOR
import com.eventbox.app.android.R
import com.eventbox.app.android.fragments.auth.MINIMUM_PASSWORD_LENGTH
import com.eventbox.app.android.ui.user.ProfileViewModel
import com.eventbox.app.android.auth.SmartAuthUtil
import com.eventbox.app.android.auth.SmartAuthViewModel
import com.eventbox.app.android.ui.settings.SettingsViewModel
import com.eventbox.app.android.utils.Utils
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.nullToEmpty
import kotlinx.android.synthetic.main.dialog_api_configuration.view.*
import kotlinx.android.synthetic.main.dialog_change_password.view.*
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOCAL_TIMEZONE = "localTimeZone"

class SettingsFragment : PreferenceFragmentCompat(), PreferenceChangeListener {

    private val PRIVACY_LINK: String = "https://eventyay.com/privacy-policy"
    private val TERMS_OF_SERVICE_LINK: String = "https://eventyay.com/terms"
    private val REFUND_POLICY_LINK: String = "https://eventyay.com/refunds"
    private val WEBSITE_LINK: String = "https://eventyay.com/"
    private val settingsViewModel by viewModel<SettingsViewModel>()
    private val profileViewModel by viewModel<ProfileViewModel>()
    private val smartAuthViewModel by viewModel<SmartAuthViewModel>()
    private val safeArgs: SettingsFragmentArgs by navArgs()

    override fun preferenceChange(evt: PreferenceChangeEvent?) {
        preferenceChange(evt)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey)
        val timeZonePreference = PreferenceManager.getDefaultSharedPreferences(context)

        setToolbar(activity, getString(R.string.settings))
        setHasOptionsMenu(true)

        // Set Email
        preferenceScreen.findPreference<Preference>(getString(R.string.key_account))?.summary =
            if (safeArgs.email.isNullOrEmpty()) getString(R.string.not_logged_in) else safeArgs.email

        // Set Build Version
        preferenceScreen.findPreference<Preference>(getString(R.string.key_version))?.title =
            "Version " + BuildConfig.VERSION_NAME

        preferenceScreen.findPreference<Preference>(getString(R.string.key_timezone_switch))?.setDefaultValue(
            timeZonePreference.getBoolean(LOCAL_TIMEZONE, false)
        )

        preferenceScreen.findPreference<Preference>(getString(R.string.key_profile))?.isVisible =
            profileViewModel.isLoggedIn()
        preferenceScreen.findPreference<Preference>(getString(R.string.key_change_password))?.isVisible =
            profileViewModel.isLoggedIn()
        preferenceScreen.findPreference<Preference>(getString(R.string.key_timezone_switch))?.isVisible =
            profileViewModel.isLoggedIn()

        preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.key_server_configuration))
            ?.isVisible = BuildConfig.FLAVOR == FDROID_BUILD_FLAVOR

        preferenceScreen.findPreference<Preference>(getString(R.string.key_api_url))?.title =
            settingsViewModel.getApiUrl()
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        settingsViewModel.snackBar
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                view?.snackbar(it)
            })

        settingsViewModel.updatedPassword
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
                    smartAuthViewModel.saveCredential(safeArgs.email.toString(),
                        it,
                        SmartAuthUtil.getCredentialsClient(requireActivity()))
                }
            })

        if (preference?.key == getString(R.string.key_api_url)) {
            showChangeApiDialog()
        }
        if (preference?.key == getString(R.string.key_visit_website)) {
            // Goes to website
            Utils.openUrl(requireContext(), WEBSITE_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_rating)) {
            // Opens our app in play store
            startAppPlayStore(activity?.packageName.nullToEmpty())
            return true
        }

        if (preference?.key == getString(R.string.key_profile)) {
            showLogoutDialog()
            return true
        }
        if (preference?.key == getString(R.string.key_change_password)) {
            showChangePasswordDialog()
            return true
        }
        if (preference?.key == getString(R.string.key_timezone_switch)) {
            val timeZonePreference = PreferenceManager.getDefaultSharedPreferences(context)
            when (timeZonePreference.getBoolean(LOCAL_TIMEZONE, false)) {
                true -> timeZonePreference.edit().putBoolean(LOCAL_TIMEZONE, false).apply()
                false -> timeZonePreference.edit().putBoolean(LOCAL_TIMEZONE, true).apply()
            }
            return true
        }
        if (preference?.key == getString(R.string.key_privacy)) {
            Utils.openUrl(requireContext(), PRIVACY_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_terms_of_service)) {
            Utils.openUrl(requireContext(), TERMS_OF_SERVICE_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_refund_policy)) {
            Utils.openUrl(requireContext(), REFUND_POLICY_LINK)
            return true
        }

        return false
    }

    private fun showChangeApiDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_api_configuration, null)
        layout.urlCheckBox.text = BuildConfig.DEFAULT_BASE_URL

        val dialog = AlertDialog.Builder(requireContext())
            .setView(layout)
            .setPositiveButton(getString(R.string.change)) { _, _ ->
                val url = if (layout.urlCheckBox.isChecked) BuildConfig.DEFAULT_BASE_URL
                                else layout.urlEditText.text.toString()
                if (url === settingsViewModel.getApiUrl()) return@setPositiveButton
                settingsViewModel.changeApiUrl(url)
                view?.snackbar("API URL changed to $url")
                findNavController().popBackStack(R.id.eventsFragment, false)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .setCancelable(false)
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layout.urlCheckBox.setOnCheckedChangeListener { _, isChecked ->
            layout.urlTextInputLayout.isVisible = !isChecked
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isChecked
        }

        layout.urlEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                val isValidUrl = (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) &&
                    URLUtil.isValidUrl(url)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isValidUrl ||
                    layout.urlCheckBox.isChecked
                if (!isValidUrl) layout.urlEditText.error = getString(R.string.invalid_url)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Implement here*/ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Implement here*/ }
        })
    }

    private fun startAppPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settingsViewModel.getMarketAppLink(packageName))))
        } catch (error: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settingsViewModel.getMarketWebLink(packageName))))
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext()).setMessage(getString(R.string.message))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                if (profileViewModel.isLoggedIn()) {
                    profileViewModel.logout()
                    findNavController().popBackStack(R.id.eventsFragment, false)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun showChangePasswordDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.title_change_password))
            .setView(layout)
            .setPositiveButton(getString(R.string.change)) { _, _ ->
                settingsViewModel.changePassword(layout.oldPassword.text.toString(), layout.newPassword.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layout.newPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                */
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

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })

        layout.confirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                 */
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

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })

        layout.oldPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                 */
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < MINIMUM_PASSWORD_LENGTH) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
