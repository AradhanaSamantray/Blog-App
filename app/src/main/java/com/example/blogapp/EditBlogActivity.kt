package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityEditBlogBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding: ActivityEditBlogBinding by lazy{
        ActivityEditBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.imageButton.setOnClickListener{
            finish()
        }
        val BlogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")
        binding.blogTitle.editText?.setText(BlogItemModel?.heading)
        binding.blogDescription.editText?.setText(BlogItemModel?.post)

        binding.SaveBlogButton.setOnClickListener {
            val updatedTitle = binding.blogTitle.editText?.text.toString().trim()
            val updatedDescription = binding.blogDescription.editText?.text.toString().trim()
            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()){
                Toast.makeText(this, "Please fill all the Details", Toast.LENGTH_SHORT).show()
            }else{
                BlogItemModel?.heading = updatedTitle
                BlogItemModel?.post = updatedDescription
                if (BlogItemModel != null) {
                    updateDataInFirebase(BlogItemModel)
                }
            }

        }


    }
    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference : DatabaseReference = FirebaseDatabase.getInstance("https://blog-app-401c7-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
        val postId : String = blogItemModel.postId
        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Update Blog", Toast.LENGTH_SHORT).show()
                finish()
            }

    }

}