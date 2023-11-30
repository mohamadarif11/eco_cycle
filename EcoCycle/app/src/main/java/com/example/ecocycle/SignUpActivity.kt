package com.example.ecocycle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ecocycle.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.btnSignup.setOnClickListener {
            val email = binding.edEmail.editText?.text.toString()
            val password1 = binding.edPassword1.editText?.text.toString()
            val password2 = binding.edPassword2.editText?.text.toString()
            val nama = binding.edNama.editText?.text.toString()
            val noTelp = binding.edNohp.editText?.text.toString()

            if (email.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty()) {
                if (password1.length >= 8 && password2.length >= 8) {
                    if (password2 == password1) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password1)
                            .addOnCompleteListener {signUpTask ->
                                if (signUpTask.isSuccessful) {
                                    val userId = firebaseAuth.currentUser?.uid
                                    val userRef =
                                        firebaseFirestore.collection("user").document(userId!!)
                                    val userData = hashMapOf(
                                        "nama" to nama,
                                        "email" to email,
                                        "nomor" to noTelp
                                    )
                                    userRef.set(userData)
                                        .addOnSuccessListener {
                                            val intent = Intent(this, LoginActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                }
                            }
                    } else {
                        Toast.makeText(this, "Password Tidak Cocok", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Password Minimal 8 Karakter", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Semua Form Harus Terisi", Toast.LENGTH_SHORT).show()
            }
        }


    }
}