package com.example.myapplication.tracking

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RoutePointEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TrackingDatabase : RoomDatabase() {
    abstract fun routePointDao(): RoutePointDao

    companion object {
        @Volatile
        private var INSTANCE: TrackingDatabase? = null

        fun getInstance(context: Context): TrackingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrackingDatabase::class.java,
                    "family_care_tracking.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
