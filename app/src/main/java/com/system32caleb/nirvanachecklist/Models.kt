package com.system32caleb.nirvanachecklist

data class Track(
    val title: String,
    val version: String = ""
)

data class Release(
    val id: String,
    val title: String,
    val type: String,
    val year: Int,
    val official: Boolean,
    val verification: String,
    val cover: String,
    val tracks: List<Track>
)
