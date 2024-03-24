package ir.ehsannarmani.calendar

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ir.ehsannarmani.calendar.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppState(
    val navController:NavHostController,
    val scope:CoroutineScope,
    val context:Context,
    val drawerState: DrawerState,
) {
    var currentRoute:MutableStateFlow<Routes?> = MutableStateFlow(null)
    private var _appBarTitle = MutableStateFlow<Pair<String,Int>?>(null)
    var appBarTitle = _appBarTitle.asStateFlow()


    fun setAppBarTitle(title:String,round:Int){
        _appBarTitle.update { title to round }
    }

    init {
        scope.launch {
            navController.currentBackStackEntryFlow.collect{entry->
                val current = Routes.entries.find { it.route == entry.destination.route }
                currentRoute.update {current }
            }
        }
    }
}

@Composable
fun rememberAppState():AppState {

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    return remember {
        AppState(navController, scope, context, drawerState)
    }
}

val LocalAppState = staticCompositionLocalOf<AppState> { error("No State Provided Yet!") }