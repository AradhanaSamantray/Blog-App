package com.example.blogapp

import android.os.Bundle
import android.service.autofill.UserData
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth:FirebaseAuth
    private lateinit var database: FirebaseDatabase
    //storage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        //storage


        //for visibility of field

        val action = intent.getStringExtra("action")
        //adjust visibility for login
        if(action == "login"){
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

        }else if(action == "register"){
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.registerButton.setOnClickListener{
                //get data from edit text field
                val registerName:String = binding.registerName.text.toString()
                val registerEmail:String = binding.registerName.text.toString()
                val registerPassword:String = binding.registerName.text.toString()
                if(registerName.isEmpty()||registerEmail.isEmpty()||registerPassword.isEmpty()){
                    Toast.makeText(this,"Please Fill All the Details",Toast.LENGTH_SHORT).show()
                }else{
                    auth.createUserWithEmailAndPassword(registerEmail,registerPassword).addOnCompleteListener{
                        task->
                        if(task.isSuccessful){
                            val user:FirebaseUser? = auth.currentUser
                            user?.let{
                                //Save user data in to Firebase realtime database
                                val userReference:DatabaseReference = database.getReference("users")
                                val userId:String = user.uid
                                val userData = com.example.blogapp.Model.UserData(registerName,registerPassword)
                                userReference.child(userId).setValue(userData)
                            }


                        }else{

                        }
                    }
                }
            }





        }
    }
}