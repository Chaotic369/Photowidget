package com.example.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

class PhotoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        PhotoStore.advanceIfDue(context)
        ids.forEach { render(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT) {
            PhotoStore.advance(context)
            refreshAll(context)
        }
    }

    companion object {
        const val ACTION_NEXT = "com.example.photowidget.ACTION_NEXT"

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, PhotoWidgetProvider::class.java))
            ids.forEach { render(context, mgr, it) }
        }

        private fun render(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_photo)
            val bmp = PhotoStore.current(context)

            val openApp = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_settings, openApp)

            if (bmp == null) {
                views.setViewVisibility(R.id.photo, View.GONE)
                views.setViewVisibility(R.id.empty_text, View.VISIBLE)
                views.setOnClickPendingIntent(R.id.root, openApp)
            } else {
                views.setViewVisibility(R.id.photo, View.VISIBLE)
                views.setViewVisibility(R.id.empty_text, View.GONE)
                views.setImageViewBitmap(R.id.photo, bmp)
                val next = PendingIntent.getBroadcast(
                    context, 1,
                    Intent(context, PhotoWidgetProvider::class.java).setAction(ACTION_NEXT),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.root, next)
            }
            mgr.updateAppWidget(id, views)
        }
    }
}
