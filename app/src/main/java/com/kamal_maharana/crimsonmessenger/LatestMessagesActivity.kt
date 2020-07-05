package com.kamal_maharana.crimsonmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kamal_maharana.crimsonmessenger.NewMessageActivity.Companion.USER_KEY
import com.kamal_maharana.crimsonmessenger.models.ChatMessage
import com.kamal_maharana.crimsonmessenger.models.User
import com.kamal_maharana.crimsonmessenger.views.LatestMessageRow
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerview_latest_messages.adapter = adapter

        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))



        //Set item click listener
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG, "Adapter Clicked")
            val intent = Intent(this, ChatLogActivity::class.java)

            val row = item as LatestMessageRow

            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
//        setupDummyRows()

        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        recyclerview_latest_messages

    }




    val latestMessagesMap = HashMap<String, ChatMessage>()
    val adapter = GroupAdapter<GroupieViewHolder>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add((LatestMessageRow(it)))
        }
    }


    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                latestMessagesMap[p0.key!!] = chatMessage!!

                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                latestMessagesMap[p0.key!!] = chatMessage!!

                refreshRecyclerViewMessages()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

//    private  fun setupDummyRows() {
//        val adapter = GroupAdapter<GroupieViewHolder>()
//
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//
//        recyclerview_latest_messages.adapter = adapter
//    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current User is ${currentUser?.username}")
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                // No flag needed COZ we want to go back to LatestMessageActivity after pressing the back button
                // Flag will caouse it to end and go back to home page of our mobile
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}