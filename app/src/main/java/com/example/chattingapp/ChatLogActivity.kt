package com.example.chattingapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.imageView
import kotlinx.android.synthetic.main.chat_from_row.view.textView_from
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG ="ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_ChatLog.adapter = adapter

        val toUser = intent.getParcelableExtra<User>(NewMessActivity.UserKEY)
        supportActionBar?.title=toUser?.username
       // setup_dummi_data()
        listenForMess()

        Send_ChatLog.setOnClickListener {
            Log.d(TAG,"u will be sending the message now")
            performSendMess()
        }
    }





    private fun performSendMess(){
        //val ref =FirebaseDatabase.getInstance().getReference("Mess/").push()
        val fromid = FirebaseAuth.getInstance().uid
        if (fromid == null) return
        val user = intent.getParcelableExtra<User>(NewMessActivity.UserKEY)
        val toid = user.uid
        val text = Mess_ChatLog.text.toString()
        val ref =FirebaseDatabase.getInstance().getReference("/userMess/$fromid/$toid").push()
        val ref2 =FirebaseDatabase.getInstance().getReference("/userMess/$toid/$fromid").push()
        val chatmess=chatMess(ref.key!!,text,fromid,toid,System.currentTimeMillis()/1000)
        ref.setValue(chatmess)
            .addOnSuccessListener {
                Log.d(TAG,"stored the message in the firebase database:${ref.key}")
                Mess_ChatLog.text.clear()
                recyclerView_ChatLog.scrollToPosition(adapter.itemCount-1)
            }
            .addOnFailureListener{
                Log.d(TAG,"the mess was not stored in the firebase database")
            }
        ref2.setValue(chatmess)

        val refLM = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromid/$toid")
        refLM.setValue(chatmess)
        val refLM2 = FirebaseDatabase.getInstance().getReference("/latest-messages/$toid/$fromid")
        refLM2.setValue(chatmess)

    }

    private fun listenForMess(){
        val fromid = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessActivity.UserKEY)
        val toid = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/userMess/$fromid/$toid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(chatMess::class.java)
               if (chatMessage != null){
                   Log.d(TAG,chatMessage?.text)
                   if(chatMessage.fromid == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessActivity.currentUser
                       adapter.add(ChatToItem(chatMessage.text,currentUser!!))
                   }else {
                       val user = intent.getParcelableExtra<User>(NewMessActivity.UserKEY)
                       adapter.add(ChatFromItem(chatMessage.text,user))
                   }
               }


                recyclerView_ChatLog.scrollToPosition(adapter.itemCount -1 )
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }



    class ChatFromItem (val text:String,val user :User): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_from.text =text
            val targetImageView = viewHolder.itemView.imageView
            Picasso.get().load(user.profileImgUrl).into(targetImageView)
        }
        override fun getLayout(): Int {
           return R.layout.chat_from_row

        }
    }



    class ChatToItem (val text:String,val user :User): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_from.text =text
            val targetImageView = viewHolder.itemView.imageView
            Picasso.get().load(user.profileImgUrl).into(targetImageView)
        }
        override fun getLayout(): Int {
            return R.layout.chat_to_row

        }
    }
}
