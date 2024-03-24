package ir.ehsannarmani.calendar.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.glance.appwidget.updateAll
import ir.ehsannarmani.calendar.LocalAppState
import ir.ehsannarmani.calendar.navigation.Routes
import ir.ehsannarmani.calendar.utils.EventCategories
import ir.ehsannarmani.calendar.utils.animateToNext
import ir.ehsannarmani.calendar.utils.animateToPrevious
import ir.ehsannarmani.calendar.utils.getNext
import ir.ehsannarmani.calendar.utils.restOfFrom
import ir.ehsannarmani.calendar.viewModels.AddEventViewModel
import ir.ehsannarmani.calendar.widgets.EventWidget
import ir.ehsannarmani.calendar.widgets.MonthGridWidget
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(viewModel: AddEventViewModel = koinViewModel()) {
    val appState = LocalAppState.current

    val eventTitle = remember {
        mutableStateOf("")
    }
    val eventTime = remember {
        mutableLongStateOf(System.currentTimeMillis())
    }
    val eventCalendar = Calendar.getInstance().apply { timeInMillis = eventTime.longValue }
    val eventCategory = remember {
        mutableStateOf(EventCategories.Birthday)
    }

    val dateDialogOpen = remember {
        mutableStateOf(false)
    }
    val timePickerDialogOpen = remember {
        mutableStateOf(false)
    }

    val eventDay = eventCalendar.get(Calendar.DAY_OF_MONTH)
    val eventMonth = eventCalendar.get(Calendar.MONTH)


    if (dateDialogOpen.value) {
        val weekStart = remember {
            mutableStateOf("Sunday")
        }
        val (months, currentMonth) = getMonthList()
        val calendarPagerState = rememberPagerState(pageCount = {
            months.count()
        }, initialPage = currentMonth - 1)
        Dialog(onDismissRequest = { dateDialogOpen.value = false }) {
            val dialogTime = remember {
                mutableLongStateOf(eventTime.longValue)
            }
            val dialogCalendar =
                Calendar.getInstance().apply { timeInMillis = dialogTime.longValue }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            ) {
                val selectedDayName = eventCalendar.getDisplayName(
                    Calendar.DAY_OF_WEEK, Calendar.SHORT,
                    Locale.getDefault()
                )
                val selectedMonthName = eventCalendar.getDisplayName(
                    Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()
                )
                val selectedDay = eventCalendar.get(Calendar.DAY_OF_MONTH)
                Text(
                    text = eventCalendar.get(Calendar.YEAR).toString(),
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Text(
                    text = "$selectedDayName, $selectedMonthName $selectedDay",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedMonth = dialogCalendar.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        Locale.getDefault()
                    )
                    IconButton(onClick = {
                        val newCalendar = dialogCalendar.apply {
                            add(Calendar.MONTH, -1)
                        }
                        dialogTime.longValue = newCalendar.timeInMillis
                        appState.scope.launch {
                            calendarPagerState.animateToPrevious()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    }
                    AnimatedContent(
                        targetState = "$selectedMonth ${dialogCalendar.get(Calendar.YEAR)}",
                        transitionSpec = {
                            (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                        }
                    ) {
                        Text(text = it)
                    }
                    IconButton(onClick = {
                        val newCalendar = dialogCalendar.apply {
                            add(Calendar.MONTH, 1)
                        }
                        dialogTime.longValue = newCalendar.timeInMillis
                        appState.scope.launch {
                            calendarPagerState.animateToNext()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    ) {
                    weekDays.restOfFrom(weekStart.value).forEach {
                        Text(
                            text = it.first().toString() /* show 3 first char of week*/,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable { weekStart.value = it },
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalPager(
                    state = calendarPagerState, modifier = Modifier
                        .fillMaxWidth()
                        , userScrollEnabled = false
                ) {
                    CalendarGrid(
                        month = it + 1,
                        weekStart = weekStart.value,
                        decideIsCurrentDay = { day, month, year ->
                            day == eventDay && month == eventMonth && year == dialogCalendar.get(Calendar.YEAR)
                        },
                        onDaySelected = { day, month ->
                            eventTime.longValue = Calendar.getInstance().apply {
                                set(Calendar.YEAR, dialogCalendar.get(Calendar.YEAR))
                                set(Calendar.DAY_OF_MONTH, day)
                                set(Calendar.MONTH, month)
                            }.timeInMillis
                        })
                }
                Spacer(modifier = Modifier.height(22.dp))
                Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { dateDialogOpen.value = false }) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = {
                        dateDialogOpen.value = false
                        dialogTime.longValue = dialogCalendar.timeInMillis
                        timePickerDialogOpen.value = true
                    }) {
                        Text(text = "Ok")
                    }
                }
            }
        }
    }
    if (timePickerDialogOpen.value){
        val timePickerState = rememberTimePickerState(is24Hour = false)
        Dialog(onDismissRequest = { timePickerDialogOpen.value = false }) {
            Column(modifier= Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd){
                    TextButton(onClick = {
                        eventTime.longValue = eventCalendar.apply {
                            set(Calendar.HOUR,timePickerState.hour)
                            set(Calendar.MINUTE,timePickerState.minute)
                        }.timeInMillis
                        timePickerDialogOpen.value = false
                    }) {
                        Text(text = "Ok")
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { appState.navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null
                )
            }
            Button(onClick = {
                if (eventTitle.value.isNotBlank()){
                    viewModel.addEvent(
                        title = eventTitle.value,
                        category = eventCategory.value,
                        time = eventTime.longValue
                    )
                    appState.scope.launch {
                        MonthGridWidget().updateAll(appState.context)
                    }
                    appState.scope.launch {
                        EventWidget().updateAll(appState.context)
                    }
                    if (appState.navController.graph.startDestinationRoute == Routes.AddEvent.route){
                        appState.navController.navigate(Routes.GridMonth.route){
                            popUpTo(0){
                                inclusive = true
                            }
                        }
                    }else{
                        appState.navController.popBackStack()
                    }

                }
            }) {
                Text(text = "Save")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = eventTitle.value,
            onValueChange = {
                eventTitle.value = it
            },
            placeholder = {
                Text(text = "Add title")
            },
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    eventCategory.value = EventCategories.entries.getNext(eventCategory.value)
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = eventCategory.value.color
                )
            ) {
                Text(text = eventCategory.value.categoryName, color = Color.White)
            }
            Button(onClick = { dateDialogOpen.value = true }) {
                val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
                Text(text = formatter.format(eventTime.longValue))
            }
        }

    }
}

@Composable
private fun CalendarGrid(
    month: Int,
    weekStart: String,
    decideIsCurrentDay: (day: Int, month: Int, year: Int) -> Boolean,
    onDaySelected: (day: Int, month: Int) -> Unit
) {
    val monthCalendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, month)
    }
    val firstOfMonth = monthCalendar.apply { set(Calendar.DAY_OF_MONTH, 1) }
    val monthStartedFrom = firstOfMonth.getDisplayName(
        Calendar.DAY_OF_WEEK, Calendar.LONG,
        Locale.getDefault()
    )
    val restOfWeekDays = weekDays.restOfFrom(weekStart)
    val maxDayOfMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth(),
        userScrollEnabled = false
    ) {
        val previousMonthDayCount = restOfWeekDays.indexOf(monthStartedFrom)
        items(previousMonthDayCount) {}
        items(maxDayOfMonth, key = { "Day$it" }) {
            val day = it + 1
            DayItem(
                day = day,
                currentDay = decideIsCurrentDay(day, month, monthCalendar.get(Calendar.YEAR)),
                onClick = {
                    onDaySelected(day, month)
                }
            )
        }
    }
}

@Composable
fun DayItem(day: Int, currentDay: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (currentDay) Color(0xFF00695C) else Color.Transparent)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.toString(), color = Color.White, fontSize = 13.sp)
    }
}