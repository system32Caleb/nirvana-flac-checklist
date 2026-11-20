package com.system32caleb.nirvanachecklist

import android.content.Context
import org.json.JSONObject

object Catalog {
    fun load(context: Context): List<Release> {
        val json = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val array = root.getJSONArray("releases")
        val releases = mutableListOf<Release>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val tracks = mutableListOf<Track>()
            val trackArray = item.getJSONArray("tracks")

            for (j in 0 until trackArray.length()) {
                val track = trackArray.getJSONObject(j)
                tracks.add(
                    Track(
                        title = track.getString("title"),
                        version = track.optString("version", "")
                    )
                )
            }

            releases.add(
                Release(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    type = item.getString("type"),
                    year = item.getInt("year"),
                    official = item.getBoolean("official"),
                    verification = item.getString("verification"),
                    cover = item.optString("cover", ""),
                    tracks = tracks
                )
            )
        }

        return releases.sortedWith(
            compareBy({ it.official.not() }, { it.year }, { it.title })
        )
    }
}
