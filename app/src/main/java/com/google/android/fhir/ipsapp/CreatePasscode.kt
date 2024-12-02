package com.google.android.fhir.ipsapp

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import com.google.android.fhir.document.generate.SHLinkGenerationData
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar

class CreatePasscode : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.create_passcode)

    val datePicker = findViewById<DatePicker>(R.id.datePicker)
    val shlData = intent.serializable<SHLinkGenerationData>("shlData",)

    val today: Calendar = Calendar.getInstance()
    datePicker.minDate = today.timeInMillis

    val submitResourcesButton = findViewById<Button>(R.id.generateSHL)
    val checkboxDate = findViewById<CheckBox>(R.id.checkboxDate)
    val passcodeField = findViewById<EditText>(R.id.passcode)
    val labelField = findViewById<EditText>(R.id.label)

    /*
    When the submit button is pressed, the state of the checkbox is checked, and the passcode
    and expiration date are added to the intent to be passed into the next activity.
    They are empty strings if they haven't been inputted
    */
    submitResourcesButton.setOnClickListener {
      val i = Intent()
      i.component = ComponentName(this@CreatePasscode, GenerateSHL::class.java)
      val passcode = passcodeField.text.toString()
      val label = labelField.text.toString()
      val exp: Instant? =
        if (checkboxDate.isChecked) {
          val year = datePicker.year
          val month = datePicker.month + 1
          val dayOfMonth = datePicker.dayOfMonth

          val localDate = LocalDate.of(year, month, dayOfMonth)
          val instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC)

          instant
        } else {
          null
        }
      val newSHLData = shlData?.let { it1 -> SHLinkGenerationData(label, exp, it1.ipsDoc) }
      i.putExtra("passcode", passcode)
      i.putExtra("shlData", newSHLData as Serializable)
      startActivity(i)
    }

    /* Set the initial state of the DatePicker based on the Checkbox state */
    datePicker.isEnabled = checkboxDate.isChecked
    checkboxDate.setOnCheckedChangeListener { _, isChecked -> datePicker.isEnabled = isChecked }
  }
}
