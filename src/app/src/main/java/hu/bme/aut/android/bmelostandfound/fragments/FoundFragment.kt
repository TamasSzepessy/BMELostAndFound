package hu.bme.aut.android.bmelostandfound.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.bmelostandfound.PostDetailActivity
import hu.bme.aut.android.bmelostandfound.R
import hu.bme.aut.android.bmelostandfound.adapter.PostsAdapter
import hu.bme.aut.android.bmelostandfound.data.Post
import hu.bme.aut.android.bmelostandfound.databinding.FragmentFoundBinding

class FoundFragment : Fragment(), PostsAdapter.PostClickListener {

    private lateinit var postsAdapter: PostsAdapter
    private lateinit var binding: FragmentFoundBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentFoundBinding.inflate(layoutInflater, container, false)
        setupRecyclerView()
        initPostsListener()
        return binding.root
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(requireContext())
        postsAdapter.itemClickListener = this
        binding.contentPosts.rvPosts.layoutManager =
            LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
                stackFromEnd = true
            }
        binding.contentPosts.rvPosts.adapter = postsAdapter
    }

    override fun onItemClick(post: Post) {
        val intent = Intent(context, PostDetailActivity::class.java)
        intent.putExtra("data", post)
        intent.putExtra("from", getString(R.string.found_title))
        startActivity(intent)
    }

    override fun onItemLongClick(position: Int, view: View, post: Post): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("found")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> postsAdapter.addPost(
                            dc.document.toObject(),
                            dc.document.reference.id
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