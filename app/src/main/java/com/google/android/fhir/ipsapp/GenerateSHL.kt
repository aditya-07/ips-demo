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

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.fhir.NetworkConfiguration
import com.google.android.fhir.document.RetrofitSHLService
import com.google.android.fhir.document.generate.EncryptionUtils
import com.google.android.fhir.document.generate.QRGeneratorImpl
import com.google.android.fhir.document.generate.QRGeneratorUtils
import com.google.android.fhir.document.generate.SHLinkGenerationData
import com.google.android.fhir.document.generate.SHLinkGeneratorImpl
import kotlinx.coroutines.launch

class GenerateSHL : AppCompatActivity() {

  private val linkGenerator =
    SHLinkGeneratorImpl(
      RetrofitSHLService.Builder("https://api.vaxx.link/", NetworkConfiguration()).build(),
      EncryptionUtils
    )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.view_shl)

    val passcode: String = intent.getStringExtra("passcode").toString()
    val shlData = intent.serializable<SHLinkGenerationData>("shlData")
    val passcodeField = findViewById<TextView>(R.id.passcode)
    val expirationDateField = findViewById<TextView>(R.id.expirationDate)
    passcodeField.text = passcode
    expirationDateField.text = shlData?.expirationTime.toString()
    var shLink = ""
    val qrGen = QRGeneratorImpl(QRGeneratorUtils(this))
    if (shlData?.ipsDoc?.document != null) {
      lifecycleScope.launch {
        shLink =
          linkGenerator.generateSHLink(
            shlData,
            passcode,
            "",
            "",
          )
        qrGen.generateAndSetQRCode(shLink, findViewById<ImageView>(R.id.qrCode))
      }
    }
  }
}
