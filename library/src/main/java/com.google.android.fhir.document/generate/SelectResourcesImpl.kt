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

package com.google.android.fhir.document.generate

import android.content.Context
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.fhir.document.IPSDocument
import com.google.android.fhir.document.Title
import com.google.android.fhir.library.R
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Resource

class SelectResourcesImpl(
  private val docGenUtils: DocumentGeneratorUtils,
  private val docUtils: DocumentUtils,
) : SelectResources {

  /**
   * Generates a new IPS document given a list of patient-selected resources.
   *
   * @param selectedResources List of selected FHIR resources.
   * @return IPSDocument representing the generated document.
   */
  override fun generateIPS(selectedResources: List<Resource>): IPSDocument {
    val composition = docGenUtils.createIPSComposition()
    val sections = docGenUtils.createIPSSections(selectedResources)
    val (missingSections, missingResources) = docGenUtils.checkSections(sections)
    val referenced = mutableListOf<Resource>()

    selectedResources.forEach { resource ->
      val references = findReferences(resource)
      referenced.addAll(references)
    }
    sections.addAll(missingSections)
    composition.section = sections
    val bundle =
      docGenUtils.addResourcesToDoc(composition, selectedResources + referenced, missingResources)
    return IPSDocument.create(bundle)
  }

  /**
   * Renders an IPS document for an Android device, providing options for selected resources.
   *
   * @param context The Android application context.
   * @param bundle IPSDocument to be displayed.
   * @param checkboxes List of CheckBox widgets to be displayed.
   * @param checkboxTitleMap Mapping of CheckBox titles to resource identifiers.
   * @return List of Title objects representing the displayed options.
   */
  override fun displayOptions(
    context: Context,
    bundle: IPSDocument,
    checkboxes: MutableList<CheckBox>,
    checkboxTitleMap: MutableMap<String, String>,
  ): List<Title> {
    val containerLayout =
      (context as AppCompatActivity).findViewById<LinearLayout>(R.id.containerLayout)
    return getDataFromDoc(bundle)
      .filter { title -> title.dataEntries.any { docUtils.getCodings(it)?.isNotEmpty() == true } }
      .map { title ->
        val headingView = docGenUtils.createHeadingView(context, title.name, containerLayout)
        containerLayout.addView(headingView)

        title.dataEntries.forEach { obj ->
          docUtils
            .getCodings(obj)
            ?.firstOrNull { it.hasDisplay() }
            ?.let { codingElement ->
              val displayValue = codingElement.display
              val checkBoxItem = docGenUtils.createCheckBox(context, displayValue, containerLayout)
              containerLayout.addView(checkBoxItem)
              checkboxTitleMap[displayValue] = title.name
              checkboxes.add(checkBoxItem)
            }
        }
        title
      }
  }

  /* Returns a map of all the sections in the document to the list of resources listed under that section */
  private fun getDataFromDoc(doc: IPSDocument): List<Title> {
    for (title in doc.titles) {
      val filteredResources =
        doc.document.entry
          .map { it.resource }
          .filter { resource ->
            val resourceType = resource.resourceType.toString()
            docUtils.getSearchingCondition(title.name, resourceType)
          }
      val resourceList =
        filteredResources.filterNot { docUtils.shouldExcludeResource(title.name, it) }
      val existingTitle = doc.titles.find { it.name == title.name }
      existingTitle?.dataEntries?.addAll(resourceList)
    }
    return doc.titles
  }

  private fun findReferences(resource: Resource): List<Resource> {
    val organizationReferences = mutableListOf<Resource>()
    if (resource is Observation) {
      val performerReferences = resource.performer
      performerReferences.forEach { performerReference ->
        if (performerReference.reference.isNotBlank()) {
          val organization = docGenUtils.createOrganizationFromReference(performerReference)
          organizationReferences.add(organization)
        }
      }
    }
    return organizationReferences
  }
}
