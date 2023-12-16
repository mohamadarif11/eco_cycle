package com.example.ecocycle

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.ecocycle.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val AMBIL_GAMBAR = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?

    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.cardUpload.setOnClickListener {
            val intent = Intent(requireContext(), UploadActivity::class.java)
            startActivity(intent)
        }

        binding.cardLogout.setOnClickListener {
            firebaseAuth.signOut()

            val sharedPref = requireContext().getSharedPreferences("login_pref", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val userRef = firebaseFirestore.collection("user").document(userId!!)
                userRef.get().addOnSuccessListener {
                        if (it.exists()) {
                            val nama = it.getString("nama")
                            val email = it.getString("email")
                            val nomor = it.getString("nomor")

                            binding.profileName.text = nama
                            binding.profileEmail.text = email
                            binding.profileNomor.text = nomor

                            val gambarUrl = it.getString("profileGambarUrl")
                            if (!gambarUrl.isNullOrEmpty()) {
                                Glide.with(requireContext()).load(gambarUrl)
                                    .into(binding.profileImage)
                            }
                        }
                    }
            }

        binding.profileImage.setOnClickListener {
            bukaGaleri()
        }
    }

    private fun bukaGaleri() {
        val intent = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, AMBIL_GAMBAR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AMBIL_GAMBAR && resultCode == Activity.RESULT_OK && data != null) {
            val gambarUri: Uri? = data.data
            if (gambarUri != null) {
                val file = Media.getBitmap(requireContext().contentResolver, gambarUri)
                uploadGambar(file)
            }

        }
    }

    private fun uploadGambar(bitmap: Bitmap) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("profile_images/${firebaseAuth.currentUser?.uid}.jpg")

        val byte = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byte)
        val imageData = byte.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val gambarUrl = uri.toString()
                saveGambar(gambarUrl)
            }
        }
    }

    private fun saveGambar(gambarUrl: String) {
        val userId = firebaseAuth.currentUser?.uid
        val userRef = firebaseFirestore.collection("user").document(userId!!)
        val userData = hashMapOf<String, Any?>("profileGambarUrl" to gambarUrl)

        userRef.update(userData).addOnSuccessListener {
                Glide.with(requireContext()).load(gambarUrl)
                    .into(binding.profileImage)
            }
    }

}