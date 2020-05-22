package com.eventbox.app.android.fragments.event

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.eventbox.app.android.ComplexBackPressFragment
import com.eventbox.app.android.MainActivity
import com.eventbox.app.android.R
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.ui.event.EventAddViewModel
import com.eventbox.app.android.utils.RotateBitmap
import com.eventbox.app.android.utils.Utils.hideSoftKeyboard
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.requireDrawable
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.extensions.nonNull
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

    private val eventViewModel by viewModel<EventAddViewModel>()
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

    private val itemsCategory = listOf("Sport", "Education", "Conference", "Culturel", "Social")
    private val itemsPrivacy = listOf("Public", "Privé")

    private val MAX_LENGTH_NORMAL = 255

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(R.layout.fragment_event_add, container, false)

        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        val progressDialog = progressDialog(context, getString(R.string.creating_event_message))
        eventViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        eventViewModel.getAddedTempFile()
            .nonNull()
            .observe(viewLifecycleOwner, Observer { file ->
                // prevent picasso from storing tempAvatar cache,
                // if user select another image picasso will display tempAvatar instead of its own cache
                Picasso.get()
                    .load(file)
                    .placeholder(requireDrawable(requireContext(), R.drawable.header))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(rootView.eventImage)
            })

        storagePermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        cameraPermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

        rootView.addEventButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            if (isValidInput()) {
                createEvent()
            } else {
                rootView.snackbar(getString(R.string.fill_required_fields_message))
            }
        }

        eventViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                if (it == getString(R.string.create_event_success_message)) {
                    val thisActivity = activity
                    if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
                }
            })

        rootView.eventImageFab.setOnClickListener{
            showEventPhotoDialog()
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

        //=== configure form field ===
        disabledKeyListener()
        setFilterFields()
        setupDropDownMenu()

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == PICK_IMAGE_REQUEST && intentData?.data != null) {
            val imageUri = intentData.data ?: return

            try {
                val selectedImage = RotateBitmap().handleSamplingAndRotationBitmap(requireContext(), imageUri)
                eventViewModel.encodedImage = selectedImage?.let { encodeImage(it) }
                eventViewModel.avatarUpdated = true
            } catch (e: FileNotFoundException) {
                Timber.d(e, "File Not Found Exception")
            }
        } else if (requestCode == TAKE_IMAGE_REQUEST) {
            val imageBitmap = intentData?.extras?.get("data")
            if (imageBitmap is Bitmap) {
                eventViewModel.encodedImage = imageBitmap.let { encodeImage(it) }
                eventViewModel.avatarUpdated = true
            }
        }
    }

    //=== disable key listener on some field ===
    private fun disabledKeyListener() {
        rootView.category.keyListener = null
        rootView.privacy.keyListener = null
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

        rootView.location.onFocusChangeListener = View.OnFocusChangeListener{ view, b ->
            hideSoftKeyBoard(view, b)
        }

    }

    private fun hideSoftKeyBoard(v : View, hasFocus : Boolean) {
        if(!hasFocus){
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }


    private fun isValidInput() : Boolean {
        var valid = true
        if(rootView.name.text.isNullOrEmpty()) {valid = false}
        if(rootView.description.text.isNullOrEmpty()) {valid = false}
        if(rootView.location.text.isNullOrEmpty()) {valid = false}
        return valid
    }

    //=== add some filter to field ===
    private fun setFilterFields() {
        rootView.name.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH_NORMAL))
        rootView.location.filters = arrayOf(InputFilter.LengthFilter(MAX_LENGTH_NORMAL))
    }

    //==== setup drop down menu ===
    private fun setupDropDownMenu() {
       // drop down menu
        val adapterCategory = ArrayAdapter(requireContext(), R.layout.item_event_dropdown_list, itemsCategory)
        val adapterPrivacy = ArrayAdapter(requireContext(), R.layout.item_event_dropdown_list, itemsPrivacy)
        rootView.category.setAdapter(adapterCategory)
        rootView.privacy.setAdapter(adapterPrivacy)
    }

    //=== setup picker field ===
    private val startsOnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView("STARTS_ON")
    }
    private val startTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        updateTimeInView("START_TIME")
    }

    private val endsOnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView("ENDS_ON")
    }

    private val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        updateTimeInView("END_TIME")
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

    private fun showEventPhotoDialog() {
        val editImageView = layoutInflater.inflate(R.layout.dialog_edit_profile_image, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(editImageView)
            .create()

        editImageView.removeImage.setOnClickListener {
            dialog.cancel()
            clearAvatar()
        }

        editImageView.takeImage.setOnClickListener {
            dialog.cancel()
            if (cameraPermissionGranted) {
                takeImage()
            } else {
                requestPermissions(CAMERA_REQUEST, CAMERA_REQUEST_CODE)
            }
        }

        editImageView.replaceImage.setOnClickListener {
            dialog.cancel()
            if (storagePermissionGranted) {
                showFileChooser()
            } else {
                requestPermissions(READ_STORAGE, READ_STORAGE_REQUEST_CODE)
            }
        }
        dialog.show()
    }

    private fun takeImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, TAKE_IMAGE_REQUEST)
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE_REQUEST)
    }

    private fun clearAvatar() {
        val drawable = requireDrawable(requireContext(), R.drawable.header)
        Picasso.get()
            .load(R.drawable.header)
            .placeholder(drawable)
            .into(rootView.eventImage)
        eventViewModel.encodedImage = encodeImage(drawable.toBitmap(120, 120))
        eventViewModel.avatarUpdated = true
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()

        // create temp file
        try {
            val tempAvatar = File(context?.cacheDir, "tempAvatar")
            if (tempAvatar.exists()) {
                tempAvatar.delete()
            }
            val fos = FileOutputStream(tempAvatar)
            fos.write(bytes)
            fos.flush()
            fos.close()

            eventViewModel.setAddedTempFile(tempAvatar)
            eventViewModel.eventAvatar = tempAvatar.toURI().toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermissionGranted = true
                rootView.snackbar(getString(R.string.permission_granted_message, getString(R.string.external_storage)))
                showFileChooser()
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.external_storage)))
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionGranted = true
                rootView.snackbar(getString(R.string.permission_granted_message, getString(R.string.camera)))
                takeImage()
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.camera)))
            }
        }
    }

    /**
     * Handles back press when up button or back button is pressed
     */
    override fun handleBackPress() {
        val thisActivity = activity
        hideSoftKeyboard(context, rootView)
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setMessage(getString(R.string.changes_not_saved))
        dialog.setNegativeButton(getString(R.string.discard)) { _, _ ->
            if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
        }
        dialog.setPositiveButton(getString(R.string.save)) { _, _ ->
            if (isValidInput()) {
                createEvent()
            } else {
                rootView.snackbar(getString(R.string.fill_required_fields_message))
            } }
        dialog.create().show()
    }

    private fun createEvent() {
        val startAt = rootView.startsOn.text.toString() + "T" + rootView.startTime.text.toString()+"Z"
        val endAt = rootView.endsOn.text.toString() + "T" + rootView.endTime.text.toString()+"Z"
        val id = eventViewModel.getId()
        val event = Event(
            name = rootView.name.text.toString(),
            description = rootView.description.text.toString(),
            locationName = rootView.location.text.toString(),
            startsAt = startAt,
            endsAt = endAt,
            codeOfConduct = "",
            isComplete = false,
            privacy = rootView.privacy.text.toString(),
            originalImageUrl = "",
            ownerName = id,
            category = rootView.category.text.toString()
        )

        eventViewModel.addEvent(event)
    }

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
