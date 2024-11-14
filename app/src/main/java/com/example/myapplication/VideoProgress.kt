package com.example.myapplication

import android.content.Context
import android.util.Log

object VideoProgress {
    private const val PREF_NAME = "video_progress"
    private const val TAG = "VideoProgress"
    
    fun saveProgress(context: Context, videoUrl: String, position: Long) {
        if (position <= 0) return
        
        try {
            val key = videoUrl.hashCode().toString()
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(key, position)
                .apply()
            Log.d(TAG, "Saved progress for $videoUrl: $position ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving progress: ${e.message}")
        }
    }
    
    fun getProgress(context: Context, videoUrl: String): Long {
        try {
            val key = videoUrl.hashCode().toString()
            val position = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getLong(key, 0L)
            Log.d(TAG, "Retrieved progress for $videoUrl: $position ms")
            return position
        } catch (e: Exception) {
            Log.e(TAG, "Error getting progress: ${e.message}")
            return 0L
        }
    }
    
    fun clearProgress(context: Context, videoUrl: String) {
        try {
            val key = videoUrl.hashCode().toString()
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply()
            Log.d(TAG, "Cleared progress for $videoUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing progress: ${e.message}")
        }
    }
}