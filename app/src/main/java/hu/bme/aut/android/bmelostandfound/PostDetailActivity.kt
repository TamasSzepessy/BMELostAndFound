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
import hu.bme.aut.android.bmelostandfound.room.db.viewmodel.ContactViewModel

class PostDetailActivity : AppCompatActivity(), ContactFragment.ContactCreatedListener {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var contactViewModel: ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        binding = ActivityPostDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val from = intent.getStringExtra("from")
        binding.tvLostOrFound.text = from

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

        if (post?.applied.isNullOrBlank()) {
            if (post?.uid==FirebaseAuth.getInstance().currentUser?.uid){
                binding.btnApply.text = getString(R.string.btn_apply_own)
                binding.btnApply.icon = getDrawable(R.drawable.ic_round_list_alt_24)
                binding.btnApply.isClickable = false
            } else {
                when (from) {
                    getString(R.string.lost_title) -> {
                        binding.btnApply.text = getString(R.string.btn_apply_find)
                        binding.btnApply.setOnClickListener {
                            applyForIt("lost", post!!.refid!!)
                        }
                    }
                    getString(R.string.found_title) -> {
                        binding.btnApply.text = getString(R.string.btn_apply_loseit)
                        binding.btnApply.setOnClickListener {
                            applyForIt("found", post!!.refid!!)
                        }
                    }
                }
            }
        } else {
            if (post?.applied==FirebaseAuth.getInstance().currentUser?.uid){
                when (from) {
                    getString(R.string.lost_title) -> {
                        binding.btnApply.text = getString(R.string.btn_apply_youfoundit)
                        binding.btnApply.setOnClickListener {
                            resetApplied("lost", post!!.refid!!)
                        }
                    }
                    getString(R.string.found_title) -> {
                        binding.btnApply.text = getString(R.string.btn_apply_youlostit)
                        binding.btnApply.setOnClickListener {
                            resetApplied("found", post!!.refid!!)
                        }
                    }
                }
                binding.btnApply.icon = getDrawable(R.drawable.ic_baseline_done_24)
            } else {
                when (from) {
                    getString(R.string.lost_title) -> {
                        binding.btnApply.text = getString(R.string.btn_apply_found)
                    }
                    getString(R.string.found_title) -> {
                        binding.btnApply.text = getString(R.string.btn_apply_gotback)
                    }
                }
                binding.btnApply.icon = getDrawable(R.drawable.ic_baseline_done_24)
                getApplicantData(post?.applied)
            }
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

    private fun applyForIt(coll: String, ref: String) {
        val db = Firebase.firestore
        db.collection(coll).document(ref)
            .update("applied", FirebaseAuth.getInstance().currentUser?.uid)
            .addOnSuccessListener {
                when (coll) {
                    "lost" -> {
                        binding.btnApply.text = getString(R.string.btn_apply_youfoundit)
                        binding.btnApply.setOnClickListener {
                            resetApplied("lost", ref)
                        }
                    }
                    "found" -> {
                        binding.btnApply.text = getString(R.string.btn_apply_youlostit)
                        binding.btnApply.setOnClickListener {
                            resetApplied("found", ref)
                        }
                    }
                }
                binding.btnApply.icon = getDrawable(R.drawable.ic_baseline_done_24)
                Toast.makeText(this, getString(R.string.btn_applied_success), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetApplied(coll: String, ref: String) {
        val db = Firebase.firestore
        db.collection(coll).document(ref)
            .update("applied", null)
            .addOnSuccessListener {
                when (coll) {
                    "lost" -> {
                        binding.btnApply.text = getString(R.string.btn_apply_find)
                        binding.btnApply.setOnClickListener {
                            applyForIt("lost", ref)
                        }
                    }
                    "found" -> {
                        binding.btnApply.text = getString(R.string.btn_apply_loseit)
                        binding.btnApply.setOnClickListener {
                            applyForIt("found", ref)
                        }
                    }
                }
                binding.btnApply.icon = getDrawable(R.drawable.ic_baseline_add_24)
                Toast.makeText(this, "Jelentkezés törölve", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun setUserData(userData: User) {
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

    private fun setAppliedData(userData: User) {
        binding.btnApply.setOnClickListener {
            val contactFragment = ContactFragment(
                userData,
                userData.uid == FirebaseAuth.getInstance().currentUser?.uid
            )
            contactFragment.show(supportFragmentManager, "Contact")
        }
    }

    fun getApplicantData(userId: String?) {
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
                            setAppliedData(userData)
                        }
                        else -> {val pass : Unit = Unit}
                    }
                }
            }
    }

    override fun onContactCreated(user: User) {
        contactViewModel.insert(user)
    }
}