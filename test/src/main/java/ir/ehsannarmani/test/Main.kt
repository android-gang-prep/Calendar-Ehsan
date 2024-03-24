import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch

fun main() {
    val start = System.currentTimeMillis()
//    SOFTWARE
//    OPENGL
//    SOFTWARE
//    SOFTWARE_COMPAT
//    SOFTWARE_FAST
//    METAL
//    DIRECT3D = default
    System.setProperty("skiko.renderApi", "SOFTWARE_FAST")
    application {
        Window({
            exitApplication()
        }) {
            println(window.renderApi.name)
            MaterialTheme(darkColors(
                background = Color(0xFF222222)
            )
            ) {
                val bg = with(MaterialTheme.colors.background) {
                    java.awt.Color(red, green, blue)
                }
                LaunchedEffect(window, bg) {
                    window.background = bg
                }
                val scope = rememberCoroutineScope()
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    val state = rememberLazyListState(initialFirstVisibleItemIndex = 50)
                    state.firstVisibleItemScrollOffset
                    val adapter = rememberScrollbarAdapter(state)

                    Row(modifier=Modifier.fillMaxWidth()) {
                        LazyColumn (state = state){
                            items(50) {
                                Text("Test",modifier=Modifier.height(50.dp).background(
                                    Color.Red))
                            }
                            item {
                                Text("Test",modifier=Modifier.height(500.dp).background(
                                    Color.Red))
                            }
                        }
                        VerticalScrollbar(adapter = adapter)
                        Button(onClick = {
                            scope.launch {
                                state.scrollBy(5f)
                            }
                        }){
                            Text("Test")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FpsCounter() {
    var fpsValue by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val fpsCounter = org.jetbrains.skiko.FPSCounter(periodSeconds =0.1)
        while (true) {
            withFrameNanos {
                fpsValue = fpsCounter.average
                fpsCounter.tick()
            }
        }
    }
    Text("$fpsValue")
}
