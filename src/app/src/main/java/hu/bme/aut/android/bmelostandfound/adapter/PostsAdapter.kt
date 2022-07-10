package hu.bme.aut.android.bmelostandfound.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.android.bmelostandfound.R
import hu.bme.aut.android.bmelostandfound.data.Post
import hu.bme.aut.android.bmelostandfound.databinding.CardPostBinding


class PostsAdapter(private val context: Context) :
    ListAdapter<Post, PostsAdapter.PostViewHolder>(itemCallback) {

    private val postList: MutableList<Post> = mutableListOf()
    private var lastPosition = -1

    var itemClickListener: PostClickListener? = null
    private var onAdapterCountListener: OnAdapterCountListener? = null
    fun setOnAdapterCountListener(l: OnAdapterCountListener) {
        onAdapterCountListener = l
    }

    inner class PostViewHolder(binding: CardPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvBuildingCode: TextView = binding.tvBuildingCode
        val tvTitle: TextView = binding.tvTitle
        val tvBody: TextView = binding.tvBody
        val tvDate: TextView = binding.tvDate
        val imgPost: ImageView = binding.imgPost
        val ivApplied: ImageView = binding.ivApplied

        var post: Post? = null

        init {
            onAdapterCountListener?.onAdapterCountListener (itemCount)
            itemView.setOnClickListener {
                post?.let { post ->
                    itemClickListener?.onItemClick(post)
                }
            }

            itemView.setOnLongClickListener { view ->
                post?.let { post ->
                    itemClickListener?.onItemLongClick(
                        adapterPosition,
                        view,
                        post
                    )
                }
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostViewHolder(CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val tmpPost = postList[position]
        holder.tvBuildingCode.text = tmpPost.building
        holder.tvTitle.text = tmpPost.title
        holder.tvBody.text = tmpPost.body
        holder.tvDate.text = tmpPost.date

        holder.post = this.getItem(position)

        if (tmpPost.imageUrl.isNullOrBlank()) {
            Glide.with(context).load(R.drawable.ic_no_img).into(holder.imgPost);
        } else {
            Glide.with(context).load(tmpPost.imageUrl).into(holder.imgPost)
        }

        if (tmpPost.applied.isNullOrBlank()) {
            holder.ivApplied.visibility = View.GONE
        } else {
            holder.ivApplied.visibility = View.VISIBLE
        }

        setAnimation(holder.itemView, position)
    }

    fun addPost(post: Post?, refid: String?, from: String? = null) {
        post ?: return

        post.refid = refid
        post.from = from
        postList += (post)
        if (from != null) {
            postList.sortByDescending { it.date }
            postList.reverse()
        }
        submitList((postList))
        notifyDataSetChanged()
    }

    fun delPost(refid: String?) {
        refid ?: return

        postList.remove(postList.find { it.refid == refid })
        submitList((postList))
        notifyDataSetChanged()
    }

    fun modifyPost(post: Post?, refid: String?, from: String? = null) {
        post ?: return

        postList.remove(postList.find { it.refid == refid })
        post.refid = refid
        post.from = from
        postList += (post)
        postList.sortByDescending { it.date }
        postList.reverse()
        submitList((postList))
        notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    companion object {

        object itemCallback : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface PostClickListener {
        fun onItemClick(post: Post)
        fun onItemLongClick(position: Int, view: View, post: Post): Boolean
    }

    interface OnAdapterCountListener {
        fun onAdapterCountListener(count: Int)
    }
}