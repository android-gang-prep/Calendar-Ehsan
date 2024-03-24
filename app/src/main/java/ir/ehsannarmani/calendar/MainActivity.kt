package ir.ehsannarmani.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ir.ehsannarmani.calendar.navigation.Routes
import ir.ehsannarmani.calendar.screens.AddEventScreen
import ir.ehsannarmani.calendar.screens.EventsScreen
import ir.ehsannarmani.calendar.screens.GridMonthScreen
import ir.ehsannarmani.calendar.ui.theme.CalendarEhsanTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarEhsanTheme(true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val appState = LocalAppState.current
                    val currentRoute by appState.currentRoute.collectAsState()
                    val appBarTitle by appState.appBarTitle.collectAsState()
                    val pagerState = remember {
                        mutableStateOf<PagerState?>(null)
                    }

                    val startDestination = intent.getStringExtra("destination") ?: Routes.GridMonth.route

                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(modifier = Modifier.height(22.dp))
                                Routes.entries.forEach {
                                    NavigationDrawerItem(
                                        modifier = Modifier.padding(end = 16.dp),
                                        label = {
                                            Text(text = it.title)
                                        },
                                        selected = it.route == currentRoute?.route,
                                        onClick = {
                                            if (it.route != currentRoute?.route) {
                                                appState.navController.navigate(it.route) {
                                                    popUpTo(0) {
                                                        inclusive = false
                                                    }
                                                }
                                                appState.setAppBarTitle(it.title, it.id)
                                            }
                                            appState.scope.launch {
                                                delay(100)
                                                appState.drawerState.close()
                                            }
                                        },
                                        shape = CircleShape.copy(
                                            bottomStart = CornerSize(0.dp),
                                            topStart = CornerSize(0.dp)
                                        ),
                                        icon = {
                                            Icon(
                                                imageVector = it.icon,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        drawerState = appState.drawerState
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(title = {
                                    AnimatedContent(
                                        targetState = appBarTitle ?: ((currentRoute?.title
                                            ?: "Calendar") to 0), transitionSpec = {
                                            if (targetState.second > initialState.second) {
                                                // If the target number is larger, it slides up and fades in
                                                // while the initial (smaller) number slides up and fades out.
                                                slideInVertically { height -> height } + fadeIn() togetherWith
                                                        slideOutVertically { height -> -height } + fadeOut()
                                            } else if (targetState.second < initialState.second) {
                                                // If the target number is smaller, it slides down and fades in
                                                // while the initial number slides down and fades out.
                                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                                        slideOutVertically { height -> height } + fadeOut()
                                            } else {
                                                (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                                            }.using(
                                                // Disable clipping since the faded slide-in/out should
                                                // be displayed out of bounds.
                                                SizeTransform(clip = false)
                                            )
                                        },
                                        label = "app bar title"
                                    ) {
                                        Text(text = it.first)
                                    }
                                }, navigationIcon = {
                                    IconButton(onClick = {
                                        appState.scope.launch {
                                            appState.drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = null
                                        )
                                    }
                                }, actions = {
                                    AnimatedVisibility(visible = currentRoute?.route == Routes.GridMonth.route) {
                                        IconButton(onClick = {
                                            val calendar = Calendar.getInstance()
                                            val thisMonth = calendar.get(Calendar.MONTH)
                                            pagerState.value?.let {
                                                appState.scope.launch {
                                                    it.animateScrollToPage(
                                                        thisMonth - 1,
                                                        animationSpec = tween(500)
                                                    )
                                                }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    AnimatedVisibility(visible = currentRoute?.route == Routes.GridMonth.route) {
                                        IconButton(onClick = {
                                            appState.navController.navigate(Routes.Events.route+"/true")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                })
                            },
                            floatingActionButton = {
                                AnimatedVisibility(
                                    visible = currentRoute?.route != Routes.AddEvent.route,
                                    enter = scaleIn(),
                                    exit = scaleOut()
                                ) {
                                    FloatingActionButton(onClick = {
                                        appState.setAppBarTitle(
                                            Routes.AddEvent.title,
                                            Routes.AddEvent.id
                                        )
                                        appState.navController.navigate(Routes.AddEvent.route)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        ) {
                            NavHost(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it),
                                navController = appState.navController,
                                startDestination = startDestination,
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(500)
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(500)
                                    )
                                },
                                popEnterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(500)
                                    )
                                },
                                popExitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(500)
                                    )
                                },
                            ) {
                                composable(Routes.GridMonth.route) {
                                    GridMonthScreen(onPagerStateReady = {
                                        pagerState.value = it
                                    })
                                }
                                composable(Routes.Events.route+"/{search}", arguments = listOf(navArgument("search"){
                                    type = NavType.BoolType
                                })) {
                                    EventsScreen(it.arguments?.getBoolean("search") ?: false)
                                }
                                composable(Routes.Events.route) {
                                    EventsScreen(false)
                                }
                                composable(Routes.AddEvent.route) {
                                    AddEventScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



