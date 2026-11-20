package com.system32caleb.nirvanachecklist

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader

class MainActivity : Activity() {

    private lateinit var releases: List<Release>
    private lateinit var adapter: AlbumAdapter
    private lateinit var progress: ProgressBar
    private lateinit var summary: TextView
    private var query = ""
    private var filter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        releases = Catalog.load(this)
        progress = findViewById(R.id.global_progress)
        summary = findViewById(R.id.global_summary)

        val search = findViewById<EditText>(R.id.search)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString()?.trim()?.lowercase() ?: ""
                refresh()
            }
        })

        val all = findViewById<Button>(R.id.filter_all)
        val downloaded = findViewById<Button>(R.id.filter_downloaded)
        val pending = findViewById<Button>(R.id.filter_pending)

        all.setOnClickListener { setFilter("all", all, downloaded, pending) }
        downloaded.setOnClickListener { setFilter("downloaded", all, downloaded, pending) }
        pending.setOnClickListener { setFilter("pending", all, downloaded, pending) }

        adapter = AlbumAdapter()
        val list = findViewById<ListView>(R.id.album_list)
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            val release = filtered()[position]
            startActivity(
                Intent(this, AlbumActivity::class.java)
                    .putExtra("release_id", release.id)
            )
        }

        refresh()
    }

    private fun setFilter(value: String, all: Button, downloaded: Button, pending: Button) {
        filter = value
        all.alpha = if (value == "all") 1f else 0.55f
        downloaded.alpha = if (value == "downloaded") 1f else 0.55f
        pending.alpha = if (value == "pending") 1f else 0.55f
        refresh()
    }

    private fun filtered(): List<Release> {
        return releases.filter { release ->
            val matchesSearch = query.isEmpty() ||
                release.title.lowercase().contains(query) ||
                release.tracks.any { it.title.lowercase().contains(query) }

            val downloadedCount = Store.downloadedCount(this, release)
            val matchesFilter = when (filter) {
                "downloaded" -> release.tracks.isNotEmpty() && downloadedCount > 0
                "pending" -> release.tracks.indices.any {
                    !Store.isDownloaded(this, release.id, it)
                }
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    private fun refresh() {
        adapter.notifyDataSetChanged()
        val total = Store.totalTracks(releases)
        val done = Store.totalDownloaded(this, releases)
        val percent = if (total == 0) 0 else done * 100 / total
        progress.max = total
        progress.progress = done
        summary.text = getString(R.string.global_progress, done, total, percent)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "nirvana-flac-checklist.json")
                }
                startActivityForResult(intent, REQUEST_EXPORT)
                true
            }
            R.id.action_import -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, REQUEST_IMPORT)
                true
            }
            R.id.action_reset -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.reset_title)
                    .setMessage(R.string.reset_message)
                    .setPositiveButton(R.string.reset_yes) { _, _ ->
                        Store.clear(this)
                        refresh()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return

        when (requestCode) {
            REQUEST_EXPORT -> exportTo(uri)
            REQUEST_IMPORT -> importFrom(uri)
        }
    }

    private fun exportTo(uri: Uri) {
        contentResolver.openOutputStream(uri)?.use { output ->
            output.write(Store.exportJson(this, releases).toByteArray())
        }
    }

    private fun importFrom(uri: Uri) {
        val json = contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use(BufferedReader::readText) ?: return

        val imported = Store.importJson(this, json, releases)
        refresh()
        Toast.makeText(this, getString(R.string.imported, imported), Toast.LENGTH_SHORT).show()
    }

    private inner class AlbumAdapter : BaseAdapter() {
        override fun getCount() = filtered().size
        override fun getItem(position: Int) = filtered()[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_album, parent, false)
            val release = getItem(position)
            val done = Store.downloadedCount(this@MainActivity, release)
            val total = release.tracks.size

            view.findViewById<TextView>(R.id.album_title).text = release.title
            view.findViewById<TextView>(R.id.album_subtitle).text = getString(
                R.string.album_subtitle,
                typeLabel(release.type),
                release.year,
                if (release.official) getString(R.string.official) else getString(R.string.unofficial),
                if (release.verification == "verified") "" else " • " + getString(R.string.pending_verification)
            )
            view.findViewById<TextView>(R.id.album_progress).text = getString(R.string.track_progress, done, total)

            val bar = view.findViewById<ProgressBar>(R.id.album_progress_bar)
            bar.max = total
            bar.progress = done

            return view
        }
    }

    fun typeLabel(type: String): String {
        return when (type) {
            "studio" -> getString(R.string.type_studio)
            "live" -> getString(R.string.type_live)
            "ep" -> getString(R.string.type_ep)
            "single" -> getString(R.string.type_single)
            "compilation" -> getString(R.string.type_compilation)
            "demos" -> getString(R.string.type_demos)
            "special" -> getString(R.string.type_special)
            else -> type
        }
    }

    companion object {
        private const val REQUEST_EXPORT = 1001
        private const val REQUEST_IMPORT = 1002
    }
}
