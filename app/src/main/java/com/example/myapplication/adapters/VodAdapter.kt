package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.R
import com.example.myapplication.models.VodItem
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

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
        view.nextFocusUpId = R.id.search_button
        return VodViewHolder(view)
    }

    override fun onBindViewHolder(holder: VodViewHolder, position: Int) {
        val vod = getItem(position)
        holder.bind(vod)
        holder.itemView.setOnClickListener { onItemClick(vod) }
        
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) 1.1f else 1.0f)
                .scaleY(if (hasFocus) 1.1f else 1.0f)
                .translationZ(if (hasFocus) 16f else 0f)
                .translationY(if (hasFocus) -20f else 0f)
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
            
            if (hasFocus) {
                onFocusChangeListener?.invoke(vod)
            }
        }
    }

    class VodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.vod_image)

        fun bind(vod: VodItem) {
            Glide.with(itemView.context)
                .load(vod.imgv)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .thumbnail(0.1f)
                .transform(RoundedCorners(8))
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