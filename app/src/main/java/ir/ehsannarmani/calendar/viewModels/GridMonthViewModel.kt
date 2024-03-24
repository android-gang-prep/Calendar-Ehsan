package ir.ehsannarmani.calendar.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.ehsannarmani.calendar.database.EventDao
import ir.ehsannarmani.calendar.database.EventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GridMonthViewModel(private val dao: EventDao): ViewModel() {
    private val _events = MutableStateFlow(emptyList<EventEntity>())
    val events = _events.asStateFlow()

    init {
        getEvents()
    }

    private fun getEvents(){
        viewModelScope.launch(Dispatchers.IO){
            dao.getEvents().collect{result->
                _events.update { result }
            }
        }
    }
}