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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton

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

        val recyclerView = findViewById<RecyclerView>(R.id.vod_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // Add item decoration for even spacing
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(8, 8, 8, 8)
            }
        })

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
            updateDetailView(vod)
        }
        
        recyclerView.adapter = adapter

        viewModel.vods.observe(this) { vods ->
            adapter.submitList(vods)
            // Set initial focus and details when list is loaded
            if (vods.isNotEmpty()) {
                recyclerView.post {
                    recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                    updateDetailView(vods[0])
                }
            }
        }

        viewModel.selectedVod.observe(this) { vod ->
            updateDetailView(vod)
        }

        viewModel.loadVods()

        // Configurar búsqueda
        val searchBox = findViewById<EditText>(R.id.search_box)
        val searchButton = findViewById<ImageButton>(R.id.search_button)
        
        searchButton.setOnClickListener {
            searchBox.visibility = if (searchBox.visibility == View.VISIBLE) {
                searchBox.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { searchBox.visibility = View.GONE }
                    .start()
                View.GONE
            } else {
                searchBox.alpha = 0f
                searchBox.visibility = View.VISIBLE
                searchBox.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
                searchBox.requestFocus()
                View.VISIBLE
            }
        }

        searchBox.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString()
                viewModel.filterVods(query)
                true
            } else {
                false
            }
        }

        // Búsqueda en tiempo real mientras se escribe
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterVods(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateDetailView(vod: VodItem) {
        Glide.with(this)
            .load(vod.fondo)
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .into(detailImage)
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