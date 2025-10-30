package com.example.blogapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private lateinit var cloudinary: Cloudinary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://aradhanablogapp-default-rtdb.asia-southeast1.firebasedatabase.app")

        // 🔹 Cloudinary Configuration
        val config = mapOf(
            "cloud_name" to "dqal35qn2",
            "api_key" to "518248655463843",
            "api_secret" to "xq9Dm-EbM-mwg220tSvAFosl57o"
        )
        cloudinary = Cloudinary(config)

        val action = intent.getStringExtra("action")

        if (action == "login") setupLoginUI()
        else if (action == "register") setupRegisterUI()

        // 🔹 Image Picker
        binding.cardView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
        }
    }

    private fun setupLoginUI() {
        binding.loginEmailAddress.visibility = View.VISIBLE
        binding.loginPassword.visibility = View.VISIBLE
        binding.loginButton.visibility = View.VISIBLE

        binding.registerButton.isEnabled = false
        binding.registerButton.alpha = 0.5f
        binding.cardView.visibility = View.GONE
        binding.registerName.visibility = View.GONE
        binding.registerEmail.visibility = View.GONE
        binding.registerPassword.visibility = View.GONE
        binding.registerNewHere.isEnabled = false
        binding.registerNewHere.alpha = 0.5f

        binding.loginButton.setOnClickListener {
            val loginEmail = binding.loginEmailAddress.text.toString()
            val loginPassword = binding.loginPassword.text.toString()

            if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful 😁", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login Failed ❌.Please Enter Correct Details", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun setupRegisterUI() {
        binding.loginButton.isEnabled = false
        binding.loginButton.alpha = 0.5f

        binding.registerButton.setOnClickListener {
            val registerName = binding.registerName.text.toString()
            val registerEmail = binding.registerEmail.text.toString()
            val registerPassword = binding.registerPassword.text.toString()

            if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                Toast.makeText(this, "Please Fill All the Details", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user: FirebaseUser? = auth.currentUser
                            val userReference: DatabaseReference = database.getReference("users")

                            user?.let {
                                val userId = user.uid

                                // 🔹 If image selected, upload to Cloudinary and save URL
                                if (imageUri != null) {
                                    uploadImageToCloudinary(imageUri!!) { imageUrl ->
                                        val userData = UserData(registerName, registerEmail, imageUrl)
                                        userReference.child(userId).setValue(userData)
                                        Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, WelcomeActivity::class.java))
                                        finish()
                                    }
                                } else {
                                    // 🔹 No image selected, save with empty URL
                                    val userData = UserData(registerName, registerEmail, "")
                                    userReference.child(userId).setValue(userData)
                                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                    finish()
                                }
                            }
                        } else {
                            val message = task.exception?.message ?: "Registration failed"
                            Toast.makeText(this, "Registration failed: $message", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    // 🔹 Image Upload Function with callback for URL
    // 🔹 Upload image to Cloudinary, save URL to Firebase, and show it in ImageView
    private fun uploadImageToCloudinary(uri: Uri, callback: (String) -> Unit) {
        Thread {
            try {
                val file = FileUtil.from(this, uri)
                val options = ObjectUtils.asMap("folder", "profile_image")
                val uploadResult = cloudinary.uploader().upload(file, options)
                val imageUrl = uploadResult["secure_url"].toString()

                Log.d("CloudinaryUpload", "✅ Uploaded Image URL: $imageUrl")

                runOnUiThread {
                    // ✅ Load uploaded image instantly into the registerUserImage view
                    Glide.with(this)
                        .load(imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.registerUserImage)

                    // ✅ Continue with the rest of the registration process
                    callback(imageUrl)

                    Toast.makeText(this, "✅ Profile image uploaded!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "❌ Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback("") // if upload failed
                }
            }
        }.start()
    }


    // 🔹 Convert URI → File
    object FileUtil {
        fun from(context: Context, uri: Uri): File {
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            return file
        }
    }

    // 🔹 Show selected image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.registerUserImage)
        }
    }
}
