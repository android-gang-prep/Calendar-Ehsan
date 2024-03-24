package ir.ehsannarmani.calendar.widgets

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import ir.ehsannarmani.calendar.MainActivity
import ir.ehsannarmani.calendar.R
import ir.ehsannarmani.calendar.database.AppDatabase
import ir.ehsannarmani.calendar.database.EventEntity
import ir.ehsannarmani.calendar.navigation.Routes
import ir.ehsannarmani.calendar.screens.weekDays
import ir.ehsannarmani.calendar.utils.filterByDayAndMonth
import ir.ehsannarmani.calendar.utils.restOfFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil





class MonthGridWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode
        get() = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {



        provideContent {

            val room = remember {
                Room.databaseBuilder(context,AppDatabase::class.java,"db")
                    .allowMainThreadQueries()
                    .build()
            }

            val events = remember {
                mutableStateOf(room.eventDao().getLatestEvents())
            }
            LaunchedEffect(Unit) {
                while (true){
                    delay(500)
                    (room.eventDao().getLatestEvents()).also { events.value = it }
                }
            }
            val now = Calendar.getInstance()
            val currentMonthName = remember {
                mutableStateOf(
                    now.getDisplayName(
                        Calendar.MONTH, Calendar.LONG,
                        Locale.getDefault()
                    )
                )
            }
            val currentMonth = remember {
                mutableIntStateOf(now.get(Calendar.MONTH))
            }
            Column(
                modifier = GlanceModifier.fillMaxSize().background(Color(0xff313131)),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = GlanceModifier.padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = currentMonthName.value,
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp)
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
                        Image(
                            modifier = GlanceModifier.size(45.dp).padding(10.dp)
                                .cornerRadius(100.dp).clickable {
                                if (currentMonth.value >= 1) {
                                    currentMonth.value -= 1
                                    currentMonthName.value = Calendar.getInstance()
                                        .apply { set(Calendar.MONTH, currentMonth.value) }
                                        .getDisplayName(
                                            Calendar.MONTH,
                                            Calendar.LONG,
                                            Locale.getDefault()
                                        )
                                }
                            },
                            provider = ImageProvider(R.drawable.ic_arr_left),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                        )
                        Image(
                            modifier = GlanceModifier.size(45.dp).padding(10.dp)
                                .cornerRadius(100.dp).clickable {
                                if (currentMonth.value < 12) {
                                    currentMonth.value += 1
                                    currentMonthName.value = Calendar.getInstance()
                                        .apply { set(Calendar.MONTH, currentMonth.value) }
                                        .getDisplayName(
                                            Calendar.MONTH,
                                            Calendar.LONG,
                                            Locale.getDefault()
                                        )
                                }
                            },
                            provider = ImageProvider(R.drawable.ic_arr_right),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                        )
                    }
                }
                CalendarGrid(
                    month = currentMonth.value,
                    weekStart = "Sunday",
                    events = events.value
                )
            }
        }
    }

}

