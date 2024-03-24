package ir.ehsannarmani.calendar.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface EventDao {
    @Insert
    fun addEvent(event:EventEntity)

    @Query("SELECT * FROM events")
    fun getEvents():Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    fun getLatestEvents():List<EventEntity>
}