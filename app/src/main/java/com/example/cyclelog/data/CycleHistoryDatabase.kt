package com.example.cyclelog.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "cycle_history")
data class CycleHistory(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val year: Int,
  val month: Int,
  val day: Int,
  val dateTime: LocalTime,
  val duration: Long,
  val distance: Double,
  val path: List<Point>,
  val calories: Int,
  val weight: Float
)

@Dao
interface CycleHistoryDao {
  @Insert
  suspend fun insert(cycleHistory: CycleHistory)

  @Query(
    """
    UPDATE cycle_history
    SET weight = :newWeight
    WHERE id = (
      SELECT id FROM cycle_history
      ORDER BY id DESC
      LIMIT 1
    )
    """
  )
  suspend fun setWeight(newWeight: Float)

  @Query("SELECT weight FROM cycle_history ORDER BY id DESC LIMIT 1")
  fun getLatestWeightFlow(): Flow<Float?>

  @Query("SELECT * FROM cycle_history ORDER BY id DESC")
  fun getAllCycleHistoryFlow(): Flow<List<CycleHistory>>

  @Query("DELETE FROM cycle_history")
  suspend fun deleteAll()
}

@Database(entities = [CycleHistory::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun cycleHistoryDao(): CycleHistoryDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "cycle_history_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}

class Converters {
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
  private val gson = Gson()

  @TypeConverter
  fun fromLocalTime(time: LocalTime): String {
    return time.format(timeFormatter)
  }

  @TypeConverter
  fun toLocalTime(timeString: String): LocalTime {
    return LocalTime.parse(timeString, timeFormatter)
  }

  @TypeConverter
  fun fromPointList(points: List<Point>): String {
    return gson.toJson(points)
  }

  @TypeConverter
  fun toPointList(data: String): List<Point> {
    val listType = object : TypeToken<List<Point>>() {}.type
    return gson.fromJson(data, listType)
  }
}