@Composable
private fun CalendarGrid(
    month: Int,
    weekStart: String,
    events: List<EventEntity>,
) {

    val dividerColor = Color.White.copy(.6f)
    val dividerStroke = .5f

    val monthCalendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, month)
    }
    val now = Calendar.getInstance()
    val today = now.get(Calendar.DAY_OF_MONTH)
    val thisMonth = now.get(Calendar.MONTH)
    val firstOfMonth = monthCalendar.apply { set(Calendar.DAY_OF_MONTH, 1) }
    val monthStartedFrom = firstOfMonth.getDisplayName(
        Calendar.DAY_OF_WEEK, Calendar.LONG,
        Locale.getDefault()
    )
    val restOfWeekDays = weekDays.restOfFrom(weekStart)
    val maxDayOfMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Row {
            restOfWeekDays.forEachIndexed { index, name->
                DayItem(
                    height = 20.dp,
                    dividerColor = dividerColor,
                    dividerStroke = dividerStroke,
                    day = name.filterIndexed { index, c -> index in 0..2 },
                    currentDay = false,
                    dayEvents = listOf(),
                    applyPadding = false,
                    firstOnRow = index == 0
                )
            }
        }

        val previousMonthDayCount = restOfWeekDays.indexOf(monthStartedFrom)
        val currentMonthItemsInFirstRow = 7 - previousMonthDayCount

        val countOfNextMonthDayCanBeDisplayed = (7 - ((maxDayOfMonth + previousMonthDayCount) % 7))
        val restOfDaysShouldShow =
            maxDayOfMonth - currentMonthItemsInFirstRow + countOfNextMonthDayCanBeDisplayed
        val totalRows = ceil(restOfDaysShouldShow / 7.0).toInt() + 1
        val itemHeight = (LocalSize.current.height - 70.dp) / totalRows
        Row {
            repeat(previousMonthDayCount) {
                val previousMonth = Calendar.getInstance().apply {
                    set(Calendar.MONTH, month - 1)
                }
                val maxDayOfPreviousMonth = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                val indexPlus = it + 1
                val day = maxDayOfPreviousMonth - (previousMonthDayCount - indexPlus)
                DayItem(
                    dividerColor = dividerColor,
                    dividerStroke = dividerStroke,
                    day = day.toString(),
                    currentDay = false,
                    dayEvents = events.filterByDayAndMonth(day, month - 1),
                    height = itemHeight,
                    firstOnRow = it == 0
                )
            }
            repeat(currentMonthItemsInFirstRow) {
                val day = it + 1
                DayItem(
                    dividerColor = dividerColor,
                    dividerStroke = dividerStroke,
                    day = day.toString(),
                    currentDay = day == today && month == thisMonth,
                    dayEvents = events.filterByDayAndMonth(day, month),
                    height = itemHeight,
                    firstOnRow = previousMonthDayCount == 0 && it == 0
                )
            }
        }


        var newMonthDay = -1
        var cycleChanged: Boolean
        repeat(totalRows - 1) { rowIndex ->
            Row {
                repeat(7) { dayIndex ->
                    var day = rowIndex * 7 + dayIndex + currentMonthItemsInFirstRow
                    if (day > maxDayOfMonth) {
                        newMonthDay++
                        cycleChanged = true
                        day = newMonthDay
                    }else{
                        cycleChanged = false
                    }
                    day+=1
                    DayItem(
                        dividerColor = dividerColor,
                        dividerStroke = dividerStroke,
                        day = day.toString(),
                        currentDay = day == today && month == thisMonth,
                        dayEvents = events.filterByDayAndMonth(day, if (cycleChanged) month+1 else month),
                        height = itemHeight,
                        firstOnRow = dayIndex == 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayItem(
    width: Dp = LocalSize.current.width / 7,
    height: Dp,
    dividerColor: Color,
    dividerStroke: Float,
    day: String,
    currentDay: Boolean,
    dayEvents: List<EventEntity>,
    applyPadding: Boolean = true,
    firstOnRow: Boolean,
) {

    Row(horizontalAlignment = Alignment.Horizontal.End) {
        Box(
            modifier = GlanceModifier.width(width).height(height),
            contentAlignment = Alignment.BottomCenter
        ) {

            Column(
                modifier = GlanceModifier.width(width).height(height).padding(
                    top = if (applyPadding) 12.dp else 0.dp,
                    bottom = if (applyPadding) 4.dp else 0.dp
                ),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    day,
                    modifier = GlanceModifier.size(20.dp).cornerRadius(100.dp)
                        .background(if (currentDay) Color(0xFF0277BD) else Color.Transparent),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color.White),
                        textAlign = TextAlign.Center
                    )
                )

            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.Bottom
            ) {
                dayEvents.reversed().forEach {
                    Box(
                        modifier = GlanceModifier.cornerRadius(4.dp)
                            .background(it.eventCategory.color)
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = it.eventCategory.categoryName, style = TextStyle(
                                color = ColorProvider(
                                    Color.White
                                ), fontSize = 10.sp
                            )
                        )
                    }
                }
            }
            Box(modifier = GlanceModifier.height(1.dp).fillMaxWidth().background(dividerColor)) {}
            if (firstOnRow){
                Box(modifier=GlanceModifier.width(width).height(height), contentAlignment = Alignment.CenterStart) {
                    Box(modifier = GlanceModifier.height(height).width(1.dp).background(dividerColor)) {}
                }
            }
        }
        Box(modifier = GlanceModifier.height(height).width(1.dp).background(dividerColor)) {}

    }
}