package com.google.android.fhir.ipsapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.fhir.document.IPSDocument
import com.google.android.fhir.document.generate.QRGeneratorUtils
import com.google.android.fhir.document.scan.SHLinkScanData
import kotlinx.coroutines.launch


class GetData : AppCompatActivity() {
  private var shl: SHLinkScanData? = null
  private var qrCodeImageView : ImageView? = null
  private var qrCodeLayout: View? = null
  private var detailsLayout : ViewGroup? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.get_data)
    detailsLayout = findViewById(R.id.scrollView)
    qrCodeImageView = findViewById(R.id.qr_code)
    qrCodeLayout = findViewById(R.id.qr_code_frame)
    val cancel : View = findViewById(R.id.cancel)
    cancel.setOnClickListener {
      removeQR()
    }
    val doc = intent.serializable<IPSDocument>("doc")
     shl = intent.serializable<SHLinkScanData>("shlData")

    val ipsRenderer = IPSRenderer(doc)
    ipsRenderer.render(this)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
     menuInflater.inflate(com.google.android.fhir.library.R.menu.share_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == com.google.android.fhir.library.R.id.share) {
      share()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (qrCodeLayout?.isVisible == true) {
      removeQR()
      return
    }
    super.onBackPressed()
  }

  override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

    menu?.let {
      menu.forEach {
        it.setVisible(detailsLayout?.isVisible ?: true)
      }
    }
    return super.onPrepareOptionsMenu(menu)
  }

  private fun share() {

    lifecycleScope.launch {
      QRGeneratorUtils(this@GetData).createQRCodeBitmap(shl!!.fullLink).let {
        qrCodeImageView?.setImageBitmap(it)
        showQR()
      }
    }
  }

  private fun showQR() {
    detailsLayout?.visibility = View.GONE
    qrCodeLayout?.visibility   = View.VISIBLE
    invalidateOptionsMenu()
  }

  private fun removeQR() {
    detailsLayout?.visibility = View.VISIBLE
    qrCodeLayout?.visibility   = View.GONE
    invalidateOptionsMenu()
  }
}