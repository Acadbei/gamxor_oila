package com.example.myapplication.tracking

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: RoutePointEntity)

    @Query("SELECT * FROM route_points ORDER BY timestamp ASC, id ASC")
    fun observeAllPoints(): Flow<List<RoutePointEntity>>

    @Query("SELECT * FROM route_points WHERE routeId = :routeId ORDER BY timestamp ASC, id ASC")
    fun observeRoute(routeId: String): Flow<List<RoutePointEntity>>

    @Query("SELECT DISTINCT routeId FROM route_points ORDER BY routeId DESC")
    fun observeRouteIds(): Flow<List<String>>

    @Query("SELECT routeId FROM route_points ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun latestRouteId(): String?

    @Query("SELECT * FROM route_points WHERE routeId = :routeId ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun latestPoint(routeId: String): RoutePointEntity?

    @Query("DELETE FROM route_points WHERE routeId = :routeId")
    suspend fun deleteRoute(routeId: String)

    @Query("DELETE FROM route_points")
    suspend fun clear()
}
