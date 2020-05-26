package com.example.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_mess.*
import kotlinx.android.synthetic.main.user_row_new_mess.view.*

class NewMessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_mess)
        supportActionBar?.title= "Select User"
        fechUsers()
    }
    companion object{
        val UserKEY ="UserKey"
    }
    private fun fechUsers(){
       val ref =  FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter =GroupAdapter<ViewHolder>()


                p0.children.forEach{
                    Log.d("NewMessage",it.toString())
                    val user =it.getValue(User::class.java)
                    if (user != null) adapter.add(UserItem(user))

                }
                
                adapter.setOnItemClickListener { item, view ->
                    val useritem =item as UserItem
                    val intent = Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(UserKEY,useritem.user)
                    startActivity(intent)
                    finish()
                }
                
                recycleView_newMess.adapter= adapter
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
    class UserItem (val user :User): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_RB_NM.text=user.username
            Picasso.get().load(user.profileImgUrl).into(viewHolder.itemView.imageView_RB_NM)
        }
        override fun getLayout(): Int {
            return R.layout.user_row_new_mess
        }
    }
}
