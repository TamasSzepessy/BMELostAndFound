package hu.bme.aut.android.bmelostandfound

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.bmelostandfound.adapter.ContactsAdapter
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.databinding.ActivityContactsBinding
import hu.bme.aut.android.bmelostandfound.fragments.ContactFragment
import hu.bme.aut.android.bmelostandfound.room.db.viewmodel.ContactViewModel
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
        contactsAdapter.setOnAdapterCountListener(object : ContactsAdapter.OnAdapterCountListener {
            override fun onAdapterCountListener(count: Int) {
                if (count > 0) binding.tvEmpty.visibility = View.GONE
                else binding.tvEmpty.visibility = View.VISIBLE
            }
        })
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
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle(getString(R.string.delete_contact_title))
                    alertDialog.setMessage(getString(R.string.delete_contact_msg))
                    alertDialog.setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.yes)
                    ) { dialog, which ->
                        if (!user.imageUrl.isNullOrBlank()) {
                            val result = deleteUserImage(user.imageUrl!!)
                        }
                        contactViewModel.delete(user)
                    }
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.no)
                    ) { dialog, which -> dialog.dismiss() }
                    alertDialog.show()
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