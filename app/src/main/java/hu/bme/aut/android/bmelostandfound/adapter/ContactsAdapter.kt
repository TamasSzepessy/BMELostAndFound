package hu.bme.aut.android.bmelostandfound.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.android.bmelostandfound.data.User
import hu.bme.aut.android.bmelostandfound.databinding.CardContactBinding

class ContactsAdapter(private val context: Context) :
    ListAdapter<User, ContactsAdapter.ContactViewHolder>(itemCallback) {

    var itemClickListener: ContactClickListener? = null
    private var onAdapterCountListener: OnAdapterCountListener? = null
    fun setOnAdapterCountListener(l: OnAdapterCountListener) {
        onAdapterCountListener = l
    }

    inner class ContactViewHolder(binding: CardContactBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvUser: TextView = binding.tvUser
        val tvUserMail: TextView = binding.tvUserMail
        val tvUserPhone: TextView = binding.tvUserPhone
        val imgUser: ImageView = binding.imgUser

        var user: User? = null

        init {
            onAdapterCountListener?.onAdapterCountListener(itemCount)
            itemView.setOnClickListener {
                user?.let { user ->
                    itemClickListener?.onItemClick(user)
                }
            }

            itemView.setOnLongClickListener { view ->
                user?.let { user ->
                    itemClickListener?.onItemLongClick(
                        adapterPosition,
                        view,
                        user
                    )
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactViewHolder(
            CardContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val tmpUser = this.getItem(position)
        holder.tvUser.text = tmpUser.username
        holder.tvUserMail.text = tmpUser.usermail
        if (tmpUser.phone.isNullOrBlank()) {
            holder.tvUserPhone.visibility=View.GONE
        } else {
            holder.tvUserPhone.text = tmpUser.phone
            holder.tvUserPhone.visibility = View.VISIBLE
        }

        holder.user = this.getItem(position)

        if (!tmpUser.imageUrl.isNullOrBlank()) {
            Glide.with(context).load(tmpUser.imageUrl).into(holder.imgUser)
        }
    }

    companion object {

        object itemCallback : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface ContactClickListener {
        fun onItemClick(user: User)
        fun onItemLongClick(position: Int, view: View, user: User): Boolean
    }

    interface OnAdapterCountListener {
        fun onAdapterCountListener(count: Int)
    }
}