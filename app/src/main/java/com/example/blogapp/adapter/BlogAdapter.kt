package com.example.blogapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.databinding.BlogItemBinding
import com.example.blogapp.ReadMoreActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter (private val items:MutableList<BlogItemModel>)
    :RecyclerView.Adapter<BlogAdapter.BlogViewHolder>(){

        private val databaseReference:DatabaseReference= FirebaseDatabase.getInstance("https://blog-app-3e6e0-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        private val currentUser= FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater:LayoutInflater=LayoutInflater.from(parent.context)
        val binding:BlogItemBinding=BlogItemBinding.inflate(inflater,parent,false)
        return BlogViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem:BlogItemModel=items[position]
        holder.bind(blogItem)
    }

    inner class BlogViewHolder(private val binding:BlogItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(blogItemModel:BlogItemModel){
            val postId= blogItemModel.postId
            val context=binding.root.context
            binding.heading.text=blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text=blogItemModel.userName
            binding.date.text=blogItemModel.date
            binding.post.text=blogItemModel.post
            binding.likeCount.text=blogItemModel.likeCount.toString()

            binding.root.setOnClickListener{
                val context: Context =binding.root.context
                val intent= Intent(context,ReadMoreActivity::class.java)
                intent.putExtra("blogItem",blogItemModel)
                context.startActivity(intent)

            }

            val postLikeReference =databaseReference.child("blogs").child(postId).child("likes")
            val currentUserLiked=currentUser?.uid?.let { uid->
                postLikeReference.child(uid).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                       if(snapshot.exists()){
                           binding.likeButton.setImageResource(R.drawable.red_heart)
                       }else{
                           binding.likeButton.setImageResource(R.drawable.black_heart)
                       }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            binding.likeButton.setOnClickListener{

                if(currentUser!=null){
                    handleLikeButtonClicked(postId,blogItemModel,binding)
                }
                else{
                    Toast.makeText(context,"you have to login first",Toast.LENGTH_SHORT)
                }
            }

            val userReference=databaseReference.child("users").child(currentUser?.uid?:"")
            val postSaveReference=userReference.child("saveBlogPosts").child(postId)

            postSaveReference.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   if(snapshot.exists())
                   {
                       //if blog already saved
                       binding.postSaveButton.setImageResource(R.drawable.red_saved_articles)
                   }else
                   {
                       binding.postSaveButton.setImageResource(R.drawable.red_bookmark)
                   }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            binding.postSaveButton.setOnClickListener{
                if(currentUser!=null){
                    handleSaveButtonClicked(postId,blogItemModel,binding)
                }
                else{
                    Toast.makeText(context,"you have to login first",Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel,binding: BlogItemBinding) {
        val userReference=databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference=databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser.uid).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    userReference.child("likes").child(postId).removeValue()
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).removeValue()
                            blogItemModel.likedBy?.remove(currentUser.uid)
                            updateLikeButtonImage(binding ,false)

                            val newLikeCount=blogItemModel.likeCount -1
                            blogItemModel.likeCount=newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                                    notifyDataSetChanged()
                        }
                        .addOnFailureListener{ e ->
                            Log.e("LikedClicked", "onDataChange: Failed to unlike the blog $e", )
                        }
                        }
                else{
                    userReference.child("likes").child(postId).setValue(true)
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).setValue(true)
                            blogItemModel.likedBy?.add(currentUser.uid)
                            updateLikeButtonImage(binding,true)

                            val newLikeCount=blogItemModel.likeCount +1
                            blogItemModel.likeCount=newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                            notifyDataSetChanged()
                        }
                        .addOnFailureListener{e ->
                            Log.e("LikedClicked", "onDataChange: Failed to like the blog $e", )
                        }

                }
                    }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding ,liked: Boolean)
   {
        if(liked) {
            binding.likeButton.setImageResource(R.drawable.red_heart)
        }
       else{
           binding.likeButton.setImageResource(R.drawable.black_heart)
        }
    }
    private fun handleSaveButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding)
    {
        val userReference=databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saveBlogPosts").child(postId).addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userReference.child("saveBlogPosts").child(postId).removeValue()
                        .addOnSuccessListener {
                            val clickedBlogItem = items.find { it.postId == postId }
                            clickedBlogItem?.isSaved = false
                            notifyDataSetChanged()

                            val context = binding.root.context
                            Toast.makeText(context, "Blog Unsaved!", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener() {
                            val context = binding.root.context
                            Toast.makeText(
                                context,
                                "Failed to  Unsave the Blog!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                binding.postSaveButton.setImageResource(R.drawable.red_saved_articles)
            }else
            {
               userReference.child("saveBlogPosts").child(postId).setValue(true)
                   .addOnSuccessListener {
                       val clickedBlogItem = items.find { it.postId == postId }
                       clickedBlogItem?.isSaved = true
                       notifyDataSetChanged()

                       val context = binding.root.context
                       Toast.makeText(context, " Blog saved!", Toast.LENGTH_SHORT).show()
                   }
                           .addOnFailureListener{
                               val context = binding.root.context
                               Toast.makeText(context, " Failed to save Blog !", Toast.LENGTH_SHORT).show()
                           }
                    binding.postSaveButton.setImageResource(R.drawable.red_bookmark)

                   }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    fun updateData(savedBlogsArticles: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogsArticles)
        notifyDataSetChanged()
    }

}