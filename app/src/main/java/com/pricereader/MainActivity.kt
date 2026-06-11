package com.pricereader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pricereader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var url = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        url = intent.getStringExtra(EXTRA_URL) ?: ""

        with(binding.webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(v: WebView, u: String, f: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
            }
            override fun onPageFinished(v: WebView, u: String) {
                binding.progress.visibility = View.GONE
                binding.swipe.isRefreshing = false
                supportActionBar?.title = v.title ?: getString(R.string.app_name)
            }
            override fun onReceivedError(v: WebView, r: WebResourceRequest, e: WebResourceError) {
                if (r.isForMainFrame) { binding.progress.visibility = View.GONE; binding.swipe.isRefreshing = false }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(v: WebView, p: Int) { binding.progress.progress = p }
        }

        binding.swipe.setOnRefreshListener { binding.webView.reload() }
        binding.webView.loadUrl(url)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack() else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu); return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_refresh -> { binding.webView.reload(); true }
        R.id.menu_change_url -> { changeUrl(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun changeUrl() {
        val input = android.widget.EditText(this).apply {
            setText(getSharedPreferences(SetupActivity.PREFS, Context.MODE_PRIVATE).getString(SetupActivity.KEY_URL, ""))
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this).setTitle("تغيير الرابط").setView(input)
            .setPositiveButton("حفظ") { _, _ ->
                val u = input.text.toString().trim()
                if (u.isNotEmpty()) {
                    val f = if (u.startsWith("http")) u else "https://$u"
                    getSharedPreferences(SetupActivity.PREFS, Context.MODE_PRIVATE).edit().putString(SetupActivity.KEY_URL, f).apply()
                    url = f; binding.webView.loadUrl(f)
                }
            }.setNegativeButton("إلغاء", null).show()
    }

    companion object { const val EXTRA_URL = "url" }
}
