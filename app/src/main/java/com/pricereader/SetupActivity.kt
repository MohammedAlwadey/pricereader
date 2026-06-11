package com.pricereader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pricereader.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val saved = getPrefs().getString(KEY_URL, null)
        if (!saved.isNullOrEmpty()) { launch(saved); return }
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnConfirm.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.isEmpty()) { binding.etUrl.error = "أدخل الرابط"; return@setOnClickListener }
            val final = if (url.startsWith("http")) url else "https://$url"
            getPrefs().edit().putString(KEY_URL, final).apply()
            launch(final)
        }
    }

    private fun launch(url: String) {
        startActivity(Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_URL, url))
        finish()
    }

    private fun getPrefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    companion object {
        const val PREFS = "prefs"
        const val KEY_URL = "url"
    }
}
