package com.example.blogapp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-401c7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-401c7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addBlogButton.setOnClickListener {
            val title: String = binding.blogTitle.editText?.text.toString().trim()
            val description: String = binding.blogDescription.editText?.text.toString().trim()
            //any field empty to check
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please Fill all the fields", Toast.LENGTH_SHORT).show()

            }
            //get current user
            val user: FirebaseUser? = auth.currentUser

            if (user != null) {
                val userId: String = user.uid
                val userName: String = user.displayName ?: "Anonymous"
                val userImageUrl: String = user.photoUrl?.toString() ?: ""

                userReference.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData: UserData? = snapshot.getValue(UserData::class.java)
                            if (userData != null) {
                                val userNameFromDB: String = userData.name
                                val userImageUrlFromDB: String = userData.profileImage

                                val currentDate: String =
                                    SimpleDateFormat("yyyy-MM-dd").format(Date())

                                //Create a blog item model
                                val blogItem = BlogItemModel(
                                    title, userNameFromDB,
                                    currentDate,
                                    description,
                                    0,
                                    userImageUrlFromDB
                                )

                                //create unique key for blog post
                                val key: String? = databaseReference.push().key
                                if (key != null) {
                                    blogItem.postId = key
                                    val blogReference: DatabaseReference =
                                        databaseReference.child(key)
                                    blogReference.setValue(blogItem).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@AddArticleActivity,
                                                "Failed to add blog",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

            }
        }
    }
    }





