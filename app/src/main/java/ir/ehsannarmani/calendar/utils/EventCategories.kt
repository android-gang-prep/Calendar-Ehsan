package ir.ehsannarmani.calendar.utils

import androidx.compose.ui.graphics.Color

enum class EventCategories(val categoryName:String, val color:Color,val contentColor:Color) {
    Job(categoryName = "Job",color = Color(0xFF009688), contentColor = Color.White),
    Birthday(categoryName = "Birthday",color = Color(0xFF03A9F4), contentColor = Color.White),
    Reminder(categoryName = "Reminder",color = Color(0xFFFF5722), contentColor = Color.White),
    Education(categoryName = "Education",color = Color(0xFFFFC107), contentColor = Color.Black),
}