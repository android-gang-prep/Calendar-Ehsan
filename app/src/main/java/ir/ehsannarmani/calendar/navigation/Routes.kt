package ir.ehsannarmani.calendar.navigation

import android.graphics.drawable.VectorDrawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

enum class Routes(val id:Int,val route:String,val title:String,val icon:ImageVector) {
    GridMonth(id = 0,route = "grid-month",title = "Grid Month", icon = Icons.Default.DateRange),
    Events(id = 0,route = "events",title = "Events", icon = Icons.Rounded.Notifications),
    AddEvent(id = 0,route = "add-event",title = "Add Event", icon = Icons.Rounded.Add),
}