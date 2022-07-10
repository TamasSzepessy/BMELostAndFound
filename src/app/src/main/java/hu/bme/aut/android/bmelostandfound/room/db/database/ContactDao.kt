package hu.bme.aut.android.bmelostandfound.room.db.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface ContactDao {

    @Insert(onConflict = REPLACE)
    fun insertContact(contact: RoomContact)

    @Query("SELECT * FROM contacts ORDER BY username DESC")
    fun getAllContacts(): LiveData<List<RoomContact>>

    @Query("SELECT * FROM contacts WHERE uid == :uid")
    fun getContactById(uid: String?): RoomContact?

    @Update
    fun updateContact(user: RoomContact): Int

    @Delete
    fun deleteContact(user: RoomContact)

    @Query("DELETE FROM contacts")
    fun deleteAllContacts()

    @Query("SELECT EXISTS (SELECT 1 FROM contacts WHERE uid = :uid)")
    fun exists(uid: String?): LiveData<Boolean>

}