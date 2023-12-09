package com.example.ecocycle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.example.ecocycle.databinding.ActivityUploadBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    companion object {
        private const val AMBIL_GAMBAR = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Tambah Limbah")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        binding.btnGambar.setOnClickListener {
            openImage()
        }

        binding.btnUpload.setOnClickListener {
            uploadLimbah()
        }

        fillEditText()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AMBIL_GAMBAR && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            binding.ivLimbah.setImageURI(selectedImageUri)
        }
    }

    private fun openImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, AMBIL_GAMBAR)
    }

    private fun uploadLimbah() {
        val nama = binding.edpemilik.text.toString()
        val nomor = binding.edno.text.toString()
        val judul = binding.edJudul.text.toString()
        val alamat = binding.edAlamat.text.toString()
        val deskripsi = binding.edDeskripsi.text.toString()

        if (judul.isNotEmpty() && alamat.isNotEmpty() && selectedImageUri != null) {
            val imageFile = UUID.randomUUID().toString()
            val imageRef = storageReference.child("images/$imageFile")

            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val image = downloadUri.toString()
                    val limbah = Limbah(alamat, image, judul, deskripsi, nama, nomor)
                    firebaseFirestore.collection("limbah").add(limbah).addOnSuccessListener {
                        Toast.makeText(this, "Upload Limbah Berhasil", Toast.LENGTH_SHORT).show()
                        finish()

                    }.addOnFailureListener {
                        Toast.makeText(this, "Gagal Upload Limbah", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    Toast.makeText(this, "Kesalahan saat upload gambar", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Data Tidak Valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fillEditText() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val userRef = firebaseFirestore.collection("user").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nama = document.getString("nama")
                    val nomor = document.getString("nomor")

                    runOnUiThread {
                        binding.edpemilik.setText(nama)
                        binding.edno.setText(nomor)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}