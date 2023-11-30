package com.example.ecocycle

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ecocycle.databinding.ActivityPrediksiBinding
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PrediksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrediksiBinding
    private lateinit var tflite: Interpreter
    private var pilihGambar: Bitmap? = null

    companion object {
        private const val AMBIL_GAMBAR = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrediksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setTitle("Prediksi Limbah")

        binding.ivPredictLimbah.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, AMBIL_GAMBAR)
        }
        val db = FirebaseFirestore.getInstance()

        binding.btnPredictLimbah.setOnClickListener {
            if (pilihGambar != null) {
                predictWaste(pilihGambar!!)
            } else {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }

            val predictedLabel = binding.resultPrediksi.text.toString()

            val collectionRef = db.collection("waste_management")
            val documentRef = collectionRef.document(predictedLabel)


            documentRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {

                    val deskripsi = document.getString("deskripsi")
                    val pengelolaan = document.getString("pengelolaan")

                    if (deskripsi != null && pengelolaan != null) {
                        showWasteManagementInfo(deskripsi, pengelolaan)
                    } else {
                        showWasteManagementInfo(
                            "Informasi pengelolaan tidak tersedia.",
                            "Field lain tidak tersedia."
                        )
                    }
                } else {
                    showWasteManagementInfo("Dokumen tidak ditemukan.", "")
                }
            }.addOnFailureListener { exception ->
                showWasteManagementInfo("Gagal mengambil data: ${exception.message}", "")
            }
        }
        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }


        binding.btnInfo.setOnClickListener {
            val predictedLabel = binding.resultPrediksi.text.toString()

            if (predictedLabel.isEmpty()) {
                Toast.makeText(this, "Lokasi Tidak Tersedia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val collectionRef = db.collection("waste_management")
            val documentRef = collectionRef.document(predictedLabel)

            documentRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {

                    val coordinatesList: List<String>? = getCoordinatesForWasteType(predictedLabel)
                    if (coordinatesList != null) {
                        for (coordinates in coordinatesList) {
                            val gmmIntentUri =
                                Uri.parse("geo:$coordinates?q=$predictedLabel")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps") // Paksa menggunakan Google Maps
                            startActivity(mapIntent)
                        }
                    }
                }
            }
        }

    }

    private fun getCoordinatesForWasteType(wasteType: String): List<String>? {
        return when (wasteType) {
            "Sampah organic" -> listOf(
                "-6.980530652245236, 107.6309643074904",
                "-7.002040262977176, 107.53254354403096",
                "-6.989261506260085, 107.56567419005793"
            )
            "Sampah plastic" -> listOf(
                "-7.021999259098582, 107.33698491331306",
                "-6.85568767465889, 107.5310981203401",
                "-6.915676651325288, 107.55513071331303"
            )
            "Sampah kayu" -> listOf(
                "-6.909485366003757, 107.58053525879336",
                "-6.8685840790572446, 107.55718931133394",
                "-6.784043629631465, 107.64302000052304"
            )
            "Sampah besi" -> listOf(
                "-7.07101046263962, 107.72321176012386",
                "-6.926526324038491, 107.65111398120499",
                "-6.442246390490268, 107.12628148406078"
            )
            "Non recycle" -> listOf(
                "-6.89920206322882, 107.72939023182545",
                "-6.475319752145567, 106.96067808639415",
                "-6.555273789003339, 106.6981512434752"
            )
            else -> null
        }
    }

    private fun showWasteManagementInfo(deskripsi: String, pengelolaan: String) {
        val info = "Deskripsi: $deskripsi\n\nPengelolaan Limbah: $pengelolaan"
        binding.tvWasteManagementInfo.text = info
        binding.tvWasteManagementInfo.visibility = View.VISIBLE
        binding.btnInfo.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        tflite.close()
    }

    private fun loadModelFile(): MappedByteBuffer {
        val modelPath = "model.tflite"
        val assetManager = assets
        val fileDescriptor = assetManager.openFd(modelPath)
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun predictWaste(bitmap: Bitmap) {
        if (!::tflite.isInitialized) {
            Toast.makeText(this, "Model TFLite belum dimuat", Toast.LENGTH_SHORT).show()
            return
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
                inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
                inputBuffer.putFloat((pixel and 0xFF) / 255.0f)
            }
        }

        val output = Array(1) { FloatArray(5) }
        tflite.run(inputBuffer, output)


        val probabilities = output[0]
        val labels =
            arrayOf("Sampah organic", "Sampah plastic", "Sampah kayu", "Sampah besi", "Non recycle")
        var maxIndex = 0
        var maxPeluang = 0.0f
        for (i in probabilities.indices) {
            if (probabilities[i] > maxPeluang) {
                maxIndex = i
                maxPeluang = probabilities[i]
            }
        }
        val predictedLabel = labels[maxIndex]

        binding.resultPrediksi.text = "$predictedLabel"

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AMBIL_GAMBAR && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri = data.data
                pilihGambar = BitmapFactory.decodeStream(selectedImageUri?.let {
                    contentResolver.openInputStream(
                        it
                    )
                })
                binding.ivPredictLimbah.setImageBitmap(pilihGambar)
            }
        }
    }
}