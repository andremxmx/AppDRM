package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.models.VodItem

class VodAdapter(
    private val onItemClick: (VodItem) -> Unit
) : ListAdapter<VodItem, VodAdapter.VodViewHolder>(VodDiffCallback()) {
    
    private var onFocusChangeListener: ((VodItem) -> Unit)? = null
    
    fun setOnFocusChangeListener(listener: (VodItem) -> Unit) {
        onFocusChangeListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vod, parent, false)
        return VodViewHolder(view)
    }

    override fun onBindViewHolder(holder: VodViewHolder, position: Int) {
        val vod = getItem(position)
        holder.bind(vod)
        holder.itemView.setOnClickListener { onItemClick(vod) }
        
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) 1.15f else 1.0f)
                .scaleY(if (hasFocus) 1.15f else 1.0f)
                .setDuration(150)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
            v.elevation = if (hasFocus) 12f else 4f
            
            if (hasFocus) {
                onFocusChangeListener?.invoke(vod)
            }
        }
    }

    class VodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.vod_image)

        fun bind(vod: VodItem) {
            Glide.with(itemView.context)
                .load(vod.imgv) // Using imgv for card image
                .centerCrop()
                .into(imageView)
        }
    }

    private class VodDiffCallback : DiffUtil.ItemCallback<VodItem>() {
        override fun areItemsTheSame(oldItem: VodItem, newItem: VodItem): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: VodItem, newItem: VodItem): Boolean {
            return oldItem == newItem
        }
    }
}