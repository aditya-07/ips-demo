package com.example.ipsapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import androidx.annotation.RequiresApi
import java.util.Calendar

class CreatePasscode : Activity() {
  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.create_passcode)

    val datePicker = findViewById<DatePicker>(R.id.datePicker)
    var expirationDate = ""

    // disables dates before today - can't set expiration date in the past
    // Do I need to include time with this?
    // Put a checkbox next to this to indicate whether the user wants to include date or not
    val today: Calendar = Calendar.getInstance()
    datePicker.minDate = today.timeInMillis

    val submitResourcesButton = findViewById<Button>(R.id.generateSHL)
    val checkboxDate = findViewById<CheckBox>(R.id.checkboxDate)

    submitResourcesButton.setOnClickListener {
      val i = Intent(this@CreatePasscode, ViewSHL::class.java)


      val passcodeField = findViewById<EditText>(R.id.passcode).text.toString()
      if (!checkboxDate.isChecked) {
        expirationDate = ""
      }
      i.putExtra("expirationDate", expirationDate)
      i.putExtra("passcode", passcodeField)
      Log.d("passcode", passcodeField)
      Log.d("date", expirationDate)
      startActivity(i)
    }

    // Set the initial state of the DatePicker based on the Checkbox state
    datePicker.isEnabled = checkboxDate.isChecked

    checkboxDate.setOnCheckedChangeListener { _, isChecked ->
      datePicker.isEnabled = isChecked
    }

    datePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
      // Handle the selected date here
      expirationDate = "$year-${monthOfYear + 1}-$dayOfMonth"
      // Do something with the selectedDate
    }
  }
}