package com.eventbox.app.android.fragments.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.eventbox.app.android.utils.CircleTransform
import com.eventbox.app.android.ComplexBackPressFragment
import com.eventbox.app.android.MainActivity
import com.eventbox.app.android.R
import com.eventbox.app.android.utils.RotateBitmap
import com.eventbox.app.android.utils.Utils.hideSoftKeyboard
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.requireDrawable
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.emptyToNull
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.nullToEmpty
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.ui.user.EditProfileViewModel
import com.eventbox.app.android.ui.user.ProfileViewModel
import kotlinx.android.synthetic.main.dialog_edit_profile_image.view.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class EditProfileFragment : Fragment(), ComplexBackPressFragment {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private val editProfileViewModel by viewModel<EditProfileViewModel>()
    private val safeArgs: EditProfileFragmentArgs by navArgs()
    private lateinit var rootView: View
    private var storagePermissionGranted = false
    private val PICK_IMAGE_REQUEST = 100
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val READ_STORAGE_REQUEST_CODE = 1

    private var cameraPermissionGranted = false
    private val TAKE_IMAGE_REQUEST = 101
    private val CAMERA_REQUEST = arrayOf(Manifest.permission.CAMERA)
    private val CAMERA_REQUEST_CODE = 2

    private lateinit var userName: String
    private lateinit var userDetails: String
    private lateinit var userPhone: String
    private lateinit var userInterest: String

    private val itemsCategory = listOf("Sport", "Education", "Conference", "Culturel", "Social")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        profileViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadUserUI(it)
            })

        val currentUser = editProfileViewModel.user.value
        if (currentUser == null) profileViewModel.getProfile() else loadUserUI(currentUser)

        val progress = progressDialog(context)
        editProfileViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progress.show(it)
            })

        editProfileViewModel.getUpdatedTempFile()
            .nonNull()
            .observe(viewLifecycleOwner, Observer { file ->
                // prevent picasso from storing tempAvatar cache,
                // if user select another image picasso will display tempAvatar instead of its own cache
                Picasso.get()
                    .load(file)
                    .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .transform(CircleTransform())
                    .into(rootView.profilePhoto)
            })

        storagePermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        cameraPermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

        rootView.updateButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            if (isValidInput()) {
                updateUser()
            } else {
                rootView.snackbar(getString(R.string.fill_required_fields_message))
            }
        }

        editProfileViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                if (it == getString(R.string.user_update_success_message)) {
                    val thisActivity = activity
                    if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
                }
            })

        rootView.profilePhotoFab.setOnClickListener {
            showEditPhotoDialog()
        }

        val adapterInterest = ArrayAdapter(requireContext(), R.layout.item_event_dropdown_list, itemsCategory)
        rootView.interest.setAdapter(adapterInterest)
        rootView.interest.keyListener = null

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == PICK_IMAGE_REQUEST && intentData?.data != null) {
            val imageUri = intentData.data ?: return

            try {
                val selectedImage = RotateBitmap()
                    .handleSamplingAndRotationBitmap(requireContext(), imageUri)
                editProfileViewModel.encodedImage = selectedImage?.let { encodeImage(it) }
                editProfileViewModel.avatarUpdated = true
            } catch (e: FileNotFoundException) {
                Timber.d(e, "File Not Found Exception")
            }
        } else if (requestCode == TAKE_IMAGE_REQUEST) {
            val imageBitmap = intentData?.extras?.get("data")
            if (imageBitmap is Bitmap) {
                editProfileViewModel.encodedImage = imageBitmap.let { encodeImage(it) }
                editProfileViewModel.avatarUpdated = true
            }
        }
    }

    private fun isValidInput(): Boolean {
        return true
    }

    private fun loadUserUI(user: User) {
        userName = user.name.nullToEmpty()
        userInterest = user.interest.nullToEmpty()
        userDetails = user.details.nullToEmpty()
        if (editProfileViewModel.userAvatar == null)
            editProfileViewModel.userAvatar = user.avatarUrl.nullToEmpty()
        userPhone = user.phone.nullToEmpty()

        if (safeArgs.croppedImage.isEmpty()) {
            if (!editProfileViewModel.userAvatar.isNullOrEmpty() && !editProfileViewModel.avatarUpdated) {
                val drawable = requireDrawable(requireContext(), R.drawable.ic_account_circle_grey)
                Picasso.get()
                    .load(editProfileViewModel.userAvatar)
                    .placeholder(drawable)
                    .transform(CircleTransform())
                    .into(rootView.profilePhoto)
            }
        } else {
            //val croppedImage = decodeBitmap(safeArgs.croppedImage)
            //editProfileViewModel.encodedImage = encodeImage(croppedImage)
            //editProfileViewModel.avatarUpdated = true
        }
        setTextIfNull(rootView.name, userName)
        setTextIfNull2(rootView.interest, userInterest)
        setTextIfNull(rootView.details, userDetails)
        setTextIfNull(rootView.phone, userPhone)
    }

    private fun setTextIfNull(input: TextInputEditText, text: String) {
        if (input.text.isNullOrBlank()) input.setText(text)
    }

    private fun setTextIfNull2(input: AutoCompleteTextView, text: String) {
        if (input.text.isNullOrBlank()) input.setText(text)
    }

    private fun showEditPhotoDialog() {
        val editImageView = layoutInflater.inflate(R.layout.dialog_edit_profile_image, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(editImageView)
            .create()

        /*editImageView.editImage.setOnClickListener {

            if (!editProfileViewModel.userAvatar.isNullOrEmpty()) {
                val currentUserAvatar = editProfileViewModel.userAvatar
                if (currentUserAvatar != null) {
                    findNavController(rootView).navigate(
                        EditProfileFragmentDirections.actionEditProfileToCropImage(
                            currentUserAvatar
                        )
                    )
                } else {
                    rootView.snackbar(getString(R.string.error_editting_image_message))
                }
            } else {
                rootView.snackbar(getString(R.string.image_not_found))
            }

            dialog.cancel()
        }*/

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

    private fun clearAvatar() {
        val drawable = requireDrawable(requireContext(), R.drawable.ic_account_circle_grey)
        Picasso.get()
            .load(R.drawable.ic_account_circle_grey)
            .placeholder(drawable)
            .transform(CircleTransform())
            .into(rootView.profilePhoto)
        editProfileViewModel.encodedImage = encodeImage(drawable.toBitmap(120, 120))
        editProfileViewModel.avatarUpdated = true
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

            editProfileViewModel.setUpdatedTempFile(tempAvatar)
            editProfileViewModel.userAvatar = tempAvatar.toURI().toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT)
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
        if (noDataChanged()) {
            findNavController(rootView).popBackStack()
        } else {
            hideSoftKeyboard(context, rootView)
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setMessage(getString(R.string.changes_not_saved))
            dialog.setNegativeButton(getString(R.string.discard)) { _, _ ->
                if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
            }
            dialog.setPositiveButton(getString(R.string.save)) { _, _ ->
                if (isValidInput()) {
                    updateUser()
                } else {
                    rootView.snackbar(getString(R.string.fill_required_fields_message))
                } }
            dialog.create().show()
        }
    }

    private fun updateUser() {
        val newUser = User(
            id = editProfileViewModel.getId(),
            name = rootView.name.text.toString(),
            interest = rootView.interest.text.toString(),
            details = rootView.details.text.toString(),
            phone = rootView.phone.text.toString().emptyToNull()
        )
        editProfileViewModel.updateProfile(newUser)
    }

    private fun noDataChanged() = !editProfileViewModel.avatarUpdated &&
        rootView.name.text.toString() == userName &&
        rootView.interest.text.toString() == userInterest &&
        rootView.details.text.toString() == userDetails &&
        rootView.phone.text.toString() == userPhone

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
