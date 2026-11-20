package com.system32caleb.nirvanachecklist

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView

class AlbumActivity : Activity() {

    private lateinit var release: Release
    private lateinit var adapter: TrackAdapter
    private lateinit var progress: ProgressBar
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        val id = intent.getStringExtra("release_id") ?: ""
        release = Catalog.load(this).firstOrNull { it.id == id } ?: return

        title = release.title
        progress = findViewById(R.id.album_progress)
        summary = findViewById(R.id.album_summary)

        adapter = TrackAdapter()
        findViewById<ListView>(R.id.track_list).adapter = adapter
        refresh()
    }

    private fun refresh() {
        adapter.notifyDataSetChanged()
        val done = Store.downloadedCount(this, release)
        val total = release.tracks.size
        val percent = if (total == 0) 0 else done * 100 / total

        progress.max = total
        progress.progress = done
        summary.text = getString(R.string.album_progress, done, total, percent)
    }

    private inner class TrackAdapter : BaseAdapter() {
        override fun getCount() = release.tracks.size
        override fun getItem(position: Int) = release.tracks[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_track, parent, false)
            val track = getItem(position)
            val checkBox = view.findViewById<CheckBox>(R.id.track_check)
            val label = view.findViewById<TextView>(R.id.track_title)

            label.text = if (track.version.isEmpty()) track.title else "${track.title} — ${track.version}"
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = Store.isDownloaded(this@AlbumActivity, release.id, position)
            checkBox.setOnCheckedChangeListener { _, checked ->
                Store.setDownloaded(this@AlbumActivity, release.id, position, checked)
                refresh()
            }

            return view
        }
    }
}
