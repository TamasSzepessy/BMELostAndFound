package hu.bme.aut.android.bmelostandfound

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.bmelostandfound.adapter.PostsAdapter
import hu.bme.aut.android.bmelostandfound.data.Post
import hu.bme.aut.android.bmelostandfound.databinding.ActivityMyPostsBinding
import java.util.*

class MyPostsActivity : AppCompatActivity(), PostsAdapter.PostClickListener {

    private lateinit var postsAdapter: PostsAdapter
    private lateinit var binding: ActivityMyPostsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        binding.toolbarMyPosts.title = getString(R.string.settings_myposts)
        setSupportActionBar(binding.toolbarMyPosts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupRecyclerView()
        initPostsListener(uid)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(applicationContext)
        postsAdapter.itemClickListener = this
        binding.contentPosts.rvPosts.layoutManager =
            LinearLayoutManager(applicationContext).apply {
                reverseLayout = true
                stackFromEnd = true
            }
        binding.contentPosts.rvPosts.adapter = postsAdapter
    }

    override fun onItemClick(post: Post) {
        val intent = Intent(this, PostDetailActivity::class.java)
        intent.putExtra("data", post)
        val page: String = when (post.from) {
            "lost" -> getString(R.string.lost_title)
            "found" -> getString(R.string.found_title)
            else -> getString(R.string.lost_title)
        }
        intent.putExtra("from", page)
        startActivity(intent)
    }

    override fun onItemLongClick(position: Int, view: View, post: Post): Boolean {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.row_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    val alertDialog = AlertDialog.Builder(this).create()
                    alertDialog.setTitle(getString(R.string.delete_post_title))
                    alertDialog.setMessage(getString(R.string.delete_post_msg))
                    alertDialog.setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.yes)
                    ) { dialog, which ->
                        if (!post.imageUrl.isNullOrBlank()) {
                            deleteImage(post, post.imageUrl.toString())
                        } else {
                            deleteFromFirestore(post)
                        }
                    }
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.no)
                    ) { dialog, which -> dialog.dismiss() }
                    alertDialog.show()
                }
            }
            false
        }
        popup.show()
        return false
    }

    private fun deleteFromFirestore(post: Post) {
        val db = Firebase.firestore
        db.collection(post.from!!)
            .document(post.refid!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Sikeres törlés", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteImage(post: Post, url: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val path = getPathStorageFromUrl(url)
        val imageReference = storageReference.child(path)

        imageReference.delete().addOnSuccessListener {
            deleteFromFirestore(post)
        }.addOnFailureListener {
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    fun getPathStorageFromUrl(url: String): String {
        val baseUrl = "https://firebasestorage.googleapis.com/v0/b/bmelostandfound.appspot.com/o/"
        var imagePath: String = url.replace(baseUrl, "")
        val indexOfEndPath = imagePath.indexOf("?")
        imagePath = imagePath.substring(0, indexOfEndPath)
        imagePath = imagePath.replace("%2F", "/")
        return imagePath
    }

    private fun initPostsListener(userId: String?) {
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
                        DocumentChange.Type.ADDED -> postsAdapter.addPost(
                            dc.document.toObject(),
                            dc.document.reference.id,
                            "lost"
                        )
                        DocumentChange.Type.MODIFIED -> postsAdapter.modifyPost(
                            dc.document.toObject(),
                            dc.document.reference.id
                        )
                        DocumentChange.Type.REMOVED -> {
                            postsAdapter.delPost(dc.document.reference.id)
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
                        DocumentChange.Type.ADDED -> postsAdapter.addPost(
                            dc.document.toObject(),
                            dc.document.reference.id,
                            "found"
                        )
                        DocumentChange.Type.MODIFIED -> postsAdapter.modifyPost(
                            dc.document.toObject(),
                            dc.document.reference.id
                        )
                        DocumentChange.Type.REMOVED -> {
                            postsAdapter.delPost(dc.document.reference.id)
                        }
                    }
                }
            }
    }
}