package com.system32caleb.nirvanachecklist

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object Store {
    private const val PREFS = "nirvana_checklist"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isDownloaded(context: Context, releaseId: String, index: Int): Boolean {
        val set = prefs(context).getStringSet(releaseId, emptySet()) ?: emptySet()
        return index.toString() in set
    }

    fun setDownloaded(context: Context, releaseId: String, index: Int, value: Boolean) {
        val prefs = prefs(context)
        val current = prefs.getStringSet(releaseId, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (value) current.add(index.toString()) else current.remove(index.toString())
        prefs.edit().putStringSet(releaseId, current).apply()
    }

    fun downloadedCount(context: Context, release: Release): Int {
        val set = prefs(context).getStringSet(release.id, emptySet()) ?: emptySet()
        return set.count { it.toIntOrNull() != null && it.toInt() < release.tracks.size }
    }

    fun totalDownloaded(context: Context, releases: List<Release>): Int {
        return releases.sumOf { downloadedCount(context, it) }
    }

    fun totalTracks(releases: List<Release>): Int {
        return releases.sumOf { it.tracks.size }
    }

    fun exportJson(context: Context, releases: List<Release>): String {
        val downloaded = JSONObject()
        releases.forEach { release ->
            val indexes = JSONArray()
            release.tracks.indices.forEach { index ->
                if (isDownloaded(context, release.id, index)) indexes.put(index)
            }
            if (indexes.length() > 0) downloaded.put(release.id, indexes)
        }

        return JSONObject()
            .put("version", 1)
            .put("downloaded", downloaded)
            .toString(2)
    }

    fun importJson(context: Context, json: String, releases: List<Release>): Int {
        val root = JSONObject(json)
        val downloaded = root.optJSONObject("downloaded") ?: return 0
        var imported = 0

        releases.forEach { release ->
            val indexes = downloaded.optJSONArray(release.id) ?: return@forEach
            for (i in 0 until indexes.length()) {
                val index = indexes.optInt(i, -1)
                if (index in release.tracks.indices) {
                    setDownloaded(context, release.id, index, true)
                    imported++
                }
            }
        }

        return imported
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
