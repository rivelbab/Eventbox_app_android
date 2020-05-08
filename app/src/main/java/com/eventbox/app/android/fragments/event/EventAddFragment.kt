package com.eventbox.app.android.fragments.event

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.eventbox.app.android.ComplexBackPressFragment
import com.eventbox.app.android.MainActivity
import com.eventbox.app.android.R
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.ui.user.EditProfileViewModel
import com.eventbox.app.android.ui.user.ProfileViewModel
import com.eventbox.app.android.utils.CircleTransform
import com.eventbox.app.android.utils.RotateBitmap
import com.eventbox.app.android.utils.Utils.hideSoftKeyboard
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.requireDrawable
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.emptyToNull
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.nullToEmpty
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_edit_profile_image.view.*
import kotlinx.android.synthetic.main.fragment_event_add.view.*
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class EventAddFragment : Fragment(), ComplexBackPressFragment {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private val editProfileViewModel by viewModel<EditProfileViewModel>()
    private val safeArgs: EventAddFragmentArgs by navArgs()
    private lateinit var rootView: View
    private var storagePermissionGranted = false
    private val PICK_IMAGE_REQUEST = 100
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val READ_STORAGE_REQUEST_CODE = 1
    private var calendar = Calendar.getInstance()

    private var cameraPermissionGranted = false
    private val TAKE_IMAGE_REQUEST = 101
    private val CAMERA_REQUEST = arrayOf(Manifest.permission.CAMERA)
    private val CAMERA_REQUEST_CODE = 2

    private val MAX_LENGTH_NORMAL = 255

    private lateinit var userFirstName: String
    private lateinit var userLastName: String
    private lateinit var userDetails: String
    private lateinit var userPhone: String

    private lateinit var eventName: String
    private lateinit var eventDescription: String
    private lateinit var eventLocation: String
    private lateinit var eventCatOne: String
    private lateinit var eventCatTwo: String
    private lateinit var eventStartOn: String
    private lateinit var eventEndOn: String
    private lateinit var eventStartTime: String
    private lateinit var eventEndTime: String
    private lateinit var eventPrivacy: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(R.layout.fragment_event_add, container, false)

        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        //=== configure form field
        disabledKeyListener()
        setFilterFields()


        val items = listOf("Sport", "Education", "Conference", "Culturel")
        val itemsPrivacy = listOf("Public", "Privé")

        val adapter = ArrayAdapter(requireContext(), R.layout.item_event_dropdown_list, items)
        val adapterPrivacy = ArrayAdapter(requireContext(), R.layout.item_event_dropdown_list, itemsPrivacy)

        rootView.categoryTwo.setAdapter(adapter)
        rootView.categoryOne.setAdapter(adapter)
        rootView.privacy.setAdapter(adapterPrivacy)

        val startsOnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView("STARTS_ON")
        }
        val startTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeInView("START_TIME")
        }

        val endsOnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView("ENDS_ON")
        }

        val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeInView("END_TIME")
        }

        rootView.startsOn.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                startsOnDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        rootView.startTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                startTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        rootView.endsOn.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                endsOnDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        rootView.endTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                endTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        return rootView
    }

    //=== disable key listener on some field ===
    private fun disabledKeyListener() {
        rootView.categoryOne.keyListener = null
        rootView.privacy.keyListener = null
        rootView.categoryTwo.keyListener = null
        rootView.startsOn.keyListener = null
        rootView.endsOn.keyListener = null
        rootView.startTime.keyListener = null
        rootView.endTime.keyListener = null

        rootView.startsOn.isFocusable = false
        rootView.startsOn.isFocusableInTouchMode = false
        rootView.endsOn.isFocusable = false
        rootView.endsOn.isFocusableInTouchMode = false
        rootView.startTime.isFocusable = false
        rootView.startTime.isFocusableInTouchMode = false
        rootView.endTime.isFocusable = false
        rootView.endTime.isFocusableInTouchMode = false

    }

    //=== add some filter to field ===
    private fun setFilterFields() {
        rootView.name.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH_NORMAL))
        rootView.location.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH_NORMAL))
    }

    private fun updateDateInView(view : String) {
        val localFormat = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(localFormat, Locale.FRANCE)
        when (view) {
            "STARTS_ON" -> rootView.startsOn.setText(simpleDateFormat.format(calendar.time))
            "ENDS_ON" -> rootView.endsOn.setText(simpleDateFormat.format(calendar.time))
        }
    }

    private fun updateTimeInView(view : String) {
        val localFormat = "HH:mm"
        val simpleDateFormat = SimpleDateFormat(localFormat, Locale.FRANCE)
        when (view) {
            "START_TIME" -> rootView.startTime.setText(simpleDateFormat.format(calendar.time))
            "END_TIME" -> rootView.endTime.setText(simpleDateFormat.format(calendar.time))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
    }


    /**
     * Handles back press when up button or back button is pressed
     */
    override fun handleBackPress() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
