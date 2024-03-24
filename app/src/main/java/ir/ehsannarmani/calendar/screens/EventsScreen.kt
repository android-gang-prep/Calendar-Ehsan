package ir.ehsannarmani.calendar.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.calendar.LocalAppState
import ir.ehsannarmani.calendar.database.EventEntity
import ir.ehsannarmani.calendar.utils.filterByDayAndMonth
import ir.ehsannarmani.calendar.utils.filterByMonth
import ir.ehsannarmani.calendar.viewModels.EventsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(search:Boolean,viewModel: EventsViewModel = koinViewModel()) {
    val appState = LocalAppState.current

    val events by viewModel.events.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()

    val groupEventsByMonth = (filteredEvents ?: events).groupBy {
        Calendar.getInstance().apply { timeInMillis = it.time }.get(Calendar.MONTH)
    }

    var topBarCurrentMonth = remember {
        groupEventsByMonth.keys.firstOrNull()
    }
    var searchQuery = remember {
        mutableStateOf("")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (search){
            TextField(
                value = searchQuery.value,
                onValueChange = {
                    searchQuery.value = it
                    viewModel.filterEvents(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Search")
                })
        }
        Events(
            currentMonth = topBarCurrentMonth,
            onCurrentChange = {
                topBarCurrentMonth = it
            },
            onTitleChange = { title, round ->
                appState.setAppBarTitle(title, round)
            },
            events = groupEventsByMonth
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Events(
    currentMonth: Int?,
    onCurrentChange: (Int) -> Unit,
    onTitleChange: (String, Int) -> Unit,
    events: Map<Int, List<EventEntity>>
) {
    val itemsState = rememberLazyListState()
    val daysState = rememberLazyListState()

    val appState = LocalAppState.current

    LaunchedEffect(itemsState.firstVisibleItemScrollOffset) {
        appState.scope.launch {
            daysState.scrollToItem(
                itemsState.firstVisibleItemIndex,
                itemsState.firstVisibleItemScrollOffset
            )
        }
    }

    LaunchedEffect(itemsState.firstVisibleItemIndex) {
        val visibleMonth = events.values.flatten().getOrNull(itemsState.firstVisibleItemIndex - 2)
        if (visibleMonth == null) {
            println("null visible")
        } else {
            val visibleCalendar = Calendar.getInstance().apply { timeInMillis = visibleMonth.time }
            val visibleMonthNumber = visibleCalendar.get(Calendar.MONTH)
            if (visibleMonthNumber != currentMonth) {
                onTitleChange(
                    visibleCalendar.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        Locale.getDefault()
                    ), visibleMonthNumber
                )
                onCurrentChange(visibleMonthNumber)
            }

        }
    }

    val gozState = rememberLazyListState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.width(70.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = daysState,
            userScrollEnabled = false,

            ) {
            events.forEach { (month, monthEvents) ->
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
                monthEvents
                    .groupBy {
                        Calendar.getInstance().apply { timeInMillis = it.time }
                            .get(Calendar.DAY_OF_MONTH)
                    }
                    .forEach { (day, dayEvents) ->
                        dayEvents.forEachIndexed { index, event ->
                            if (index == 0) {
                                stickyHeader {
                                    Column(
                                        modifier = Modifier.size(56.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = Calendar.getInstance().apply {
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, day)
                                            }.getDisplayName(
                                                Calendar.DAY_OF_WEEK,
                                                Calendar.SHORT,
                                                Locale.getDefault()
                                            ), fontSize = 13.sp, color = Color.Gray
                                        )
                                        Text(
                                            text = day.toString(),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Box(modifier = Modifier.size(56.dp))
                                }
                            }
                        }
                    }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), state = itemsState) {
            events.forEach { (month, monthEvents) ->
                item {
                    Box(
                        modifier = Modifier.height(40.dp),
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
                            text = "$monthName $minDay - $maxDay",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
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
                                Column {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(event.eventCategory.color)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = event.title,
                                            color = event.eventCategory.contentColor
                                        )
                                        val formatter = SimpleDateFormat("HH:mm")
                                        Text(
                                            text = formatter.format(event.time),
                                            fontSize = 13.sp,
                                            color = event.eventCategory.color
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}



