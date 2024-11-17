package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.models.VodItem
import com.example.myapplication.viewmodels.VodViewModel
import com.example.myapplication.adapters.VodAdapter

class VodActivity : AppCompatActivity() {
    private val viewModel: VodViewModel by viewModels()
    private lateinit var detailImage: ImageView
    private lateinit var detailTitle: TextView
    private lateinit var detailDescription: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod)

        detailImage = findViewById(R.id.detail_image)
        detailTitle = findViewById(R.id.detail_title)
        detailDescription = findViewById(R.id.detail_description)
        val detailSection = findViewById<View>(R.id.detail_section)

        val recyclerView = findViewById<RecyclerView>(R.id.vod_recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 8) // Changed to 8 columns
        
        // Make detail section visible initially
        findViewById<View>(R.id.detail_section).visibility = View.VISIBLE

        val adapter = VodAdapter { vod ->
            viewModel.selectVod(vod)
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("videoUrl", vod.url)
                putExtra("licenseUrl", "https://proxy.uat.widevine.com/proxy")
                putExtra("drmKey", vod.key)
            }
            startActivity(intent)
        }

        adapter.setOnFocusChangeListener { vod ->
            detailSection.visibility = View.VISIBLE
            updateDetailView(vod)
        }
        
        recyclerView.adapter = adapter

        viewModel.vods.observe(this) { vods ->
            adapter.submitList(vods)
        }

        viewModel.selectedVod.observe(this) { vod ->
            updateDetailView(vod)
        }

        viewModel.loadVods()
    }

    private fun updateDetailView(vod: VodItem) {
        Glide.with(this).load(vod.fondo).into(detailImage)
        detailTitle.text = vod.titulo
        
        // Convert seconds to hours and minutes
        val durationSeconds = vod.duracion.toInt()
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        
        val durationText = when {
            hours > 0 -> "${hours}h ${minutes}min"
            else -> "${minutes}min"
        }
        
        detailDescription.text = "${vod.year} • $durationText\n${vod.descripcion}"
    }
}