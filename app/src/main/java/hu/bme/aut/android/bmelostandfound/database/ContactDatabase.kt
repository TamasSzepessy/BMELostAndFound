package hu.bme.aut.android.bmelostandfound.database

import androidx.room.Database
import androidx.room.RoomDatabase
import hu.bme.aut.android.bmelostandfound.database.ContactDao
import hu.bme.aut.android.bmelostandfound.database.RoomContact

@Database(
    version = 1,
    exportSchema = false,
    entities = [RoomContact::class]
)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

}