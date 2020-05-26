package com.example.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.chattingapp.NewMessActivity.Companion.UserKEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_mess.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessActivity : AppCompatActivity() {

    companion object{
        var currentUser :User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_mess)
        verify_if_Logged_in()
        ID_RV_LM.adapter=adapter

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessageRow
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(UserKEY,row.chatPartnerUser)
            startActivity(intent)
        }

        fetchcurrentUser()

        listenForlatestMess()

    }

    class LatestMessageRow(val chatMess : chatMess): Item<ViewHolder>(){
        var chatPartnerUser :User? = null
        override fun getLayout(): Int {
                return R.layout.latest_message_row
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.ID_LM_LM.text= chatMess.text
            val chatPartnerid : String
            if(chatMess.fromid == FirebaseAuth.getInstance().uid){
                chatPartnerid =  chatMess.toid
            }else{
                chatPartnerid = chatMess.fromid
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerid")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java) ?: return
                    viewHolder.itemView.ID_UN_LM.text=chatPartnerUser!!.username
                    Picasso.get().load(chatPartnerUser!!.profileImgUrl).into(viewHolder.itemView.imageViewLatest)
                }

            })
        }

    }
    val LatestMessMap = HashMap<String, chatMess>()

    private  fun refresh(){
        adapter.clear()
        LatestMessMap  .values.forEach{
            adapter.add(LatestMessageRow(it))
        }

    }


    private fun listenForlatestMess(){

        val fromid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromid")
        ref.addChildEventListener(object :ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMess = p0.getValue(chatMess::class.java) ?: return
                LatestMessMap[p0.key!!] = chatMess
                refresh()
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMess = p0.getValue(chatMess::class.java) ?: return
                LatestMessMap[p0.key!!] = chatMess
                refresh()
            }


        })



    }

    val adapter = GroupAdapter<ViewHolder>()


    private fun fetchcurrentUser(){
        val uid =FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                currentUser=p0.getValue(User::class.java)
                Log.d("LatestMess", "the current user username is :${currentUser?.username}")
            }

        })
    }

    private fun verify_if_Logged_in()
    {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this,RegActivity::class.java)
            intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item?.itemId){
            R.id.menu_new_mess->{
                val intent = Intent(this,NewMessActivity::class.java)
                intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,RegActivity::class.java)
                intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}
