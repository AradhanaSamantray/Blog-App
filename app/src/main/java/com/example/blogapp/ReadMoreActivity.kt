package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs: BlogItemModel? = intent.getParcelableExtra("blogItem")

        if (blogs != null) {

            binding.titleText.text = blogs.heading
            binding.userNameText.text = blogs.userName
            binding.dateText.text = blogs.date
            binding.blogDescriptionTextView.text = blogs.post

            Glide.with(this)
                .load(blogs.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ProfileImage)

        } else {
            Toast.makeText(this, "Failed to load blog", Toast.LENGTH_SHORT).show()
        }
    }
}
