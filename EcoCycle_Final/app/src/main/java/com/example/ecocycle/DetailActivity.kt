package com.example.ecocycle

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.ecocycle.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val limbahId = intent.getStringExtra("limbahId")
        val db = FirebaseFirestore.getInstance()
        val limbahRef = limbahId?.let { db.collection("limbah").document(it) }

        limbahRef?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val nama = document.getString("nama")
                    val nomor = document.getString("nomor")
                    val alamat = document.getString("alamat")
                    val image = document.getString("image")
                    val judul = document.getString("judul")
                    val deskripsi = document.getString("deskripsi")

                    Glide.with(this).load(image).into(binding.ivDetailLimbah)

                    binding.judulDetailLimbah.text = judul
                    binding.alamatDetailLimbah.text = alamat
                    binding.deskripsiDetailLimbah.text = deskripsi
                    binding.namaPemilikLimbah.text = nama
                    binding.nomorPemilikLimbah.text = nomor

                    binding.btnChat.setOnClickListener{
                        bukaWA(nomor)
                    }
                }
            }
        }
    }

    private fun bukaWA(nomor: String?){
        if (!nomor.isNullOrEmpty()) {
            try {
                val nomorWhatsApp = if (nomor.startsWith("+")) nomor else "+62$nomor"

                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$nomorWhatsApp")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal Membuka Whatsapp", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Nomor Tidak Tersedia", Toast.LENGTH_SHORT).show()
        }
    }

}