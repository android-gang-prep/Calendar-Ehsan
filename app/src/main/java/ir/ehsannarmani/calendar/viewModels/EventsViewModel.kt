package ir.ehsannarmani.calendar.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.ehsannarmani.calendar.database.EventDao
import ir.ehsannarmani.calendar.database.EventEntity
import ir.ehsannarmani.calendar.utils.EventCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class EventsViewModel(private val dao: EventDao) : ViewModel() {
    private val _events = MutableStateFlow(emptyList<EventEntity>())
    val events = _events.asStateFlow()

    private val _filteredEvents = MutableStateFlow<List<EventEntity>?>(null)
    val filteredEvents = _filteredEvents.asStateFlow()

    init {
        getEvents()
    }

    private fun getEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getEvents().collect { result ->
                _events.update { result + generateFakeEvents() }
            }
        }
    }

    fun filterEvents(query:String){
        if (query.isEmpty()) {
            _filteredEvents.update { null }
        }else{
            _filteredEvents.update { _events.value.filter { it.title.contains(query) } }
        }
    }

}

fun generateFakeEvents():List<EventEntity>{
    val calendar = Calendar.getInstance()
    val results = mutableListOf<EventEntity>()
    repeat(10) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        repeat(3){
            results.add(
                EventEntity(
                    title = "Fake Event Generated For Test",
                    category = EventCategories.entries.map { it.categoryName }.random(),
                    time = calendar.timeInMillis
                )
            )
        }
    }
    return results
}