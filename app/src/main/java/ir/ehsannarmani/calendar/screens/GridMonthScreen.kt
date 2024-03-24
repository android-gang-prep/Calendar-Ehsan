package ir.ehsannarmani.calendar.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.calendar.LocalAppState
import ir.ehsannarmani.calendar.database.EventEntity
import ir.ehsannarmani.calendar.navigation.Routes
import ir.ehsannarmani.calendar.utils.filterByDayAndMonth
import ir.ehsannarmani.calendar.utils.restOfFrom
import ir.ehsannarmani.calendar.viewModels.GridMonthViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridMonthScreen(onPagerStateReady:(PagerState)->Unit,viewModel:GridMonthViewModel = koinViewModel()) {
    val appState = LocalAppState.current

    val events by viewModel.events.collectAsState()

    val weekStartFrom = remember {
        mutableStateOf("Sunday")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val (months, currentMonth) = getMonthList()
        val pagerState =
            rememberPagerState(pageCount = { months.count() }, initialPage = currentMonth - 1)
        onPagerStateReady(pagerState)
        val appBarTitle = months[pagerState.currentPage]
        LaunchedEffect(appBarTitle) {
            appState.setAppBarTitle(appBarTitle,0   )
        }
        HorizontalPager(state = pagerState) {
            val month = it+1
            CalendarGrid(
                month = month,
                weekStart = weekStartFrom.value,
                onNewWeekStart = {weekStartFrom.value = it},
                events = events
            )
        }
    }
}

@Composable
private fun CalendarGrid(month: Int,weekStart:String,events:List<EventEntity>,onNewWeekStart:(String)->Unit) {

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

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
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        val dividerColor = Color.White.copy(.6f)
        val dividerStroke = .5f

        val spaceBetweenWeekNamesAndDays = 12.dp
        val dayBoxHeight = 130.dp

        items(restOfWeekDays, key = { "Week${it}" }) {
            Text(
                text = it.filterIndexed {i,c-> i in 0..2  } /* show 3 first char of week*/,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val heightWithPadding =
                            size.height + (spaceBetweenWeekNamesAndDays.value * density.density)
                        drawLine(
                            color = dividerColor,
                            strokeWidth = dividerStroke,
                            start = Offset(0f, heightWithPadding),
                            end = Offset(size.width, heightWithPadding)
                        )
                        drawLine(
                            color = dividerColor,
                            strokeWidth = dividerStroke,
                            start = Offset(size.width, heightWithPadding),
                            end = Offset(size.width, heightWithPadding - heightWithPadding / 3)
                        )
                    }
                    .clickable { onNewWeekStart(it) },
                textAlign = TextAlign.Center
            )
        }
        items(7, key = { "Spacer$it" }) {
            Spacer(modifier = Modifier.height(spaceBetweenWeekNamesAndDays))
        }
        val previousMonthDayCount = restOfWeekDays.indexOf(monthStartedFrom)

        val screenHeightWithComponents = configuration.screenHeightDp - 56 - 40
        val maxRowsInScreen = ceil(screenHeightWithComponents / dayBoxHeight.value)
        val minRowsInScreen = ceil((maxDayOfMonth+previousMonthDayCount) / 7.0)
        val additionalRows = (maxRowsInScreen - minRowsInScreen).toInt()

        items(previousMonthDayCount){
            val previousMonth = Calendar.getInstance().apply {
                set(Calendar.MONTH,month-1)
            }
            val maxDayOfPreviousMonth = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val indexPlus = it+1
            val day = maxDayOfPreviousMonth-(previousMonthDayCount-indexPlus)
            DayItem(
                dividerColor = dividerColor,
                dividerStroke = dividerStroke,
                day = day,
                currentDay = false,
                dayEvents = events.filterByDayAndMonth(day,month-1)
            )
        }
        items(maxDayOfMonth, key = { "Day$it" }) {
            val day = it + 1
            DayItem(
                dividerColor = dividerColor,
                dividerStroke = dividerStroke,
                day = day,
                currentDay = day == today && month == thisMonth,
                dayEvents = events.filterByDayAndMonth(day,month)
            )
        }

        // next month items
        val countOfNextMonthDayCanBeDisplayed = (additionalRows * 7) + (7 - ((maxDayOfMonth+previousMonthDayCount) % 7))
        items(countOfNextMonthDayCanBeDisplayed, key = { "RestDay$it" }) {
            val day = it + 1
            DayItem(
                dividerColor = dividerColor,
                dividerStroke = dividerStroke,
                day = day,
                currentDay = false,
                dayEvents = events.filterByDayAndMonth(day,month+1)
            )
        }
    }
}

@Composable
private fun DayItem(
    dividerColor: Color,
    dividerStroke: Float,
    day: Int,
    currentDay: Boolean,
    dayEvents:List<EventEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    strokeWidth = dividerStroke,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height)
                )
                drawLine(
                    color = dividerColor,
                    strokeWidth = dividerStroke,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height)
                )
            }
            .padding(
                top = 12.dp,
                bottom = 4.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.toString(), modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (currentDay) MaterialTheme.colorScheme.primary else Color.Transparent),
            fontSize = 12.sp,
            color = if (currentDay) Color.Black else Color.White,
            textAlign = TextAlign.Center
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            dayEvents.reversed().forEach {
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(it.eventCategory.color)
                    .padding(vertical = 2.dp, horizontal = 4.dp)){
                    Text(text = it.eventCategory.categoryName, color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

fun getMonthList(): Pair<List<String>, Int> {
    val result = mutableListOf<String>()
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    var currentMonth = 0
    repeat(12) {
        val month = it + 1
        val isCurrent = month == now.get(Calendar.MONTH)
        calendar.set(Calendar.MONTH, month)
        val monthName =
            calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Unknown"
        if (isCurrent) currentMonth = month
        result.add(monthName)
    }
    return result to currentMonth
}