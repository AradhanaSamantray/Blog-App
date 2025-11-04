package com.example.blogapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.*
import java.io.File

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    // Cloudinary instance
    private lateinit var cloudinary: Cloudinary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-app-3e6e0-default-rtdb.asia-southeast1.firebasedatabase.app")

        // Configure Cloudinary
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dc2wycsfb"
        config["api_key"] = "756633542912518"
        config["api_secret"] = "yhDPSVOxbXIPL56FOpZZa4z327w"
        cloudinary = Cloudinary(config)

        val action = intent.getStringExtra("action")

        if (action == "login") {
            setupLoginUI()
        } else if (action == "register") {
            setupRegisterUI()
        }

        // Select image from gallery
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                PICK_IMAGE_REQUEST
            )
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
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful 😁", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login Failed ❌. Please enter correct details", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user: FirebaseUser? = auth.currentUser
                            user?.let {
                                val userReference: DatabaseReference = database.getReference("users")
                                val userId = user.uid
                                val userData = UserData(registerName, registerEmail)
                                userReference.child(userId).setValue(userData)

                                // Upload image to Cloudinary (Coroutine-based)
                                imageUri?.let { uri ->
                                    uploadImageToCloudinary(uri, userReference, userId)
                                } ?: run {
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

    // 🔹 Coroutine-based upload to Cloudinary + Firebase update
    private fun uploadImageToCloudinary(uri: Uri, userReference: DatabaseReference, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = FileUtil.from(this@SignInAndRegistrationActivity, uri)
                val uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "profile_image"))
                val imageUrl = uploadResult["secure_url"].toString()

                withContext(Dispatchers.Main) {
                    // Save image URL to Firebase Database
                    userReference.child(userId).child("profileImage").setValue(imageUrl)

                    Toast.makeText(this@SignInAndRegistrationActivity, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignInAndRegistrationActivity, WelcomeActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignInAndRegistrationActivity, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 🔹 Helper to convert Uri to File
    object FileUtil {
        fun from(context: Context, uri: Uri): File {
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            return file
        }
    }
}
