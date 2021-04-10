package hu.bme.aut.android.bmelostandfound

import android.app.Application
import androidx.room.Room
import hu.bme.aut.android.bmelostandfound.database.ContactDatabase

class ContactApplication : Application() {

    companion object {
        lateinit var contactDatabase: ContactDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        contactDatabase = Room.databaseBuilder(
            applicationContext,
            ContactDatabase::class.java,
            "contact_database"
        ).fallbackToDestructiveMigration().build()
    }

}