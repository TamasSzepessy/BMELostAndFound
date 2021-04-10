package hu.bme.aut.android.bmelostandfound.room.db.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    exportSchema = false,
    entities = [RoomContact::class]
)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

}