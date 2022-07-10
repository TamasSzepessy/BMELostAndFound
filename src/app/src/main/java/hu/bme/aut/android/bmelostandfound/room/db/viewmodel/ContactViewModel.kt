package hu.bme.aut.android.bmelostandfound.room.db.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.android.bmelostandfound.ContactApplication
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.room.db.repository.Repository
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {

    private val repository: Repository

    val allContacts: LiveData<List<User>>

    init {
        val contactDao = ContactApplication.contactDatabase.contactDao()
        repository = Repository(contactDao)
        allContacts = repository.getAllContacts()
    }

    fun insert(user: User) = viewModelScope.launch {
        repository.insert(user)
    }

    fun update(user: User) = viewModelScope.launch {
        repository.update(user)
        //Log.d("TEST", "I am in ViewModel")
    }

    fun delete(user: User) = viewModelScope.launch {
        repository.delete(user)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllContacts()
    }

    fun check(user: User) : LiveData<Boolean> {
        return repository.check(user)
    }
}