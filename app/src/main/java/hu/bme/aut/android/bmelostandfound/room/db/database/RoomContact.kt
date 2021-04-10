package hu.bme.aut.android.bmelostandfound.room.db.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class RoomContact(
    @PrimaryKey()
    var uid: String,
    var usermail: String? = null,
    var username: String? = null,
    var phone: String? = null,
    var imageUrl: String? = null
)