/*
 * Copyright 2023-2024 Google LLC
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

import android.app.Application
import android.content.Context
import android.util.Log
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.BuildConfig
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.document.generate.DocumentUtils
import com.google.android.fhir.sync.remote.HttpLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Bundle

class FhirApplication : Application() {
  // Only initiate the FhirEngine when used for the first time, not when the app is created.
  private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

  override fun onCreate() {
    super.onCreate()
    FhirEngineProvider.init(
      FhirEngineConfiguration(
        enableEncryptionIfSupported = true,
        DatabaseErrorStrategy.RECREATE_AT_OPEN,
        ServerConfiguration(
          baseUrl = "http://hapi.fhir.org/baseR4/",
          httpLogger =
          HttpLogger(
            HttpLogger.Configuration(
              if (BuildConfig.DEBUG) HttpLogger.Level.BODY else HttpLogger.Level.BASIC,
            ),
          ) {
            Log.d("App-HttpLog", it)
          },
        ),
      ),
    )
    val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)
    val jsonParser = fhirContext.newJsonParser()
    val document = DocumentUtils.readFileFromAssets(this, "immunizationBundle.json")
    val ipsDocument = jsonParser.parseResource(document) as Bundle
    CoroutineScope(Dispatchers.IO).launch {
      for (entry in ipsDocument.entry) {
        fhirEngine.create(entry.resource)
      }
    }
  }

  private fun constructFhirEngine(): FhirEngine {
    return FhirEngineProvider.getInstance(this)
  }

  companion object {
    fun fhirEngine(context: Context) = (context.applicationContext as FhirApplication).fhirEngine
  }
}
