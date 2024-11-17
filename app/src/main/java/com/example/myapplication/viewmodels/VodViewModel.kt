
package com.example.myapplication.viewmodels

import androidx.lifecycle.*
import com.example.myapplication.models.VodItem
import com.example.myapplication.models.VodResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class VodViewModel : ViewModel() {
    private val _vods = MutableLiveData<List<VodItem>>()
    val vods: LiveData<List<VodItem>> = _vods

    private val _selectedVod = MutableLiveData<VodItem>()
    val selectedVod: LiveData<VodItem> = _selectedVod

    fun loadVods() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://dslive.site/json/vod_keys.json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val json = response.body?.string()
                    val vodResponse = Gson().fromJson(json, VodResponse::class.java)
                    _vods.postValue(vodResponse.data)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectVod(vod: VodItem) {
        _selectedVod.value = vod
    }
}