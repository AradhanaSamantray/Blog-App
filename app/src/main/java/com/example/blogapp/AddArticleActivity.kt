package com.example.blogapp

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.ktx.auth
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

    private val databaseReference: DatabaseReference= FirebaseDatabase.getInstance("https://blog-app-3e6e0-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
    private val userReference: DatabaseReference= FirebaseDatabase.getInstance("https://blog-app-3e6e0-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    private val auth= FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
       binding.addBlogButton.setOnClickListener{
           val title :String=binding.blogTitle.editText?.text.toString().trim()
           val description:String=binding.blogDescription.editText?.text.toString().trim()
           //any field empty to check
           if(title.isEmpty()||description.isEmpty()){
               Toast.makeText(this,"Please Fill all the fields", Toast.LENGTH_SHORT).show()

           }
           //get current user
           val user: FirebaseUser?=auth.currentUser

           if(user!=null){
               val userId:String=user.uid
               val userName:String=user.displayName?:"Anonymous"
               val userImageUrl: String = user.photoUrl?.toString() ?: ""

               userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
                   override fun onDataChange(snapshot: DataSnapshot) {
                       val userData: UserData?=snapshot.getValue(UserData::class.java)
                       if(userData!=null){
                           val userNameFromDB:String=userData.name
                           val userImageUrlFromDB:String=userData.profileImage

                           val currentDate:String= SimpleDateFormat("yyyy-MM-dd").format(Date())

                           //Create a blog item model
                           val blogItem= BlogItemModel(
                               title,userNameFromDB,
                               currentDate,
                               description,
                               0,
                               userImageUrlFromDB
                           )

                           //create uniquekey for blog post
                           val key:String?=databaseReference.push().key
                           if(key!=null){
                               val blogReference: DatabaseReference=databaseReference.child(key)
                               blogReference.setValue(blogItem).addOnCompleteListener {
                                   if (it.isSuccessful) {
                                       finish()
                                   } else {
                                       Toast.makeText(this@AddArticleActivity,"Failed to add blog",Toast.LENGTH_SHORT)
                                   }
                               }
                           }
                       }
                   }

                   override fun onCancelled(error: DatabaseError) {

                   }
               }
               )
           }
       }
    }
}