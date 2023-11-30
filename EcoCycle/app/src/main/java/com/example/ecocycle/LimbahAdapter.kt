package com.example.ecocycle

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecocycle.databinding.ItemLimbahBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class LimbahAdapter(options: FirestoreRecyclerOptions<Limbah>) :
    FirestoreRecyclerAdapter<Limbah, LimbahAdapter.ViewHolder>(options)  {

    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LimbahAdapter.ViewHolder {
        val binding = ItemLimbahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LimbahAdapter.ViewHolder, position: Int, model: Limbah) {
        holder.bind(model)
    }

    inner class ViewHolder(private val binding: ItemLimbahBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(limbah: Limbah) {
            binding.tvAlamat.text = limbah.alamat
            Glide.with(binding.root)
                .load(limbah.image)
                .into(binding.ivCompany)
            binding.tvJudul.text = limbah.judul

            itemView.setOnClickListener {
                onItemClickListener?.invoke(snapshots.getSnapshot(adapterPosition).id)
            }

        }
    }
}
