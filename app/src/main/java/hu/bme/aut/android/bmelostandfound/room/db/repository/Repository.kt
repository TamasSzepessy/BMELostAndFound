package hu.bme.aut.android.bmelostandfound.room.db.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.room.db.database.ContactDao
import hu.bme.aut.android.bmelostandfound.room.db.database.RoomContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val contactDao: ContactDao) {

    fun getAllContacts(): LiveData<List<User>> {
        return contactDao.getAllContacts()
            .map { roomContacts ->
                roomContacts.map { roomContact ->
                    roomContact.toDomainModel()
                }
            }
    }

    suspend fun deleteAllContacts() = withContext(Dispatchers.IO) {
        contactDao.deleteAllContacts()
    }

    suspend fun insert(user: User) = withContext(Dispatchers.IO) {
        contactDao.insertContact(user.toRoomModel())
    }

    suspend fun update(user: User) = withContext(Dispatchers.IO) {
        val resp: Int = contactDao.updateContact(user.toRoomModel())
        //Log.d("TEST", "I am in Repository $resp")
    }

    suspend fun delete(user: User) = withContext(Dispatchers.IO) {
        val roomTodo = contactDao.getContactById(user.uid) ?: return@withContext
        contactDao.deleteContact(roomTodo)
    }

    fun check(user: User) : LiveData<Boolean> {
        return contactDao.exists(user.uid)
    }

    private fun RoomContact.toDomainModel(): User {
        return User(
            uid = uid,
            usermail = usermail,
            username = username,
            phone = phone,
            imageUrl = imageUrl
        )
    }

    private fun User.toRoomModel(): RoomContact {
        return RoomContact(
            uid = uid!!,
            usermail = usermail,
            username = username,
            phone = phone,
            imageUrl = imageUrl
        )
    }
}