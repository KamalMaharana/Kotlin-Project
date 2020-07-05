package com.kamal_maharana.crimsonmessenger.views

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kamal_maharana.crimsonmessenger.R
import com.kamal_maharana.crimsonmessenger.models.ChatMessage
import com.kamal_maharana.crimsonmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>() {


    var chatPartnerUser: User? = null


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_textview_latest_message.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        }
        else {
            chatPartnerId = chatMessage.fromId
        }

        val ref =  FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.username
                val target = viewHolder.itemView.imageview_latest_message
                val uri = chatPartnerUser?.profileImageUri
                Picasso.get().load(uri).into(target)
            }
        })

    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}