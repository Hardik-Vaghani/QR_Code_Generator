package com.hardik.qrcodegenerate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hardik.qrcodegenerate.R
import com.hardik.qrcodegenerate.room.entity.User

class UserListAdapter(private val userList: List<User>, private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        holder.textFullName.text = currentUser.fullName
        holder.textMobileNo.text = currentUser.mobileNo
        holder.textEmail.text = currentUser.email
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val textFullName: TextView = itemView.findViewById(R.id.text_full_name)
        val textMobileNo: TextView = itemView.findViewById(R.id.text_mobile_no)
        val textEmail: TextView = itemView.findViewById(R.id.text_email)

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedUser = userList[position]
                onItemClickListener.onItemClick(clickedUser)
            }
        }
    }
}