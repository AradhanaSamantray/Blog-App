package com.example.blogapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
   private lateinit var databaseReference: DatabaseReference
   private val blogItems=mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Initializations
        auth= FirebaseAuth.getInstance()
        databaseReference= FirebaseDatabase.getInstance("https://blog-app-3e6e0-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("blogs")
        //finduser id
        val userId:String?=auth.currentUser?.uid

        if(userId!=null){
            loadUserProfileImage(userId)
        }

     binding.floatingAddArticleButton.setOnClickListener{
         startActivity(Intent(this,AddArticleActivity::class.java))
         finish()
     }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference= FirebaseDatabase.getInstance("https://blog-app-3e6e0-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users").child(userId)

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl:Any?=snapshot.getValue(String::class.java)
                if(profileImageUrl!=null){
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
              Toast.makeText(this@MainActivity,"Error loading profile image ",Toast.LENGTH_SHORT).show()
            }

        })
    }
}