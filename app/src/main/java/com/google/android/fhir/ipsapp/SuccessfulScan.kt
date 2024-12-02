/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.ipsapp

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.fhir.NetworkConfiguration
import com.google.android.fhir.document.RetrofitSHLService
import com.google.android.fhir.document.decode.ReadSHLinkUtils
import com.google.android.fhir.document.decode.SHLinkDecoderImpl
import com.google.android.fhir.document.scan.SHLinkScanData
import java.io.Serializable
import kotlinx.coroutines.launch

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
  else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

class SuccessfulScan : AppCompatActivity() {

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.successfulscan)

    val shlData = intent.serializable<SHLinkScanData>("shlData")

    val decoder = SHLinkDecoderImpl(
      ReadSHLinkUtils,
      //      RetrofitSHLService.Builder("https://api.vaxx.link/", NetworkConfiguration()).build()?
      RetrofitSHLService.Builder("https://smart-health-links-portal.kenya-hie.jembi.cloud/api/v1/share-links/", NetworkConfiguration()).build()
    )

    /* only display the passscode field if one is required */
    val passcodeEditText = findViewById<EditText>(R.id.passcode)
    // viewModel.togglePasscodeFieldView(passcodeEditText)

    /* when the button is pressed fetch the data using the user entered data */
    val button = findViewById<Button>(R.id.getData)
    button.setOnClickListener {
      val recipientField = findViewById<EditText>(R.id.recipient).text.toString()
      val passcodeField = passcodeEditText.text.toString()
      lifecycleScope.launch {
        val doc = shlData?.fullLink?.let { it1 -> decoder.decodeSHLinkToDocument(it1, recipientField, passcodeField) }
        if (doc == null) {
          runOnUiThread {
            Toast.makeText(
              this@SuccessfulScan, "Incorrect passcode", Toast.LENGTH_SHORT
            ).show()
          }
          return@launch
        }
        val i = Intent()
        i.component = ComponentName(this@SuccessfulScan, GetData::class.java)
        i.putExtra("doc", doc as java.io.Serializable)
        i.putExtra("shlData", shlData as java.io.Serializable)
        startActivity(i)
      }
    }
  }
}
