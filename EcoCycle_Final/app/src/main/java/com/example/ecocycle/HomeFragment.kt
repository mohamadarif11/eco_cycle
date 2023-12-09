package com.example.ecocycle

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ecocycle.databinding.FragmentHomeBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LimbahAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val data = firestore.collection("limbah")

        val fetch = FirestoreRecyclerOptions.Builder<Limbah>()
            .setQuery(data, Limbah::class.java)
            .build()

        binding.btnPrediksi.setOnClickListener{
            val intent = Intent(requireContext(), PrediksiActivity::class.java)
            startActivity(intent)
        }

        adapter = LimbahAdapter(fetch)
        val layoutManager = WrapContentGridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { limbahId ->

            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("limbahId", limbahId)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        adapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}