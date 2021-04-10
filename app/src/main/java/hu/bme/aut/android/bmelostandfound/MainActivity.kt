package hu.bme.aut.android.bmelostandfound

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.bmelostandfound.adapter.ViewPagerAdapter
import hu.bme.aut.android.bmelostandfound.data.Post
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.databinding.ActivityMainBinding
import hu.bme.aut.android.bmelostandfound.fragments.ContactFragment
import kotlin.random.Random


class MainActivity : AppCompatActivity(), ContactFragment.ContactCreatedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Applied notification"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.ivUser.visibility = View.GONE

        setSupportActionBar(binding.bottomAppBar)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        getUserData(uid)
        checkApplied(uid)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(
            supportFragmentManager,
            getString(R.string.lost_title),
            getString(R.string.found_title)
        )
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            val page: String = when (viewPager.currentItem) {
                0 -> "lost"
                1 -> "found"
                else -> "lost"
            }
            val createPostIntent = Intent(
                this, CreatePostActivity()::class.java
            )
            createPostIntent.putExtra("from", page)
            startActivity(createPostIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val contactsIntent = Intent(
                    this, ContactsActivity()::class.java
                )
                startActivity(contactsIntent)
            }
            R.id.menu_logout -> logout()
            R.id.menu_myposts -> {
                val myPostsIntent = Intent(
                    this, MyPostsActivity()::class.java
                )
                startActivity(myPostsIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUserData(userData: User) {
        binding.tvUser.text = userData.username
        if (!userData.imageUrl.isNullOrBlank()) {
            Glide.with(this).load(userData.imageUrl).into(binding.ivUser)
        }
        binding.ivUser.visibility = View.VISIBLE

        binding.appBarUser.setOnClickListener {
            val contactFragment = ContactFragment(userData, true)
            contactFragment.show(supportFragmentManager, "Self")
        }
    }

    private fun getUserData(userId: String?) {
        var userData: User
        val db = Firebase.firestore
        db.collection("users")
            .whereEqualTo("uid", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            userData = dc.document.toObject()
                            setUserData(userData)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            userData = dc.document.toObject()
                            setUserData(userData)
                        }
                        DocumentChange.Type.REMOVED -> {
                            //TODO
                        }
                    }
                }
            }
    }

    private fun checkApplied(userId: String?) {
        val db = Firebase.firestore
        db.collection("lost")
            .whereEqualTo("uid", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("TAG", "lost\n$e")
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.REMOVED -> {
                            val pass: Unit = Unit
                        }
                        else -> {
                            val post = dc.document.toObject<Post>()
                            if (!post.applied.isNullOrBlank()) {
                                notifyApplied(post, getString(R.string.lost_title))
                            }
                        }
                    }
                }
            }

        db.collection("found")
            .whereEqualTo("uid", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("TAG", "found\n$e")
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.REMOVED -> {
                            val pass: Unit = Unit
                        }
                        else -> {
                            val post = dc.document.toObject<Post>()
                            if (!post.applied.isNullOrBlank()) {
                                notifyApplied(post, getString(R.string.found_title))
                            }
                        }
                    }
                }
            }
    }

    private fun notifyApplied(post: Post, from: String){
        val intent = Intent(this, PostDetailActivity::class.java)
        intent.putExtra("data", post)
        intent.putExtra("from", from)
        var notifTitle : String? = null
        var notifText : String? = null
        when (from) {
            getString(R.string.lost_title) -> {
                notifTitle = getString(R.string.notify_lost_title)
                notifText = getString(R.string.notify_lost_text, post.title)
            }
            getString(R.string.found_title) -> {
                notifTitle = getString(R.string.notify_found_title)
                notifText = getString(R.string.notify_found_text, post.title)
            }
        }
        val pendingIntent = PendingIntent.getActivity(this, Random.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = getColor(R.color.secondaryColor)
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContentTitle(notifTitle)
                .setContentText(notifText)
                .setStyle(Notification.BigTextStyle().bigText(notifText))
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(this)
                .setContentTitle(notifTitle)
                .setContentText(notifText)
                .setStyle(Notification.BigTextStyle().bigText(notifText))
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(Random.nextInt(), builder.build())
    }

    private fun logout() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Kijelentkezés")
        alertDialog.setMessage("Biztosan ki akarsz lépni?")
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            "Igen"
        ) { dialog, which ->
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            "Nem"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    override fun onContactCreated(user: User) {
        // do nothing
    }
}