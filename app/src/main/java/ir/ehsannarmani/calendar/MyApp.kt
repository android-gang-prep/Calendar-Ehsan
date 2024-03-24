package ir.ehsannarmani.calendar

import android.app.Application
import androidx.room.Room
import ir.ehsannarmani.calendar.database.AppDatabase
import ir.ehsannarmani.calendar.viewModels.AddEventViewModel
import ir.ehsannarmani.calendar.viewModels.EventsViewModel
import ir.ehsannarmani.calendar.viewModels.GridMonthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(module {
                single {
                    Room.databaseBuilder(this@MyApp,AppDatabase::class.java,"db")
                        .build()
                }
                single {
                    val room:AppDatabase = get()
                    room.eventDao()
                }
                viewModel {
                    AddEventViewModel(get())
                }
                viewModel {
                    GridMonthViewModel(get())
                }
                viewModel {
                    EventsViewModel(get())
                }
            })
        }
    }
}