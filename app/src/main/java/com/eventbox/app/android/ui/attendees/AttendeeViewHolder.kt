package com.eventbox.app.android.ui.attendees

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.item_attendee.view.address
import kotlinx.android.synthetic.main.item_attendee.view.addressLayout
import kotlinx.android.synthetic.main.item_attendee.view.attendeeBillingAddress
import kotlinx.android.synthetic.main.item_attendee.view.attendeeBillingAddressLayout
import kotlinx.android.synthetic.main.item_attendee.view.city
import kotlinx.android.synthetic.main.item_attendee.view.cityLayout
import kotlinx.android.synthetic.main.item_attendee.view.email
import kotlinx.android.synthetic.main.item_attendee.view.emailLayout
import kotlinx.android.synthetic.main.item_attendee.view.firstName
import kotlinx.android.synthetic.main.item_attendee.view.firstNameLayout
import kotlinx.android.synthetic.main.item_attendee.view.genderLayout
import kotlinx.android.synthetic.main.item_attendee.view.genderSpinner
import kotlinx.android.synthetic.main.item_attendee.view.genderText
import kotlinx.android.synthetic.main.item_attendee.view.lastName
import kotlinx.android.synthetic.main.item_attendee.view.lastNameLayout
import kotlinx.android.synthetic.main.item_attendee.view.phone
import kotlinx.android.synthetic.main.item_attendee.view.phoneLayout
import com.eventbox.app.android.R
import com.eventbox.app.android.models.attendees.Attendee
import com.eventbox.app.android.adapters.AttendeeDetailChangeListener
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.models.attendees.FormIdentifier
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.databinding.ItemAttendeeBinding
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.models.payment.TicketId
import com.eventbox.app.android.utils.checkEmpty
import com.eventbox.app.android.utils.checkValidEmail
import com.eventbox.app.android.utils.emptyToNull
import com.eventbox.app.android.utils.nullToEmpty
import com.eventbox.app.android.utils.setRequired

class AttendeeViewHolder(private val binding: ItemAttendeeBinding) : RecyclerView.ViewHolder(binding.root) {
    private val resource = Resource()
    private val requiredList = mutableListOf<Pair<TextInputEditText, TextInputLayout>>()
    var onAttendeeDetailChanged: AttendeeDetailChangeListener? = null

    fun bind(
        attendee: Attendee,
        ticket: Ticket,
        customForm: List<CustomForm>,
        position: Int,
        eventId: Long,
        firstAttendee: Attendee?
    ) {
        with(binding) {
            this.attendee = attendee
            this.ticket = ticket
        }

        if (position == 0) {
            if (firstAttendee != null) {
                itemView.firstName.text = SpannableStringBuilder(firstAttendee.firstname.nullToEmpty())
                itemView.lastName.text = SpannableStringBuilder(firstAttendee.lastname.nullToEmpty())
                itemView.email.text = SpannableStringBuilder(firstAttendee.email.nullToEmpty())
                setFieldEditable(false)
            } else {
                itemView.firstName.text = SpannableStringBuilder("")
                itemView.lastName.text = SpannableStringBuilder("")
                itemView.email.text = SpannableStringBuilder("")
                setFieldEditable(true)
            }
        } else {
            itemView.firstName.setText(attendee.firstname)
            itemView.lastName.setText(attendee.lastname)
            itemView.email.setText(attendee.email)
        }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newAttendee = getAttendeeInformation(attendee.id, ticket, eventId)
                onAttendeeDetailChanged?.onAttendeeDetailChanged(newAttendee, position)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do nothing*/ }
        }
        requiredList.clear()
        setupGendersSpinner(attendee, ticket, eventId, position)
        customForm.forEach { form ->
            setupCustomFormWithFields(form, textWatcher)
        }
    }
    private fun setupCustomFormWithFields(form: CustomForm, textWatcher: TextWatcher) {
        when (form.fieldIdentifier) {
            FormIdentifier.FIRST_NAME ->
                setupField(itemView.firstNameLayout, itemView.firstName, form.isRequired, textWatcher)
            FormIdentifier.LAST_NAME ->
                setupField(itemView.lastNameLayout, itemView.lastName, form.isRequired, textWatcher)
            FormIdentifier.EMAIL ->
                setupField(itemView.emailLayout, itemView.email, form.isRequired, textWatcher)
            FormIdentifier.ADDRESS ->
                setupField(itemView.addressLayout, itemView.address, form.isRequired, textWatcher)
            FormIdentifier.CITY ->
                setupField(itemView.cityLayout, itemView.city, form.isRequired, textWatcher)
            FormIdentifier.PHONE ->
                setupField(itemView.phoneLayout, itemView.phone, form.isRequired, textWatcher)
            FormIdentifier.BILLING_ADDRESS ->
                setupField(itemView.attendeeBillingAddressLayout, itemView.attendeeBillingAddress, form.isRequired,
                    textWatcher)
            FormIdentifier.GENDER -> {
                itemView.genderLayout.isVisible = true
                if (form.isRequired) {
                    itemView.genderText.text = "${resource.getString(R.string.gender)}*"
                }
            }
            else -> return
        }
    }

    private fun setupGendersSpinner(attendee: Attendee, ticket: Ticket, eventId: Long, position: Int) {
        val genders = mutableListOf(resource.getString(R.string.male),
            resource.getString(R.string.female), resource.getString(R.string.others))
        itemView.genderSpinner.adapter =
            ArrayAdapter(itemView.context, android.R.layout.simple_spinner_dropdown_item, genders)

        val genderSelected = genders.indexOf(attendee.gender)
        if (genderSelected != -1)
            itemView.genderSpinner.setSelection(genderSelected)

        itemView.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /* Do Nothing */ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, p: Int, id: Long) {
                val newAttendee = getAttendeeInformation(attendee.id, ticket, eventId)
                onAttendeeDetailChanged?.onAttendeeDetailChanged(newAttendee, position)
            }
        }
    }

    private fun setupField(
        layout: TextInputLayout,
        editText: TextInputEditText,
        isRequired: Boolean,
        textWatcher: TextWatcher
    ) {
        layout.isVisible = true
        editText.addTextChangedListener(textWatcher)
        if (isRequired) {
            layout.setRequired()
            requiredList.add(Pair(editText, layout))
        }
    }

    private fun setFieldEditable(editable: Boolean) {
        itemView.firstName.isEnabled = editable
        itemView.lastName.isEnabled = editable
        itemView.email.isEnabled = editable
    }

    fun checkValidFields(): Boolean {
        var valid = true
        requiredList.forEach {
            valid = it.first.checkEmpty(it.second) &&
                when (it.second) {
                    itemView.emailLayout -> it.first.checkValidEmail(it.second)
                    else -> true
                }
        }
        return valid
    }

    private fun getAttendeeInformation(id: Long, ticket: Ticket, eventId: Long): Attendee {
        return Attendee(
            id = id,
            firstname = itemView.firstName.text.toString(),
            lastname = itemView.lastName.text.toString(),
            email = itemView.email.text.toString(),
            address = itemView.address.text.toString().emptyToNull(),
            city = itemView.city.text.toString().emptyToNull(),
            phone = itemView.phone.text.toString().emptyToNull(),
            billingAddress = itemView.attendeeBillingAddress.text.toString().emptyToNull(),
            gender = itemView.genderSpinner.selectedItem.toString(),
            ticket = TicketId(ticket.id.toLong()),
            event = EventId(eventId)
        )
    }
}
