package ir.ehsannarmani.calendar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.ehsannarmani.calendar.utils.EventCategories

@Entity("events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    val title:String,
    val category:String,
    val time:Long
){
    val eventCategory:EventCategories get() {
        return EventCategories.entries.find { it.categoryName == category }!!
    }
}