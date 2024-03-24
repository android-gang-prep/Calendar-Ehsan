package ir.ehsannarmani.calendar.utils

import ir.ehsannarmani.calendar.database.EventEntity
import java.util.Calendar

fun List<EventEntity>.filterByDayAndMonth(day:Int,month: Int): List<EventEntity> {
    return filter {
        val calendar = Calendar.getInstance().apply { timeInMillis = it.time }
        calendar.get(Calendar.DAY_OF_MONTH) == day&&
        calendar.get(Calendar.MONTH) == month
    }
}
fun List<EventEntity>.filterByMonth(month: Int): List<EventEntity> {
    return filter {
        val calendar = Calendar.getInstance().apply { timeInMillis = it.time }
        calendar.get(Calendar.MONTH) == month
    }
}