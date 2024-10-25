package com.ybzl.track

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction

@Entity("tracking_event_model")
data class TrackingEventDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    val eventCode: String,

    val json: String,

    val timestamp: Long
)

@Dao
interface TrackingEventDBModelDao {

    @Insert
    suspend fun insert(trackingEventDBModel: TrackingEventDBModel)


    @Query("select * from tracking_event_model order by timestamp desc")
    suspend fun queryAll(): List<TrackingEventDBModel>

    @Query("select * from tracking_event_model where eventCode=:eventCode order by timestamp desc")
    suspend fun queryEventCode(eventCode: String): List<TrackingEventDBModel>?

    // 新增查询，获取所有唯一的 eventCode
    @Query("SELECT DISTINCT eventCode FROM tracking_event_model")
    fun getAllEventCodes(): List<String>

    @Query("delete from tracking_event_model")
    suspend fun clearEvent()
}

@Database(
    entities = [TrackingEventDBModel::class],
    version = 1,
    exportSchema = false
)
abstract class TrackingEventDataBase : RoomDatabase() {
    companion object {
        private const val DATABASE_NAME = "tracking_db"

        // For Singleton instantiation
        @Volatile
        private var instance: TrackingEventDataBase? = null

        fun getInstance(context: Context): TrackingEventDataBase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): TrackingEventDataBase {
            return Room.databaseBuilder(
                context, TrackingEventDataBase::class.java, DATABASE_NAME
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
        }
    }

    abstract fun trackingEventDao(): TrackingEventDBModelDao

    suspend fun insert(trackingEvent: TrackingEvent) {
        withTransaction {
            trackingEventDao().insert(
                trackingEventDBModel = TrackingEventDBModel(
                    eventCode = trackingEvent.eventCode,
                    timestamp = trackingEvent.timestamp,
                    json = trackingEvent.toJson()
                )
            )
        }
    }

    suspend fun queryAll(): List<TrackingEventDBModel> {
        return withTransaction {
            trackingEventDao().queryAll()
        }
    }

    suspend fun clearEvent(){
        return withTransaction {
            trackingEventDao().clearEvent()
        }
    }

}