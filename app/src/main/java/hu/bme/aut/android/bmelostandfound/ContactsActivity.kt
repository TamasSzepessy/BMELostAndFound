package hu.bme.aut.android.bmelostandfound

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.bmelostandfound.adapter.ContactsAdapter
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.databinding.ActivityContactsBinding
import hu.bme.aut.android.bmelostandfound.fragments.ContactFragment
import hu.bme.aut.android.bmelostandfound.viewmodel.ContactViewModel
import java.io.File
import java.util.*

class ContactsActivity : AppCompatActivity(), ContactsAdapter.ContactClickListener,
    ContactFragment.ContactCreatedListener {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var contactsAdapter: ContactsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactsBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        contactViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        contactViewModel.allContacts.observe(this, { users ->
            contactsAdapter.submitList(users)
        })

        binding.toolbar.title = getString(R.string.saved_contacts)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupRecyclerView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(applicationContext)
        contactsAdapter.itemClickListener = this
        binding.contentPosts.rvPosts.layoutManager =
            LinearLayoutManager(applicationContext).apply {
                reverseLayout = true
                stackFromEnd = true
            }
        binding.contentPosts.rvPosts.adapter = contactsAdapter
    }

    override fun onItemClick(user: User) {
        val contactFragment = ContactFragment(user)
        contactFragment.show(supportFragmentManager, "Contact")
    }

    override fun onItemLongClick(position: Int, view: View, user: User): Boolean {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.row_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    if (!user.imageUrl.isNullOrBlank()) {
                        val result = deleteUserImage(user.imageUrl!!)
                    }
                    contactViewModel.delete(user)
                    //Toast.makeText(this, "Névjegy törölve", Toast.LENGTH_LONG).show()
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        popup.show()
        return false
    }

    private fun deleteUserImage(url: String): Boolean {
        val cw = ContextWrapper(this)
        val directory: File = cw.getDir("images", Context.MODE_PRIVATE)
        val filename: String = url.substringAfterLast("/")
        val file = File(directory, filename)
        return file.delete()
    }

    override fun onContactCreated(user: User) {
        contactViewModel.insert(user)
    }
}