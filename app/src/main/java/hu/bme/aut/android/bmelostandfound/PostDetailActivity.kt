package hu.bme.aut.android.bmelostandfound

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.bmelostandfound.data.Post
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.databinding.ActivityPostDetailBinding
import hu.bme.aut.android.bmelostandfound.fragments.ContactFragment
import hu.bme.aut.android.bmelostandfound.viewmodel.ContactViewModel

class PostDetailActivity : AppCompatActivity(), ContactFragment.ContactCreatedListener {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var contactViewModel: ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        binding = ActivityPostDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.tvLostOrFound.text = intent.getStringExtra("from")

        binding.cardAuthor.contactsView.visibility = View.GONE
        contactViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)

        val post = intent.getSerializableExtra("data") as Post?
        binding.tvTitleDetail.text = post?.title
        binding.tvBody.text = post?.body
        binding.tvBuilding.text = post?.building
        binding.tvDate.text = post?.date
        if (post?.imageUrl.isNullOrBlank()) {
            Glide.with(this).load(R.drawable.ic_no_img).into(binding.imgPost);
        } else {
            Glide.with(this).load(post?.imageUrl).into(binding.imgPost)
        }

        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        userDataListener(post?.uid)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    fun setUserData(userData: User) {
        binding.cardAuthor.tvUser.text = userData.username
        binding.cardAuthor.tvUserMail.text = userData.usermail
        if (userData.phone.isNullOrBlank()) {
            binding.cardAuthor.tvUserPhone.visibility = View.GONE
        } else {
            binding.cardAuthor.tvUserPhone.text = userData.phone
            binding.cardAuthor.tvUserPhone.visibility = View.VISIBLE
        }
        if (!userData.imageUrl.isNullOrBlank()) {
            Glide.with(this).load(userData.imageUrl).into(binding.cardAuthor.imgUser)
        }
        binding.cardAuthor.contactsView.visibility = View.VISIBLE
        binding.cardAuthor.root.setOnClickListener {
            val contactFragment = ContactFragment(
                userData,
                userData.uid == FirebaseAuth.getInstance().currentUser?.uid
            )
            contactFragment.show(supportFragmentManager, "Contact")
        }
    }

    fun userDataListener(userId: String?) {
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

    override fun onContactCreated(user: User) {
        contactViewModel.insert(user)
    }
}