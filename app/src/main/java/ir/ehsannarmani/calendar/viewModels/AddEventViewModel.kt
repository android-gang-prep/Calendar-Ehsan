package ir.ehsannarmani.calendar.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.ehsannarmani.calendar.database.EventDao
import ir.ehsannarmani.calendar.database.EventEntity
import ir.ehsannarmani.calendar.utils.EventCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEventViewModel(private val dao: EventDao): ViewModel() {

    fun addEvent(
        title:String,
        category: EventCategories,
        time:Long
    ){
        viewModelScope.launch(Dispatchers.IO){
            dao.addEvent(EventEntity(
                title = title,
                category = category.categoryName,
                time = time
            ))
        }
    }

}