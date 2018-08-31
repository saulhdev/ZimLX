package org.zimmob.zimlx.backup

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import org.zimmob.zimlx.R
import org.zimmob.zimlx.Utilities
import org.zimmob.zimlx.blur.BlurWallpaperProvider
import org.zimmob.zimlx.config.FeatureFlags

@SuppressLint("Registered")
open class BackupBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FeatureFlags.applyDarkTheme(this, 0)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(Utilities.getPrefs(this).primaryColor)
        setSupportActionBar(toolbar)

        if (FeatureFlags.getCurrentTheme() != 2)
            BlurWallpaperProvider.applyBlurBackground(this)
    }

    override fun setContentView(v: View) {
        val contentParent = findViewById<ViewGroup>(R.id.content)
        contentParent.removeAllViews()
        contentParent.addView(v)
    }

    override fun setContentView(resId: Int) {
        val contentParent = findViewById<ViewGroup>(R.id.content)
        contentParent.removeAllViews()
        LayoutInflater.from(this).inflate(resId, contentParent)
    }

    override fun setContentView(v: View, lp: ViewGroup.LayoutParams) {
        val contentParent = findViewById<ViewGroup>(R.id.content)
        contentParent.removeAllViews()
        contentParent.addView(v, lp)
    }
}