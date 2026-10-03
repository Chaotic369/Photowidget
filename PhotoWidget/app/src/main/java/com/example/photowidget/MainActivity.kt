package com.example.photowidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private lateinit var countText: TextView
    private lateinit var preview: ImageView

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult
        Toast.makeText(this, R.string.importing, Toast.LENGTH_SHORT).show()
        thread {
            val added = PhotoStore.addFromUris(applicationContext, uris)
            runOnUiThread {
                Toast.makeText(this, getString(R.string.added_n, added), Toast.LENGTH_SHORT).show()
                refresh()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countText = findViewById(R.id.count_text)
        preview = findViewById(R.id.preview)

        findViewById<Button>(R.id.btn_add).setOnClickListener { picker.launch(arrayOf("image/*")) }
        findViewById<Button>(R.id.btn_next).setOnClickListener {
            PhotoStore.advance(this); refresh()
        }
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            PhotoStore.clear(this); refresh()
        }
        findViewById<Button>(R.id.btn_pin).setOnClickListener { pinWidget() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val n = PhotoStore.count(this)
        countText.text = resources.getQuantityString(R.plurals.photo_count, n, n)
        val bmp = PhotoStore.current(this)
        if (bmp != null) preview.setImageBitmap(bmp) else preview.setImageDrawable(null)
        PhotoWidgetProvider.refreshAll(this)
    }

    private fun pinWidget() {
        val mgr = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= 26 && mgr.isRequestPinAppWidgetSupported) {
            mgr.requestPinAppWidget(ComponentName(this, PhotoWidgetProvider::class.java), null, null)
        } else {
            Toast.makeText(this, R.string.pin_manual, Toast.LENGTH_LONG).show()
        }
    }
}
