package com.ouattararomuald.statussaver.images.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.databinding.ActivityFullscreenImageViewerBinding
import com.ouattararomuald.statussaver.images.adapters.FullScreenImagePager

class FullScreenImageViewerActivity : AppCompatActivity() {

  companion object {
    private const val IMAGES_KEY = "images_key"

    fun start(context: Context, images: List<Media>) {
      val intent = Intent(context, FullScreenImageViewerActivity::class.java)
      intent.apply {
        putParcelableArrayListExtra(IMAGES_KEY, ArrayList(images))
      }
      context.startActivity(intent)
    }
  }

  private lateinit var binding: ActivityFullscreenImageViewerBinding
  private lateinit var images: List<Media>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFullscreenImageViewerBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    if (intent.extras?.containsKey(IMAGES_KEY) == true) {
      images = intent.getParcelableArrayListExtra<Media>(IMAGES_KEY)!!.toList()
    }

    binding.pager.adapter = FullScreenImagePager(this, images)
  }
}