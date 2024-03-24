package ir.ehsannarmani.calendar.widgets

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import ir.ehsannarmani.calendar.MainActivity
import ir.ehsannarmani.calendar.R
import ir.ehsannarmani.calendar.database.AppDatabase
import ir.ehsannarmani.calendar.database.EventEntity
import ir.ehsannarmani.calendar.navigation.Routes
import ir.ehsannarmani.calendar.utils.EventCategories
import ir.ehsannarmani.calendar.viewModels.EventsViewModel
import ir.ehsannarmani.calendar.viewModels.generateFakeEvents
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val room = remember {
                Room.databaseBuilder(context, AppDatabase::class.java, "db")
                    .allowMainThreadQueries()
                    .build()
            }
            val fakeEvents = remember {
                generateFakeEvents()
            }
            val events = remember {
                mutableStateOf(room.eventDao().getLatestEvents() + fakeEvents)
            }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(500)
                    (room.eventDao().getLatestEvents() + fakeEvents).also { events.value = it }
                }
            }
            val groupEventsByMonth = events.value.groupBy {
                Calendar.getInstance().apply { timeInMillis = it.time }.get(Calendar.MONTH)
            }

            Column(modifier = GlanceModifier.fillMaxSize().background(Color(0xff313131))) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp).background(Color.DarkGray),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = Calendar.getInstance().getDisplayName(
                            Calendar.MONTH, Calendar.LONG,
                            Locale.getDefault()
                        ), style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp)
                    )
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.End
                    ) {
                        val key = ActionParameters.Key<String>("destination")
                        Image(
                            modifier = GlanceModifier.size(45.dp).padding(10.dp)
                                .cornerRadius(100.dp).clickable(
                                    actionStartActivity<MainActivity>(actionParametersOf(key to Routes.AddEvent.route))
                                ),
                            provider = ImageProvider(R.drawable.ic_add),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                        )
                    }
                }
                Box(modifier = GlanceModifier.padding(horizontal = 12.dp)) {
                    Events(
                        events = groupEventsByMonth
                    )
                }

            }
        }
    }
}

@Composable
fun Events(
    events: Map<Int, List<EventEntity>>
) {
    LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
        events.forEach { (month, monthEvents) ->
            item {
                Box(
                    modifier = GlanceModifier.height(40.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    val minDay = monthEvents.minOf {
                        Calendar.getInstance().apply { timeInMillis = it.time }
                            .get(Calendar.DAY_OF_MONTH)
                    }
                    val maxDay = monthEvents.maxOf {
                        Calendar.getInstance().apply { timeInMillis = it.time }
                            .get(Calendar.DAY_OF_MONTH)
                    }
                    val calendar = Calendar.getInstance()
                    val monthName = calendar.apply { set(Calendar.MONTH, month) }
                        .getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                    Text(
                        modifier = GlanceModifier.padding(start = 60.dp),
                        text = "$monthName $minDay - $maxDay",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = ColorProvider(Color.Gray)
                        ),
                    )
                }
            }
            item { Spacer(modifier = GlanceModifier.height(8.dp)) }

            monthEvents
                .groupBy {
                    Calendar.getInstance().apply { timeInMillis = it.time }
                        .get(Calendar.DAY_OF_MONTH)
                }
                .forEach { (day, dayEvents) ->
                    dayEvents.sortedBy {
                        Calendar.getInstance().apply { timeInMillis = it.time }
                            .get(Calendar.DAY_OF_MONTH)
                    }.forEachIndexed { index, event ->

                        item {
                            Row {
                                Column(
                                    modifier = GlanceModifier.size(56.dp),
                                    verticalAlignment = Alignment.Vertical.CenterVertically,
                                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                                ) {
                                    if (index == 0){
                                        Text(
                                            text = "Sun",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = ColorProvider(Color.Gray)
                                            )
                                        )
                                        Text(
                                            text = day.toString(),
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold, color = ColorProvider(
                                                    Color.White
                                                )
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Column {
                                    Column(
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .cornerRadius(12.dp)
                                            .background(event.eventCategory.color)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = event.title,
                                            style = TextStyle(
                                                color = ColorProvider(event.eventCategory.contentColor)
                                            ),
                                        )
                                        val formatter = SimpleDateFormat("HH:mm")
                                        Text(
                                            text = formatter.format(event.time),
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = ColorProvider(event.eventCategory.contentColor)
                                            ),
                                        )
                                    }
                                    Spacer(modifier = GlanceModifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

        }
    }

}