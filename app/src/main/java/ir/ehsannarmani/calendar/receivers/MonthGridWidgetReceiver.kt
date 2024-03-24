package ir.ehsannarmani.calendar.receivers

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import ir.ehsannarmani.calendar.widgets.EventWidget
import ir.ehsannarmani.calendar.widgets.MonthGridWidget

class MonthGridWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = MonthGridWidget()
}