package hu.bme.aut.android.bmelostandfound.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import hu.bme.aut.android.bmelostandfound.data.User

@Entity(tableName = "contacts")
data class RoomContact(
    @PrimaryKey()
    var uid: String,
    var usermail: String? = null,
    var username: String? = null,
    var phone: String? = null,
    var imageUrl: String? = null
)