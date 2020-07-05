package com.kamal_maharana.crimsonmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.kamal_maharana.crimsonmessenger.models.ChatMessage
import com.kamal_maharana.crimsonmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()


    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)


        recyclerview_chatlog.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username


        //setupDummyData()
        listenForMessages()



        // When SEND button is pressed
        send_button_chatlog.setOnClickListener {
            Log.d(TAG, "Send button clicked")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                Log.d(TAG, "Chat message object results: $chatMessage")
                if (chatMessage != null) {
                    Log.d(TAG, "This is the actual message retrieved from FIREBASE ${chatMessage.text}")
                    //adapter.add(ChatFromItem(chatMessage.text))
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    }
                    else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }


                }

                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }




    // Send Message Function
    private fun performSendMessage() {

        val text = edittext_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        if (fromId == null) return
        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId!!, System.currentTimeMillis()/1000)

        toReference.setValue(chatMessage)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved the chat message: ${reference.key}")
                edittext_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        latestMessageReference.setValue(chatMessage)

        val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToReference.setValue(chatMessage)
    }
}


class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text


        // load user image in chat log
        val uri = user.profileImageUri
        val target = viewHolder.itemView.imageview_from_row
        Picasso.get().load(uri).into(target)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = text

        //Load user image into star
        val uri = user.profileImageUri
        val target = viewHolder.itemView.imageview_to_row
        Picasso.get().load(uri).into(target)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}